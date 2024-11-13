package unical.enterpriceapplication.onlycards.viewmodels

import net.openid.appauth.ResponseTypeValues
import unical.enterpriceapplication.onlycards.R

object AuthConfig {
    const val AUTH_GOOGLE_URI = "https://accounts.google.com/o/oauth2/v2/auth"
    const val TOKEN_GOOGLE_URI = "https://oauth2.googleapis.com/token"
    const val RESPONSE_TYPE = ResponseTypeValues.CODE
    const val SCOPE_GOOGLE = "openid profile email"
    val CLIENT_ID_GOOGLE = R.string.clientIdGoogle
    const val CALLBACK_URL = "unical.enterpriceapplication.onlycards:/oauth2callback"
}