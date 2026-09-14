package com.saney.ytmimporter.youtube

import kotlin.math.max

object MatchScorer {
    fun score(artist: String, title: String, candidateTitle: String, channel: String): Double {
        val wantedArtist = tokens(artist)
        val wantedTitle = tokens(title)
        val candidate = tokens(candidateTitle + " " + channel)

        val artistScore = overlap(wantedArtist, candidate)
        val titleScore = overlap(wantedTitle, candidate)

        val versionTerms = versionTokens(title)
        val candidateVersionTerms = versionTokens(candidateTitle)
        val versionScore = when {
            versionTerms.isEmpty() -> 1.0
            candidateVersionTerms.containsAll(versionTerms) -> 1.0
            candidateVersionTerms.intersect(versionTerms).isNotEmpty() -> 0.65
            else -> 0.25
        }

        val penalty = when {
            "live" in candidate && "live" !in wantedTitle -> 0.12
            "cover" in candidate && "cover" !in wantedTitle -> 0.25
            else -> 0.0
        }

        return (0.38 * artistScore + 0.47 * titleScore + 0.15 * versionScore - penalty)
            .coerceIn(0.0, 1.0)
    }

    private fun overlap(wanted: Set<String>, got: Set<String>): Double {
        if (wanted.isEmpty()) return 1.0
        val intersection = wanted.intersect(got).size.toDouble()
        return intersection / max(1, wanted.size)
    }

    private fun tokens(value: String): Set<String> =
        normalize(value)
            .split(' ')
            .filter { it.length > 1 && it !in stopWords }
            .toSet()

    private fun versionTokens(value: String): Set<String> {
        val all = tokens(value)
        return all.filterTo(mutableSetOf()) { it in versionWords }
    }

    fun normalize(value: String): String = value
        .lowercase()
        .replace("&", " and ")
        .replace(Regex("feat\\.?|ft\\.?"), " ")
        .replace(Regex("[^a-z0-9а-яіїєґ]+"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")

    private val stopWords = setOf("the", "a", "an", "and", "of", "feat", "ft", "official", "video", "audio", "music")
    private val versionWords = setOf("mix", "remix", "edit", "radio", "extended", "club", "vocal", "dub", "original", "rework", "version")
}
