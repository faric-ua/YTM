#!/usr/bin/env python3
"""Synthetic fixtures only. These tests do not run Android or YouTube."""
from contextlib import redirect_stdout
from copy import deepcopy
import hashlib
import importlib.util
import io
import json
from pathlib import Path
import sys
import tempfile
import unittest

sys.dont_write_bytecode = True
spec = importlib.util.spec_from_file_location("delta_qa", Path(__file__).with_name("v1430-delta-status-qa.py"))
qa = importlib.util.module_from_spec(spec)
spec.loader.exec_module(qa)

BASE_ID, EMPTY_ID, TARGET_ID = "PL_SYNTHETIC_BASE", "PL_SYNTHETIC_EMPTY", "PL_SYNTHETIC_QA"
BASE_TRACKS = [["Base0000001", "기억", "Тест канал", "기억", "Тест виконавець"]]
AB = [["Test000000A", "A song", "Channel A", "A song", "Artist A"],
      ["Test000000B", "B song", "Channel B", "B song", "Artist B"]]


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def fixture_fingerprint(tracks):
    # Independent byte stream, matching the published Kotlin fingerprint definition.
    parts = ["ytm-account-backup-fingerprint-v1"]
    for i, row in enumerate(tracks):
        parts.extend([str(i), *row[:3]])
    return hashlib.sha256("\0".join(parts).encode()).hexdigest()


def record(folder, pid, title, tracks, status, write_project, source_count=None):
    filename = ("Тест_" + pid + ".ytm-project.json") if write_project else None
    if write_project:
        items = [{"position": i, "videoId": t[0], "selectedTitle": t[1], "selectedChannel": t[2],
                  "originalTitle": t[3], "originalArtist": t[4], "sourceStatus": "MATCHED",
                  "manuallySelected": False, "sourceError": None, "candidates": []}
                 for i, t in enumerate(tracks)]
        save(folder / filename, {"format": "ytm-importer-playlist-project", "schemaVersion": 2,
                                "appVersion": "1.4.30", "exportedAt": 100,
                                "playlist": {"name": title, "sourcePlaylistId": pid,
                                             "privacyStatus": "private", "tracks": items}})
    return {"playlistId": pid, "title": title, "privacyStatus": "private",
            "sourceItemCount": len(tracks) if source_count is None else source_count,
            "exportedTrackCount": 0 if status == "MISSING" else len(tracks),
            "playlistItemsRequests": 0, "status": status, "fileName": filename, "error": None,
            **({"contentFingerprint": fixture_fingerprint(tracks)}
               if status in ("NEW", "UPDATED", "UNCHANGED", "MISSING") else {})}


def root(records, **fields):
    return {"format": "ytm-importer-account-library-export", "appVersion": "1.4.30",
            "exportedAt": 100, "playlists": records, "playlistCount": len(records),
            "exportedProjects": sum(r["fileName"] is not None for r in records),
            "failedPlaylists": 0, "playlistItemsRequests": 0, **fields}


