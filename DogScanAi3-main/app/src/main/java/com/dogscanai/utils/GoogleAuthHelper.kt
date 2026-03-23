package com.firstapp.dogscanai.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

object GoogleAuthHelper {

    const val WEB_CLIENT_ID = "450342018163-rcp4l5apnfs2htjri72ot2o7snb93dtq.apps.googleusercontent.com"
    const val RC_SIGN_IN = 1001

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()
        Log.d("GOOGLE_AUTH", "GSO configured with Web Client ID: $WEB_CLIENT_ID")
        return GoogleSignIn.getClient(context, gso)
    }
}