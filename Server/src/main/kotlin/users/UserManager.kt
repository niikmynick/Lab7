package users

import serverUtils.DBManager
import serverUtils.User
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Predicate

class UserManager(
    private val dbManager: DBManager
) {
    private val users = TreeSet<User>()
    private val lock = ReentrantLock()


    fun getUsers(): TreeSet<User> {
        return users
    }

    fun add(user: User) {
        if (user == getByID(user.getName())) {
            throw Exception ("User" +
                        "$user cannot be added to collection as a Space Marine with this id already exists")
            }

        lock.lock()
        try {
            users.add(user)
        } finally {
            lock.unlock()
        }
    }

    fun update(data: User, user: User) {
        lock.lock()
        try {
            user.setName(data.getName())
            user.setPassword(data.getPassword())
        } finally {
            lock.unlock()
        }
    }

    fun remove(user: User) : Boolean {
        lock.lock()
        try {
            return users.remove(user)
        } finally {
            lock.unlock()
        }
    }

    fun clear() {
        users.clear()
    }

    /**
     * Searches for element with provided [id]
     * @param id id of the element to search
     * @return Found element or null
     */
    private fun getByID(name: String) : User? {
        lock.lock()
        try {
            for (user in users) {
                if (user.getName() == name) {
                    return user
                }
            }
        } finally {
            lock.unlock()
        }

        return null
    }

    fun filter(predicate: Predicate<User>): List<User> {
        lock.lock()
        try {
            return users.filter { e -> predicate.test(e) }
        } finally {
            lock.unlock()
        }
    }

    fun logIn(username: String, userPassword: String) {
        dbManager.loginUser(username, password.hash(userPassword))
        activeUsers[username] = token.generate()
        usersElements[username] = dbManager.getUserElements(username)
    }

    fun signIn(username: String, userPassword: String) {
        dbManager.registerUser(username, password.hash(userPassword))
        activeUsers[username] = token.generate()
        usersElements[username] = mutableListOf()
    }

    fun logOut(username: String) {
        activeUsers.remove(username)
        usersElements.remove(username)
    }

    fun changePassword(username: String, oldPassword: String, newPassword: String) {
        dbManager.changePassword(username, oldPassword, newPassword)
    }

    fun changeUsername(oldUsername: String, newUsername: String, password: String) {
        dbManager.changeUsername(oldUsername, newUsername, password)
    }

    fun delete(username: String, password: String) {
        dbManager.deleteUser(username, password)
    }
}