package com.example.todolist.domain.usecase

import com.example.todolist.core.model.User
import com.example.todolist.core.model.Gender
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// Debug-only fake implementations of user use-cases. These live in the `debug` source set
// so they will be compiled into debug builds and can be swapped for real implementations in release.

private val _userState = MutableStateFlow<User?>(
    User(
        id = 1,
        fullName = "Nguyễn Văn A",
        age = 25,
        gender = Gender.MALE,
        avatarUrl = null // Mặc định không có avatar
    )
)

class FakeGetUserUseCase : GetUserUseCase {
    override operator fun invoke(): Flow<User?> = _userState
}

class FakeSaveUserUseCase : SaveUserUseCase {
    override suspend operator fun invoke(user: User) {
        // Auto-generate id if not provided
        val savedUser = if (user.id == 0) {
            user.copy(id = 1)
        } else {
            user
        }
        _userState.value = savedUser
    }
}

class FakeUpdateUserUseCase : UpdateUserUseCase {
    override suspend operator fun invoke(user: User) {
        _userState.value = user
    }
}

class FakeDeleteUserUseCase : DeleteUserUseCase {
    override suspend operator fun invoke() {
        _userState.value = null
    }
}

// Aggregator instance for debug builds
val fakeUserUseCases = UserUseCases(
    getUser = FakeGetUserUseCase(),
    saveUser = FakeSaveUserUseCase(),
    updateUser = FakeUpdateUserUseCase(),
    deleteUser = FakeDeleteUserUseCase()
)
