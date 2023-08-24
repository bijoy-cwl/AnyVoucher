package com.anyvoucher.app.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.anyvoucher.app.CompanyData
import com.anyvoucher.app.utility.AppPreference
import com.anyvoucher.app.R
import com.anyvoucher.app.UserData
import com.anyvoucher.app.databinding.SetCompanyDataLayoutBinding
import com.anyvoucher.app.utility.Dialog
import com.anyvoucher.app.utility.Utils
import com.anyvoucher.app.utility.Utils.hideKeyboard
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class InitActivity : AppCompatActivity() {
    private lateinit var prefs: AppPreference

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInButton: SignInButton

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var loadingDialog: Dialog
    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)

        prefs = AppPreference(this)
        googleSignInButton = findViewById(R.id.sign_in_button)

         loadingDialog = Dialog(this)



        // [START config_signin]
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        // [END config_signin]

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]

        googleSignInButton.setOnClickListener {
            checkCurrentUser();
        }
    }

    private fun checkCurrentUser() {
        // [START check_current_user]
        val user = Firebase.auth.currentUser
        if (user != null) {
            // User is signed in
            Log.e("IA", "name: ${user.displayName}")
            Log.e("IA", "email: ${user.email}")
            Log.e("IA", "udid: ${user.uid}")
        } else {
            // No user is signed in
            signIn()
        }
        // [END check_current_user]
    }

    private fun getUserProfile() {
        // [START get_user_profile]
        val user = Firebase.auth.currentUser
        user?.let {
            // Name, email address, and profile photo Url
            val name = it.displayName
            val email = it.email
            val photoUrl = it.photoUrl

            // Check if user's email is verified
            val emailVerified = it.isEmailVerified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            val uid = it.uid

            Log.e("IA", "Name: $name")
        }
        // [END get_user_profile]
    }

    // [START onactivityresult]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }
    // [END onactivityresult]


    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String) {
        loadingDialog.startLoadingDialog()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                loadingDialog.dismissDialog()
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser

                    user?.let {
                        // Name, email address, and profile photo Url
                        val name = it.displayName.toString()
                        val email = it.email.toString()
                        val photoUrl = it.photoUrl

                        // Check if user's email is verified
                        val emailVerified = it.isEmailVerified

                        // The user's ID, unique to the Firebase project. Do NOT use this value to
                        // authenticate with your backend server, if you have one. Use
                        // FirebaseUser.getIdToken() instead.
                        val uid = it.uid

                        Log.e("IA", "Name: $name")
                        val userData = UserData(uid, name, email)

                        prefs.setUserData(userData, true)

                        setUpData(userData)
                    }

                } else {
                    Toast.makeText(this, "Sign In failed", Toast.LENGTH_SHORT).show()
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    //  updateUI(null)
                }
            }
    }

    private fun setUpData(userData: UserData) {

        val dialogBinding: SetCompanyDataLayoutBinding? =
            DataBindingUtil.inflate(
                LayoutInflater.from(this),
                R.layout.set_company_data_layout,
                null,
                false
            )

        val customDialog = AlertDialog.Builder(this, 0).create()

        customDialog.apply {
            setView(dialogBinding?.root)
            setCancelable(false)
        }.show()

        dialogBinding?.submitButton?.setOnClickListener {

            val name = dialogBinding.nameET.text.toString().trim()
            val number = dialogBinding.numberET.text.toString().trim()
            val address = dialogBinding.addressET.text.toString().trim()

            if (name == "") {
                showToast("Enter name")
                dialogBinding.nameTIL.editText?.requestFocus();
            } else if (number == "") {
                showToast("Enter mobile number")
                dialogBinding.numberTIL.editText?.requestFocus();
            } else if (address == "") {
                showToast("Enter address")
                dialogBinding.addressTIL.editText?.requestFocus();
            } else {
                hideKeyboard()
                loadingDialog.startLoadingDialog()
                val db = Firebase.firestore
                val company = hashMapOf(
                    "uID" to userData.uID,
                    "name" to userData.name,
                    "email" to userData.email,
                    "cName" to name,
                    "cMobile" to number,
                    "cAddress" to address
                )


                db.collection("company").document(userData.uID).get().addOnSuccessListener {
                    if (it.exists()){
                        loadingDialog.dismissDialog()
                        val intent2 = Intent(this@InitActivity, MainActivity::class.java)
                        startActivity(intent2)
                        finish()
                    }else{
                        // Add a new document with a generated ID
                        db.collection("company")
                            .document(userData.uID)
                            .set(company)
                            .addOnSuccessListener {
                                prefs.setCompanyData(CompanyData(name,number,address))
                                loadingDialog.dismissDialog()
                                val intent2 = Intent(this@InitActivity, MainActivity::class.java)
                                startActivity(intent2)
                                finish()
                                customDialog.dismiss();
                                showToast("Login Successful")

                            }
                            .addOnFailureListener { e ->
                                loadingDialog.dismissDialog()
                                Log.w(TAG, "Error adding document", e)
                                showToast("Something went wrong")
                            }

                    }
                }



            }

        }

    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // [END auth_with_google]
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    // [END signin]


}