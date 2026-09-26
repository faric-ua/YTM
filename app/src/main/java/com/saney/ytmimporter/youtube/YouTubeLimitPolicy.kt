package com.saney.ytmimporter.youtube

enum class YouTubeLimitKind {
    DAILY_QUOTA,
    RATE_LIMIT,
    RESOURCE_LIMIT,
    UNKNOWN_429
}

object YouTubeLimitPolicy {
    fun classify(
        httpCode: Int,
        reason: String?,
        status: String?,
        detailReasons: List<String>,
        message: String
    ): YouTubeLimitKind? {
        val reasons =
            buildList {
                reason
                    ?.takeIf { it.isNotBlank() }
                    ?.let(::add)

                addAll(
                    detailReasons.filter {
                        it.isNotBlank()
                    }
                )
            }
                .map(::normalize)

        val normalizedStatus =
            normalize(
                status.orEmpty()
            )

        val normalizedMessage =
            normalize(
                message
            )

        if (
            reasons.any(
                ::isExplicitDailyQuotaReason
            )
        ) {
            return YouTubeLimitKind.DAILY_QUOTA
        }

        if (
            reasons.any(
                ::isRateLimitReason
            )
        ) {
            return YouTubeLimitKind.RATE_LIMIT
        }

        if (
            reasons.any(
                ::isResourceLimitReason
            )
        ) {
            return YouTubeLimitKind.RESOURCE_LIMIT
        }

        if (
            httpCode != 429 &&
            (
                normalizedMessage.contains(
                    "dailylimit"
                ) ||
                    normalizedMessage.contains(
                        "quotaexceeded"
                    )
                )
        ) {
            return YouTubeLimitKind.DAILY_QUOTA
        }

        if (
            normalizedStatus ==
                "ratelimitexceeded"
        ) {
            return YouTubeLimitKind.RATE_LIMIT
        }

        if (
            normalizedStatus ==
                "resourcequotaexceeded"
        ) {
            return YouTubeLimitKind.RESOURCE_LIMIT
        }

        if (httpCode == 429) {
            return YouTubeLimitKind.UNKNOWN_429
        }

        return null
    }

    private fun isExplicitDailyQuotaReason(
        value: String
    ): Boolean =
        value == "quotaexceeded" ||
            value == "dailylimitexceeded" ||
            value == "dailylimit" ||
            value.contains(
                "variabletermlimit"
            ) ||
            value.contains(
                "variabletermexpireddaily"
            )

    private fun isRateLimitReason(
        value: String
    ): Boolean =
        value == "ratelimitexceeded" ||
            value == "userratelimitexceeded" ||
            value == "toomanyrequests" ||
            value.contains(
                "ratelimit"
            )

    private fun isResourceLimitReason(
        value: String
    ): Boolean =
        value == "resourcequotaexceeded"

    private fun normalize(
        value: String
    ): String =
        value
            .lowercase()
            .filter {
                it.isLetterOrDigit()
            }
}
