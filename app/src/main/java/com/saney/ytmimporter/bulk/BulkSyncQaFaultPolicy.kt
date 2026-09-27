package com.saney.ytmimporter.bulk

enum class BulkSyncQaFaultKind {
    DAILY_QUOTA,
    RATE_LIMIT
}

data class BulkSyncQaFaultPlan(
    val kind: BulkSyncQaFaultKind,
    val remainingAttemptsBeforeFault: Int
)

sealed class BulkSyncQaFaultDecision {
    data class Allow(
        val nextPlan: BulkSyncQaFaultPlan
    ) : BulkSyncQaFaultDecision()

    data class Inject(
        val kind: BulkSyncQaFaultKind
    ) : BulkSyncQaFaultDecision()
}

object BulkSyncQaFaultPolicy {
    fun beforeAttempt(
        plan: BulkSyncQaFaultPlan
    ): BulkSyncQaFaultDecision =
        if (
            plan.remainingAttemptsBeforeFault <= 0
        ) {
            BulkSyncQaFaultDecision.Inject(
                plan.kind
            )
        } else {
            BulkSyncQaFaultDecision.Allow(
                plan.copy(
                    remainingAttemptsBeforeFault =
                        plan.remainingAttemptsBeforeFault - 1
                )
            )
        }
}
