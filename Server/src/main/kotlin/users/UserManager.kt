package users

import org.apache.logging.log4j.LogManager
import serverUtils.DBManager
import serverUtils.User
import java.security.MessageDigest
import java.security.SecureRandom
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Predicate
import kotlin.experimental.and

class UserManager(
    private val dbManager: DBManager
) {
    private val users = TreeSet<User>()
    private val lock = ReentrantLock()
    private val logger = LogManager.getLogger(UserManager::class.java)


    fun addUser(user: User) {
        if (user == getByID(user.getName())) {
            throw Exception ("User $user cannot be added to collection")
        }
        lock.lock()
        try {
            users.add(user)
        } finally {
            lock.unlock()
        }
    }

    fun getTokenTime(token: String) : Timestamp {
        lock.lock()
        try {
            for (user in users) {
                if (user.getToken() == token) {
                    return user.getAccessTime()
                }
            }
        } finally {
            lock.unlock()
        }
        return Timestamp(0)
    }
    fun updateUser(data: User, user: User) {
        lock.lock()
        try {
            user.setName(data.getName())
            user.setPassword(data.getPassword())
        } finally {
            lock.unlock()
        }
    }

    fun removeUser(user: User) : Boolean {
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

    fun getTokens() : List<String> {
        lock.lock()
        try {
            return users.map { e -> e.getToken() }
        } finally {
            lock.unlock()
        }
    }
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

    private fun createToken(username: String) : String {
        val token = hashing(username, createSalt())
        for (user in users) {
            if (user.getName() == username) {
                user.setToken(token)
                break
            }
        }
        logger.debug("Created token")
        return token
    }

    fun removeToken(token: String) {
        for (user in users) {
            if (user.getToken() == token) {
                users.remove(user)
                break
            }
        }
    }

    private fun hashing(stringToHash: String, salt: String) : String {
        var hashedString = ""
        try {
            val md = MessageDigest.getInstance("SHA-512")
            md.update(salt.toByteArray())
            val bytes = md.digest(stringToHash.toByteArray())
            md.reset()
            val sb = StringBuilder()
            for (element in bytes) {
                sb.append(((element and 0xff.toByte()) + 0x100).toString(16).substring(1))
            }
            hashedString = sb.toString()
        } catch (e:Exception) {
            logger.error(e.message)
        }
        return hashedString
    }

    private fun createSalt(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        val sb = StringBuilder()
        for (element in bytes) {
            sb.append(((element and 0xff.toByte()) + 0x100).toString(16).substring(1))
        }
        val salt = sb.toString()
        return salt
    }

    fun login(username: String, password: String) : String {
        val salt = dbManager.retrieveSalt(username)
        val hashedPassword = hashing(password, salt)
        val authorized = dbManager.loginUser(username, hashedPassword)
        return if (authorized) {
            logger.debug("User $username authorized")
            createToken(username)
        } else {
            ""
        }
    }

    fun register(username: String, password: String) : String {
        val salt = createSalt()
        val registered = dbManager.registerUser(username, hashing(password, salt), salt)
        return if (registered) {
            logger.debug("User $username registered")
            createToken(username)
        } else {
            ""
        }
    }

    fun userExists(username: String) : Boolean{
        return dbManager.userExists(username)
    }

}