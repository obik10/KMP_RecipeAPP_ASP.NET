package org.robiul.kmprecipeapp

object Constants {
    // Base URLs
    const val BASE_URL_KEYCLOAK = "http://10.0.2.2:8080"
    const val BASE_URL_API = "http://10.0.2.2:5076" // Android emulator localhost
//    const val BASE_URL_RELEASE: String = "https://api.yourproductiondomain.com"
//    const val DEFAULT_BASE_URL: String = BASE_URL_DEBUG
//    // Keycloak / ASP.NET token endpoint
    const val TOKEN_PATH: String = "/realms/recipe-app/protocol/openid-connect/token"

    // OAuth client configuration
    const val OAUTH_CLIENT_ID: String = "recipe-app-api"   // from your script
    val OAUTH_CLIENT_SECRET: String? = null          // public client

    // Demo test user credentials (for development/testing only)
    const val TEST_USERNAME: String = "testuser"
    const val TEST_PASSWORD: String = "test123"

    // Clock skew safety window (ms)
    const val EXPIRY_SKEW_MS: Long = 15_000L
}
