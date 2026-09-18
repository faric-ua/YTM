#!/usr/bin/env python3
"""Offline, sequential phone-evidence verifier. No network or source writes."""
import argparse
from collections import Counter
from copy import deepcopy
from datetime import datetime, timezone
import hashlib
import json
from pathlib import Path
import sys

sys.dont_write_bytecode = True
FORMAT = "ytm-importer-account-library-export"
PROJECT = "ytm-importer-playlist-project"
RUN_FORMAT = "ytm-v1430-delta-status-qa-r1"
DEFAULT_RUN = Path("/sdcard/Download/YTM-v1.4.30-Delta-QA-R1")
STAGES = ("new", "updated", "missing")


class Stop(ValueError):
    pass


def require(ok, message):
    if not ok:
        raise Stop(message)


def no_duplicates(pairs):
    result = {}
    for key, value in pairs:
        require(key not in result, "Duplicate JSON key")
        result[key] = value
    return result


def read_json(path):
    require(path.is_file() and not path.is_symlink(), "Missing/unsafe file: " + str(path))
    value = json.loads(path.read_text(encoding="utf-8"), object_pairs_hook=no_duplicates)
    require(isinstance(value, dict), "Expected JSON object: " + str(path))
    return value


def write_json(path, value):
    # Only our run metadata/reports are written, never a backup session.
    require(not path.is_symlink(), "Unsafe output path")
    temp = path.with_name(path.name + ".tmp")
    require(not temp.exists() and not temp.is_symlink(), "Metadata temp file already exists")
    with temp.open("x", encoding="utf-8", newline="\n") as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)
        stream.write("\n")
    temp.replace(path)


def text(value):
    return "" if value is None else str(value).strip()


def leaf(value):
    require(isinstance(value, str) and value not in ("", ".", "..")
            and "/" not in value and "\\" not in value, "Unsafe session/file name")
    return value


def inventory(folder):
    require(folder.is_dir() and not folder.is_symlink(), "Missing/unsafe session folder")
    result = {}
    for path in sorted(folder.iterdir()):
        require(path.is_file() and not path.is_symlink(), "Unexpected nested/unsafe session entry")
        result[path.name] = hashlib.sha256(path.read_bytes()).hexdigest()
    require("manifest.json" in result, "Session has no manifest.json")
    return result


def manifest(folder):
    data = read_json(folder / "manifest.json")
    require(data.get("format") == FORMAT, "Wrong manifest format")
    require(data.get("appVersion") == "1.4.30", "This QA run requires appVersion 1.4.30")
    records = data.get("playlists")
    require(isinstance(records, list), "Missing playlists array")
    require(all(isinstance(record, dict) for record in records), "Invalid playlist record")
    ids = [r.get("playlistId") for r in records]
    require(all(isinstance(i, str) and i.strip() == i and i for i in ids), "Invalid playlistId")
    require(len(ids) == len(set(ids)), "Duplicate playlistId")
    require(data.get("playlistCount") == len(records), "playlistCount mismatch")
    require(data.get("failedPlaylists") == 0, "failedPlaylists must be 0; inspect phone evidence")
    requests = []
    for record in records:
        require(record.get("error") is None, "Record contains an error")
        for field in ("sourceItemCount", "exportedTrackCount", "playlistItemsRequests"):
            value = record.get(field)
            require(type(value) is int and value >= 0, "Invalid record " + field)
        requests.append(record["playlistItemsRequests"])
    require(data.get("playlistItemsRequests") == sum(requests), "Request total mismatch")
    names = [r["fileName"] for r in records if r.get("fileName") is not None]
    for name in names:
        leaf(name)
        require(name.endswith(".ytm-project.json"), "Unexpected project extension")
    require(len(names) == len(set(names)), "Two records reference the same file")
    require(set(inventory(folder)) == {"manifest.json", *names}, "Missing or extra session files")
    require(data.get("exportedProjects") == len(names), "exportedProjects mismatch")
    return data, {r["playlistId"]: r for r in records}


