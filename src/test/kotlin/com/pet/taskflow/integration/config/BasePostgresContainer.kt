package com.pet.taskflow.integration.config

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

abstract class BasePostgresContainer {

    companion object {
        val postgres = PostgreSQLContainer("postgres:15").apply {
            withDatabaseName("testdb")
            withUsername("postgres")
            withPassword("postgres")
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun overrideProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}