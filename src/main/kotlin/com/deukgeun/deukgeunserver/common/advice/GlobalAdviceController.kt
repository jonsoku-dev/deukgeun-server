package com.deukgeun.deukgeunserver.common.advice

import com.deukgeun.deukgeunserver.common.exception.BizException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
@RestController
class GlobalAdviceController {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(GlobalAdviceController::class.java)
    }

    @ExceptionHandler(BizException::class)
    fun bizExceptionAdvice(e: BizException, webRequest: WebRequest): ResponseEntity<ErrorResponse> {
        LOG.error("BizExceptionAdvice 입니당")
        LOG.error("${e.message}\n${e.stackTrace.joinToString("\n")}")
        return ResponseEntity(ErrorResponse(e.message), e.httpStatus)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun globalMethodArgumentNotMatchAdvice(e: Exception, webRequest: WebRequest): ResponseEntity<ErrorResponse> {
        LOG.error("globalMethodArgumentNotMatchAdvice 입니당")
        LOG.error("${e.cause?.message ?: ""}\n${e.cause?.stackTrace?.joinToString("\n") ?: ""}")
        return ResponseEntity(ErrorResponse(message = e.cause?.cause?.message ?: ""), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun globalExceptionAdvice(e: Exception, webRequest: WebRequest): ResponseEntity<ErrorResponse> {
        LOG.error("globalExceptionAdvice 입니당")
        LOG.error("${e.message}\n${e.stackTrace.joinToString("\n")}")
        return ResponseEntity(ErrorResponse(e.stackTrace), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(value = [BindException::class, MethodArgumentNotValidException::class, HttpMessageNotReadableException::class])
    fun handleMethodArgumentNotValidException(e: Exception): ResponseEntity<*> {
        LOG.error("handleMethodArgumentNotValidException 입니당")
        LOG.error("${e.message}\n${e.stackTrace.joinToString("\n")}")

        var errorMessage: String = when (e) {
            is BindException -> {
                e.bindingResult.allErrors[0].defaultMessage?: ""
            }
            is MethodArgumentNotValidException -> {
                e.bindingResult.allErrors[0].defaultMessage?: ""
            }
            else -> {
                e.message?: ""
            }
        }

        return ResponseEntity(ErrorResponse(errorMessage), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ExpiredJwtException::class)
    fun handleExpiredJwtException(e: Exception): ResponseEntity<ErrorResponse> = ResponseEntity(ErrorResponse("JWT 토큰이 만료되었습니다."), HttpStatus.UNAUTHORIZED)

    @ExceptionHandler(JwtException::class)
    fun handleJwtException(e: Exception): ResponseEntity<ErrorResponse> = ResponseEntity(ErrorResponse("JWT 토큰이 유효하지 않습니다"), HttpStatus.UNAUTHORIZED)
}