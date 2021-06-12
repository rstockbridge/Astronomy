package com.github.rstockbridge.astronomy.api;

import androidx.annotation.NonNull;

import com.github.rstockbridge.astronomy.api.models.DailyPicture;
import com.github.rstockbridge.astronomy.util.ApiKeyLibrary;

import io.reactivex.Single;

public final class NasaApi {

    private static NasaApi sharedInstance;

    @NonNull
    public static NasaApi getSharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new NasaApi();
        }

        return sharedInstance;
    }

    private final NasaService service;

    private NasaApi() {
        service = RetrofitWrapper.getRetrofitInstance().create(NasaService.class);
    }

    public Single<DailyPicture> getDailyPicture(@NonNull final String inputDate) {
        return service.getDailyPicture(inputDate, ApiKeyLibrary.getInstance().getAPIKey());
    }
}
