package io.quiet.auth.domain

enum class OtpAlgorithm {
    SHA1, SHA256, SHA512;

    companion object {
        fun fromRaw(value: String?): OtpAlgorithm = when (value?.uppercase()) {
            "SHA256" -> SHA256
            "SHA512" -> SHA512
            else -> SHA1
        }
    }
}

data class TwoFAItem(
    val id: String,
    val name: String,
    val account: String,
    val secret: String,
    val digits: Int = 6,
    val period: Int = 30,
    val algorithm: OtpAlgorithm = OtpAlgorithm.SHA1,
)

data class AddTwoFAInput(
    val name: String,
    val account: String,
    val secret: String,
    val digits: Int? = null,
    val period: Int? = null,
    val algorithm: OtpAlgorithm? = null,
)

data class NormalizedTwoFA(
    val name: String,
    val account: String,
    val secret: String,
    val digits: Int,
    val period: Int,
    val algorithm: OtpAlgorithm,
)

private val NON_BASE32_CHARS = Regex("[^A-Z2-7]")
private val TRAILING_BASE32_PADDING = Regex("=+$")

/**
 * Normalizes a manual secret or otpauth URI into a canonical Base32 secret string.
 * Returns an empty string when the input cannot yield a valid secret.
 */
fun normalizeSecretInput(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.startsWith("otpauth://", ignoreCase = true)) {
        val parsed = parseOtpAuthUri(trimmed) ?: return ""
        return sanitizeBase32Secret(parsed.secret)
    }
    return sanitizeBase32Secret(trimmed)
}

private fun sanitizeBase32Secret(raw: String): String =
    raw.replace("\\s".toRegex(), "")
        .uppercase()
        .replace(NON_BASE32_CHARS, "")
        .replace(TRAILING_BASE32_PADDING, "")

fun normalizeTwoFAInput(input: AddTwoFAInput): NormalizedTwoFA = NormalizedTwoFA(
    name = input.name.trim(),
    account = input.account.trim(),
    secret = normalizeSecretInput(input.secret),
    digits = if (input.digits == 8) 8 else 6,
    period = input.period?.takeIf { it > 0 } ?: 30,
    algorithm = input.algorithm ?: OtpAlgorithm.SHA1,
)
