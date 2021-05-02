package com.deukgeun.deukgeunserver.app.web.dto

data class KakaoToken (
    val token_type   : String? = "",
    val access_token : String? = "",
    val expires_in   : Int?    = 0,
    val refresh_token: String? = "",
    val refresh_token_expires_in: Int? = 0
)