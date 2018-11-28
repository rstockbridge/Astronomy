package com.github.rstockbridge.astronomy.api.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DailyPicture {

    @NonNull
    private String date;

    @NonNull
    private String explanation;

    @NonNull
    private String title;

    @NonNull
    @Json(name = "media_type")
    private String mediaType;

    @NonNull
    private String url;

    @NonNull
    public String getDate() {
        return date;
    }

    @NonNull
    public String getExplanation() {
        return removeUnintendedExplanation();
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getMediaType() {
        return mediaType;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    @Nullable
    public String getVideoId() {
        if (mediaType.equals("video")) {
            final String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
            final Pattern compiledPattern = Pattern.compile(pattern);
            final Matcher matcher = compiledPattern.matcher(url);

            if (matcher.find()) {
                return matcher.group();
            } else {
                return null;
            }
        }

        return null;
    }

    @NonNull
    private String removeUnintendedExplanation() {
        if (explanation.contains("   Latest:")) {
            return explanation.substring(0, explanation.indexOf("   Latest:"));
        } else if (explanation.contains("    Latest:")) {
            return explanation.substring(0, explanation.indexOf("    Latest:"));
        } else if (explanation.contains("   Gallery:")) {
            return explanation.substring(0, explanation.indexOf("   Gallery:"));
        } else if (explanation.contains("   New:")) {
            return explanation.substring(0, explanation.indexOf("   New:"));
        } else if (explanation.contains("  Open Science:")) {
            return explanation.substring(0, explanation.indexOf("  Open Science:"));
        } else if (explanation.contains("   Free Download:")) {
            return explanation.substring(0, explanation.indexOf("   Free Download:"));
        } else if (explanation.contains("   Follow APOD on:")) {
            return explanation.substring(0, explanation.indexOf("   Follow APOD on:"));
        } else if (explanation.contains("  Watch:")) {
            return explanation.substring(0, explanation.indexOf("  Watch:"));
        } else if (explanation.contains("   Jump around the Universe:")) {
            return explanation.substring(0, explanation.indexOf("   Jump around the Universe:"));
        } else if (explanation.contains("    Anniversary:")) {
            return explanation.substring(0, explanation.indexOf("    Anniversary:"));
        } else if (explanation.contains("   Space Telescope Live:")) {
            return explanation.substring(0, explanation.indexOf("   Space Telescope Live:"));
        } else if (explanation.contains("   Share the Sky:")) {
            return explanation.substring(0, explanation.indexOf("   Share the Sky:"));
        } else if (explanation.contains("   Teachers:")) {
            return explanation.substring(0, explanation.indexOf("   Teachers:"));
        } else if (explanation.contains("    APOD in other languages:")) {
            return explanation.substring(0, explanation.indexOf("    APOD in other languages:"));
        } else if (explanation.contains("    Sky Link:")) {
            return explanation.substring(0, explanation.indexOf("    Sky Link:"));
        } else if (explanation.contains("   How to:")) {
            return explanation.substring(0, explanation.indexOf("   How to:"));
        } else if (explanation.contains("   Still going on:")) {
            return explanation.substring(0, explanation.indexOf("   Still going on:"));
        } else if (explanation.contains("   Free APOD Lecture:")) {
            return explanation.substring(0, explanation.indexOf("   Free APOD Lecture:"));
        } else if (explanation.contains("    Free APOD Lectures:")) {
            return explanation.substring(0, explanation.indexOf("    Free APOD Lectures:"));
        } else if (explanation.contains("   Free APOD Lecture Tomorrow:")) {
            return explanation.substring(0, explanation.indexOf("   Free APOD Lecture Tomorrow:"));
        } else if (explanation.contains("   Free APOD Lecture Tonight near Washington, DC:")) {
            return explanation.substring(0, explanation.indexOf("   Free APOD Lecture Tonight near Washington, DC:"));
        }else if (explanation.contains("  Astrophysicists:")) {
            return explanation.substring(0, explanation.indexOf("  Astrophysicists:"));
        }else if (explanation.contains("    Note:")) {
            return explanation.substring(0, explanation.indexOf("    Note:"));
        }else if (explanation.contains("   APOD in other languages:")) {
            return explanation.substring(0, explanation.indexOf("   APOD in other languages:"));
        }else if (explanation.contains("   Surf the Universe:")) {
            return explanation.substring(0, explanation.indexOf("   Surf the Universe:"));
        } else {
            return explanation;
        }
    }
}
