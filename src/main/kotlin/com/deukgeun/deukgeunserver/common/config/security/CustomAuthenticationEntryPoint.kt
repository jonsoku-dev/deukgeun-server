package com.deukgeun.deukgeunserver.common.config.security

import com.deukgeun.deukgeunserver.common.exception.BizException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.web.servlet.HandlerExceptionResolver
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CustomAuthenticationEntryPoint : AuthenticationEntryPoint {

    override fun commence(request: HttpServletRequest, response: HttpServletResponse, authException: AuthenticationException?) {
//        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        throw BizException("fuck Unauthorized", HttpStatus.UNAUTHORIZED)
    }
}