package com.example.uberapp.gui.fragments.account;

import android.app.DatePickerDialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.uberapp.R;
import com.example.uberapp.core.auth.TokenManager;
import com.example.uberapp.core.dto.ReportDTO;
import com.example.uberapp.core.services.APIClient;
import com.example.uberapp.core.services.ReportService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerReportFragment extends Fragment {
    ImageButton startDate,endDate;
    TextView startDateText,endDateText;
    TextView diagramType;
    BarChart chart;
    ReportService reportService = APIClient.getClient().create(ReportService.class);
    public PassengerReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        View v=inflater.inflate(R.layout.fragment_passenger_report, container, false);
        startDate=(ImageButton) v.findViewById(R.id.startDateReport);
        endDate=(ImageButton) v.findViewById(R.id.endDateReport);
        startDateText=(TextView) v.findViewById(R.id.startDateTextReport);
        endDateText=(TextView) v.findViewById(R.id.endDateTextReport);
        startDateText.setText("2022-12-01");
        endDateText.setText(formatter.format(LocalDateTime.now()));

        chart = v.findViewById(R.id.barchart);


        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        v.getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                String month;
                                if (monthOfYear+1>=10)
                                    month=String.valueOf(monthOfYear+1);
                                else
                                    month="0"+String.valueOf(monthOfYear+1);
                                String day=String.valueOf(dayOfMonth);
                                if (dayOfMonth<10)
                                    day="0"+day;
                                startDateText.setText(year + "-" + month + "-" + day);
                                setData();

                            }
                        },
                        year, month, day);
                datePickerDialog.show();
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        v.getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                String month;
                                if (monthOfYear+1>=10)
                                    month=String.valueOf(monthOfYear+1);
                                else
                                    month="0"+String.valueOf(monthOfYear+1);
                                String day=String.valueOf(dayOfMonth);
                                if (dayOfMonth<10)
                                    day="0"+day;
                                endDateText.setText(year + "-" + month + "-" + day);
                                setData();

                            }
                        },
                        year, month, day);
                datePickerDialog.show();
            }
        });

            //UI reference of textView
        final AutoCompleteTextView diagramTV = v.findViewById(R.id.diagramTextView);
        diagramTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setData();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        diagramType=diagramTV;

        // create list of customer
        ArrayList<String> diagramTypes = new ArrayList<>();
        diagramTypes.add("Rides Per Day");
        diagramTypes.add("Kilometers Per Day");
        diagramTypes.add("Spendings Per Day");
        diagramTV.setInputType(InputType.TYPE_NULL);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_spinner_item, diagramTypes);
        diagramTV.setAdapter(adapter);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.getDescription().setEnabled(false);
        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(15);
        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                chart.getLegend().setTextColor(Color.rgb(233,234,236));
                chart.getAxisLeft().setTextColor(Color.rgb(233,234,236)); // left y-axis
                chart.getAxisRight().setTextColor(Color.rgb(233,234,236));
                chart.getXAxis().setTextColor(Color.rgb(233,234,236));
                chart.getLegend().setTextColor(Color.rgb(233,234,236));
                chart.getDescription().setTextColor(Color.rgb(233,234,236));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                chart.getLegend().setTextColor(Color.rgb(66,66,66));
                chart.getAxisLeft().setTextColor(Color.rgb(66,66,66)); // left y-axis
                chart.getAxisRight().setTextColor(Color.rgb(66,66,66));
                chart.getXAxis().setTextColor(Color.rgb(66,66,66));
                chart.getLegend().setTextColor(Color.rgb(66,66,66));
                chart.getDescription().setTextColor(Color.rgb(66,66,66));
                break;
        }



        setData();




        return v;
    }
    private void setData() {
        Call<ReportDTO> reportDTOCall;
        if (diagramType.getText().toString().equals("Rides Per Day"))
            reportDTOCall = reportService.ridesPerDay(TokenManager.getUserId(),startDateText.getText().toString(),endDateText.getText().toString());
        else if(diagramType.getText().toString().equals("Kilometers Per Day"))
            reportDTOCall = reportService.kilometersPerDay(TokenManager.getUserId(),startDateText.getText().toString(),endDateText.getText().toString());
        else
            reportDTOCall = reportService.spendingsPerDay(TokenManager.getUserId(),startDateText.getText().toString(),endDateText.getText().toString());
        reportDTOCall.enqueue(new Callback<ReportDTO>() {
            @Override
            public void onResponse(Call<ReportDTO> call, Response<ReportDTO> response) {
                ReportDTO report=response.body();
                ArrayList<BarEntry> barEntries=new ArrayList<>();
                ArrayList<String> dates=new ArrayList<>();
                for (int i=0;i< report.getValues().size();i++){
                    barEntries.add(new BarEntry(i,report.getValues().get(i).floatValue()));
                    List<Integer> keys=report.getKeys().get(i);
                    dates.add(keys.get(0)+"-"+keys.get(1)+"-"+keys.get(2));

                }
                BarDataSet barDataSet= new BarDataSet(barEntries,"Entries");

                switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        barDataSet.setValueTextColor(Color.rgb(233,234,236));
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        barDataSet.setValueTextColor(Color.rgb(66,66,66));
                        break;
                }

                barDataSet.setColor(Color.rgb(250,208,44));
                BarData barData=new BarData(barDataSet);

                chart.setFitBars(true);
                chart.setData(barData);
                XAxis.XAxisPosition position = XAxis.XAxisPosition.BOTTOM;
                XAxis xAxis = chart.getXAxis();
                xAxis.setPosition(position);
                xAxis.setValueFormatter(new ClaimsXAxisValueFormatter(dates));



                chart.animateY(2000);
                chart.invalidate();
            }

            @Override
            public void onFailure(Call<ReportDTO> call, Throwable t) {
                System.out.println(t.getMessage());

            }
        });
    }
    public class ClaimsXAxisValueFormatter extends ValueFormatter {

        List<String> datesList;

        public ClaimsXAxisValueFormatter(List<String> arrayOfDates) {
            this.datesList = arrayOfDates;
        }


        @Override
        public String getAxisLabel(float value, AxisBase axis) {

            Integer position = Math.round(value);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");

            if (value > 1 && value < 2) {
                position = 0;
            } else if (value > 2 && value < 3) {
                position = 1;
            } else if (value > 3 && value < 4) {
                position = 2;
            } else if (value > 4 && value <= 5) {
                position = 3;
            }
            if (position < datesList.size() && position>=0)
                return sdf.format(new Date((getDateInMilliSeconds(datesList.get(position), "yyyy-MM-dd"))));
            return "";
        }
        private long getDateInMilliSeconds(String givenDateString, String format) {
            String DATE_TIME_FORMAT = format;
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.US);
            long timeInMilliseconds = 1;
            try {
                Date mDate = sdf.parse(givenDateString);
                timeInMilliseconds = mDate.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return timeInMilliseconds;
        }

    }




}