import java.security.SecureRandom
import java.util.Base64

// secret 생성기

fun generateSecureSecret(): String {
    val secureRandom = SecureRandom()
    val randomBytes = ByteArray(32) // 256비트 (32바이트) 시크릿 값
    secureRandom.nextBytes(randomBytes)
    return Base64.getEncoder().encodeToString(randomBytes)
}

// main을 실행하면 랜덤 secret 생성
fun main() {
    println(generateSecureSecret())
}
