package com.example.memorylane.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import com.example.memorylane.R
import com.example.memorylane.databinding.ActivityUserDetailsBinding
import com.example.memorylane.viewmodels.UserViewModel

class UserDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDetailsBinding
    private lateinit var userViewModel: UserViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = UserViewModel(application)

        val toolbar: Toolbar = binding.toolbarUserDetails
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val loggedInUser = userViewModel.getLoggedInUser()

        if (loggedInUser != null) {
            binding.textViewName.text = loggedInUser.name
            binding.textViewEmail.text = loggedInUser.email
        } else {
            Log.e("UserProfileActivity", "Logged-in user is null")
        }

        binding.btnSignOut.setOnClickListener {
            userViewModel.clearSession()
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}