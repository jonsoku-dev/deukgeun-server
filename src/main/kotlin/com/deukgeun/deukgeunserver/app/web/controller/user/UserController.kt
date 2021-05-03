package com.deukgeun.deukgeunserver.app.web.controller.user

import com.deukgeun.deukgeunserver.app.domain.user.User
import com.deukgeun.deukgeunserver.app.domain.user.UserService
import com.deukgeun.deukgeunserver.app.web.dto.*
import com.deukgeun.deukgeunserver.common.config.argument.resolver.auth.AuthUser
import com.deukgeun.deukgeunserver.common.config.security.AppToken
import com.deukgeun.deukgeunserver.common.exception.BizException
import com.deukgeun.deukgeunserver.common.util.kakao.KakaoOAuth
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val userService: UserService,
    private var kakaoOAuth: KakaoOAuth,
) {
    companion object {
        val LOG = LoggerFactory.getLogger(UserController::class.java)
    }

    /**
     * https://kauth.kakao.com/oauth/authorize?client_id=481c793762510daf7d8e29920dcc6ac1&redirect_uri=http://localhost:8080/api/v1/user/kakao-code&response_type=code
     */
    @GetMapping("/kakao-code")
    fun kakaoLogin(
        @RequestParam("code") code: String,
        ra: RedirectAttributes,
        session: HttpSession,
        response: HttpServletResponse
    ): ResponseDto<AppToken> {
        val kakaoToken = kakaoOAuth.getKakaoAccessToken(code) ?: throw BizException("kakaoToken이 없습니다.")
        LOG.debug("kakaoToken: $kakaoToken")
        return ResponseDto(data = userService.saveKakaoToken(kakaoToken))
    }

    @PostMapping("/kakao-auth")
    fun kakaoAuth(@Valid @RequestBody token: KakaoToken): ResponseDto<AppToken> {
        LOG.debug("token: $token")
        return ResponseDto(data = userService.saveKakaoToken(token))
    }

    @GetMapping("/kakao-profile")
    fun getKakaoUserInfo(@AuthUser user: User): ResponseDto<KakaoUserInfo> {
        return ResponseDto(data = userService.getKakaoUserInfo(user))
    }
}