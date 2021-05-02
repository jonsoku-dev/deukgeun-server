package com.deukgeun.deukgeunserver.common.exception.auth

import com.deukgeun.deukgeunserver.common.exception.BizException
import org.springframework.http.HttpStatus

class AccessTokenExpiredException(
    message: String,
    httpStatus: HttpStatus
) : BizException(message, httpStatus)