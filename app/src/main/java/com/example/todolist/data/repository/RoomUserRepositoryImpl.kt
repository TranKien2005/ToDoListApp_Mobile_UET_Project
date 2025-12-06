package com.example.todolist.data.repository

import com.example.todolist.data.local.dao.UserDao
import com.example.todolist.data.mapper.UserEntityMapper
import com.example.todolist.domain.repository.UserRepository
import com.example.todolist.core.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomUserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {

    override fun getUser(): Flow<User?> {
        return userDao.getUser().map { entity ->
            entity?.let { UserEntityMapper.toDomain(it) }
        }
    }

    override suspend fun saveUser(user: User) {
        val entity = UserEntityMapper.fromDomain(user)
        userDao.insertUser(entity)
    }

    override suspend fun updateUser(user: User) {
        val entity = UserEntityMapper.fromDomain(user)
        userDao.updateUser(entity)
    }

    override suspend fun deleteUser() {
        userDao.deleteAll()
    }
}

