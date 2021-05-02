package com.deukgeun.deukgeunserver.common.config

import com.deukgeun.deukgeunserver.common.config.argument.resolver.auth.AuthorizeArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.format.FormatterRegistry
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport

@Configuration
class WebMvcConfig(
    private val authorizeArgumentResolver: AuthorizeArgumentResolver
): WebMvcConfigurationSupport()  {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/docs/**")
            .addResourceLocations("classpath:/static/docs/")
    }

    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers.add(authorizeArgumentResolver)
        argumentResolvers.add(PageableHandlerMethodArgumentResolver());
    }
}