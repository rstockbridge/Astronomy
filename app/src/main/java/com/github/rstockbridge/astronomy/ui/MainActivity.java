package com.github.rstockbridge.astronomy.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.github.rstockbridge.astronomy.R;
import com.github.rstockbridge.astronomy.api.ImageFetcher;
import com.github.rstockbridge.astronomy.api.MyImageCallback;
import com.github.rstockbridge.astronomy.api.models.DailyPicture;
import com.github.rstockbridge.astronomy.util.MyResult;
import com.github.rstockbridge.astronomy.util.SimpleDate;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerFullScreenListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public final class MainActivity
        extends AppCompatActivity
        implements DatePickerFragment.OnDateSetListener {

    private static final int REQUEST_CODE_SHARE_INTENT = 5810;
    private static final String PREF_FILE_LABELS = "savedFiles";

    private MainViewModel viewModel;

    private ProgressBar progressBar;
    private ScrollView imageLayout;
    private LinearLayout youtubeLayout;

    private ImageButton previousButton;
    private ImageButton nextButton;

    private TextView imageDateLabel;
    private TextView imageTitleLabel;
    private ImageView imageView;
    private TextView imageExplanationLabel;

    private ScrollView youtubeScrollView;
    private TextView youtubeDateLabel;
    private TextView youtubeTitleLabel;
    private TextView youtubeExplanationLabel;
    private YouTubePlayerView youTubePlayerView;

    @NonNull
    private SimpleDate currentDate = SimpleDate.getCurrentDate();

    @Nullable
    private DailyPicture currentDailyPicture;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        initializeViews();
        observeDailyPicture();

        viewModel.fetchDailyPicture(currentDate.toQueryString());

        previousButton.setOnClickListener(it -> {
            currentDate = currentDate.getPrevious();
            viewModel.fetchDailyPicture(currentDate.toQueryString());
        });

        nextButton.setOnClickListener(it -> {
            currentDate = currentDate.getNext();
            viewModel.fetchDailyPicture(currentDate.toQueryString());
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        deleteSavedFiles();
    }

    @Override
    public void onBackPressed() {
        if (youTubePlayerView.isFullScreen()) {
            youTubePlayerView.exitFullScreen();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_date:
                showDatePickerDialog(currentDate);
                return true;

            case R.id.share:
                // image layout is active
                if (!imageTitleLabel.getText().toString().equals("")) {
                    final Bitmap imageBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    final Uri imageUri = getImageUri(imageBitmap, currentDate.toQueryString());

                    if (imageUri != null) {
                        final Intent intent = getImageShareIntent(imageUri);
                        startActivityForResult(intent, REQUEST_CODE_SHARE_INTENT);
                    } else {
                        makeToast("Unable to share this date!");
                    }
                }
                // youtube layout is active
                else if (!youtubeTitleLabel.getText().toString().equals("")) {
                    final Intent intent = getYoutubeShareIntent();
                    startActivity(intent);
                } else {
                    makeToast("Unable to share this date!");
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfiguration) {
        super.onConfigurationChanged(newConfiguration);
        youTubePlayerView.getPlayerUIController().getMenu().dismiss();
    }

    @Override
    public void processDatePickerResult(final int year, final int month, final int dayOfMonth) {
        final SimpleDate newDate = new SimpleDate(year, month, dayOfMonth);

        if (newDate.greaterThan(SimpleDate.getCurrentDate())) {
            makeToast(getResources().getString(R.string.future_date, newDate.toPrintString()));
        } else {
            currentDate = newDate;
            viewModel.fetchDailyPicture(currentDate.toQueryString());
        }
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progress_bar);
        imageLayout = findViewById(R.id.image_layout);
        youtubeLayout = findViewById(R.id.youtube_layout);

        previousButton = findViewById(R.id.previous_button);
        nextButton = findViewById(R.id.next_button);

        imageDateLabel = findViewById(R.id.image_date);
        imageTitleLabel = findViewById(R.id.image_title);
        imageView = findViewById(R.id.image_view);
        imageExplanationLabel = findViewById(R.id.image_explanation);

        youtubeScrollView = findViewById(R.id.youtube_scrollview);
        youtubeDateLabel = findViewById(R.id.youtube_date);
        youtubeTitleLabel = findViewById(R.id.youtube_title);
        youTubePlayerView = findViewById(R.id.youtube_player_view);
        youtubeExplanationLabel = findViewById(R.id.youtube_explanation);
    }

    private void observeDailyPicture() {
        final LiveData<MyResult<DailyPicture>> dailyPictureObservable = viewModel.getDailyPicture();

        dailyPictureObservable.observe(this, result -> {
            switch (result.getStatus()) {
                case SUCCESS:
                    if (!isUiValid()) {
                        return;
                    }

                    syncViewsWithNetworkCall(false);
                    currentDailyPicture = result.getData();
                    processDailyPicture();
                    break;
                case ERROR:
                    syncViewsWithNetworkCall(false);
                    makeToast(getResources().getString(R.string.wrong_message, currentDate.toPrintString()));
                    break;
                case LOADING:
                    syncViewsWithNetworkCall(true);
                    break;
            }
        });
    }

    private void processDailyPicture() {
        if (currentDailyPicture.getMediaType().equals("image")) {
            bindImageLayoutData();
        }
        // can only process Youtube videos (not e.g. Vimeo videos)
        else if (currentDailyPicture.getMediaType().equals("video") && currentDailyPicture.getUrl().contains("youtube")) {
            if (currentDailyPicture.getVideoId() != null) {
                bindYoutubeLayoutData();
            } else {
                makeToast(getResources().getString(R.string.wrong_message, currentDate.toPrintString()));
            }
        } else {
            makeToast(getResources().getString(R.string.wrong_message, currentDate.toPrintString()));
        }

        // disable previous button if on June 16, 1995
        configureButton(previousButton, !currentDate.equals(new SimpleDate(1995, 5, 16)));

        // disable next button if on today's date
        configureButton(nextButton, !currentDate.equals(SimpleDate.getCurrentDate()));
    }

    private void showDatePickerDialog(@NonNull final SimpleDate date) {
        final DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(date.getYear(), date.getMonth(), date.getDayOfMonth());
        datePickerFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private void syncViewsWithNetworkCall(final boolean inProgress) {
        configureButton(previousButton, !inProgress);
        configureButton(nextButton, !inProgress);

        if (inProgress) {
            clearImageLayoutData();
            clearYoutubeLayoutData();
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void configureButton(@NonNull final ImageButton button, final boolean isEnabled) {
        button.setEnabled(isEnabled);

        if (isEnabled) {
            button.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.silver)));
        } else {
            button.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        }
    }

    private boolean isUiValid() {
        return !isFinishing() && !isDestroyed();
    }

    private void makeToast(@NonNull final String message) {
        final Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);

        final View view = toast.getView();
        view.setBackgroundResource(R.color.colorPrimary);

        final TextView toastMessage = toast.getView().findViewById(android.R.id.message);
        toastMessage.setGravity(Gravity.CENTER);
        toastMessage.setTextColor(getResources().getColor(R.color.silver));

        toast.show();
    }

    private void bindImageLayoutData() {
        ImageFetcher.loadImage(currentDailyPicture.getUrl(), imageView, new MyImageCallback() {
            @Override
            public void onSuccess() {
                imageLayout.bringToFront();

                // show ScrollView as scrolled to the top
                imageLayout.post(() -> imageLayout.fullScroll(ScrollView.FOCUS_UP));

                imageDateLabel.setText(currentDate.toPrintString());
                imageTitleLabel.setText(currentDailyPicture.getTitle());
                imageExplanationLabel.setText(currentDailyPicture.getExplanation());
            }

            @Override
            public void onFailure() {
                makeToast(getResources().getString(R.string.wrong_message, currentDate.toPrintString()));
            }
        });
    }

    private void clearImageLayoutData() {
        imageDateLabel.setText("");
        imageTitleLabel.setText("");
        imageExplanationLabel.setText("");
        imageView.setImageDrawable(null);
    }

    private void bindYoutubeLayoutData() {
        youtubeLayout.bringToFront();

        // show ScrollView as scrolled to the top
        youtubeScrollView.post(() -> youtubeScrollView.fullScroll(ScrollView.FOCUS_UP));

        youtubeDateLabel.setText(currentDate.toPrintString());
        youtubeTitleLabel.setText(currentDailyPicture.getTitle());
        youtubeExplanationLabel.setText(currentDailyPicture.getExplanation());

        getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerView.setVisibility(View.VISIBLE);
        youTubePlayerView.initialize(initializedYouTubePlayer ->
                initializedYouTubePlayer.addListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady() {
                        loadVideo(initializedYouTubePlayer, currentDailyPicture.getVideoId());
                    }
                }), true);

        addFullScreenListenerToPlayer();
    }

    private void clearYoutubeLayoutData() {
        youtubeDateLabel.setText("");
        youtubeTitleLabel.setText("");
        youtubeExplanationLabel.setText("");
        youTubePlayerView.setVisibility(View.INVISIBLE);
    }

    private void loadVideo(@NonNull final YouTubePlayer youTubePlayer, @NonNull final String videoId) {
        youTubePlayer.cueVideo(videoId, 0);
    }

    private void addFullScreenListenerToPlayer() {
        youTubePlayerView.addFullScreenListener(new YouTubePlayerFullScreenListener() {
            @Override
            public void onYouTubePlayerEnterFullScreen() {
                enterFullScreen(
                        youtubeDateLabel,
                        youtubeTitleLabel,
                        youtubeExplanationLabel,
                        nextButton,
                        previousButton
                );
            }

            @Override
            public void onYouTubePlayerExitFullScreen() {
                exitFullScreen(youtubeDateLabel,
                        youtubeTitleLabel,
                        youtubeExplanationLabel,
                        nextButton,
                        previousButton);
            }
        });
    }

    private void enterFullScreen(@NonNull final View... views) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        youTubePlayerView.setPadding(0, 0, 0, 0);

        // hide status bar
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        // hide action bar
        getSupportActionBar().hide();

        for (final View view : views) {
            view.setVisibility(View.GONE);
            view.invalidate();
        }
    }

    private void exitFullScreen(@NonNull final View... views) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        int valueInDp = 16;
        int valueInPixels = (int) (valueInDp * getResources().getDisplayMetrics().density);
        youTubePlayerView.setPadding(valueInPixels, 0, valueInPixels, 0);

        // show status bar
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        // show action bar
        getSupportActionBar().show();

        for (final View view : views) {
            view.setVisibility(View.VISIBLE);
            view.invalidate();
        }
    }

    private String formatShareText(
            @NonNull final String date,
            @NonNull final String title,
            @NonNull final String explanation) {

        return date + "\n\n" + title + "\n\n" + explanation;
    }

    private String formatShareTextAndVideo(
            @NonNull final String date,
            @NonNull final String title,
            @NonNull final String explanation,
            @NonNull final String videoURL) {

        return formatShareText(date, title, explanation) + "\n\n" + videoURL;
    }

    private Intent getImageShareIntent(@NonNull final Uri imageUri) {
        final Intent result = new Intent();

        final String text = formatShareText(currentDate.toPrintString(), currentDailyPicture.getTitle(), currentDailyPicture.getExplanation());
        result.setAction(Intent.ACTION_SEND);
        result.putExtra(Intent.EXTRA_TEXT, text);
        result.putExtra(Intent.EXTRA_STREAM, imageUri);
        result.setType("image/jpg");

        return result;
    }

    private Intent getYoutubeShareIntent() {
        final Intent result = new Intent();

        final String text = formatShareTextAndVideo(currentDate.toPrintString(), currentDailyPicture.getTitle(),
                currentDailyPicture.getExplanation(), currentDailyPicture.getUrl());
        result.setAction(Intent.ACTION_SEND);
        result.putExtra(Intent.EXTRA_TEXT, text);
        result.setType("text/plain");

        return result;
    }

    private Uri getImageUri(@NonNull final Bitmap imageBitmap, @NonNull final String label) {
        final File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "apod_" + label + ".jpg");
        final String newSharedPrefString = sharedPreferences.getString(PREF_FILE_LABELS, "") + label + ";";

        if (!imageFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                imageFile.createNewFile();

                try {
                    FileOutputStream out = new FileOutputStream(imageFile);
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    sharedPreferences.edit().putString(PREF_FILE_LABELS, newSharedPrefString).apply();
                    return FileProvider.getUriForFile(this,
                            "com.github.rstockbridge.astronomy.fileprovider", imageFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sharedPreferences.edit().putString(PREF_FILE_LABELS, newSharedPrefString).apply();
            return FileProvider.getUriForFile(this,
                    "com.github.rstockbridge.astronomy.fileprovider", imageFile);
        }

        return null;
    }

    private void deleteSavedFiles() {
        final String concatenatedFileLabels = sharedPreferences.getString(PREF_FILE_LABELS, "");
        if (concatenatedFileLabels != null) {
            final String[] fileLabels = concatenatedFileLabels.split(";");

            for (final String fileLabel : fileLabels) {
                final File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "apod_" + fileLabel + ".jpg");
                if (imageFile.exists()) {
                    final boolean deletionSuccessful = imageFile.delete();

                    if (deletionSuccessful) {
                        final String newSharedPrefString = concatenatedFileLabels.replace(fileLabel + ";", "");
                        sharedPreferences.edit().putString(PREF_FILE_LABELS, newSharedPrefString).apply();

                    }
                }
            }
        }

    }
}
