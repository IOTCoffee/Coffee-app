package com.darrienglasser.iotcoffee

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.support.v7.app.AppCompatActivity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.core.content.edit
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (getDefaultSharedPreferences(applicationContext).contains(getString(R.string.token_storage))) {
            startActivity(Intent(this, CoffeeActivity::class.java))
        }

        val r = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

        val lModel = r.create(LoginModel::class.java)

        auth_switch.setOnCheckedChangeListener { _, b ->
            // Registering if true
            if (b) {
                register_details.visibility = VISIBLE
                auth_title_view.text = getString(R.string.register)
                switch_text.text = getString(R.string.register)
            } else {
                register_details.visibility = GONE
                auth_title_view.text = getString(R.string.login)
                switch_text.text = getString(R.string.login)
            }
        }

        continue_button.setOnClickListener {
            // Registering if checked
            if (auth_switch.isChecked) {
                rLogin(lModel)
            } else {
                login(lModel)
            }
        }
    }

    private fun rLogin(lModel: LoginModel) {
        val email = email_input.text.toString()
        val pw = password_input.text.toString()
        val repw = reenter_password_input.text.toString()

        if (repw != pw) {
            reenter_password_input.error = "Passwords do not match!"
            return
        }

        lModel.register(email, pw).enqueue(object : Callback<Msg> {
            override fun onFailure(call: Call<Msg>, t: Throwable) {
                fastToast("Server error - unable to register")
                return
            }

            override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                if (response.isSuccessful) {
                    fastToast("Successfully registered! Attempting login...")
                    login(lModel)
                } else {
                    if (response.code() == 401) {
                        fastToast("User already exists, attempting login...")
                        login(lModel)
                    } else {
                        fastToast("Failure to register, please try again.")
                    }
                }
            }

        })
    }

    private fun login(lModel: LoginModel) {
        val email = email_input.text.toString()
        val pw = password_input.text.toString()

        lModel.login(email, pw).enqueue(object: Callback<Msg> {
            override fun onFailure(call: Call<Msg>, t: Throwable?) {
                fastToast("Server error - unable to login")
            }

            override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                when {
                    response.isSuccessful -> {
                        response.body()?.let {
                            getDefaultSharedPreferences(applicationContext).edit {
                                putString(getString(R.string.email_storage), email)
                                putString(getString(R.string.token_storage), it.msg)
                            }
                            val intent = Intent(applicationContext, CoffeeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                    response.code() == 401 -> fastToast("Invalid password!")
                    else -> fastToast("Failure to login, please try again...")
                }
            }

        })
    }

    private fun fastToast(msg: String) {
        Toast.makeText(applicationContext, msg, LENGTH_LONG).show()
    }
}
