package com.example.todolist.domain.usecase

import com.example.todolist.core.model.User
import com.example.todolist.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

// Release implementations that use real UserRepository
class RealGetUserUseCase(
    private val repository: UserRepository
) : GetUserUseCase {
    override operator fun invoke(): Flow<User?> = repository.getUser()
}

class RealSaveUserUseCase(
    private val repository: UserRepository
) : SaveUserUseCase {
    override suspend operator fun invoke(user: User) {
        repository.saveUser(user)
    }
}

class RealUpdateUserUseCase(
    private val repository: UserRepository
) : UpdateUserUseCase {
    override suspend operator fun invoke(user: User) {
        repository.updateUser(user)
    }
}

class RealDeleteUserUseCase(
    private val repository: UserRepository
) : DeleteUserUseCase {
    override suspend operator fun invoke() {
        repository.deleteUser()
    }
}

