package com.example.uberapp.gui.account;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextSwitcher;

import com.example.uberapp.R;
import com.example.uberapp.gui.main.PassengerMainActivity;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;

public class PassengerReportFragment extends Fragment {
    public PassengerReportFragment() {
        // Required empty public constructor
    }

    private LocalDate startDate;
    private LocalDate endDate;
    private final String[] reportTypes = {"report 1", "report 2", "report 3"};
    private int currentReportIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passenger_report, container, false);
        ImageButton buttonPickStartDate = view.findViewById(R.id.buttonPickStartingDate);
        ImageButton buttonPickEndDate = view.findViewById(R.id.buttonPickEndDate);

        TextSwitcher switcher = view.findViewById(R.id.textSwitcherReport);
        switcher.setCurrentText(reportTypes[currentReportIndex]);
        ImageButton nextReport = view.findViewById(R.id.buttonNextReport);
        ImageButton prevReport = view.findViewById(R.id.buttonPreviousReport);

        Calendar now = Calendar.getInstance();

        buttonPickStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                startDate = LocalDate.of(year, monthOfYear, dayOfMonth);
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "DatePickerDialog");
            }
        });

        buttonPickEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                endDate = LocalDate.of(year, monthOfYear, dayOfMonth);
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "DatePickerDialog");
            }
        });


        nextReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentReportIndex++;
                currentReportIndex %= reportTypes.length;
                switcher.setCurrentText(reportTypes[currentReportIndex]);
            }
        });

        prevReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentReportIndex--;
                currentReportIndex = Math.floorMod(currentReportIndex, reportTypes.length);
                switcher.setCurrentText(reportTypes[currentReportIndex]);
            }
        });
        return view;
    }

}