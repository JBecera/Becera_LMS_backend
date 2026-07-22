package edu.cit.becera.lrbms.mobile.data.model

import com.google.gson.annotations.SerializedName

data class Book(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("author") val author: String,
    @SerializedName("isbn") val isbn: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("coverImage") val coverImage: String? = null,
    @SerializedName("totalCopies") val totalCopies: Int? = null,
    @SerializedName("availableCopies") val availableCopies: Int = 0,
    @SerializedName("availableOnDate") val availableOnDate: Int? = null
)

data class BookRequest(
    @SerializedName("title") val title: String,
    @SerializedName("author") val author: String,
    @SerializedName("isbn") val isbn: String,
    @SerializedName("category") val category: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("coverImage") val coverImage: String? = null,
    @SerializedName("totalCopies") val totalCopies: Int? = null,
    @SerializedName("availableCopies") val availableCopies: Int? = null
)
