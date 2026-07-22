package edu.cit.becera.lrbms.mobile.data.remote

import org.json.JSONObject
import retrofit2.HttpException

fun HttpException.errorMessage(): String? = try {
    response()?.errorBody()?.string()?.let { JSONObject(it).optString("error").ifBlank { null } }
} catch (e: Exception) {
    null
}
