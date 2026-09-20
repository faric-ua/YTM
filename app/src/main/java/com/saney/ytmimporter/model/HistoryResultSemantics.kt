package com.saney.ytmimporter.model

enum class HistoryResultKind {
    YTM_WRITE,
    IMPORT,
    RESTORE
}

data class HistoryPrimaryResult(
    val kind: HistoryResultKind,
    val label: String,
    val value: String
)

/**
 * Presentation semantics for History's primary result line.
 *
 * Legacy/local History entries do not carry an explicit operation kind.
 * Remote-write evidence therefore wins first. Only a clean COMPLETED entry
 * with no remote-write evidence is treated as a local import/restore.
 */
object HistoryResultSemantics {
    fun primary(
        entry: HistoryEntry
    ): HistoryPrimaryResult {
        if (hasRemoteWriteEvidence(entry)) {
            return HistoryPrimaryResult(
                kind = HistoryResultKind.YTM_WRITE,
                label = "Додано в YTM",
                value =
                    "${entry.addedCount}/${entry.writeTargetCount}"
            )
        }

        val localCount =
            maxOf(
                entry.totalImportedCount,
                entry.tracks.size
            )

        return if (looksLikeRestore(entry.sourceLabel)) {
            HistoryPrimaryResult(
                kind = HistoryResultKind.RESTORE,
                label = "Відновлено",
                value = "$localCount треків"
            )
        } else {
            HistoryPrimaryResult(
                kind = HistoryResultKind.IMPORT,
                label = "Імпортовано",
                value = "$localCount треків"
            )
        }
    }

    private fun hasRemoteWriteEvidence(
        entry: HistoryEntry
    ): Boolean =
        !entry.playlistId.isNullOrBlank() ||
            entry.addedCount > 0 ||
            entry.failedCount > 0 ||
            entry.pendingCount > 0 ||
            entry.status != HistoryStatus.COMPLETED

    private fun looksLikeRestore(
        sourceLabel: String
    ): Boolean {
        val normalized =
            sourceLabel.lowercase()

        return listOf(
            "restore",
            "backup",
            "віднов",
            "safety snapshot",
            "history json",
            "rollback"
        ).any { marker ->
            marker in normalized
        }
    }
}
