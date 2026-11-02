package ch.protonmail.android.di

import android.content.Context
import ch.protonmail.android.api.MailRustApiConfig
import ch.protonmail.android.mailcommon.domain.model.ApiEnvironment
import ch.protonmail.android.mailsession.domain.model.RustApiConfig
import ch.protonmail.android.useragent.BuildUserAgent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.proton.android.core.auth.presentation.addaccount.CustomServer
import me.proton.core.configuration.EnvironmentConfiguration
import me.proton.core.configuration.EnvironmentConfigurationDefaults
import okhttp3.HttpUrl
import uniffi.mail_uniffi.setSrpModulusKey
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiConfigurationModule {

    @Provides
    @Singleton
    fun provideRustApiConfig(
        buildUserAgent: BuildUserAgent,
        httpUrl: HttpUrl,
        apiEnvironment: ApiEnvironment
    ): RustApiConfig = MailRustApiConfig(buildUserAgent, httpUrl, apiEnvironment)

    @Provides
    @Singleton
    fun provideApiEnvironment(
        @ApplicationContext context: Context,
        envConfig: EnvironmentConfiguration
    ): ApiEnvironment {
        val customDomain = CustomServer.read(context)
        // Every SRP modulus from a self-hosted server is checked against the key pinned when it was added.
        setSrpModulusKey(if (customDomain != null) CustomServer.readKey(context)?.publicKey else null)
        return if (customDomain != null) {
            ApiEnvironment("https://${EnvironmentConfigurationDefaults.apiPrefix}.$customDomain", isCustom = true)
        } else {
            ApiEnvironment(envConfig.baseUrl)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object BaseUrlModule {
    @Provides
    @Singleton
    fun provideProtonApiUrl(apiEnvironment: ApiEnvironment): HttpUrl = apiEnvironment.baseUrl.toHttpUrl()
}
