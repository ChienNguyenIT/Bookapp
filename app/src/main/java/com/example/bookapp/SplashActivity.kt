package com.example.bookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.bookapp.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {
    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed(Runnable {
            checkUser()
        },3000) //  means 1 seconds
    }

    private fun checkUser() {
        // get current user, if logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null ){
            // user not logged in, goto main screen
            startActivity(Intent(this, MainActivity::class.java))
        }else{
            // user logged in, check user type, same as done in login screen
            val ref = FirebaseDatabase.getInstance().getReference("User")

            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get User type e.g user or admin
                        val userType = snapshot.child("userType").value
                        if (userType == "user") {
                            // its simple user, open user dashboard
                            startActivity(Intent(this@SplashActivity, DashboardUserActivity::class.java))
                            finish()
                        } else if (userType == "admin") {
                            // its admin, open admin dashboard
                            startActivity(
                                Intent(
                                    this@SplashActivity,
                                    DashboardAdminActivity::class.java
                                )
                            )
                        }

                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

    }
}
/* keep user in  logged in
    1) check if user logged in
    2) check type of user
 */