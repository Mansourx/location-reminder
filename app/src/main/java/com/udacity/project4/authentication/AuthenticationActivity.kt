package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*

class AuthenticationActivity : AppCompatActivity() {

    private val viewModel by viewModels<AuthenticationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        observeAuthState()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            val displayName = FirebaseAuth.getInstance().currentUser?.displayName
            val successMsg = getString(R.string.success_signin_msg) + displayName
            val errorMsg = getString(R.string.failed_signin_msg) + response?.error?.errorCode
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, successMsg + displayName, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeAuthState() {
        viewModel.authenticationState.observe(this) { state ->
            when (state) {
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    val intent = Intent(this@AuthenticationActivity, RemindersActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                AuthenticationViewModel.AuthenticationState.UNAUTHENTICATED -> {
                    login_btn.setOnClickListener { signIn() }
                    val msg = getString(R.string.unauthenticated_msg)
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    private fun signIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(), SIGN_IN_REQUEST_CODE
        )
    }

    companion object {
        const val SIGN_IN_REQUEST_CODE = 10001
    }
}
