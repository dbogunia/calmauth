package io.quiet.auth.ui.nav

object Routes {
    const val START = "start"
    const val ONBOARDING = "onboarding"
    /** Pattern for [pin] destinations — use with [pin] helper for concrete routes. */
    const val PIN_ROUTE = "pin/{pinMode}"
    const val TWOFAS = "twofas"
    const val BACKUP_PROCESSING = "backup-processing/{action}"
    const val DEVELOPER_MODE = "developer-mode"

    fun pin(pinMode: String) = "pin/$pinMode"

    fun backupProcessing(action: String) = "backup-processing/$action"

    const val ARG_ACTION = "action"
    const val ARG_PIN_MODE = "pinMode"
}

object PinRouteMode {
    const val SETUP = "setup"
    const val UNLOCK = "unlock"
    const val VERIFY_DISABLE = "verify_disable"
}
