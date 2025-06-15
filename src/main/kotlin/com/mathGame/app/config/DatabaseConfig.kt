package com.mathGame.app.config

import com.mathGame.app.constants.DatabaseConstants
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import com.zaxxer.hikari.HikariDataSource

@Configuration
class DatabaseConfig {
    
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    fun dataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @Primary
    fun dataSource(properties: DataSourceProperties): HikariDataSource {
        return properties.initializeDataSourceBuilder()
            .type(HikariDataSource::class.java)
            .build()
            .apply {
                maximumPoolSize = DatabaseConstants.MAX_POOL_SIZE
                minimumIdle = DatabaseConstants.MIN_IDLE_CONNECTIONS
                idleTimeout = DatabaseConstants.IDLE_TIMEOUT
                connectionTimeout = DatabaseConstants.CONNECTION_TIMEOUT
                maxLifetime = DatabaseConstants.MAX_LIFETIME
            }
    }
} 