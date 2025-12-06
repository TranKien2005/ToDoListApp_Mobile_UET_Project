package com.example.todolist.data.mapper

import com.example.todolist.data.local.entity.UserEntity
import com.example.todolist.core.model.User
import com.example.todolist.core.model.Gender

object UserEntityMapper {
    fun toDomain(entity: UserEntity): User {
        return User(
            id = entity.id,
            fullName = entity.fullName,
            age = entity.age,
            gender = try {
                Gender.valueOf(entity.gender)
            } catch (_: Exception) {
                Gender.OTHER
            },
            avatarUrl = entity.avatarUrl
        )
    }

    fun fromDomain(user: User): UserEntity {
        return UserEntity(
            id = user.id,
            fullName = user.fullName,
            age = user.age,
            gender = user.gender.name,
            avatarUrl = user.avatarUrl
        )
    }
}
