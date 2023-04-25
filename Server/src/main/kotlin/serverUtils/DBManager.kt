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

    fun initUsers() {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (login VARCHAR(50) PRIMARY KEY, password VARCHAR(50))")
        statement.close()
        connection.close()
    }

    fun initCollection() {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS collection (ID VARCHAR(1000) PRIMARY KEY, INFO VARCHAR(1000))")
        statement.close()
        connection.close()
    }

    fun userExists(login: String) : Boolean {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM users WHERE login = '$login'")
        val result = resultSet.next()
        resultSet.close()
        statement.close()
        connection.close()
        return result
    }

    fun registerUser(login: String, password: String) : Boolean {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM users WHERE login = '$login'")
        val result = resultSet.next()
        if (!result) {
            statement.executeUpdate("INSERT INTO users (login, password) VALUES ('$login', '$password')")
        }
        resultSet.close()
        statement.close()
        connection.close()
        return !result
    }

    fun loginUser(login: String, password: String) : Boolean {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM users WHERE login = '$login' AND password = '$password'")
        val result = resultSet.next()
        resultSet.close()
        statement.close()
        connection.close()
        return result
    }

    fun deleteUser(login: String) {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        statement.executeUpdate("DELETE FROM users WHERE login = '$login'")
        statement.close()
        connection.close()
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