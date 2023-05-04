package tokenutils

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.sql.Time


class JWTManager() {
    private val keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256)

    fun createJWS(issuer: String, subject: String) : String {
        val date = Time(System.currentTimeMillis() + 300000) // 5 minutes
        return Jwts.builder().setIssuer(issuer).setSubject(subject).setExpiration(date).setId(keyPair.public.toString()).signWith(keyPair.private, SignatureAlgorithm.RS256).compact()
    }

    fun validateJWS(jws : String) : Boolean {
        val key = getPublicKey(jws).toByteArray()
        return try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jws)
            true
        } catch (e:JwtException) {
            false
        }
    }

    private fun getPublicKey(jws : String) : String {
        return Jwts.claims().id
    }
}