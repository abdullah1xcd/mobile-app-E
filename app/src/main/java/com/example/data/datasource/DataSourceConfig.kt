package com.example.data.datasource

enum class DataSourceMode {
    MOCK,
    REAL_API
}

/**
 * Global DataSource configuration allowing seamless switching between
 * Mock/Offline Development Mode and Real REST API backend integration.
 */
object DataSourceConfig {
    /**
     * Set to [DataSourceMode.MOCK] for standalone prototype/demo runs without backend.
     * Switch to [DataSourceMode.REAL_API] when pointing to a live NestJS / Spring / Node backend.
     */
    var mode: DataSourceMode = DataSourceMode.MOCK

    /**
     * If true, when a network call fails in REAL_API mode (e.g. backend offline or unreachable),
     * automatically fall back to local mock data to prevent app crashes.
     */
    var fallbackToMockOnError: Boolean = true

    val isMockMode: Boolean
        get() = mode == DataSourceMode.MOCK
}
