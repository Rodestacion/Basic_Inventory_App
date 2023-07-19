package com.data.miniapp2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.data.miniapp2.databinding.ActivityLoginScreenBinding
import com.data.miniapp2.databinding.NewUserLayoutBinding

class LoginScreen : AppCompatActivity() {
    private lateinit var binding: ActivityLoginScreenBinding
    private lateinit var databaseHelper: DatabaseHelper
    private var tempUsername: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        binding.btnRegister.setOnClickListener {
            showNewUserDialog()
        }

        binding.btnLogin.setOnClickListener {

            if (binding.etUsername.text!!.isNotEmpty()&& binding.etPassword.text!!.isNotEmpty()){
                val userList = getUserList()
                val userName = binding.etUsername.text.toString()
                val password = binding.etPassword.text.toString()

                if(checkUserLogin(userList,userName.uppercase(),password)){
                    val nextScreen = Intent(this,MainActivity::class.java)
                    nextScreen.putExtra("user",userName.uppercase())
                    Toast.makeText(applicationContext, "Login successfully", Toast.LENGTH_SHORT).show()
                    startActivity(nextScreen)
                    finish()
                }else{
                    binding.etUsername.text!!.clear()
                    binding.etPassword.text!!.clear()
                    Toast.makeText(applicationContext, "Username or password not match", Toast.LENGTH_SHORT).show()
                }


            }else{
                binding.etUsername.text!!.clear()
                binding.etPassword.text!!.clear()
                Toast.makeText(applicationContext, "Please fill the input box Completely", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUserLogin(userList: MutableList<User>, username: String, password: String):Boolean{
        var exist = false
        repeat(userList.size){
            if(userList[it].username==username && userList[it].password==password){
                exist=true
            }
        }
        return exist
    }

    private fun checkUserExist(userList: MutableList<User>, username: String):Boolean{
        var exist = true
        repeat(userList.size){
            if(userList[it].username==username){
                exist=false
            }
        }
        return exist
    }

    private fun getUserList():MutableList<User> {
        return databaseHelper.getUserList()
    }

    private fun showNewUserDialog(){
        val alertDialogBuilder = AlertDialog.Builder(this,R.style.MyDialogTheme)
        alertDialogBuilder.setTitle("Register New User")

        val dialogLayout = layoutInflater.inflate(R.layout.new_user_layout,null)
        val dialogBinding = NewUserLayoutBinding.bind(dialogLayout)

        if (tempUsername.isNotEmpty()){
            dialogBinding.etNewUsername.setText(tempUsername)
        }
        alertDialogBuilder.setIcon(getImageID("baseline_person_add_24"))
        alertDialogBuilder.setView(dialogLayout)

        alertDialogBuilder.setPositiveButton("Register"){dialog,_->
            val userList = getUserList()
            val username = dialogBinding.etNewUsername.text.toString()
            val newPassword = dialogBinding.etNewPassword.text.toString()
            val confirmPassword = dialogBinding.etConfirrmPassword.text.toString()

            if (username.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty()){
                if(checkUserExist(userList,username.uppercase())){
                    if(newPassword==confirmPassword){
                        tempUsername =""
                        val newUser = User(0,username.uppercase(), newPassword)
                        databaseHelper.insertUser(newUser)
                        Toast.makeText(applicationContext, "New username was successfully added", Toast.LENGTH_SHORT).show()
                    }else{
                        tempUsername = username
                        Toast.makeText(applicationContext, "Password is incorrect", Toast.LENGTH_SHORT).show()
                        showNewUserDialog()
                    }
                }else{
                    tempUsername = ""
                    Toast.makeText(applicationContext, "Username already exist", Toast.LENGTH_SHORT).show()
                    showNewUserDialog()
                }
            }else{
                tempUsername = username
                Toast.makeText(applicationContext, "Please fill the input box Completely", Toast.LENGTH_SHORT).show()
                showNewUserDialog()
            }
        }

        alertDialogBuilder.setNegativeButton("Cancel"){dialog,_->
            tempUsername=""
            dialog.dismiss()
        }

        val alertDialog:AlertDialog = alertDialogBuilder.create()
        alertDialog.show()

    }

    private fun getImageID(imageName: String): Int {
        //val myPackage = android.content.ContextWrapper(context)
        return resources.getIdentifier(imageName,"drawable",packageName)
    }
}