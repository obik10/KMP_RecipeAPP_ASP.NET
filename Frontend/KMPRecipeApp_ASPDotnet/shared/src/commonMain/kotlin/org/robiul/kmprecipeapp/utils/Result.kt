package org.robiul.kmprecipeapp.utils

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: AppError) : Result<Nothing>()

    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    inline fun onSuccess(block: (T) -> Unit): Result<T> = apply { if (this is Success) block(data) }
    inline fun onError(block: (AppError) -> Unit): Result<T> = apply { if (this is Error) block(error) }
}
