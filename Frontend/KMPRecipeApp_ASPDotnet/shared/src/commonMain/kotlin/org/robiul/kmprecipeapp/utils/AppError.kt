package org.robiul.kmprecipeapp.utils

sealed class AppError(message: String? = null, cause: Throwable? = null) : Throwable(message, cause) {
    data class Network(val reason: String? = null, val throwable: Throwable? = null) : AppError(reason, throwable)
    data class Timeout(val reason: String? = null, val throwable: Throwable? = null) : AppError(reason, throwable)
    data class Server(val code: Int, val body: String? = null) : AppError("Server error $code: $body")
    data object Unauthorized : AppError("Unauthorized")
    data object NotFound : AppError("Not Found")
    data class Db(val reason: String? = null, val throwable: Throwable? = null) : AppError(reason, throwable)
    data class Unknown(val reason: String? = null, val throwable: Throwable? = null) : AppError(reason, throwable)
}
