package com.example.memorylane.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import com.example.memorylane.database.DatabaseHandler
import com.example.memorylane.models.UserModel

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHandler: DatabaseHandler = DatabaseHandler(application)
    private val sharedPreferences =
        application.getSharedPreferences("CURRENT_USER_ID", Context.MODE_PRIVATE)

    fun createSession(userId: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("USER_ID", userId)
        editor.apply()
    }

    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    fun getLoggedInUserId(): Int {
        return sharedPreferences.getInt("USER_ID", -1)
    }

    fun getLoggedInUser(): UserModel? {
        val userId = sharedPreferences.getInt("USER_ID", -1)

        return if (userId != -1) {
            dbHandler.getUserById(userId)
        } else {
            null
        }
    }

    fun addUser(userModel: UserModel): Long {
        Log.d("Database", "Added user: $userModel")
        createSession(userModel.id)
        return dbHandler.createUser(userModel)
    }

    fun getUserByEmail(email: String): UserModel? {
        return dbHandler.getUserByEmail(email)
    }

    fun validateName(name: String): Boolean {
        return name.matches(Regex("[^0-9]+"))
    }

    fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun emailAlreadyExists(email: String): Boolean {
        val existingUser = dbHandler.getUserByEmail(email)
        return existingUser != null
    }

    fun validatePasswordPattern(password: String): Boolean {
        // Password pattern: At least one letter, one number, one special character, and a minimum length of 6
        val pattern = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{6,}\$")
        return pattern.matches(password)
    }

    fun validatePassword(password: String, repeatPassword: String): Boolean {
        return password == repeatPassword
    }
}