package com.safe.resident.pro.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.safe.resident.pro.app.data.User
import com.safe.resident.pro.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@LoginActivity, R.layout.activity_login)

        setupBackPressHandler()
        database = FirebaseDatabase.getInstance().reference
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
            finish()
        }
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val rootView = findViewById<View>(android.R.id.content)
            val message = "Enter Valid Credentials"
            val duration = Snackbar.LENGTH_SHORT
            Log.e("Login", "onCreate: Login \n Email : $email \n Password: $password")
            if (validateInputs(email, password)) {
                loginUser(email,password)
            }else{
                val snackbar = Snackbar.make(rootView, message, duration)
                snackbar.show()
            }
        }
    }
    private fun loginUser(email: String, password: String) {
        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var userFound = false
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null && user.email == email && user.password == password) {
                        userFound = true
                        showToast("Login successful!")
                        val sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                        sharedPrefs.edit().putBoolean("isLoggedIn", true).apply()
                        sharedPrefs.edit().putString("email", user.userid).apply()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                        break
                    }
                }
                if (!userFound) {
                    Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("LoginActivity", "loadUser:onCancelled", databaseError.toException())
                Toast.makeText(this@LoginActivity, "An error occurred", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun setupBackPressHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }
    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            showToast("Please enter your email.")
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Invalid email format.")
            return false
        }

        if (password.isEmpty()) {
            showToast("Please enter a password.")
            return false
        }

        if (password.length < 8) {
            showToast("Password must be at least 8 characters long.")
            return false
        }

        return true
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}