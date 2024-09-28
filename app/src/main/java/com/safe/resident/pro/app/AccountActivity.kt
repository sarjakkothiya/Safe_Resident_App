package com.safe.resident.pro.app

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.safe.resident.pro.app.data.Incident
import com.safe.resident.pro.app.data.User
import com.safe.resident.pro.app.databinding.ActivityAccountBinding

class AccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding
    private lateinit var database: DatabaseReference
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account)

        database = FirebaseDatabase.getInstance().reference
        val sharedPrefs = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userId = sharedPrefs.getString("email", "") ?: ""
        setupViews()
        loadUserData()
        loadIncidents()
    }

    private fun setupViews() {
        binding.tvChangePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.tvSave.setOnClickListener {
            updatePassword()
        }
    }

    private fun togglePasswordVisibility() {
        binding.tvPassword.visibility = View.GONE
        binding.etPassword.visibility = View.VISIBLE
        binding.tvSave.visibility = View.VISIBLE
    }

    private fun updatePassword() {
        val newPassword = binding.etPassword.text.toString().trim()
        if (newPassword.isNotEmpty()) {
            database.child("users").child(userId).child("password").setValue(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.etPassword.visibility = View.GONE
                        binding.tvSave.visibility = View.GONE
                        binding.tvPassword.visibility = View.VISIBLE
                    } else {
                        // Handle error while updating password
                    }
                }
        }
    }

    private fun loadUserData() {
        database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                user?.let {
                    binding.tvEmail.text = it.email
                    binding.tvPassword.text = "********"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun loadIncidents() {
        database.child("incidents").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var totalIncidents = 0
                for (snapshot in dataSnapshot.children) {
                    val incident = snapshot.getValue(Incident::class.java)
                    if (incident != null && incident.userID == userId) {
                        totalIncidents++
                    }
                }
                binding.tvTotalIncident.text = "Submitted Incidents: $totalIncidents"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }
}