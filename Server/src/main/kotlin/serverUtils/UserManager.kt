package serverUtils

import org.apache.logging.log4j.LogManager
class UserManager (
    private val dbManager: DBManager
) {

    private val logger = LogManager.getLogger(UserManager::class.java)

    fun login(username: String, password: String) {
        dbManager.loginUser(username, password)
    }

    fun register(username: String, password: String) {
        dbManager.registerUser(username, password)
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