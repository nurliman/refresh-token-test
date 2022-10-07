import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
data class WrappedResponse<T>(
    var status: Boolean,
    var data: T? = null,
    var error: String? = null,
)

@Serializable
data class Credential(
    val id: String,
    val username: String,
    val accessToken: String,
    val refreshToken: String,
    val roles: List<String>,
)

@Serializable
data class SignInBodyRequest(
    val username: String,
    val password: String,
)

@Serializable
data class RefreshTokenBodyRequest(
    val refreshToken: String,
)

object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) {
        val localDateTime = value.atStartOfDay()
        encoder.encodeString(localDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString(), DateTimeFormatter.ISO_DATE_TIME)
    }
}

@Serializable
data class PatientListItem(
    val id: String,
    val name: String,
    val address: String,
    @Serializable(LocalDateSerializer::class) val dateOfBirth: LocalDate,
)

val BASE_PATH = Url("http://172.17.132.139:3000/api/")

suspend fun main() {
    val bearerTokenStorage = mutableListOf<BearerTokens>()

    val client = HttpClient(OkHttp) {
        defaultRequest {
            url.takeFrom(URLBuilder().takeFrom(BASE_PATH).apply {
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
                    request.url.host == BASE_PATH.host && request.url.pathSegments[2] != "auth"
                }
            }
        }
    }

    println("username:")
    val username = readln()

    println("password:")
    val password = readln()

    val credential: WrappedResponse<Credential> = client.post {
        url { path("auth", "signin") }
        contentType(ContentType.Application.Json)
        setBody(SignInBodyRequest(username, password))
    }.body()

    credential.data?.let {
        val tokens = BearerTokens(it.accessToken, it.refreshToken)
        bearerTokenStorage.add(tokens)
    }

    while (true) {
        println("Make a request? Type 'yes' and press Enter to proceed.")
        when (readln()) {
            "yes" -> {
                val response: HttpResponse = client.get {
                    url { path("patients") }
                }
                try {
                    val patientsResponse: WrappedResponse<List<PatientListItem>> = response.body()
                    val patients = patientsResponse.data ?: emptyList()
                    println("Patients count: ${patients.size}")
                } catch (e: Exception) {
                    val errorInfo: Any = response.body()
                    println(errorInfo)
                }
            }

            else -> return
        }
    }
}
