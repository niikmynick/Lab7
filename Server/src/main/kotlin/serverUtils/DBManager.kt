package serverUtils

import basicClasses.SpaceMarine
import utils.JsonCreator
import java.sql.DriverManager

class DBManager(
    private val dbUrl: String,
    private val dbUser: String,
    private val dbPassword: String
) {
    private val jsonCreator = JsonCreator()

    private fun initUsers() {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (login VARCHAR(50) PRIMARY KEY, password VARCHAR(500), salt varchar(100))")
        statement.close()
        connection.close()
    }

    private fun initCollection() {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS collection (ID VARCHAR(1000) PRIMARY KEY, INFO VARCHAR(1000))")
        statement.close()
        connection.close()
    }

    fun initRelationships() {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS relationships (username VARCHAR(50) references users(login), spacemarine VARCHAR(1000) references collection(id))")
        statement.close()
        connection.close()
    }

    private fun initRelationship() {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS relationship (user_login VARCHAR(50), element_id VARCHAR(1000))")
        statement.close()
        connection.close()
    }

    fun getRelationship(id:String): String {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.prepareStatement("SELECT * FROM relationships WHERE spacemarine = ?")
        statement.setString(1, id)
        val resultSet = statement.executeQuery()
        resultSet.next()
        val result = resultSet.getString("username")
        resultSet.close()
        statement.close()
        connection.close()
        return result
    }
    fun initDB() {
        initUsers()
        initCollection()
        initRelationship()
    }

    fun userExists(login: String) : Boolean {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.prepareStatement("SELECT * FROM users WHERE login = ?")
        statement.setString(1, login)
        val resultSet = statement.executeQuery()
        val result = resultSet.next()
        resultSet.close()
        statement.close()
        connection.close()
        return result
    }

    fun retrieveSalt(login: String) : String {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.prepareStatement("SELECT salt FROM users WHERE login = ?")
        statement.setString(1, login)
        val resultSet = statement.executeQuery()
        resultSet.next()
        return resultSet.getString("salt")
    }

    fun registerUser(login: String, password: String, salt: String) : Boolean {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.prepareStatement("SELECT * FROM users WHERE login = ?")
        statement.setString(1, login)
        val resultSet = statement.executeQuery()
        val result = resultSet.next()
        if (!result) {
            val st = connection.prepareStatement("INSERT INTO users (login, password, salt) VALUES (?, ?, ?)")
            st.setString(1, login)
            st.setString(2, password)
            st.setString(3, salt)
            st.executeUpdate()
        }
        resultSet.close()
        statement.close()
        connection.close()
        return !result
    }

    fun loginUser(login: String, password: String) : Boolean {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.prepareStatement("SELECT * FROM users WHERE login = ? AND password = ?")
        statement.setString(1, login)
        statement.setString(2, password)
        val resultSet = statement.executeQuery()
        val result = resultSet.next()
        resultSet.close()
        statement.close()
        connection.close()
        return result
    }

    fun changePassword(login: String, oldPassword: String, newPassword: String) {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM users WHERE login = '$login' AND password = '$oldPassword'")
        val result = resultSet.next()
        if (result) {
            statement.executeUpdate("UPDATE users SET password = '$newPassword' WHERE login = '$login'")
        }
        resultSet.close()
        statement.close()
        connection.close()
    }

    fun changeUsername(oldLogin: String, newLogin: String, password: String) {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM users WHERE login = '$oldLogin' AND password = '$password'")
        val result = resultSet.next()
        if (result) {
            statement.executeUpdate("UPDATE users SET login = '$newLogin' WHERE login = '$oldLogin'")
        }
        resultSet.close()
        statement.close()
        connection.close()
    }

    fun deleteUser(login: String, password: String) {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM users WHERE login = '$login' AND password = '$password'")
        val result = resultSet.next()
        if (result) {
            statement.executeUpdate("DELETE FROM users WHERE login = '$login'")
        }
        resultSet.close()
        statement.close()
        connection.close()
    }

    fun getUserElements(login: String) : MutableList<String> {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT element_id FROM relationship WHERE user_login = '$login'")
        val result = mutableListOf<String>()
        while (resultSet.next()) {
            result.add(resultSet.getString("element_id"))
        }
        resultSet.close()
        statement.close()
        connection.close()
        return result
    }
    fun save(spaceMarine: SpaceMarine) {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM collection WHERE ID = '${spaceMarine.getId()}'")
        val result = resultSet.next()
        if (result) {
            statement.executeUpdate("UPDATE collection SET INFO = '${jsonCreator.objectToString(spaceMarine)}' WHERE ID = '${spaceMarine.getId()}'")
        } else {
            statement.executeUpdate("INSERT INTO collection (ID, INFO) VALUES ('${spaceMarine.getId()}', '${jsonCreator.objectToString(spaceMarine)}')")
        }
        resultSet.close()
        statement.close()
        connection.close()
    }

    fun loadCollection() : List<String> {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM collection")
        val result = mutableListOf<String>()
        while (resultSet.next()) {
            result.add(resultSet.getString("INFO"))
        }
        resultSet.close()
        statement.close()
        connection.close()
        return result
    }

}