package com.deukgeun.deukgeunserver.app.web.controller.user

import com.deukgeun.deukgeunserver.app.domain.user.UserService
import com.deukgeun.deukgeunserver.app.web.dto.LoginRequestDto
import com.deukgeun.deukgeunserver.app.web.dto.LoginResponseDto
import com.deukgeun.deukgeunserver.app.web.dto.RegisterRequestDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.logging.Logger

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val userService: UserService
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
}