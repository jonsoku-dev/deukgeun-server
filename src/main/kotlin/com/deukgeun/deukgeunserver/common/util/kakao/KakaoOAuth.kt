package com.deukgeun.deukgeunserver.common.util.kakao

import com.deukgeun.deukgeunserver.app.web.dto.KakaoToken
import com.deukgeun.deukgeunserver.app.web.dto.KakaoUserInfo
import com.deukgeun.deukgeunserver.common.exception.auth.AccessTokenExpiredException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.NameValuePair
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.logging.Logger

@Component
class KakaoOAuth(
    private val kakaoApi: RestTemplate,
    private val kakaoAuth: RestTemplate,

    @Value("\${oauth.kakao.client-id}")
    var client_id: String
) {
    companion object {
        val LOG: Logger = Logger.getLogger(KakaoOAuth::class.java.name)
        const val KAPI_USER_PROFILE = "/v2/user/me"
        const val KAPI_TOKEN_INFO = "/v1/user/access_token_info"
        const val KAUTH_TOKEN = "/oauth/token"

    }

    fun refreshIfTokenExpired(kakaoToken: KakaoToken): KakaoToken {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer ${kakaoToken.access_token}")

        val request = HttpEntity<MultiValueMap<String, String>>(headers)
        var token = kakaoToken

        try {
            kakaoApi.exchange(KAPI_TOKEN_INFO, HttpMethod.GET, request, Any::class.java)
        } catch (e: AccessTokenExpiredException) {
            token = refreshKakaoToken(kakaoToken)
        }

        return token
    }

    fun getKakaoUserProfile(kakaoToken: KakaoToken): KakaoUserInfo {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set("Authorization", "Bearer ${kakaoToken.access_token}")

        val request = HttpEntity<MultiValueMap<String, String>>(headers)
        val userProfile = kakaoApi.exchange(KAPI_USER_PROFILE, HttpMethod.GET, request, KakaoUserInfo::class.java)
        println("userProfile: $userProfile")
        return userProfile.body!!
    }

    private fun refreshKakaoToken(token: KakaoToken): KakaoToken {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val body = LinkedMultiValueMap(
            mapOf(
                "client_id" to listOf(client_id),
                "refresh_token" to listOf(token.refresh_token!!),
                "grant_type" to listOf("refresh_token")
            )
        )

        val request = HttpEntity<LinkedMultiValueMap<String, String>>(body, headers)
        val response = kakaoAuth.postForEntity(KAUTH_TOKEN, request, KakaoToken::class.java)
        return response.body!!
    }

    fun getKakaoAccessToken(code: String): KakaoToken? {
        val postParams = mutableListOf<NameValuePair>(
            BasicNameValuePair("grant_type", "authorization_code"),
            BasicNameValuePair("client_id", client_id),
            BasicNameValuePair("redirect_uri", "http://localhost:8080/api/v1/user/kakaologin"),
            BasicNameValuePair("code", code)
        )
        println("postParams: ${postParams}")

        val client = HttpClientBuilder.create().build();
        val post = HttpPost("https://kauth.kakao.com/oauth/token")

        var returnNode: JsonNode? = null
        val mapper = ObjectMapper()

        try {
            post.entity = UrlEncodedFormEntity(postParams)
            val response = client.execute(post)
            val responseCode = response.statusLine.statusCode

            returnNode = mapper.readTree(response.entity.content)
            println("returnNode: $returnNode")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: ClientProtocolException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {

        }
        val kakaoToken = mapper.treeToValue(returnNode, KakaoToken::class.java)
        println("kakaoToken: $kakaoToken")
        return kakaoToken
    }
}
