import di.appModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import model.Credential
import model.PatientListItem
import model.SignInBodyRequest
import model.WrappedResponse
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext.startKoin

class MyApplication : KoinComponent {
    private val client: HttpClient by inject()
    private val bearerTokenStorage: MutableList<BearerTokens> by inject()

    suspend fun run() {
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
}


suspend fun main() {
    startKoin {
        modules(
            appModule,
        )
    }

    return MyApplication().run()
}
