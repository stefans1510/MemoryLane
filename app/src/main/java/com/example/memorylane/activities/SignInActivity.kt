package com.example.memorylane.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.memorylane.databinding.ActivitySignInBinding
import com.example.memorylane.models.UserModel
import com.example.memorylane.viewmodels.UserViewModel
import com.google.android.material.textfield.TextInputEditText

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var userViewModel: UserViewModel
    private var isSignInFormVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = UserViewModel(application)

        // initialize visibility for the sign-in form
        setSignInFormVisibility()

        binding.tvSignUpLink.setOnClickListener {
            toggleForms()
        }

        binding.tvSignInLink.setOnClickListener {
            toggleForms()
        }

        binding.btnSignIn.setOnClickListener {
            signIn()
        }

        binding.btnSignUp.setOnClickListener {
            signUp()
        }

        binding.constraintLayout.post {
            toggleForms()
        }
    }

    private fun setSignInFormVisibility() {
        binding.etName.visibility = View.GONE
        binding.etRepeatPassword.visibility = View.GONE
        binding.btnSignUp.visibility = View.GONE
        binding.tvSignIn.visibility = View.GONE
        binding.tvSignInLink.visibility = View.GONE
        binding.tvSignUp.visibility = View.VISIBLE
        binding.tvSignUpLink.visibility = View.VISIBLE
    }

    private fun setSignUpFormVisibility() {
        binding.etName.visibility = View.VISIBLE
        binding.etRepeatPassword.visibility = View.VISIBLE
        binding.btnSignUp.visibility = View.VISIBLE
        binding.tvSignIn.visibility = View.VISIBLE
        binding.tvSignInLink.visibility = View.VISIBLE
        binding.tvSignUp.visibility = View.GONE
        binding.tvSignUpLink.visibility = View.GONE
    }

    private fun toggleForms() {
        val transition = AutoTransition()
        transition.duration = 300
        TransitionManager.beginDelayedTransition(binding.constraintLayout, transition)

        // Adjust visibility based on the new state
        if (isSignInFormVisible) {
            setSignInFormVisibility()
        } else {
            setSignUpFormVisibility()
        }

        isSignInFormVisible = !isSignInFormVisible
    }

    private fun signIn() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        when {
            email.isEmpty() -> {
                showValidationError(binding.etEmail, "Please enter your email")
                return
            }

            !userViewModel.validateEmail(email) -> {
                showValidationError(binding.etEmail, "Invalid email format")
                return
            }

            password.isEmpty() -> {
                showValidationError(binding.etPassword, "Please enter your password")
                return
            }
        }

        val user = userViewModel.getUserByEmail(email)

        if (user != null && user.password == password) {
            val userId = user.id
            Log.d("SignInActivity", "User ID: $userId")

            userViewModel.createSession(userId)

            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signUp() {
        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val repeatPassword = binding.etRepeatPassword.text.toString()

        // Form validation checks
        when {
            name.isEmpty() -> {
                showValidationError(binding.etName, "Please enter your name")
                return
            }

            !userViewModel.validateName(name) -> {
                showValidationError(binding.etName, "Name cannot contain numbers")
                return
            }

            email.isEmpty() -> {
                showValidationError(binding.etEmail, "Please enter your email")
                return
            }

            !userViewModel.validateEmail(email) -> {
                showValidationError(binding.etEmail, "Invalid email format")
                return
            }

            userViewModel.emailAlreadyExists(email) -> {
                showValidationError(binding.etEmail, "This email is already being used")
                return
            }

            password.isEmpty() -> {
                showValidationError(binding.etPassword, "Please enter your password")
                return
            }

            !userViewModel.validatePasswordPattern(password) -> {
                showValidationError(binding.etPassword, "Password must contain one letter, one number, one special character and six characters")
                return
            }

            !userViewModel.validatePassword(password, repeatPassword) -> {
                showValidationError(binding.etRepeatPassword, "Passwords do not match")
                return
            }
        }

        val user= UserModel(0, name, email, password)
        val addUser = userViewModel.addUser(user)

        if (addUser > 0) {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Invalid registration data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showValidationError(editText: TextInputEditText, errorMessage: String) {
        editText.error = errorMessage
    }
}

