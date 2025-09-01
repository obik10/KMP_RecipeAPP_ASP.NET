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

/** 🔹 Extension functions defined OUTSIDE the sealed class */
fun <T> Result<T>.getOrNull(): T? =
    when (this) {
        is Result.Success -> data
        is Result.Error -> null
    }

fun <T> Result<T>.getOrThrow(): T =
    when (this) {
        is Result.Success -> data
        is Result.Error -> throw error
    }

fun <T> Result<T>.exceptionOrNull(): Throwable? =
    when (this) {
        is Result.Success -> null
        is Result.Error -> error
    }
