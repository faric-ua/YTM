package com.saney.ytmimporter.storage

object YtmArtifactClassificationCache {
    private const val MAX_ENTRIES =
        256

    private data class Key(
        val uri: String,
        val lastModified: Long,
        val size: Long?
    )

    private val entries =
        LinkedHashMap<
            Key,
            YtmArtifactType
        >(
            32,
            0.75f,
            true
        )

    @Synchronized
    fun get(
        uri: String,
        lastModified: Long,
        size: Long?
    ): YtmArtifactType? =
        entries[
            Key(
                uri = uri,
                lastModified = lastModified,
                size = size
            )
        ]

    @Synchronized
    fun put(
        uri: String,
        lastModified: Long,
        size: Long?,
        type: YtmArtifactType
    ) {
        entries
            .keys
            .removeAll {
                it.uri == uri &&
                    (
                        it.lastModified !=
                            lastModified ||
                            it.size != size
                    )
            }

        entries[
            Key(
                uri = uri,
                lastModified = lastModified,
                size = size
            )
        ] =
            type

        while (
            entries.size >
                MAX_ENTRIES
        ) {
            val eldest =
                entries
                    .entries
                    .iterator()

            if (
                eldest.hasNext()
            ) {
                eldest.next()
                eldest.remove()
            } else {
                break
            }
        }
    }

    @Synchronized
    fun clearForTests() {
        entries.clear()
    }

    @Synchronized
    fun sizeForTests():
        Int =
        entries.size
}
