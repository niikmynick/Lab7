package serverUtils

import basicClasses.SpaceMarine
import users.User
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

    private fun initTokens() {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        statement.executeUpdate("create table if not exists tokens(token varchar(1000) primary key,user_login varchar(50) references users (login) ON DELETE SET NULL ON UPDATE CASCADE,access_time timestamp not null);")
        statement.close()
        connection.close()
    }

    private fun initCollection() {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        statement.executeUpdate("create table if not exists collection(id character varying(1000) primary key,info character varying(1000) not null);")
        statement.close()
        connection.close()
    }

    private fun initRelationship() {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        statement.executeUpdate("create table if not exists relationships(user_login character varying(50) references users (login) ON DELETE SET NULL ON UPDATE CASCADE,element_id character varying(1000) UNIQUE references collection (id) ON DELETE CASCADE ON UPDATE CASCADE);")
        statement.close()
        connection.close()
    }

    fun getRelationship(id:String): String {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.prepareStatement("SELECT * FROM relationships WHERE element_id = ?")
        statement.setString(1, id)
        val resultSet = statement.executeQuery()
        resultSet.next()
        val result = resultSet.getString("user_login")
        resultSet.close()
        statement.close()
        connection.close()
        return result
    }
    fun initDB() {
        initUsers()
        initCollection()
        initRelationship()
        initTokens()
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

    fun deleteToken(token: String) {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM tokens WHERE token = '$token'")
        val result = resultSet.next()
        if (result) {
            statement.executeUpdate("DELETE FROM tokens WHERE token = '$token'")
        }
        resultSet.close()
        statement.close()
        connection.close()
    }

    fun getUserElements(login: String) : MutableList<String> {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT element_id FROM relationships WHERE user_login = '$login'")
        val result = mutableListOf<String>()
        while (resultSet.next()) {
            result.add(resultSet.getString("element_id"))
        }
        resultSet.close()
        statement.close()
        connection.close()
        return result
    }
    fun saveSpacemarine(spaceMarine: SpaceMarine) {
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

    fun saveRelationship(username: String, elementId: Long) {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeUpdate("INSERT INTO relationships (user_login, element_id) VALUES ('${username}','${elementId}') ON CONFLICT DO NOTHING;")
        statement.close()
        connection.close()
    }

    fun saveTokens(token: String, user: User) {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeUpdate("INSERT INTO tokens (token, user_login, access_time) VALUES ('${token}','${user.getName()}','${user.getAccessTime()}') ON CONFLICT DO NOTHING;")
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

    fun loadTokens() : MutableMap<String, User>{
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM tokens")
        val result = mutableMapOf<String, User>()
        while (resultSet.next()) {
            val username = resultSet.getString("user_login")
            val accessTime = resultSet.getTimestamp("access_time")
            val password = loadPasswordByUsername(username)
            val user = User(username, password)
            user.setAccessTime(accessTime)
            result[resultSet.getString("token")] = user
        }
        resultSet.close()
        statement.close()
        connection.close()
        return result
    }

    fun loadPasswordByUsername(username: String) : String {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM users where login = '$username'")
        resultSet.next()
        return resultSet.getString("password")
    }

}