def fingerprint(tracks):
    digest = hashlib.sha256(b"ytm-account-backup-fingerprint-v1")
    for index, track in enumerate(tracks):
        for part in (str(index), track[0], track[1], track[2]):
            digest.update(b"\0")
            digest.update(part.encode("utf-8"))
    return digest.hexdigest()


def record_state(folder, record):
    tracks = []
    if record.get("fileName") is not None:
        project = read_json(folder / leaf(record["fileName"]))
        require(project.get("format") == PROJECT and project.get("schemaVersion") == 2,
                "Expected account playlist project schema 2")
        require(project.get("appVersion") == "1.4.30", "Project appVersion mismatch")
        playlist = project["playlist"]
        require(isinstance(playlist, dict), "Invalid project playlist")
        require(playlist.get("sourcePlaylistId") == record["playlistId"], "Project sourcePlaylistId mismatch")
        require(text(playlist.get("privacyStatus")).lower() == text(record.get("privacyStatus")).lower(),
                "Project privacy mismatch")
        require(playlist.get("name") == record.get("title"), "Project name mismatch")
        items = playlist.get("tracks")
        require(isinstance(items, list) and items, "Project has no tracks")
        require(all(isinstance(item, dict) for item in items), "Invalid track record")
        positions = [item.get("position", i) for i, item in enumerate(items)]
        require(all(type(p) is int and p >= 0 for p in positions) and len(set(positions)) == len(items),
                "Ambiguous project track positions")
        for _, item in sorted(zip(positions, items), key=lambda pair: pair[0]):
            original = text(item.get("originalTitle"))
            artist = text(item.get("originalArtist"))
            require(original and artist, "Project track would be dropped by the app importer")
            video = text(item.get("videoId"))
            selected = text(item.get("selectedTitle"))
            if not video:
                selected = ""
            elif not selected or selected == "Ручне посилання":
                selected = "YouTube video " + video
            tracks.append([video, selected, text(item.get("selectedChannel")), original, artist])
    else:
        require(record["sourceItemCount"] == 0, "Nonempty state lacks a project file")
    require(record["exportedTrackCount"] == len(tracks), "exportedTrackCount mismatch")
    return {"title": record["title"], "privacy": text(record.get("privacyStatus")).lower(),
            "sourceCount": record["sourceItemCount"], "tracks": tracks}


def same_metadata(record, value):
    return (record.get("title") == value["title"]
            and text(record.get("privacyStatus")).lower() == value["privacy"]
            and record.get("sourceItemCount") == value["sourceCount"])


def full_state(folder, consolidated=False):
    data, records = manifest(folder)
    require(data.get("selectionMode") == "ALL", "Fresh ALL baseline required; SELECTED cannot detect a new ID")
    if consolidated:
        require(data.get("schemaVersion") == 3 and data.get("backupMode") == "CONSOLIDATED_FULL",
                "Expected consolidated full schema 3")
        require(data.get("syncScopeMode") == "ALL" and data.get("scopePlaylistIds") == [],
                "Consolidated scope changed")
        require(data.get("materializedFromChain") is True, "Missing chain materialization marker")
        require(data.get("playlistItemsRequests") == 0, "Consolidation must be local")
    else:
        require(data.get("schemaVersion") == 2 and not data.get("backupMode"),
                "Create a fresh ALL export, not a prior delta/consolidated backup")
    result = {}
    for pid, record in records.items():
        require(record.get("status") in ("EXPORTED", "SKIPPED_EMPTY"), "Full backup has unsupported/failed status")
        require((record["status"] == "EXPORTED") == (record.get("fileName") is not None),
                "Full backup status/file mismatch")
        result[pid] = record_state(folder, record)
    skipped = sum(not value["tracks"] for value in result.values())
    require(data.get("skippedPlaylists") == skipped, "skippedPlaylists mismatch")
    return data, result


