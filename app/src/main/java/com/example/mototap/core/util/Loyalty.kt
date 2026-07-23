package com.example.mototap.core.util

import com.example.mototap.core.model.JobRequest
import com.example.mototap.core.model.JobStatus
import com.example.mototap.core.model.UserProfile

/** Matches web `js/utils/loyalty.js`: earned from completed jobs, minus redeemed rewards. */
const val POINTS_PER_SERVICE = 10

private val EARNING_JOB_STATUSES = setOf(
    JobStatus.COMPLETED,
    JobStatus.PAID,
    JobStatus.CLOSED,
)

data class LoyaltyBalance(
    val completedServices: Int,
    val earned: Int,
    val redeemed: Int,
    val available: Int,
)

fun getEarningServiceCount(jobs: List<JobRequest>): Int =
    jobs.count { it.status in EARNING_JOB_STATUSES }

fun getRedeemedPoints(profile: UserProfile?): Int =
    profile?.redeemedRewards?.sumOf { it.points } ?: 0

fun computeLoyalty(profile: UserProfile?, jobs: List<JobRequest>): LoyaltyBalance {
    val completedServices = getEarningServiceCount(jobs)
    val earned = completedServices * POINTS_PER_SERVICE
    val redeemed = getRedeemedPoints(profile)
    val available = (earned - redeemed).coerceAtLeast(0)
    return LoyaltyBalance(
        completedServices = completedServices,
        earned = earned,
        redeemed = redeemed,
        available = available,
    )
}
