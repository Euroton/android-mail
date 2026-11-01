package me.proton.android.core.auth.presentation.addaccount

import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import java.net.URI
import kotlin.system.exitProcess

/**
 * The server chosen on the welcome screen. Null means the one the build points at.
 *
 * The Rust session reads it once at start-up, so changing it restarts the process.
 */
public object CustomServer {

    private const val PREFS = "custom_server"
    private const val KEY_DOMAIN = "domain"
    private val APP_PREFIXES = listOf("mail-api.", "account-api.", "mail.", "account.", "calendar.")

    public fun read(context: Context): String? = context
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(KEY_DOMAIN, null)

    /** Reduces what the user typed to `domain[:port]`, or null if it isn't a usable address. */
    public fun normalize(input: String): String? {
        val trimmed = input.trim().trimEnd('/')
        if (trimmed.isEmpty() || trimmed.startsWith("http://", ignoreCase = true)) return null
        val withScheme = if (trimmed.contains("://")) trimmed else "https://$trimmed"
        val uri = runCatching { URI(withScheme) }.getOrNull() ?: return null
        val host = uri.host?.lowercase() ?: return null
        if (!uri.scheme.equals("https", ignoreCase = true) || !host.contains('.')) return null
        val domain = APP_PREFIXES.firstOrNull(host::startsWith)?.let(host::removePrefix) ?: host
        return if (uri.port == -1) domain else "$domain:${uri.port}"
    }

    public fun saveAndRestart(context: Context, domain: String?) {
        // commit, because the process exits right after.
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit(commit = true) {
            if (domain == null) remove(KEY_DOMAIN) else putString(KEY_DOMAIN, domain)
        }
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(launch)
        exitProcess(0)
    }
}
