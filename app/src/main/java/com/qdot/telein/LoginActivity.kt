package com.qdot.telein

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.qdot.telein.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val LOGIN_PFREF_KEY = "login_pref"
    private val LOGIN_SAVE_KEY = "login_key"
    private lateinit var sharedPref : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        sharedPref = getSharedPreferences(LOGIN_PFREF_KEY, Context.MODE_PRIVATE)
        val linkUri = intent.data

        if (linkUri != null){
            binding.loginButton.visibility = View.GONE
            binding.loadingLay.visibility = View.VISIBLE
            binding.inputLayout.visibility = View.GONE
            val parameters = linkUri.pathSegments
            val uid = parameters[parameters.size - 1]
            checkAccount(uid)
        }else {
            binding.loginButton.visibility = View.VISIBLE
            binding.loadingLay.visibility = View.GONE
            binding.inputLayout.visibility = View.GONE
        }

        binding.loginButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://t.me/telein_bot?start")
            startActivity(intent)
        }

    }

    private fun checkAccount(uid: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://telein.onrender.com/login/$uid")
                .build()
            runCatching {
                val response = client.newCall(request).execute()
                val data = response.body?.string()
                val jsonObj = JSONObject(data.toString())
                if (jsonObj.has("key")){
                    if (jsonObj.getInt("key") == 0){
                        withContext(Dispatchers.Main){
                            binding.firstName.setText(jsonObj.getString("fname"))
                            binding.lastName.setText(jsonObj.getString("lname"))
                            binding.loadingLay.visibility = View.GONE
                            binding.inputLayout.visibility = View.VISIBLE
                            binding.submitBtn.setOnClickListener {
                                binding.loadingLay.visibility = View.VISIBLE
                                binding.inputLayout.visibility = View.GONE
                                jsonObj.remove("key")
                                jsonObj.remove("msg")
                                jsonObj.remove("tempuid")
                                jsonObj.put("uid",uid)
                                jsonObj.put("fname",binding.firstName.text.toString().trim())
                                jsonObj.put("lname",binding.lastName.text.toString().trim())
                                CoroutineScope(Dispatchers.IO).launch {
                                    val jsonMedia = "application/json; charset=utf-8".toMediaType()
                                    val reqBody = jsonObj.toString().toRequestBody(jsonMedia)
                                    val request1 = Request.Builder()
                                        .url("https://telein.onrender.com/create")
                                        .post(reqBody)
                                        .build()
                                    runCatching {
                                        val response1 = client.newCall(request1).execute()
                                        val resData = response1.body?.string()
                                        val jsonData = JSONObject(resData.toString())
                                        if (jsonData.has("key") && jsonData.getInt("key")==1){
                                            withContext(Dispatchers.Main){
                                                binding.loadingLay.visibility = View.GONE
                                                Toast.makeText(this@LoginActivity,"Account created",
                                                    Toast.LENGTH_SHORT).show()
                                                with (sharedPref.edit()) {
                                                    putString(LOGIN_SAVE_KEY,jsonObj.toString())
                                                    apply()
                                                }
                                                startActivity(
                                                    Intent(this@LoginActivity,
                                                    MainActivity::class.java)
                                                )
                                                finish()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else{
                    with (sharedPref.edit()) {
                        putString(LOGIN_SAVE_KEY,jsonObj.toString())
                        apply()
                    }
                    startActivity(Intent(this@LoginActivity,
                        MainActivity::class.java))
                    finish()
                }
            }
        }
    }


}