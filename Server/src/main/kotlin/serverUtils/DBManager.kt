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
        statement.executeUpdate("create table if not exists users(login character varying(50) primary key,password character varying(500),salt character varying(100));")
        statement.close()
        connection.close()
    }

    private fun initCollection() {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        statement.executeUpdate("create table if not exists collection(id serial primary key,info character varying(1000) not null, user_login character varying(50) references users (login) ON DELETE SET NULL ON UPDATE CASCADE);")
        statement.close()
        connection.close()
    }

    fun initDB() {
        initUsers()
        initCollection()
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

    fun deleteSpaceMarine(id: Long) {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM collection WHERE id = '$id'")
        val result = resultSet.next()
        if (result) {
            statement.executeUpdate("DELETE FROM collection WHERE id = '$id'")
        }
        resultSet.close()
        statement.close()
        connection.close()
    }


    fun saveSpacemarine(spaceMarine: SpaceMarine, username: String) {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val id = spaceMarine.getId().toInt()
        val resultSet = statement.executeQuery("SELECT * FROM collection WHERE ID = '${id}'")
        val result = resultSet.next()
        if (result) {
            statement.executeUpdate("UPDATE collection SET INFO = '${jsonCreator.objectToString(spaceMarine)}' WHERE ID = '${spaceMarine.getId()}'")
        } else {
            statement.executeUpdate("INSERT INTO collection (INFO, USER_LOGIN) VALUES ('${jsonCreator.objectToString(spaceMarine)}', '${username}');")
        }
        resultSet.close()
        statement.close()
        connection.close()
    }


    fun loadCollection() : Map<String, String> {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM collection")
        val result = mutableMapOf<String, String>()
        while (resultSet.next()) {
            result[resultSet.getString("INFO")] = resultSet.getString("USER_LOGIN")
        }
        resultSet.close()
        statement.close()
        connection.close()
        return result
    }


    private fun loadPasswordByUsername(username: String) : String {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM users where login = '$username'")
        resultSet.next()
        return resultSet.getString("password")
    }

    fun getIdBySpacemarine(spaceMarine: SpaceMarine) : Long {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM collection WHERE INFO = '${jsonCreator.objectToString(spaceMarine)}'")
        resultSet.next()
        return resultSet.getLong("id")
    }

}