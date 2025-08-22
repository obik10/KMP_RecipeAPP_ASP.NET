package org.robiul.kmprecipeapp.domain.usecase

import org.robiul.kmprecipeapp.data.repository.AuthRepositoryImpl
import org.robiul.kmprecipeapp.utils.Result

class RegisterUser(private val auth: AuthRepositoryImpl) {
    suspend operator fun invoke(username: String, email: String, password: String): Result<Unit> =
        auth.register(username, email, password)
}
