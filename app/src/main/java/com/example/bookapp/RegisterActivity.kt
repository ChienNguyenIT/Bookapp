package com.example.bookapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    // viewbinding
    private lateinit var binding:ActivityRegisterBinding

    // firebase auth
    private  lateinit var firebaseAuth: FirebaseAuth

    // progress dialog
    private  lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // init  =  progress dialog, will show white creating acount | register user
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        // handle back button click
        binding.backbtn.setOnClickListener{
            onBackPressed() // goto previous screen
        }

        // handle click, begin register
        binding.registerBtn.setOnClickListener{
            /* Steps
            1) Input Data
            2) Validate data
            3) create Account - Firebase Auth
            4) save User Info - Firebase  realtime database
             */
            validatedata()
        }

    }
    private var name =""
    private var email =""
    private var password =""

    private fun validatedata() {
        //1) Input data
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        //2) Validate data
        if(name.isEmpty()){
            //empty name...
            Toast.makeText(this, "Enter your name...",Toast.LENGTH_SHORT).show()
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            // invalid email pattern
            Toast.makeText(this, "Invalid Email Pattern...",Toast.LENGTH_SHORT).show()
        }else if(cPassword.isEmpty()){
            // empty config password
            Toast.makeText(this, "Empty Config Password...",Toast.LENGTH_SHORT).show()
        }else if(password != cPassword){
            Toast.makeText(this, "Password doesn't match...",Toast.LENGTH_SHORT).show()
        }else{
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        //3) create Account - Firebase Auth

        //show progress
        progressDialog.setMessage("Creating Account...")

        // Thực hiện tạo tài khoản người dùng trong Firebase Authentication
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                // Tài khoản đã được tạo, lấy thông tin user ID (UID)
                val uid = authResult.user?.uid

                if (uid != null) {
                    // Thực hiện cập nhật thông tin người dùng vào Firebase Realtime Database
                    updateUserInfo()
                } else {
                    // Xử lý khi không có UID (user ID)
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed creating account. User ID not available.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // Xử lý khi việc tạo tài khoản thất bại
                progressDialog.dismiss()
                Toast.makeText(this, "Failed creating account due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInfo() {
        // 4) save User Info - Firebase Realtime Database
        progressDialog.setMessage("Saving User info...")

        // Thời điểm hiện tại
        val timestamp = System.currentTimeMillis()

        // Lấy UID từ FirebaseAuth
        val uid = firebaseAuth.uid

        if (uid != null) {
            // Thiết lập dữ liệu để thêm vào cơ sở dữ liệu
            val hashMap: HashMap<String, Any?> = HashMap()
            hashMap["uid"] = uid
            hashMap["email"] = email
            hashMap["name"] = name
            hashMap["profileImage"] = "" // Thêm trống, sẽ được cập nhật trong phần chỉnh sửa hồ sơ
            hashMap["userType"] = "User" // Các giá trị có thể là user/admin, sẽ được thay đổi giá trị thành admin thủ công trên Firebase DB
            hashMap["timestamp"] = timestamp

            // Đặt dữ liệu vào cơ sở dữ liệu
            val ref = FirebaseDatabase.getInstance().getReference("User")
            ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener {
                    // Thông tin người dùng đã được lưu, mở trang dashboard của người dùng
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account created successfully.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, DashboardAdminActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    // Xử lý khi việc lưu thông tin người dùng thất bại
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed creating account due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Xử lý khi UID không tồn tại
            progressDialog.dismiss()
            Toast.makeText(this, "Failed creating account. User ID not available.", Toast.LENGTH_SHORT).show()
        }
    }
}