package com.example.modul9

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.modul9.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    private var auth: FirebaseAuth? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var loadingDialog: LoadingDialog
    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        auth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this@MainActivity)

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.your_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .build()

        binding.btnMasuk.setOnClickListener {
            login(binding.etEmail.text.toString(), binding.etPass.text.toString())
        }

        binding.btnDaftar.setOnClickListener {
            register(binding.etEmail.text.toString(), binding.etPass.text.toString())
        }

        binding.btnGoogle.setOnClickListener {
            loadingDialog.startDialog()
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this) { res ->
                    loadingDialog.dismissDialog()
                    try {
                        startIntentSenderForResult(
                            res.pendingIntent.intentSender, REQ_ONE_TAP, null, 0, 0, 0, null
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                    }
                }
                .addOnFailureListener(this) { e ->
                    Log.d(TAG, e.localizedMessage)
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with Firebase.
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth!!.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        val user = auth!!.currentUser
                                        updateUI(user)
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                                        updateUI(null)
                                    }
                                }
                        }
                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG, "No ID token!")
                        }
                    }

                } catch (e: ApiException) {
                    // ...
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth!!.currentUser
        updateUI(currentUser)
    }

    private fun login(email: String, password: String){
        loadingDialog.startDialog()
        auth!!.signInWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(this) {task ->
                if (task.isSuccessful) {
                    loadingDialog.dismissDialog()
                    Log.d(ContentValues.TAG, "signInWithEmail: success")
                    val user = auth!!.currentUser
                    updateUI(user)
                } else{
                    loadingDialog.dismissDialog()
                    Log.w(ContentValues.TAG, "signInWithEmail: failure", task.exception)
                    Toast.makeText(this@MainActivity, "Auth Failed", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun register(email: String, password: String){
        auth!!.createUserWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(this) {task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "createUserWithEmail:success")
                    val user = auth!!.currentUser
                    updateUI(user)
                    Toast.makeText(this@MainActivity, user.toString(), Toast.LENGTH_SHORT).show()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this@MainActivity, task.exception.toString(), Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun validateForm(): Boolean {
        var result = true
        var etEmail = binding.etEmail
        var etPass = binding.etPass
        if (TextUtils.isEmpty(etEmail!!.text.toString())) {
            etEmail!!.error = "Required"
            result = false
        } else {
            etEmail!!.error = null
        }
        if (TextUtils.isEmpty(etPass!!.text.toString())) {
            etPass!!.error = "Required"
            result = false
        } else {
            etPass!!.error = null
        }
        return result
    }

    fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this@MainActivity, NoteActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this@MainActivity, "Log In First", Toast.LENGTH_SHORT).show()
        }
    }


}