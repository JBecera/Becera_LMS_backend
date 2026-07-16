package edu.cit.becera.lrbms.mobile.data.remote

import edu.cit.becera.lrbms.mobile.data.model.Book
import retrofit2.http.GET
import retrofit2.http.Query

interface BookApi {
    @GET("books")
    suspend fun getBooks(): List<Book>

    @GET("books/search")
    suspend fun searchBooks(@Query("query") query: String): List<Book>
}
