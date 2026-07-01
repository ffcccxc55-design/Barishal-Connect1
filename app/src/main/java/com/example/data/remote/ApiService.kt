package com.example.data.remote

import com.example.data.model.DirectoryItem
import com.example.data.model.SmartRoad
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    @GET
    suspend fun getDirectoryItems(
        @Url url: String,
        @Query("action") action: String
    ): Response<GasResponse<List<DirectoryItem>>>

    @GET
    suspend fun getSmartRoads(
        @Url url: String,
        @Query("action") action: String = "getRoads"
    ): Response<GasResponse<List<SmartRoad>>>

    @POST
    suspend fun postAction(
        @Url url: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<GasPostResponse>
}

data class GasResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String? = null
)

data class GasPostResponse(
    val success: Boolean,
    val itemId: String? = null,
    val userId: String? = null,
    val roadId: String? = null,
    val donorId: String? = null,
    val status: String? = null,
    val message: String? = null
)
