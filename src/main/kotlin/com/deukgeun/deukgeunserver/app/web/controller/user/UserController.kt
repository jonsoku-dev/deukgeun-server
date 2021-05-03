package com.deukgeun.deukgeunserver.app.web.controller.user

import com.deukgeun.deukgeunserver.app.domain.user.User
import com.deukgeun.deukgeunserver.app.domain.user.UserService
import com.deukgeun.deukgeunserver.app.web.dto.*
import com.deukgeun.deukgeunserver.common.config.argument.resolver.auth.AuthUser
import com.deukgeun.deukgeunserver.common.config.security.AppToken
import com.deukgeun.deukgeunserver.common.exception.BizException
import com.deukgeun.deukgeunserver.common.util.kakao.KakaoOAuth
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.logging.Logger
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val userService: UserService,
    private var kakaoOAuth: KakaoOAuth,
) {
    companion object {
        val LOG: Logger = Logger.getLogger(UserController::class.java.name)
    }

    @PostMapping("/register")
    fun register(
        @RequestBody registerRequestDto: RegisterRequestDto
    ): ResponseEntity<String> {
        userService.register(registerRequestDto)
        return ResponseEntity.ok().body("OK")
    }

    @PostMapping("/login")
    fun login(
        @RequestBody loginRequestDto: LoginRequestDto
    ): ResponseEntity<LoginResponseDto> {
        val token = userService.login(loginRequestDto)
        return ResponseEntity.ok().body(LoginResponseDto(token))
    }

    @GetMapping("/kakaologin")
    fun kakaoLogin(
        @RequestParam("code") code: String,
        ra: RedirectAttributes,
        session: HttpSession,
        response: HttpServletResponse
    ): ResponseDto<AppToken> {
        val kakaoToken = kakaoOAuth.getKakaoAccessToken(code) ?: throw BizException("kakaoToken이 없습니다.")
        println("kakaoToken: $kakaoToken")
        return ResponseDto(data = userService.saveKakaoToken(kakaoToken))
    }

    @GetMapping("/profile")
    fun getMe(@AuthUser user: User): ResponseEntity<User> {
        return ResponseEntity.ok().body(user)
    }

    @GetMapping("/profile2")
    fun getMe2(@RequestBody test: String): ResponseEntity<String> {
        return ResponseEntity.ok().body(test)
    }
}

/**
 * https://kauth.kakao.com/oauth/authorize?client_id=481c793762510daf7d8e29920dcc6ac1&redirect_uri=http://localhost:8080/api/v1/user/kakaologin&response_type=code
 */

/**
 * 1. 카카오로그인
 * 2. 토큰을 발급받는다.
 *
 */