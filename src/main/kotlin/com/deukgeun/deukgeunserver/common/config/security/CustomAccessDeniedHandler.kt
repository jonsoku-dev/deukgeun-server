package com.deukgeun.deukgeunserver.common.config.security

import com.deukgeun.deukgeunserver.common.exception.BizException
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CustomAccessDeniedHandler : AccessDeniedHandler {
    override fun handle(request: HttpServletRequest?, response: HttpServletResponse?, accessDeniedException: AccessDeniedException?) {
//        response?.sendError(HttpServletResponse.SC_FORBIDDEN, "AccessDenied")
        throw BizException("fuck", HttpStatus.FORBIDDEN)
    }
}