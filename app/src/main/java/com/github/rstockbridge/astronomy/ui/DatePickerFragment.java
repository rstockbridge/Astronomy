package com.github.rstockbridge.astronomy.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;

public final class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public interface OnDateSetListener {
        void processDatePickerResult(int year, int month, int dayOfMonth);
    }

    private OnDateSetListener listener;

    private static final String ARG_YEAR = "year";
    private static final String ARG_MONTH = "month";
    private static final String ARG_DAY_OF_MONTH = "dayOfMonth";

    static DatePickerFragment newInstance(final int year, final int month, final int dayOfMonth) {
        final Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_DAY_OF_MONTH, dayOfMonth);

        final DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        try {
            listener = (OnDateSetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnDateSetListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int year = getArguments().getInt(ARG_YEAR);
        final int month = getArguments().getInt(ARG_MONTH);
        int day = getArguments().getInt(ARG_DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(final DatePicker view, final int year, final int month, final int dayOfMonth) {
        listener.processDatePickerResult(year, month, dayOfMonth);
    }
}
