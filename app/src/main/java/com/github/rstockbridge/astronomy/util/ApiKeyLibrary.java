package com.github.rstockbridge.astronomy.util;

public final class ApiKeyLibrary {

    private static final ApiKeyLibrary INSTANCE = new ApiKeyLibrary();

    private ApiKeyLibrary() {
        // specify module name
        System.loadLibrary("api-keys");
    }

    public native String getAPIKey();

    public static ApiKeyLibrary getInstance() {
        return INSTANCE;
    }
}
