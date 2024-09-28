package com.safe.resident.pro.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.safe.resident.pro.app.data.User
import com.safe.resident.pro.app.databinding.ActivitySignupBinding
import java.util.UUID

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@SignupActivity, R.layout.activity_signup)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        setupBackPressHandler()
        binding.btnSignup.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            Log.e("SignUp", "onCreate: SignUp \n Email : $email \n Password: $password \n Re_Pass: $confirmPassword", )
            if (validateInputs(email, password, confirmPassword)) {
                // Create user object
                checkEmailExists(email) { exists ->
                    if (exists) {
                        showToast("Account already exists.")
                    } else {
                        val user = User(UUID.randomUUID().toString(), email, password)

                        database.child("users").child(user.userid).setValue(user)
                            .addOnSuccessListener {
                                showToast("Sign-up successful!")
                                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                showToast("Signup failed! Please try again.")
                            }
                    }
                }
            }
        }

        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
            finish()
        }

    }

    private fun checkEmailExists(email: String, callback: (Boolean) -> Unit) {
        database.child("users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback(dataSnapshot.exists()) // Returns true if any child has the email
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("DBError", "loadPost:onCancelled", databaseError.toException())
            }
        })
    }
    private fun setupBackPressHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle back press here, such as navigating to a different activity or fragment
                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
        if (email.isEmpty()) {
            showToast("Please enter your email.")
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Invalid email format.")
            return false
        }

        if (password.isEmpty()) {
            showToast("Please enter a password.")
            return false
        }

        if (password.length < 6) {
            showToast("Password must be at least 6 characters long.")
            return false
        }

        if (password != confirmPassword) {
            showToast("Passwords do not match.")
            return false
        }

        return true
    }

    private fun signUpUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showToast("Sign-up successful!")
                    startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                    finish()
                } else {
                    showToast("Sign-up failed. Please try again.")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}