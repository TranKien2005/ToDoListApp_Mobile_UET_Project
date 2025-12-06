package com.example.todolist.domain.usecase

import com.example.todolist.core.model.User
import kotlinx.coroutines.flow.Flow

// Keep only interface definitions in `main`. Concrete implementations must live in debug/release.
interface GetUserUseCase {
    operator fun invoke(): Flow<User?>
}

interface SaveUserUseCase {
    suspend operator fun invoke(user: User)
}

interface UpdateUserUseCase {
    suspend operator fun invoke(user: User)
}

interface DeleteUserUseCase {
    suspend operator fun invoke()
}

// Aggregator contains only the interfaces; implementations come from debug/release.
data class UserUseCases(
    val getUser: GetUserUseCase,
    val saveUser: SaveUserUseCase,
    val updateUser: UpdateUserUseCase,
    val deleteUser: DeleteUserUseCase
)