class PhoneFixture(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix="ytm-synthetic-qa-")
        self.addCleanup(self.temp.cleanup)
        self.run = Path(self.temp.name) / "run"
        self.call(qa.prepare, self.run)
        self.title = qa.load_run(self.run)["targetTitle"]
        self.base = self.run / "sources" / "260918-100000-YTM-Export"
        records = [record(self.base, BASE_ID, "Звичайний плейлист", BASE_TRACKS, "EXPORTED", True),
                   record(self.base, EMPTY_ID, "Порожній", [], "SKIPPED_EMPTY", False)]
        save(self.base / "manifest.json", root(records, schemaVersion=2, selectionMode="ALL", skippedPlaylists=1))

    def call(self, fn, *args):
        with redirect_stdout(io.StringIO()):
            return fn(*args)

    def baseline(self):
        self.call(qa.accept_baseline, self.run)

    def stage_files(self, stage):
        number = {"new": 1, "updated": 2, "missing": 3}[stage]
        delta = self.run / "sources" / ("260918-10000%d-YTM-Sync" % number)
        full = self.run / "restored" / ("260918-11000%d-YTM-Full" % number)
        tracks = AB if stage == "new" else AB[::-1]
        records = [record(delta, BASE_ID, "Звичайний плейлист", BASE_TRACKS, "UNCHANGED", False),
                   record(delta, EMPTY_ID, "Порожній", [], "UNCHANGED", False),
                   record(delta, TARGET_ID, self.title, tracks, stage.upper(), stage != "missing")]
        chain = qa.load_run(self.run)["chain"]
        save(delta / "manifest.json", root(records, schemaVersion=3, selectionMode="SYNC",
             backupMode="INCREMENTAL_DELTA", syncScopeMode="ALL", scopePlaylistIds=[],
             baseSessionName=chain[-1], currentPlaylistCount=2 if stage == "missing" else 3,
             newPlaylists=int(stage == "new"), updatedPlaylists=int(stage == "updated"),
             unchangedPlaylists=2, missingPlaylists=int(stage == "missing")))
        records = [record(full, BASE_ID, "Звичайний плейлист", BASE_TRACKS, "EXPORTED", True),
                   record(full, EMPTY_ID, "Порожній", [], "SKIPPED_EMPTY", False)]
        if stage != "missing":
            records.append(record(full, TARGET_ID, self.title, tracks, "EXPORTED", True))
        save(full / "manifest.json", root(records, schemaVersion=3, selectionMode="ALL", skippedPlaylists=1,
             backupMode="CONSOLIDATED_FULL", syncScopeMode="ALL", scopePlaylistIds=[], materializedFromChain=True,
             chainBaseSessionName=chain[0], chainHeadSessionName=delta.name,
             chainLength=number + 1, sourceSessions=chain + [delta.name]))
        return delta, full

    def through(self, stage):
        self.baseline()
        for current in ("new", "updated", "missing"):
            paths = self.stage_files(current)
            self.call(qa.accept_stage, self.run, current)
            if current == stage:
                return paths

    def mutate_manifest(self, folder, callback):
        path = folder / "manifest.json"
        data = json.loads(path.read_text())
        callback(data)
        save(path, data)

    def reject(self, stage, message):
        before = (self.run / "run.json").read_bytes()
        with self.assertRaisesRegex(qa.Stop, message):
            self.call(qa.accept_stage, self.run, stage)
        self.assertEqual(before, (self.run / "run.json").read_bytes())

    def test_full_chain_empty_baseline_order_and_idempotence(self):
        self.through("missing")
        state = qa.load_run(self.run)
        self.assertEqual(state["baselineCounts"], {"N": 2, "E": 1, "K": 1})
        self.assertEqual([r["playlistCount"] for r in state["checks"]], [3, 3, 2])
        self.assertEqual([r["exportedProjects"] for r in state["checks"]], [2, 2, 1])
        self.assertEqual([r["chainLength"] for r in state["checks"]], [2, 3, 4])
        self.assertFalse(state["checks"][-1]["targetPresent"])
        before = (self.run / "run.json").read_bytes()
        for stage in ("new", "updated", "missing"):
            self.call(qa.accept_stage, self.run, stage)
        self.call(qa.accept_baseline, self.run)
        self.call(qa.prepare, self.run)
        self.call(qa.status, self.run)
        self.assertEqual(before, (self.run / "run.json").read_bytes())
        for key, hashes in state["hashes"].items():
            self.assertEqual(hashes, qa.inventory(self.run / key))

    def test_fingerprint_vectors_and_unicode(self):
        self.assertEqual(qa.fingerprint([]), hashlib.sha256(b"ytm-account-backup-fingerprint-v1").hexdigest())
        self.assertEqual(qa.fingerprint(BASE_TRACKS), fixture_fingerprint(BASE_TRACKS))
        self.assertEqual(qa.fingerprint(AB), fixture_fingerprint(AB))
        self.assertNotEqual(qa.fingerprint(AB), qa.fingerprint(AB[::-1]))

    def test_selected_baseline_rejected(self):
        self.mutate_manifest(self.base, lambda d: d.update(selectionMode="SELECTED"))
        with self.assertRaisesRegex(qa.Stop, "ALL baseline"):
            self.baseline()

    def test_failed_baseline_rejected(self):
        self.mutate_manifest(self.base, lambda d: d.update(failedPlaylists=1))
        with self.assertRaisesRegex(qa.Stop, "failedPlaylists"):
            self.baseline()

    def test_wrong_link_rejected(self):
        self.baseline()
        delta, _ = self.stage_files("new")
        self.mutate_manifest(delta, lambda d: d.update(baseSessionName="wrong"))
        self.reject("new", "wrong baseline")

    def test_stale_order_in_consolidated_rejected(self):
        self.through("new")
        _, full = self.stage_files("updated")
        record(full, TARGET_ID, self.title, AB, "EXPORTED", True)
        self.reject("updated", "Consolidated state differs")

    def test_union_instead_of_replacement_rejected(self):
        self.through("new")
        _, full = self.stage_files("updated")
        record(full, TARGET_ID, self.title, AB + AB[::-1], "EXPORTED", True)
        self.mutate_manifest(full, lambda d: d["playlists"][-1].update(exportedTrackCount=4))
        self.reject("updated", "Consolidated state differs")

    def test_missing_retained_in_full_rejected(self):
        self.through("updated")
        _, full = self.stage_files("missing")
        added = record(full, TARGET_ID, self.title, AB[::-1], "EXPORTED", True)
        def change(d):
            d["playlists"].append(added)
            d.update(playlistCount=3, exportedProjects=2)
        self.mutate_manifest(full, change)
        self.reject("missing", "Consolidated state differs")

    def test_bad_fingerprint_rejected(self):
        self.baseline()
        delta, _ = self.stage_files("new")
        self.mutate_manifest(delta, lambda d: d["playlists"][-1].update(contentFingerprint="0" * 64))
        self.reject("new", "Fingerprint")

    def test_old_source_mutation_rejected(self):
        self.through("new")
        self.stage_files("updated")
        with (self.base / "manifest.json").open("a") as stream:
            stream.write("\n")
        self.reject("updated", "Previously accepted session changed")

    def test_old_output_mutation_rejected(self):
        _, full = self.through("new")
        self.stage_files("updated")
        with (full / "manifest.json").open("a") as stream:
            stream.write("\n")
        self.reject("updated", "Previously accepted session changed")

    def test_unrelated_metadata_change_rejected(self):
        self.baseline()
        delta, _ = self.stage_files("new")
        self.mutate_manifest(delta, lambda d: d["playlists"][0].update(title="Changed real playlist"))
        self.reject("new", "Metadata changed")

    def test_wrong_current_count_rejected(self):
        self.through("updated")
        delta, _ = self.stage_files("missing")
        self.mutate_manifest(delta, lambda d: d.update(currentPlaylistCount=3))
        self.reject("missing", "currentPlaylistCount")

    def test_out_of_order_phase_rejected(self):
        self.baseline()
        self.reject("missing", "Run stages in order")

    def test_duplicate_json_key_rejected(self):
        self.baseline()
        delta, _ = self.stage_files("new")
        path = delta / "manifest.json"
        path.write_text(path.read_text().replace('"schemaVersion": 3', '"schemaVersion": 2, "schemaVersion": 3'))
        self.reject("new", "Duplicate JSON key")

    def test_missing_project_rejected(self):
        self.baseline()
        delta, _ = self.stage_files("new")
        next(delta.glob("*.ytm-project.json")).unlink()
        self.reject("new", "Missing or extra session files")

    def test_path_traversal_rejected(self):
        self.baseline()
        delta, _ = self.stage_files("new")
        self.mutate_manifest(delta, lambda d: d["playlists"][-1].update(fileName="../other.ytm-project.json"))
        self.reject("new", "Unsafe session/file")

    def test_wrong_head_in_consolidated_rejected(self):
        self.baseline()
        _, full = self.stage_files("new")
        self.mutate_manifest(full, lambda d: d.update(chainHeadSessionName=self.base.name))
        self.reject("new", "Wrong consolidated head")

    def test_position_order_matches_app_importer(self):
        self.baseline()
        _, full = self.stage_files("new")
        path = full / ("Тест_" + TARGET_ID + ".ytm-project.json")
        data = json.loads(path.read_text())
        data["playlist"]["tracks"].reverse()  # Positions stay 1,0; Kotlin sorts them back.
        save(path, data)
        self.call(qa.accept_stage, self.run, "new")

    def test_duplicate_positions_rejected(self):
        self.baseline()
        delta, _ = self.stage_files("new")
        path = next(delta.glob("*.ytm-project.json"))
        data = json.loads(path.read_text())
        data["playlist"]["tracks"][1]["position"] = 0
        save(path, data)
        self.reject("new", "Ambiguous project track positions")


if __name__ == "__main__":
    unittest.main(verbosity=2)
