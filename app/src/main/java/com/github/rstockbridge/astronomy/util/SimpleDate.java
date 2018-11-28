package com.github.rstockbridge.astronomy.util;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Objects;

public final class SimpleDate implements Parcelable {

    private static final String[] monthText =
            {"January", "February", "March", "April", "May", "June", "July",
                    "August", "September", "October", "November", "December"};

    private final int year;
    private final int month;
    private final int dayOfMonth;

    public SimpleDate(final int year, final int month, final int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    @NonNull
    public static SimpleDate getCurrentDate() {
        final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        final int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        final int currentDayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        return new SimpleDate(currentYear, currentMonth, currentDayOfMonth);
    }

    public SimpleDate getPrevious() {
        final Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.set(Calendar.YEAR, year);
        currentCalendar.set(Calendar.MONTH, month);
        currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        currentCalendar.add(Calendar.DATE, -1);

        final int year = currentCalendar.get(Calendar.YEAR);
        final int month = currentCalendar.get(Calendar.MONTH);
        final int dayOfMonth = currentCalendar.get(Calendar.DAY_OF_MONTH);

        return new SimpleDate(year, month, dayOfMonth);
    }

    public SimpleDate getNext() {
        final Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.set(Calendar.YEAR, year);
        currentCalendar.set(Calendar.MONTH, month);
        currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        currentCalendar.add(Calendar.DATE, 1);

        final int year = currentCalendar.get(Calendar.YEAR);
        final int month = currentCalendar.get(Calendar.MONTH);
        final int dayOfMonth = currentCalendar.get(Calendar.DAY_OF_MONTH);

        return new SimpleDate(year, month, dayOfMonth);
    }

    public String toQueryString() {
        final String monthAsString;
        if (month + 1 < 10) {
            monthAsString = "0" + (month + 1);
        } else {
            monthAsString = String.valueOf(month + 1);
        }

        final String dayAsString;
        if (dayOfMonth < 10) {
            dayAsString = "0" + dayOfMonth;
        } else {
            dayAsString = String.valueOf(dayOfMonth);
        }

        return year + "-" + monthAsString + "-" + dayAsString;
    }

    public String toPrintString() {
        return monthText[month] + " " + dayOfMonth + ", " + year;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SimpleDate that = (SimpleDate) o;
        return year == that.year && month == that.month && dayOfMonth == that.dayOfMonth;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, dayOfMonth);
    }

    public boolean greaterThan(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SimpleDate that = (SimpleDate) o;

        if (year > that.year) {
            return true;
        } else if (year == that.year && month > that.month) {
            return true;
        } else return year == that.year && month == that.month && dayOfMonth > that.dayOfMonth;
    }

    private SimpleDate(Parcel in) {
        year = in.readInt();
        month = in.readInt();
        dayOfMonth = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeInt(dayOfMonth);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SimpleDate> CREATOR = new Creator<SimpleDate>() {
        @Override
        public SimpleDate createFromParcel(Parcel in) {
            return new SimpleDate(in);
        }

        @Override
        public SimpleDate[] newArray(int size) {
            return new SimpleDate[size];
        }
    };
}
