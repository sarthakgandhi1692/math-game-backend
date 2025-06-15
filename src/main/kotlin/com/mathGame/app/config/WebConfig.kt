package com.mathGame.app.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Disable favicon requests
        registry.addResourceHandler("/favicon.ico")
            .addResourceLocations("classpath:/static/")
            .resourceChain(false)
    }
} 