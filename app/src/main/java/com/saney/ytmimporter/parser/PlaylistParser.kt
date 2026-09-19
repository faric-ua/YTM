package com.saney.ytmimporter.parser

import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.Track

object PlaylistParser {
    fun parse(fileName: String, text: String): ImportedPlaylist {
        val clean = text.removePrefix("\uFEFF").trim()
        require(clean.isNotBlank()) { "Файл порожній" }
        return if (looksLikeCsv(clean)) parseCsv(fileName, clean) else parseText(fileName, clean)
    }

    private fun looksLikeCsv(text: String): Boolean {
        val firstLine = text.lineSequence().firstOrNull().orEmpty().lowercase()
        return firstLine.contains("track name") ||
            firstLine.contains("artist name") ||
            firstLine.contains("title,artist") ||
            (firstLine.count { it == ',' } >= 1 && firstLine.contains("artist"))
    }

    private fun parseCsv(fileName: String, text: String): ImportedPlaylist {
        val rows = parseCsvRows(text)
        require(rows.size >= 2) { "CSV не містить треків" }

        val header = rows.first().map { normalizeHeader(it) }
        val titleIndex = indexOfAny(header, "trackname", "title", "track")
        val artistIndex = indexOfAny(header, "artistname", "artist")
        val playlistIndex = indexOfAny(header, "playlistname", "playlist")

        require(titleIndex >= 0) { "Не знайдено колонку Track name/title" }
        require(artistIndex >= 0) { "Не знайдено колонку Artist name/artist" }

        val tracks = rows.drop(1).mapNotNull { row ->
            val title = row.getOrNull(titleIndex).orEmpty().trim()
            val artist = row.getOrNull(artistIndex).orEmpty().trim()
            if (title.isBlank() || artist.isBlank()) null else Track(title, artist)
        }.toMutableList()

        require(tracks.isNotEmpty()) { "Не знайдено жодного коректного треку" }

        val playlistName = rows.drop(1)
            .firstNotNullOfOrNull { it.getOrNull(playlistIndex).orEmpty().trim().ifBlank { null } }
            ?: fallbackPlaylistName(fileName)

        return ImportedPlaylist(playlistName, tracks)
    }

    private fun parseText(fileName: String, text: String): ImportedPlaylist {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val tracks = mutableListOf<Track>()
        var playlistName: String? = null

        for (raw in lines) {
            val line = raw
                .replace(Regex("^\\s*(?:[-*•·▪◦]|\\d{1,3}[.)-]?)\\s+"), "")
                .trim()
            val separator = when {
                " - " in line -> " - "
                " – " in line -> " – "
                " — " in line -> " — "
                else -> null
            }
            if (separator == null) {
                if (playlistName == null) playlistName = raw
                continue
            }
            val parts = line.split(separator, limit = 2)
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                tracks += Track(originalTitle = parts[1].trim(), originalArtist = parts[0].trim())
            }
        }

        require(tracks.isNotEmpty()) { "TXT не містить рядків формату Artist - Track" }
        return ImportedPlaylist(
            name = playlistName ?: fallbackPlaylistName(fileName),
            tracks = tracks
        )
    }

    private fun fallbackPlaylistName(
        fileName: String
    ): String {
        val base =
            fileName
                .substringBeforeLast('.')
                .replace('_', ' ')
                .replace(
                    Regex("\\s+"),
                    " "
                )
                .trim()

        val withoutServiceMarker =
            base.replace(
                Regex(
                    "(?i)\\s*(?:[-–—]\\s*)?YTM(?:\\s+Importer)?" +
                        "(?:\\s*(?:[-_]\\s*\\d+|\\(\\s*\\d+\\s*\\)|\\s+\\d+))*\\s*$"
                ),
                ""
            ).trim()

        val normalizedVolumes =
            withoutServiceMarker.replace(
                Regex(
                    "(?i)\\bVol\\.?\\s*(\\d+)\\b"
                )
            ) { match ->
                "Vol.${match.groupValues[1]}"
            }

        return normalizedVolumes
            .trim()
            .ifBlank {
                "YTM Import"
            }
    }

    private fun normalizeHeader(value: String): String =
        value.lowercase().replace(Regex("[^a-z0-9]"), "")

    private fun indexOfAny(header: List<String>, vararg options: String): Int {
        for (option in options) {
            val i = header.indexOf(option)
            if (i >= 0) return i
        }
        return -1
    }

    internal fun parseCsvRows(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val cell = StringBuilder()
        var quoted = false
        var i = 0

        fun pushCell() {
            row += cell.toString()
            cell.setLength(0)
        }
        fun pushRow() {
            pushCell()
            if (row.any { it.isNotBlank() }) rows += row.toList()
            row.clear()
        }

        while (i < text.length) {
            val c = text[i]
            when {
                c == '"' && quoted && i + 1 < text.length && text[i + 1] == '"' -> {
                    cell.append('"')
                    i++
                }
                c == '"' -> quoted = !quoted
                c == ',' && !quoted -> pushCell()
                (c == '\n' || c == '\r') && !quoted -> {
                    if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                    pushRow()
                }
                else -> cell.append(c)
            }
            i++
        }
        if (cell.isNotEmpty() || row.isNotEmpty()) pushRow()
        return rows
    }
}
