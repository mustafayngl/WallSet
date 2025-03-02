package com.rhino.wallset;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiService {

    @Headers("Authorization: LPcBoWFrVxShADqVr0InFqciuoIBQ1xrZdDT5ICiTgTdBEHhqu0mmeNy") // Pexels API key'inizi buraya ekleyin
    @GET("v1/curated")
    Call<WallpaperResponse> getWallpapers(@Query("page") int page, @Query("per_page") int perPage);
}
