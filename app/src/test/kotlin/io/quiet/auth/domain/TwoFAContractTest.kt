package io.quiet.auth.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TwoFAContractTest {
    @Test fun `normalizes user input for token persistence`() {
        val normalized = normalizeTwoFAInput(
            AddTwoFAInput(
                name = " GitHub ",
                account = " dominik ",
                secret = "ab cd ef",
                digits = 7,
                period = -1,
            )
        )
        assertEquals(
            NormalizedTwoFA(
                name = "GitHub",
                account = "dominik",
                secret = "ABCDEF",
                digits = 6,
                period = 30,
                algorithm = OtpAlgorithm.SHA1,
            ),
            normalized,
        )
    }

    @Test fun `keeps pin hash deterministic and 4-digit constraint`() {
        assertTrue(isPinFormatValid("1234"))
        assertFalse(isPinFormatValid("12345"))
        assertEquals(hashPin("1234"), hashPin("1234"))
    }

    @Test fun `normalizeSecretInput extracts secret from otpauth uri`() {
        val uri = "otpauth://totp/Test%20Token?secret=2FASTEST&issuer=2FAS"
        assertEquals("2FASTEST", normalizeSecretInput(uri))
    }

    @Test fun `normalizeSecretInput rejects invalid otpauth uri`() {
        assertEquals("", normalizeSecretInput("otpauth://totp/NoSecret"))
    }

    @Test fun `manual and qr paths yield same secret for 2FAS test token`() {
        val uri = "otpauth://totp/Test%20Token?secret=2FASTEST&issuer=2FAS"
        val parsed = parseOtpAuthUri(uri)!!
        val fromManual = normalizeTwoFAInput(
            AddTwoFAInput(name = "2FAS", account = "Test", secret = "2FASTEST"),
        )
        val fromQr = normalizeTwoFAInput(
            AddTwoFAInput(
                name = parsed.name,
                account = parsed.account,
                secret = parsed.secret,
                digits = parsed.digits,
                period = parsed.period,
                algorithm = parsed.algorithm,
            ),
        )
        assertEquals("2FASTEST", fromManual.secret)
        assertEquals(fromManual.secret, fromQr.secret)
    }

    @Test fun `pasted otpauth uri must not be reduced to bare base32 strip only`() {
        val uri = "otpauth://totp/Test%20Token?secret=2FASTEST&issuer=2FAS"
        val legacyStripOnly = uri
            .replace("\\s".toRegex(), "")
            .uppercase()
            .replace(Regex("[^A-Z2-7]"), "")
            .replace(Regex("=+$"), "")
        assertNotEquals("2FASTEST", legacyStripOnly)
        assertEquals("2FASTEST", normalizeSecretInput(uri))
    }
}
