package com.safeguard.parentalcontrol.util

import java.security.MessageDigest

object SecurityHelper {

    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(enteredPin: String, storedHash: String?): Boolean {
        if (storedHash == null) return false
        return hashPin(enteredPin) == storedHash
    }
}
