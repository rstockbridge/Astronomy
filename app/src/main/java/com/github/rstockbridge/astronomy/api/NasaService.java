package com.github.rstockbridge.astronomy.api;

import com.github.rstockbridge.astronomy.api.models.DailyPicture;

import androidx.annotation.NonNull;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NasaService {
    @GET("planetary/apod")
    Single<DailyPicture> getDailyPicture(@Query("date") @NonNull final String dateAsString, @Query("api_key") @NonNull final String apiKey);
}
