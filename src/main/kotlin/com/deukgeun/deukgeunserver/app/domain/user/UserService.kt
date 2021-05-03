package com.deukgeun.deukgeunserver.app.domain.user

import com.deukgeun.deukgeunserver.app.domain.user.userRole.RoleName
import com.deukgeun.deukgeunserver.app.domain.user.userRole.UserRoleService
import com.deukgeun.deukgeunserver.app.web.dto.KakaoToken
import com.deukgeun.deukgeunserver.app.web.dto.KakaoUserInfo
import com.deukgeun.deukgeunserver.common.config.security.AppToken
import com.deukgeun.deukgeunserver.common.config.security.JwtTokenProvider
import com.deukgeun.deukgeunserver.common.exception.BizException
import com.deukgeun.deukgeunserver.common.util.kakao.KakaoOAuth
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
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
        val LOG = LoggerFactory.getLogger(UserService::class.java)
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
        /** KakaoToken Example
         *  {
         *      "token_type": "bearer",
         *      "access_token": "cVRWtDQYPNsoGW7KmTBixDuFsg4mXdDlGNy89Ao9dZoAAAF5MEhSgw",
         *      "expires_in" : 21599,
         *      "refresh_token": "W-hWQqqdIsX0gt9DvL3XYUdhh6etVdHuK88DYgo9dZoAAAF5MEhSgg",
         *      "refresh_token_expires_in": 5183999,
         *      "scope": "profile"
         *  }
         */
        val token = kakaoOAuth.refreshIfTokenExpired(kakaoToken)

        // [0] KakaoToken 으로 Kakao API 에서 KakaoId 를 가져온다.
        val kakaoId = kakaoOAuth.getKakaoUserProfile(token).id

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

    fun getKakaoUserInfo(user: User): KakaoUserInfo {
        val kakaoToken = KakaoToken(
            refresh_token = user.refreshToken,
            access_token  = user.accessToken
        )

        val token = kakaoOAuth.refreshIfTokenExpired(kakaoToken)

        // 토큰이 만료되었을 때
        if(kakaoToken.access_token.isNullOrBlank() || token.access_token != kakaoToken.access_token) {
            LOG.info("[TOKEN EXPIRE] - ${user.userId}의 카카오 토큰이 만료되어 새로 갱신합니다.")
            updateUserToken(user, token)
        }

        return kakaoOAuth.getKakaoUserProfile(token)
    }
}