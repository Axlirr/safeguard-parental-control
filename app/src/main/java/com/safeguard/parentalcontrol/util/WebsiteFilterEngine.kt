package com.safeguard.parentalcontrol.util

import com.safeguard.parentalcontrol.data.local.entity.BlockedWebsiteEntity

object WebsiteFilterEngine {

    // Common adult & gambling keywords for heuristic protection
    private val BLOCKED_KEYWORDS = listOf(
        "porn", "xxx", "sex", "nude", "erotic", "nsfw",
        "casino", "betting", "gambling", "poker", "slots",
        "onlyfans", "xvideos", "xnxx", "redtube", "youporn"
    )

    data class FilterResult(
        val isBlocked: Boolean,
        val matchedPattern: String = "",
        val category: String = ""
    )

    fun evaluateUrl(
        normalizedUrl: String,
        activeBlockedRules: List<BlockedWebsiteEntity>
    ): FilterResult {
        val cleanUrl = normalizedUrl.lowercase()

        // 1. Check against Parent's configured block rules in Database
        for (rule in activeBlockedRules) {
            val pattern = rule.domainPattern.lowercase().trim()
            if (pattern.isBlank()) continue

            // Exact match or subdomain match (e.g., "m.tiktok.com" matches "tiktok.com")
            if (cleanUrl == pattern || cleanUrl.endsWith(".$pattern") || cleanUrl.contains(pattern)) {
                return FilterResult(
                    isBlocked = true,
                    matchedPattern = rule.domainPattern,
                    category = rule.category
                )
            }
        }

        // 2. Keyword heuristic checks
        for (keyword in BLOCKED_KEYWORDS) {
            if (cleanUrl.contains(keyword)) {
                return FilterResult(
                    isBlocked = true,
                    matchedPattern = keyword,
                    category = "Content Filter ($keyword)"
                )
            }
        }

        return FilterResult(isBlocked = false)
    }
}
