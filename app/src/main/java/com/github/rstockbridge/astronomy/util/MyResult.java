package com.github.rstockbridge.astronomy.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyResult<T> {
    private final Status status;
    private final T data;

    public MyResult(@NonNull final Status status, @Nullable final T data) {
        this.status = status;
        this.data = data;
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }
}