def delta_state(folder, previous, stage, target_title, target_id, original_target, base_name):
    data, records = manifest(folder)
    require(data.get("schemaVersion") == 3 and data.get("backupMode") == "INCREMENTAL_DELTA",
            "Expected incremental delta schema 3")
    require(data.get("selectionMode") == "SYNC" and data.get("syncScopeMode") == "ALL"
            and data.get("scopePlaylistIds") == [], "Delta scope must remain ALL")
    require(data.get("baseSessionName") == base_name, "Delta points to wrong baseline/head")
    if stage == "new":
        new_ids = set(records) - set(previous)
        require(len(new_ids) == 1, "NEW needs exactly one new test playlist; inspect unrelated changes")
        target_id = next(iter(new_ids))
        require(records[target_id].get("title") == target_title, "NEW is not the prepared test playlist")
    require(target_id in records, "Target record missing")
    wanted_ids = set(previous) | ({target_id} if stage == "new" else set())
    require(set(records) == wanted_ids, "Delta record IDs do not cover the expected state")
    expected_counts = Counter({"UNCHANGED": len(previous) - (stage != "new"), stage.upper(): 1})
    actual_counts = Counter(r.get("status") for r in records.values())
    require(actual_counts == +expected_counts, "Unexpected delta status counts: " + str(dict(actual_counts)))
    for status, field in (("NEW", "newPlaylists"), ("UPDATED", "updatedPlaylists"),
                          ("UNCHANGED", "unchangedPlaylists"), ("MISSING", "missingPlaylists"),
                          ("FAILED", "failedPlaylists")):
        require(data.get(field) == actual_counts[status], "Delta counter mismatch: " + field)
    result = deepcopy(previous)
    for pid, record in records.items():
        expected_status = stage.upper() if pid == target_id else "UNCHANGED"
        require(record.get("status") == expected_status, "Unexpected change to another playlist")
        if expected_status in ("NEW", "UPDATED"):
            value = record_state(folder, record)
            require(value["title"] == target_title and value["privacy"] == "private", "Target title/privacy changed")
            require(value["sourceCount"] == 2 and len(value["tracks"]) == 2, "Test target must contain exactly 2 tracks")
            ids = [track[0] for track in value["tracks"]]
            require(all(ids) and len(set(ids)) == 2, "Target needs two distinct exact video IDs")
            if stage == "updated":
                expected = deepcopy(original_target)
                expected["tracks"].reverse()
                require(value == expected, "UPDATED must reverse A,B to B,A with title/privacy/count unchanged")
            result[pid] = value
        else:
            require(record.get("fileName") is None, "UNCHANGED/MISSING must not write projects")
            value = previous[pid]
            require(same_metadata(record, value), "Metadata changed without UPDATED")
            track_count = 0 if expected_status == "MISSING" else len(value["tracks"])
            require(record["exportedTrackCount"] == track_count, "Unchanged/missing track count mismatch")
            if expected_status == "MISSING":
                require(record["playlistItemsRequests"] == 0, "MISSING must have 0 item requests")
                del result[pid]
        require(record.get("contentFingerprint") == fingerprint(value["tracks"]), "Fingerprint mismatch")
    require(data.get("currentPlaylistCount") == len(result), "currentPlaylistCount mismatch")
    return result, target_id


def check_full(folder, expected, chain):
    data, actual = full_state(folder, consolidated=True)
    require(data.get("chainBaseSessionName") == chain[0], "Wrong consolidated base")
    require(data.get("chainHeadSessionName") == chain[-1], "Wrong consolidated head")
    require(data.get("chainLength") == len(chain), "Wrong chainLength")
    require(data.get("sourceSessions") == chain, "Wrong sourceSessions/order")
    require(actual == expected, "Consolidated state differs from expected playlists/metadata/ordered tracks")
    return {"playlistCount": len(actual), "exportedProjects": data["exportedProjects"],
            "skippedPlaylists": data["skippedPlaylists"], "chainLength": len(chain)}


