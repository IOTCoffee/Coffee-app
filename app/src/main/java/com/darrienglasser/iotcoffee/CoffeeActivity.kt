package com.darrienglasser.iotcoffee

import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.support.v7.app.AppCompatActivity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import kotlinx.android.synthetic.main.activity_coffee.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class CoffeeActivity : AppCompatActivity() {

    private lateinit var bm: BrewModel
    private lateinit var email: String
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coffee)

        email = getDefaultSharedPreferences(applicationContext)
                .getString(getString(R.string.email_storage), "")

        token = getDefaultSharedPreferences(applicationContext)
                .getString(getString(R.string.token_storage), "")

        if (email.isBlank() || token.isBlank()) {
            throw RuntimeException("Somehow got this far without a token and email???")
        }

        val r = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

        bm = r.create(BrewModel::class.java)

        checkCoffeeMaker()
    }

    private fun checkCoffeeMaker() {
        checked_view.visibility = GONE
        init_view.visibility = VISIBLE
        set_url_text.visibility = GONE

        Handler().postDelayed({
            bm.brewPing(email, token).enqueue(object: Callback<Msg> {
                override fun onFailure(ret: Call<Msg>, t: Throwable) {
                    fastToast("Server error, try again in a moment...")
                    setOffline()
                    return
                }

                override fun onResponse(ret: Call<Msg>, response: Response<Msg>) {
                    when {
                        response.isSuccessful -> {
                            setOnline()
                            return
                        }
                        response.code() == 404 -> setNeedsCoffee()
                        else -> {
                            setOffline()
                            return
                        }
                    }
                }
            })
        }, 1000)
    }

    private fun setOnline() {
        init_view.visibility = GONE
        checked_view.visibility = VISIBLE
        action_button.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_coffee),
                null,
                null,
                null)

        status_msg.text = getString(R.string.online)
        action_button.text = getText(R.string.brew)

        action_button.setOnClickListener {
            bm.brew(email, token).enqueue(object: Callback<Msg> {
                override fun onFailure(call: Call<Msg>?, t: Throwable?) {
                    fastToast("Your coffee is brewing????")
                }

                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                    if (response.isSuccessful) {
                        fastToast("Brewing coffee!")
                    } else {
                        fastToast("Brewing coffee!")
                    }
                }

            })
        }
    }

    private fun setOffline() {
        init_view.visibility = GONE
        checked_view.visibility = VISIBLE
        action_button.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_refresh),
                null,
                null,
                null)

        status_msg.text = getString(R.string.offline)
        action_button.text = getText(R.string.retry)

        action_button.setOnClickListener {
            Toast.makeText(applicationContext, "Checking connectivity", LENGTH_LONG).show()
            bm.brewPing(email, token).enqueue(object: Callback<Msg> {
                override fun onFailure(call: Call<Msg>, t: Throwable?) {
                    fastToast("Coffee maker still offline. Try again in a moment...")
                }

                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                    when {
                        response.code() == 404 -> setNeedsCoffee()
                        response.isSuccessful -> {
                            fastToast("Coffee maker is online again and ready to brew!")
                            setOnline()
                        }
                        else -> {
                            fastToast("Unable to reach coffee maker. Try again in a moment!")
                            setOffline()
                        }
                    }
                }

            })
        }
    }

    private fun setNeedsCoffee() {
        init_view.visibility = GONE
        checked_view.visibility = VISIBLE
        action_button.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_explore_black_48px),
                null,
                null,
                null)

        status_msg.text = getString(R.string.set_url)
        action_button.text = getString(R.string.set_url_button_text)
        set_url_text.visibility = VISIBLE

        action_button.setOnClickListener {
            bm.setBrew(email, token, set_url_text.text.toString(), "").enqueue(object :Callback<Msg> {
                override fun onFailure(call: Call<Msg>?, t: Throwable?) {
                    fastToast("Server error, try again in a moment...")
                }

                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                    if (response.isSuccessful) {
                        fastToast("Successfully set URL!")
                        checkCoffeeMaker()
                    } else {
                        fastToast("Unable to set URL, check again in a moment...")
                    }
                }

            })
        }
    }

    private fun fastToast(msg: String) {
        Toast.makeText(applicationContext, msg, LENGTH_LONG).show()
    }
}
