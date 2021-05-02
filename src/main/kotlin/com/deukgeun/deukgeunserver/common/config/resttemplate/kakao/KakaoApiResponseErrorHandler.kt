package com.deukgeun.deukgeunserver.common.config.resttemplate.kakao

import com.deukgeun.deukgeunserver.app.web.dto.KakaoOAuthResponse
import com.deukgeun.deukgeunserver.common.exception.auth.AccessTokenExpiredException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.Series.CLIENT_ERROR
import org.springframework.http.HttpStatus.Series.SERVER_ERROR
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.ResponseErrorHandler
import java.util.logging.Logger

@Component
class KakaoApiResponseErrorHandler(
    val objectMapper: ObjectMapper
) : ResponseErrorHandler {

    companion object {
        val LOG: Logger = Logger.getLogger(KakaoApiResponseErrorHandler::class.java.name)
    }

    override fun hasError(response: ClientHttpResponse): Boolean {
        return (
                response.statusCode.series() == CLIENT_ERROR // 400 번대
                        || response.statusCode.series() == SERVER_ERROR // 500 번대
                )
    }

    override fun handleError(response: ClientHttpResponse) {
        val msg = "[KAKAO-API-ERROR]: 원인 ${response.statusCode} ${response.rawStatusCode} ${response.statusText}"
        LOG.info(msg)

        when (response.statusCode) {
            HttpStatus.UNAUTHORIZED, HttpStatus.BAD_REQUEST -> throw AccessTokenExpiredException(
                "Access Token Expired",
                HttpStatus.UNAUTHORIZED
            )
            else -> {
                val authResponse = objectMapper.readValue(response.body, KakaoOAuthResponse::class.java)
                LOG.info("[KAKAO-API-ERROR]: 원인 ${authResponse.code}-${authResponse.msg}")
                throw Exception()
            }
        }
    }
}