def sessions(parent):
    require(parent.is_dir() and not parent.is_symlink(), "Missing/unsafe QA parent folder")
    found = set()
    for path in parent.iterdir():
        require(path.is_dir() and not path.is_symlink() and (path / "manifest.json").is_file(),
                "Only complete app-created session folders belong in " + str(parent))
        found.add(path.name)
    return found


def load_run(run):
    state = read_json(run / "run.json")
    require(state.get("format") == RUN_FORMAT, "Wrong QA run format")
    for name in state["chain"] + state["outputs"]:
        leaf(name)
    require(len(state["chain"]) == len(set(state["chain"])) and len(state["chain"]) <= 4,
            "Invalid saved chain")
    require(len(state["outputs"]) == max(0, len(state["chain"]) - 1), "Invalid saved phase counts")
    return state


def verify_saved(run, state):
    # Pin both inputs and outputs after each accepted phase, before the next phone action.
    for parent, names in (("sources", state["chain"]), ("restored", state["outputs"])):
        for name in names:
            key = parent + "/" + name
            require(inventory(run / parent / name) == state["hashes"].get(key),
                    "Previously accepted session changed: " + key)
    if not state["chain"]:
        return None, None, None
    _, current = full_state(run / "sources" / state["chain"][0])
    require(not any(v["title"] == state["targetTitle"] for v in current.values()), "Target already existed at baseline")
    original = None
    target_id = None
    for index, name in enumerate(state["chain"][1:]):
        current, target_id = delta_state(run / "sources" / name, current, STAGES[index],
                                         state["targetTitle"], target_id, original, state["chain"][index])
        if index == 0:
            original = deepcopy(current[target_id])
        check_full(run / "restored" / state["outputs"][index], current, state["chain"][:index + 2])
    require(target_id == state["targetId"], "Saved target ID mismatch")
    return current, target_id, original


def pin(run, state, parent, name):
    state["hashes"][parent + "/" + name] = inventory(run / parent / name)


def prepare(run):
    if run.exists():
        state = load_run(run)
        verify_saved(run, state)
        print("SKIP: QA run already prepared; existing evidence preserved")
    else:
        run.mkdir(parents=True)
        for name in ("sources", "restored", "reports"):
            (run / name).mkdir()
        target = "YTM-QA-1430-" + datetime.now(timezone.utc).strftime("%Y%m%d-%H%M%S") + "-TEST"
        state = {"format": RUN_FORMAT, "targetTitle": target, "targetId": None,
                 "chain": [], "outputs": [], "hashes": {}, "checks": []}
        write_json(run / "run.json", state)
        print("PASS: QA folders prepared; phone QA NOT RUN")
    print("Run: " + str(run))
    print("Source parent: " + str(run / "sources"))
    print("Restore parent: " + str(run / "restored"))
    print("Test-only playlist title: " + state["targetTitle"])
    if not state["chain"]:
        print("NEXT: fresh ALL export into sources, then run baseline. Do not create target before baseline.")
    elif len(state["outputs"]) < 3:
        print("NEXT phase: " + STAGES[len(state["outputs"])].upper() + "; follow PLAN.md after evidence review.")
    else:
        print("All three file checks accepted; phone evidence review/closeout remains separate.")


