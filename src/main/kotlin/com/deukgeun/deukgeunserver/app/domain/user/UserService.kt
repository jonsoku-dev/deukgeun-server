package com.deukgeun.deukgeunserver.app.domain.user

import com.deukgeun.deukgeunserver.app.domain.user.userRole.RoleName
import com.deukgeun.deukgeunserver.app.domain.user.userRole.UserRoleService
import com.deukgeun.deukgeunserver.app.web.dto.KakaoToken
import com.deukgeun.deukgeunserver.common.config.security.AppToken
import com.deukgeun.deukgeunserver.common.config.security.JwtTokenProvider
import com.deukgeun.deukgeunserver.common.exception.BizException
import com.deukgeun.deukgeunserver.common.util.kakao.KakaoOAuth
import org.springframework.http.HttpStatus
wimport org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.logging.Logger

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val userRoleService: UserRoleService,
    private val jwtTokenProvider: JwtTokenProvider,
    private var kakaoOAuth: KakaoOAuth,
) {
    companion object {
        val LOG: Logger = Logger.getLogger(UserService::class.java.name)
    }

    @Transactional
    fun updateUserToken(user: User, token: KakaoToken) {
        val targetUser = userRepository.findByUserId(user.userId)!!
        targetUser.accessToken = token.access_token

        if(token.refresh_token?.isNotBlank()!!) {
            targetUser.refreshToken = token.refresh_token
        }
        userRepository.save(targetUser)
    }

    @Transactional
    fun saveKakaoToken(kakaoToken: KakaoToken): AppToken {
        println("saveKakaoToken.kakaoToken: $kakaoToken")
        val token = kakaoOAuth.refreshIfTokenExpired(kakaoToken)
        println("token: $token")
        val kakaoId = kakaoOAuth.getKakaoUserProfile(token).id
        println("kakaoId: $kakaoId")

        // [1] kakao 유저 존재 x
        if (kakaoId.isBlank()) {
            LOG.info("unknown kakao token received")
            throw BizException("존재하지 않는 kakao userid입니다", HttpStatus.NOT_FOUND)
        }

        var user: User? = userRepository.findByUserId(kakaoId)

        // [2] 유저 최초 가입시
        if (user == null) {
            user = User().apply {
                this.userId = kakaoId
                this.accessToken = token.access_token
                this.refreshToken = token.refresh_token
                this.userType = UserType.KAKAO
            }
            userRepository.save(user)
            userRoleService.addRole(user, RoleName.MEMBER)
        }

        // 토큰이 만료되었을 때
        if (token.access_token != kakaoToken.access_token) {
            LOG.info("[TOKEN EXPIRE] - ${user.userId}의 카카오 토큰이 만료되어 새로 갱신합니다.")
            updateUserToken(user, token)
        }

        user.accessToken = token.access_token
        user.refreshToken = token.refresh_token

        return AppToken(
            user.registered,
            jwtTokenProvider.createAppToken(user.userId)
        )
    }
}