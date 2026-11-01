package ch.protonmail.android.di

import android.content.Context
import ch.protonmail.android.BuildConfig
import ch.protonmail.android.api.MailRustApiConfig
import ch.protonmail.android.mailcommon.domain.model.ApiEnvironment
import ch.protonmail.android.mailsession.domain.model.RustApiConfig
import ch.protonmail.android.useragent.BuildUserAgent
import ch.protonmail.android.useragent.GetAppVersion
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.android.core.auth.presentation.addaccount.CustomServer
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiConfigurationModule {

    @Provides
    @Singleton
    fun provideRustApiConfig(
        buildUserAgent: BuildUserAgent,
        getAppVersion: GetAppVersion,
        httpUrl: HttpUrl,
        apiEnvironment: ApiEnvironment
    ): RustApiConfig = MailRustApiConfig(buildUserAgent, getAppVersion, httpUrl, apiEnvironment)

    @Provides
    @Singleton
    fun provideApiEnvironment(@ApplicationContext context: Context): ApiEnvironment {
        val customDomain = CustomServer.read(context)
        return ApiEnvironment(
            host = customDomain ?: BuildConfig.API_HOST,
            apiPrefix = BuildConfig.API_PREFIX,
            isCustom = customDomain != null
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object BaseUrlModule {
    @Provides
    @Singleton
    fun provideProtonApiUrl(apiEnvironment: ApiEnvironment): HttpUrl = apiEnvironment.baseUrl.toHttpUrl()
}
