package com.example.easychat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hbb20.CountryCodePicker

class LoginPhoneNumberActivity : AppCompatActivity() {
    var countryCodePicker: CountryCodePicker? = null
    var phoneInput: EditText? = null
    var sendOtpBtn: Button? = null
    var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_login_phone_number)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        countryCodePicker = findViewById(R.id.login_countrycode)
        phoneInput = findViewById(R.id.login_mobile_number)
        sendOtpBtn = findViewById(R.id.send_otp_btn)
        progressBar = findViewById(R.id.login_progress_bar)

        progressBar.setVisibility(View.GONE)

        countryCodePicker.registerCarrierNumberEditText(phoneInput)
        sendOtpBtn.setOnClickListener(View.OnClickListener { v: View? ->
            if (!countryCodePicker.isValidFullNumber()) {
                phoneInput.setError("Phone number not valid")
                return@setOnClickListener
            }
            val intent = Intent(this@LoginPhoneNumberActivity, LoginOtpActivity::class.java)
            intent.putExtra("phone", countryCodePicker.getFullNumberWithPlus())
            startActivity(intent)
        })
    }
}