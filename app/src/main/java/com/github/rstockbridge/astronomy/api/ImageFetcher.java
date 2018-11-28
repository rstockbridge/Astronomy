package com.github.rstockbridge.astronomy.api;

import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;

public final class ImageFetcher {

    private ImageFetcher() {
    }

    public static void loadImage(
            @NonNull final String url,
            @NonNull final ImageView imageView,
            @NonNull final MyImageCallback myImageCallback) {

        Picasso.get()
                .load(url)
                .resize(imageView.getWidth(), 0)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        myImageCallback.onSuccess();
                    }

                    @Override
                    public void onError(final Exception e) {
                        myImageCallback.onFailure();
                    }
                });
    }
}
