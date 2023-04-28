package serverUtils

import org.apache.logging.log4j.LogManager
import java.security.MessageDigest
import java.security.SecureRandom
import java.sql.Timestamp
import kotlin.experimental.and


class UserManager (
    private val dbManager: DBManager
) {

    private val logger = LogManager.getLogger(UserManager::class.java)
    val userMap = mutableMapOf<String,Map<String, Timestamp>>()

    private fun createToken(username: String) : String {
        val token = hashing(username, createSalt())
        userMap[token] = mapOf(username to Timestamp(System.currentTimeMillis()))
        logger.debug("Created token")
        return token
    }

    fun removeToken(token: String) {
        userMap.remove(token)
        logger.debug("Removed token")
    }

    fun hashing(stringToHash: String, salt: String) : String {
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

    fun changePassword(username: String, oldPassword: String, newPassword: String) {
        dbManager.changePassword(username, oldPassword, newPassword)
    }

    fun changeUsername(oldUsername: String, newUsername: String, password: String) {
        dbManager.changeUsername(oldUsername, newUsername, password)
    }

    fun deleteUser(username: String, password: String) {
        dbManager.deleteUser(username, password)
    }
}