package me.proton.android.core.auth.presentation.addaccount

import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import kotlin.system.exitProcess

/**
 * The server chosen on the welcome screen. Null means the one the build points at.
 *
 * The Rust session reads it once at start-up, so changing it restarts the process.
 */
public object CustomServer {

    private const val PREFS = "custom_server"
    private const val KEY_DOMAIN = "domain"
    private const val KEY_MODULUS_KEY = "modulus_key"
    private const val KEY_FINGERPRINT = "modulus_key_fingerprint"
    private const val TIMEOUT_MS = 15_000

    /** The key a self-hosted server signs its SRP moduli with, pinned when the server is added. */
    public data class ServerKey(val publicKey: String, val fingerprint: String)
    private val APP_PREFIXES = listOf("mail-api.", "account-api.", "mail.", "account.", "calendar.")

    public fun read(context: Context): String? = context
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(KEY_DOMAIN, null)

    public fun readKey(context: Context): ServerKey? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = prefs.getString(KEY_MODULUS_KEY, null) ?: return null
        return ServerKey(key, prefs.getString(KEY_FINGERPRINT, null).orEmpty())
    }

    /** Fetches the server's modulus signing key. Runs over the same TLS checks as the rest of the app. */
    public suspend fun fetchKey(domain: String): Result<ServerKey> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL("https://mail-api.$domain/core/v4/auth/modulus/key")
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                check(connection.responseCode == HttpURLConnection.HTTP_OK) { "HTTP ${connection.responseCode}" }
                val body = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                ServerKey(body.getString("PublicKey"), body.getString("Fingerprint"))
            } finally {
                connection.disconnect()
            }
        }
    }

    /** `ABCD 1234 ...`, so a person can compare it against what the server's admin publishes. */
    public fun formatFingerprint(fingerprint: String): String =
        fingerprint.uppercase().chunked(4).joinToString(" ")

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

    public fun saveAndRestart(context: Context, domain: String?, key: ServerKey? = null) {
        // commit, because the process exits right after.
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit(commit = true) {
            if (domain == null) remove(KEY_DOMAIN) else putString(KEY_DOMAIN, domain)
            if (domain == null || key == null) {
                remove(KEY_MODULUS_KEY)
                remove(KEY_FINGERPRINT)
            } else {
                putString(KEY_MODULUS_KEY, key.publicKey)
                putString(KEY_FINGERPRINT, key.fingerprint)
            }
        }
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(launch)
        exitProcess(0)
    }
}
