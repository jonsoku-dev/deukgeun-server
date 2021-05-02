package com.deukgeun.deukgeunserver.common.config.security

import com.deukgeun.deukgeunserver.app.domain.user.UserDetailsProvider
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


@Component
class JwtTokenProvider(
    private val userDetailsProvider: UserDetailsProvider,
) {
    @Value("\${security.jwt.token.secretKey}")
    lateinit var secretKey: String

    @Value("\${security.jwt.token.expiration-time}")
    lateinit var expireMinute: String

    companion object {
        val LOG = LoggerFactory.getLogger(JwtTokenProvider::class.java)
        const val TOKEN_HEADER = "Authorization"
        const val TIME_ZONE_KST = "Asia/Seoul"
    }

    fun createAppToken(userId: String?): String {
        val payloads: Claims = Jwts.claims()
        val now = LocalDateTime.now().atZone(ZoneId.of(TIME_ZONE_KST))

        val issuedDate = Date.from(now.toInstant())
        val expiredDate = Date.from(now.plusMinutes(expireMinute.toLong()).toInstant())

        payloads["userId"] = userId

        val keyBytes = Decoders.BASE64.decode(secretKey)
        val key = Keys.hmacShaKeyFor(keyBytes)

        return Jwts.builder()
            .setClaims(payloads)
            .setSubject(userId)
            .setIssuedAt(issuedDate)
            .setExpiration(expiredDate)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getAuthentication(token: String): Authentication {
        val userId = getUserId(token)
        val userDetails = userDetailsProvider.loadUserByUsername(userId)

        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    fun validateToken(token: String?): Boolean {
        Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token)
        return true
    }

    private fun getUserId(token: String): String {
        return Jwts
            .parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
            .subject
    }
}
