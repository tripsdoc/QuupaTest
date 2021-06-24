package com.hsc.quupa.data.network

import com.hsc.quupa.data.model.quupa.position.TagPositionResponse
import com.hsc.quupa.data.model.quupa.qda.QdaPositionResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface QuupaServices {
    @GET("getTagPosition?version=2&humanReadable=true&ignoreUnknownTags=true&maxAge=900000000")
    fun getTagPosition(
        @Query("tag") tags: String
    ): Observable<TagPositionResponse>

    @GET("getTagPosition")
    fun getQdaPosition(
        @Query("tag") tags: String
    ): Observable<List<QdaPositionResponse>>
}