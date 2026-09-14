package com.saney.ytmimporter.youtube

import java.text.Normalizer
import kotlin.math.max

object MatchScorer {
    fun score(
        artist: String,
        title: String,
        candidateTitle: String,
        channel: String
    ): Double {
        val wantedArtist = tokens(artist)
        val wantedPrimaryArtist = primaryArtistTokens(artist)
        val wantedTitle = tokens(title)

        val candidateTitleTokens = tokens(candidateTitle)
        val channelTokens = tokens(channel)
        val candidateAll = candidateTitleTokens + channelTokens

        val artistScore = max(
            overlap(wantedPrimaryArtist, candidateAll),
            overlap(wantedArtist, candidateAll) * 0.92
        )

        // Artist names and generic version words should not make a title look less similar.
        // Specific remix/remixer names remain and therefore still influence matching.
        val wantedCoreTitle = wantedTitle - versionWords
        val candidateCoreTitle = candidateTitleTokens - wantedArtist - versionWords

        val titleRecall = overlap(wantedCoreTitle, candidateCoreTitle)
        val titleF1 = f1(wantedCoreTitle, candidateCoreTitle)
        val titleScore = 0.65 * titleRecall + 0.35 * titleF1

        val wantedVersions = wantedTitle.intersect(versionWords)
        val candidateVersions = candidateTitleTokens.intersect(versionWords)

        val versionScore = when {
            wantedVersions.isEmpty() && candidateVersions.isEmpty() -> 1.0
            wantedVersions.isEmpty() -> 0.88
            candidateVersions.containsAll(wantedVersions) -> 1.0
            candidateVersions.intersect(wantedVersions).isNotEmpty() -> 0.62
            else -> 0.20
        }

        val sourceScore = sourceQuality(
            artistTokens = wantedPrimaryArtist,
            candidateTitle = candidateTitle,
            channel = channel
        )

        val penalty = noisePenalty(wantedTitle, candidateAll)

        val exactCoreBonus =
            if (wantedCoreTitle.isNotEmpty() && wantedCoreTitle == candidateCoreTitle) 0.035 else 0.0

        return (
            0.32 * artistScore +
            0.46 * titleScore +
            0.14 * versionScore +
            0.08 * sourceScore +
            exactCoreBonus -
            penalty
        ).coerceIn(0.0, 1.0)
    }

    private fun sourceQuality(
        artistTokens: Set<String>,
        candidateTitle: String,
        channel: String
    ): Double {
        val normalizedTitle = normalize(candidateTitle)
        val normalizedChannel = normalize(channel)
        val channelTokens = tokens(channel)

        val artistChannelScore =
            if (artistTokens.isEmpty()) 0.0 else overlap(artistTokens, channelTokens)

        return when {
            "topic" in channelTokens -> 1.00
            "vevo" in channelTokens -> 0.96
            "official" in normalizedChannel -> 0.93
            "official" in normalizedTitle &&
                ("audio" in normalizedTitle || "video" in normalizedTitle) -> 0.90
            artistChannelScore >= 0.75 -> 0.86
            artistChannelScore >= 0.50 -> 0.80
            channelTokens.any { it in labelWords } -> 0.66
            else -> 0.45
        }
    }

    private fun noisePenalty(
        wantedTitle: Set<String>,
        candidateAll: Set<String>
    ): Double {
        var penalty = 0.0

        for ((word, value) in noisePenalties) {
            if (word in candidateAll && word !in wantedTitle) {
                penalty += value
            }
        }

        return penalty.coerceAtMost(0.42)
    }

    private fun overlap(wanted: Set<String>, got: Set<String>): Double {
        if (wanted.isEmpty()) return 1.0
        val intersection = wanted.intersect(got).size.toDouble()
        return intersection / max(1, wanted.size)
    }

    private fun f1(wanted: Set<String>, got: Set<String>): Double {
        if (wanted.isEmpty()) return 1.0
        if (got.isEmpty()) return 0.0

        val intersection = wanted.intersect(got).size.toDouble()
        if (intersection == 0.0) return 0.0

        val precision = intersection / got.size
        val recall = intersection / wanted.size
        return 2.0 * precision * recall / (precision + recall)
    }

    private fun primaryArtistTokens(value: String): Set<String> {
        val primary = value
            .split(
                Regex("(?i)\\b(?:feat\\.?|ft\\.?|featuring)\\b"),
                limit = 2
            )
            .firstOrNull()
            .orEmpty()

        return tokens(primary)
    }

    private fun tokens(value: String): Set<String> =
        normalize(value)
            .split(' ')
            .filter { it.length > 1 && it !in stopWords }
            .toSet()

    fun normalize(value: String): String {
        val withoutDiacritics = Normalizer
            .normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")

        return withoutDiacritics
            .lowercase()
            .replace("&", " and ")
            .replace(Regex("\\b(?:feat\\.?|ft\\.?|featuring)\\b"), " ")
            .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    private val stopWords = setOf(
        "the", "a", "an", "and", "of",
        "feat", "ft", "featuring",
        "official", "video", "audio", "music",
        "lyrics", "lyric", "hq", "hd", "full",
        "vs", "versus"
    )

    private val versionWords = setOf(
        "mix", "remix", "edit", "radio", "extended",
        "club", "vocal", "dub", "original", "rework",
        "version", "remaster", "remastered",
        "acoustic", "instrumental"
    )

    private val noisePenalties = mapOf(
        "live" to 0.18,
        "cover" to 0.25,
        "karaoke" to 0.30,
        "reaction" to 0.30,
        "tutorial" to 0.30,
        "nightcore" to 0.25,
        "slowed" to 0.22,
        "sped" to 0.22,
        "mashup" to 0.16,
        "bootleg" to 0.14
    )

    private val labelWords = setOf(
        "records", "recordings", "label",
        "armada", "anjunabeats", "blackhole",
        "spinnin", "ministry", "defected"
    )
}
