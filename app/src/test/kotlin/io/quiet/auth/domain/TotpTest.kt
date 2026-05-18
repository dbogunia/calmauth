package io.quiet.auth.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TotpTest {
    @Test fun `generates stable RFC-like code for known input`() {
        val code = generateTimeBasedCode("JBSWY3DPEHPK3PXP", 0L, digits = 6, period = 30, algorithm = OtpAlgorithm.SHA1)
        assertEquals("282 760", code)
    }

    @Test fun `returns masked default code for invalid secret`() {
        assertEquals("000 000", generateTimeBasedCode("@@@", 0L))
    }

    @Test fun `supports 8 digit output without spacing`() {
        val code = generateTimeBasedCode("JBSWY3DPEHPK3PXP", 0L, digits = 8, period = 30)
        assertTrue("expected 8-digit numeric, was '$code'", code.matches(Regex("^\\d{8}$")))
    }

    @Test fun `generates stable code for 2FAS test secret`() {
        val code = generateTimeBasedCode("2FASTEST", 0L, digits = 6, period = 30, algorithm = OtpAlgorithm.SHA1)
        assertEquals("154 007", code)
    }
}
