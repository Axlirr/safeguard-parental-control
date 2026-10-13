package com.safeguard.parentalcontrol.util

import android.view.accessibility.AccessibilityNodeInfo

object BrowserUrlDetector {

    // Supported browser packages
    val SUPPORTED_BROWSERS = setOf(
        "com.android.chrome",
        "com.chrome.beta",
        "com.chrome.dev",
        "com.chrome.canary",
        "org.mozilla.firefox",
        "com.microsoft.emmx",
        "com.brave.browser",
        "com.opera.browser",
        "com.opera.mini.native",
        "com.sec.android.app.sbrowser",
        "com.duckduckgo.mobile.android"
    )

    // Known URL bar view IDs across browsers
    private val URL_BAR_IDS = listOf(
        "com.android.chrome:id/url_bar",
        "com.android.chrome:id/search_box_text",
        "com.microsoft.emmx:id/url_bar",
        "com.brave.browser:id/url_bar",
        "com.sec.android.app.sbrowser:id/location_bar_edit_text",
        "org.mozilla.firefox:id/toolbar",
        "org.mozilla.firefox:id/url_bar_title",
        "com.opera.browser:id/url_field"
    )

    fun isBrowserPackage(packageName: CharSequence?): Boolean {
        if (packageName == null) return false
        return SUPPORTED_BROWSERS.contains(packageName.toString())
    }

    /**
     * Extracts the active URL or hostname from the browser's accessibility node hierarchy.
     */
    fun extractUrl(rootNode: AccessibilityNodeInfo?, packageName: String): String? {
        if (rootNode == null) return null

        // 1. Direct ID lookup
        for (viewId in URL_BAR_IDS) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
            if (!nodes.isNullOrEmpty()) {
                for (node in nodes) {
                    val text = node.text?.toString()
                    if (!text.isNullOrBlank()) {
                        return normalizeUrl(text)
                    }
                }
            }
        }

        // 2. Fallback heuristic: Traverse nodes to find EditText or text resembling a domain/URL
        return findUrlRecursive(rootNode)
    }

    private fun findUrlRecursive(node: AccessibilityNodeInfo?): String? {
        if (node == null) return null

        val text = node.text?.toString()?.trim()
        if (!text.isNullOrBlank()) {
            if (looksLikeWebAddress(text)) {
                return normalizeUrl(text)
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val found = findUrlRecursive(child)
            if (found != null) return found
        }
        return null
    }

    private fun looksLikeWebAddress(text: String): Boolean {
        val lower = text.lowercase()
        if (lower.startsWith("http://") || lower.startsWith("https://")) return true
        if (lower.contains(".com") || lower.contains(".org") || lower.contains(".net") ||
            lower.contains(".io") || lower.contains(".tv") || lower.contains(".co") ||
            lower.contains(".gov") || lower.contains(".edu") || lower.contains(".xyz") ||
            lower.contains(".ru") || lower.contains(".cn") || lower.contains(".info")
        ) {
            return !text.contains(" ") // URLs typically don't have spaces in host
        }
        return false
    }

    fun normalizeUrl(rawText: String): String {
        return rawText.trim()
            .lowercase()
            .replace("https://", "")
            .replace("http://", "")
            .replace("www.", "")
            .split("/")[0]
            .split("?")[0]
            .split("#")[0]
    }
}
