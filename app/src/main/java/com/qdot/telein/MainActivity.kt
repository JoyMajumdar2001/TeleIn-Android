package com.qdot.telein

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.qdot.telein.databinding.ActivityMainBinding
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val LOGIN_PFREF_KEY = "login_pref"
    private val LOGIN_SAVE_KEY = "login_key"
    private val defaultValue = "null"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val sharedPref = getSharedPreferences(LOGIN_PFREF_KEY,Context.MODE_PRIVATE)
        val loginData = sharedPref.getString(LOGIN_SAVE_KEY,defaultValue)
        if (loginData.equals(defaultValue)){
            doLogin()
        }else{
            updateUser(loginData)
        }
        binding.logoutBtn.setOnClickListener {
            with (sharedPref.edit()) {
                putString(LOGIN_SAVE_KEY,defaultValue)
                apply()
            }
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUser(loginData: String?) {
        val obj = JSONObject(loginData.toString())
        binding.welcomeText.text = "Welcome ${obj.getString("fname")} ( ${obj.getString("tid")} )"
    }

    private fun doLogin() {
        startActivity(Intent(this,LoginActivity::class.java))
        finish()
    }
}