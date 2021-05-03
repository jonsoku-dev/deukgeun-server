package com.deukgeun.deukgeunserver.common.config.security

import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.BeanIds
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.logout.LogoutFilter
import org.springframework.security.web.csrf.CsrfFilter
import org.springframework.web.filter.CharacterEncodingFilter

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class WebSecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val filterChainExceptionHandler: FilterChainExceptionHandler,
) : WebSecurityConfigurerAdapter() {


    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12)
    }

    @Bean(name = [BeanIds.AUTHENTICATION_MANAGER])
    override fun authenticationManager(): AuthenticationManager {
        return super.authenticationManager()
    }

    override fun configure(web: WebSecurity) {
        web.ignoring()
            .antMatchers("/resources/**")
            .antMatchers("/h2-console/**", "/favicon.ico", "/error")

    }

    override fun configure(http: HttpSecurity) {
        val filter = CharacterEncodingFilter()
        filter.encoding = "UTF-8"
        filter.setForceEncoding(true)

        // Apply JWT
        http.apply(JwtTokenFilterConfigurer(jwtTokenProvider));

        // [1] X-Frame-Options 비활성화
        http.headers().frameOptions().disable()

        // [2] CSRF 방지 비활성화
        http.csrf().disable()

        // [3] Spring Security가 세션을 생성하지 않음
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        // [4] entry point
        http.authorizeRequests()
            .antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .antMatchers("/api/v1/user/login").permitAll()
            .antMatchers("/api/v1/user/register").permitAll()
            .anyRequest().authenticated()
            .and().exceptionHandling().accessDeniedHandler(CustomAccessDeniedHandler())
            .and().exceptionHandling().authenticationEntryPoint(CustomAuthenticationEntryPoint())

        http.addFilterBefore(filterChainExceptionHandler, LogoutFilter::class.java)
    }
}