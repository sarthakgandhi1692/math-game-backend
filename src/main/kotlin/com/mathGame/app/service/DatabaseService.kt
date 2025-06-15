package com.mathGame.app.service

import com.mathGame.app.config.SupabaseProperties
import org.springframework.stereotype.Service
import java.sql.Connection
import java.sql.DriverManager
import javax.sql.DataSource
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.slf4j.LoggerFactory
import jakarta.annotation.PostConstruct
import java.net.URI

@Service
class DatabaseService(private val supabaseProperties: SupabaseProperties) {
    
    private val logger = LoggerFactory.getLogger(DatabaseService::class.java)
    
    private val dbConfig = supabaseProperties.database
    private val dataSource: DataSource by lazy {
        logger.info("Initializing database connection to ${dbConfig.url}")
        try {
            val uri = URI(dbConfig.url.replace("jdbc:", ""))
            logger.info("Attempting to connect to host: ${uri.host}:${uri.port}")
        } catch (e: Exception) {
            logger.error("Failed to parse database URL: ${e.message}")
        }
        
        DriverManagerDataSource().apply {
            setDriverClassName("org.postgresql.Driver")
            url = dbConfig.url
            username = dbConfig.username
            password = dbConfig.password
        }
    }

    @PostConstruct
    fun validateConnection() {
        try {
            getConnection().use { connection ->
                val databaseName = connection.catalog
                val databaseVersion = connection.metaData.databaseProductVersion
                logger.info("Successfully connected to database: $databaseName (Version: $databaseVersion)")
            }
        } catch (e: Exception) {
            logger.error("Failed to establish database connection: ${e.message}", e)
            throw e
        }
    }

    fun getConnection(): Connection {
        return dataSource.connection
    }

    // Example method to execute a query
    fun executeQuery(query: String, params: List<Any> = emptyList()): List<Map<String, Any>> {
        logger.debug("Executing query: $query with params: $params")
        getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                params.forEachIndexed { index, param ->
                    statement.setObject(index + 1, param)
                }
                
                val resultSet = statement.executeQuery()
                val results = mutableListOf<Map<String, Any>>()
                
                while (resultSet.next()) {
                    val row = mutableMapOf<String, Any>()
                    for (i in 1..resultSet.metaData.columnCount) {
                        row[resultSet.metaData.getColumnName(i)] = resultSet.getObject(i)
                    }
                    results.add(row)
                }
                
                logger.debug("Query returned ${results.size} results")
                return results
            }
        }
    }

    // Example method to execute an update
    fun executeUpdate(query: String, params: List<Any> = emptyList()): Int {
        logger.debug("Executing update: $query with params: $params")
        getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                params.forEachIndexed { index, param ->
                    statement.setObject(index + 1, param)
                }
                val affectedRows = statement.executeUpdate()
                logger.debug("Update affected $affectedRows rows")
                return affectedRows
            }
        }
    }
} 