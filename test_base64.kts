import java.util.Base64

fun main() {
    val unpadded = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjYxNDg4NjMzNWNjNDhmNGMyNmNjZjQyZGU1ODBkZGMwODhiNDNiMTEiLCJ0eXAiOiJKV1QifQ" // Contains no padding
    try {
        val result = Base64.getUrlDecoder().decode(unpadded)
        println("Success: " + String(result))
    } catch (e: Exception) {
        println("Failed: ${e.message}")
    }
}
