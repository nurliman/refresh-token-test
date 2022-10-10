package di

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import model.Credential
import model.RefreshTokenBodyRequest
import model.WrappedResponse
import org.koin.dsl.module

val appModule = module {
    single { Url("http://192.168.81.164:3000/api/") }
    single { mutableListOf<BearerTokens>() }
    single {
        val basePath: Url = get()
        val bearerTokenStorage: MutableList<BearerTokens> = get()

        HttpClient(OkHttp) {
            expectSuccess = false

            defaultRequest {
                url.takeFrom(URLBuilder().takeFrom(basePath).apply {
                    encodedPath += url.encodedPath
                })
            }

            install(ContentNegotiation) {
                json()
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        bearerTokenStorage.lastOrNull()
                    }
                    refreshTokens {
                        val credential: WrappedResponse<Credential> = client.post {
                            url { path("auth", "refresh-token") }
                            contentType(ContentType.Application.Json)
                            setBody(RefreshTokenBodyRequest(oldTokens?.refreshToken ?: ""))
                            markAsRefreshTokenRequest()
                        }.body()

                        credential.data?.let {
                            val newTokens = BearerTokens(it.accessToken, it.refreshToken)
                            bearerTokenStorage.add(newTokens)
                            bearerTokenStorage.last()
                        }
                    }
                    sendWithoutRequest { request ->
                        request.url.host == basePath.host && request.url.pathSegments[2] != "auth"
                    }
                }
            }
        }
    }
}