def accept_baseline(run):
    state = load_run(run)
    if state["chain"]:
        verify_saved(run, state)
        print("SKIP: baseline already accepted; original snapshot unchanged")
        return
    found = sessions(run / "sources")
    require(len(found) == 1 and not sessions(run / "restored"), "Need exactly one fresh baseline and no restored sessions")
    name = next(iter(found))
    data, current = full_state(run / "sources" / name)
    require(current, "Baseline must contain at least one ordinary playlist")
    require(not any(v["title"] == state["targetTitle"] for v in current.values()), "Create test target AFTER baseline")
    state["chain"].append(name)
    state["baselineCounts"] = {"N": len(current), "E": data["exportedProjects"], "K": data["skippedPlaylists"]}
    pin(run, state, "sources", name)
    write_json(run / "run.json", state)
    report = {"stage": "baseline", "result": "OFFLINE_EVIDENCE_PASS", **state["baselineCounts"],
              "baseline": name, "targetTitle": state["targetTitle"], "phoneUI": "PENDING_REVIEW"}
    write_json(run / "reports" / "baseline.json", report)
    print(json.dumps(report, ensure_ascii=False, indent=2))
    print("BASELINE FILE CHECK PASSED. NEXT: create only the named private test playlist with A,B.")


def accept_stage(run, stage):
    state = load_run(run)
    require(state["chain"], "Accept baseline first")
    current, target_id, original = verify_saved(run, state)
    index = STAGES.index(stage)
    done = len(state["outputs"])
    if index < done:
        print("SKIP: " + stage.upper() + " already accepted; saved sources/results verified")
        return
    require(index == done, "Run stages in order: NEW -> UPDATED -> MISSING")
    source_names = sessions(run / "sources")
    output_names = sessions(run / "restored")
    require(source_names.issuperset(state["chain"]) and output_names.issuperset(state["outputs"]),
            "Previously accepted folder missing")
    new_sources = source_names - set(state["chain"])
    new_outputs = output_names - set(state["outputs"])
    require(len(new_sources) == len(new_outputs) == 1,
            "Need exactly one new delta AND one new consolidated result for this stage")
    delta_name, full_name = next(iter(new_sources)), next(iter(new_outputs))
    current, target_id = delta_state(run / "sources" / delta_name, current, stage,
                                     state["targetTitle"], target_id, original, state["chain"][-1])
    chain = state["chain"] + [delta_name]
    summary = check_full(run / "restored" / full_name, current, chain)
    state["targetId"] = target_id
    state["chain"], state["outputs"] = chain, state["outputs"] + [full_name]
    pin(run, state, "sources", delta_name)
    pin(run, state, "restored", full_name)
    report = {"stage": stage.upper(), "result": "OFFLINE_EVIDENCE_PASS", **summary,
              "targetTitle": state["targetTitle"], "targetPlaylistId": target_id,
              "targetPresent": target_id in current, "sourceSessions": chain,
              "consolidatedSession": full_name, "phoneUI": "PENDING_REVIEW"}
    state["checks"].append(report)
    write_json(run / "run.json", state)
    write_json(run / "reports" / (stage + ".json"), report)
    print(json.dumps(report, ensure_ascii=False, indent=2))
    print(stage.upper() + " DELTA + CONSOLIDATED FILE CHECK PASSED")
    print("Send this output and phone screenshots before the next stage. UI/API behavior still needs phone evidence.")


def status(run):
    state = load_run(run)
    verify_saved(run, state)
    print(json.dumps({key: state.get(key) for key in
                      ("targetTitle", "targetId", "baselineCounts", "chain", "outputs", "checks")},
                     ensure_ascii=False, indent=2))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", choices=("prepare", "baseline", "check", "status"))
    parser.add_argument("stage", nargs="?", choices=STAGES)
    parser.add_argument("--run", type=Path, default=DEFAULT_RUN)
    args = parser.parse_args()
    require(not args.run.is_symlink(), "Run root must not be a symlink")
    run = args.run.resolve()
    if args.command == "check":
        require(args.stage is not None, "check requires new, updated, or missing")
        accept_stage(run, args.stage)
    else:
        require(args.stage is None, "Stage is valid only with check")
        {"prepare": prepare, "baseline": accept_baseline, "status": status}[args.command](run)


if __name__ == "__main__":
    try:
        main()
    except (Stop, OSError, ValueError, KeyError, TypeError) as error:
        print("STOP: " + str(error), file=sys.stderr)
        sys.exit(1)
