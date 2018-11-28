package com.github.rstockbridge.astronomy.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.rstockbridge.astronomy.api.NasaApi;
import com.github.rstockbridge.astronomy.api.models.DailyPicture;
import com.github.rstockbridge.astronomy.util.MyResult;
import com.github.rstockbridge.astronomy.util.Status;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainViewModel extends ViewModel {

    private final CompositeDisposable disposable = new CompositeDisposable();
    private final MutableLiveData<MyResult<DailyPicture>> dailyPicture = new MutableLiveData<>();

    @Override
    protected void onCleared() {
        disposable.dispose();
        super.onCleared();
    }

    LiveData<MyResult<DailyPicture>> getDailyPicture() {
        return dailyPicture;
    }

    void fetchDailyPicture(@NonNull final String inputDate) {
        final DisposableSingleObserver<DailyPicture> observer = NasaApi.getSharedInstance()
                .getDailyPicture(inputDate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(it -> dailyPicture.setValue(new MyResult<>(Status.LOADING, null)))
                .subscribeWith(new DisposableSingleObserver<DailyPicture>() {
                    @Override
                    public void onSuccess(final DailyPicture dailyPictureResult) {
                        dailyPicture.setValue(new MyResult<>(Status.SUCCESS, dailyPictureResult));
                    }

                    @Override
                    public void onError(final Throwable e) {
                        dailyPicture.setValue(new MyResult<>(Status.ERROR, null));
                    }
                });

        disposable.add(observer);
    }
}
