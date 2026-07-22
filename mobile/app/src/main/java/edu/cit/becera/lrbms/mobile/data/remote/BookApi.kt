package edu.cit.becera.lrbms.mobile.data.remote

import edu.cit.becera.lrbms.mobile.data.model.Book
import edu.cit.becera.lrbms.mobile.data.model.BookRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BookApi {
    @GET("books")
    suspend fun getBooks(): List<Book>

    @GET("books/search")
    suspend fun searchBooks(@Query("query") query: String): List<Book>

    @GET("books/availability")
    suspend fun getBooksAvailableOn(@Query("date") date: String): List<Book>

    @POST("books")
    suspend fun createBook(@Body book: BookRequest): Book

    @PUT("books/{id}")
    suspend fun updateBook(@Path("id") id: Long, @Body book: BookRequest): Book

    @DELETE("books/{id}")
    suspend fun deleteBook(@Path("id") id: Long)
}
