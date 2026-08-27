package com.example.mobile_front_ma.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.models.dto.DailyRideReport;
import com.example.mobile_front_ma.models.dto.RideReport;
import com.example.mobile_front_ma.models.dto.RideSummary;
import com.example.mobile_front_ma.util.Resource;
import com.example.mobile_front_ma.viewmodels.ReportsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {

    private ReportsViewModel viewModel;

    private ProgressBar progressBar;
    private TextView emptyText;

    private MaterialButton fromDateButton;
    private MaterialButton toDateButton;
    private MaterialButton generateButton;

    private BarChart ridesChart;
    private BarChart distanceChart;
    private BarChart moneyChart;

    private TextView summaryRides;
    private TextView summaryDistance;
    private TextView summaryMoney;

    private Calendar fromCal;
    private Calendar toCal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reports);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            bars.left,
                            bars.top,
                            bars.right,
                            bars.bottom
                    );

                    return insets;
                }
        );

        viewModel = new ViewModelProvider(this)
                .get(ReportsViewModel.class);

        setupViews();
        setupDatePickers();
        setupButtons();

        viewModel.getReport().observe(
                this,
                this::render
        );
    }

    private void setupViews() {

        /*
         * IDs must match activity_reports.xml exactly.
         */

        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);

        fromDateButton = findViewById(R.id.fromDateButton);
        toDateButton = findViewById(R.id.toDateButton);
        generateButton = findViewById(R.id.generateButton);

        ridesChart = findViewById(R.id.ridesChart);
        distanceChart = findViewById(R.id.distanceChart);
        moneyChart = findViewById(R.id.moneyChart);

        summaryRides = findViewById(R.id.summaryRides);
        summaryDistance = findViewById(R.id.summaryDistance);
        summaryMoney = findViewById(R.id.summaryPrice);

        emptyText.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void setupDatePickers() {

        fromCal = Calendar.getInstance();
        toCal = Calendar.getInstance();

        // Default range: last 7 days -> today
        fromCal.add(Calendar.DAY_OF_MONTH, -7);

        updateDateButtonText();

        fromDateButton.setOnClickListener(v ->
                showDatePicker(true)
        );

        toDateButton.setOnClickListener(v ->
                showDatePicker(false)
        );
    }

    private void showDatePicker(boolean selectingFromDate) {

        Calendar selected =
                selectingFromDate ? fromCal : toCal;

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            Calendar selectedDate =
                                    Calendar.getInstance();

                            selectedDate.set(
                                    Calendar.YEAR,
                                    year
                            );

                            selectedDate.set(
                                    Calendar.MONTH,
                                    month
                            );

                            selectedDate.set(
                                    Calendar.DAY_OF_MONTH,
                                    dayOfMonth
                            );

                            // Reset time.
                            selectedDate.set(
                                    Calendar.HOUR_OF_DAY,
                                    0
                            );

                            selectedDate.set(
                                    Calendar.MINUTE,
                                    0
                            );

                            selectedDate.set(
                                    Calendar.SECOND,
                                    0
                            );

                            selectedDate.set(
                                    Calendar.MILLISECOND,
                                    0
                            );

                            if (selectingFromDate) {
                                fromCal = selectedDate;
                            } else {
                                toCal = selectedDate;
                            }

                            updateDateButtonText();
                        },
                        selected.get(Calendar.YEAR),
                        selected.get(Calendar.MONTH),
                        selected.get(Calendar.DAY_OF_MONTH)
                );

        dialog.show();
    }

    private void updateDateButtonText() {

        SimpleDateFormat displayFormat =
                new SimpleDateFormat(
                        "dd.MM.yyyy.",
                        Locale.getDefault()
                );

        fromDateButton.setText(
                "From: " +
                        displayFormat.format(
                                fromCal.getTime()
                        )
        );

        toDateButton.setText(
                "To: " +
                        displayFormat.format(
                                toCal.getTime()
                        )
        );
    }

    private void setupButtons() {

        generateButton.setOnClickListener(
                v -> generateReport()
        );
    }

    private void generateReport() {

        if (fromCal.after(toCal)) {

            Toast.makeText(
                    this,
                    "The starting date cannot be after the ending date.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Spring expects LocalDateTime:
         *
         * from -> beginning of selected day
         * to   -> end of selected day
         */

        String from = formatBackendDate(
                fromCal,
                false
        );

        String to = formatBackendDate(
                toCal,
                true
        );

        /*
         * null means the currently authenticated user.
         *
         * For administrator reports this can later be replaced
         * with the selected user's ID.
         */

        viewModel.setTargetUser(null);
        viewModel.setDateRange(from, to);
        viewModel.load();
    }

    private String formatBackendDate(
            Calendar calendar,
            boolean endOfDay
    ) {

        Calendar copy =
                (Calendar) calendar.clone();

        if (endOfDay) {

            copy.set(
                    Calendar.HOUR_OF_DAY,
                    23
            );

            copy.set(
                    Calendar.MINUTE,
                    59
            );

            copy.set(
                    Calendar.SECOND,
                    59
            );

        } else {

            copy.set(
                    Calendar.HOUR_OF_DAY,
                    0
            );

            copy.set(
                    Calendar.MINUTE,
                    0
            );

            copy.set(
                    Calendar.SECOND,
                    0
            );
        }

        copy.set(
                Calendar.MILLISECOND,
                0
        );

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss",
                        Locale.US
                );

        return format.format(
                copy.getTime()
        );
    }

    private void render(Resource<RideReport> resource) {

        if (resource == null) {
            return;
        }

        switch (resource.status) {

            case LOADING:

                progressBar.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.GONE);

                generateButton.setEnabled(false);

                hideCharts();

                break;

            case SUCCESS:

                progressBar.setVisibility(View.GONE);
                generateButton.setEnabled(true);

                if (resource.data == null ||
                        resource.data.getDailyReports() == null ||
                        resource.data.getDailyReports().isEmpty()) {

                    emptyText.setVisibility(View.VISIBLE);

                    hideCharts();
                    clearSummary();

                    return;
                }

                emptyText.setVisibility(View.GONE);

                displayReport(resource.data);

                break;

            case ERROR:

                progressBar.setVisibility(View.GONE);
                generateButton.setEnabled(true);

                hideCharts();

                if (resource.message != null) {

                    Toast.makeText(
                            this,
                            resource.message,
                            Toast.LENGTH_LONG
                    ).show();
                }

                break;
        }
    }

    private void displayReport(RideReport report) {

        List<DailyRideReport> dailyReports =
                new ArrayList<>(
                        report.getDailyReports()
                );

        Collections.sort(
                dailyReports,
                new Comparator<DailyRideReport>() {
                    @Override
                    public int compare(
                            DailyRideReport a,
                            DailyRideReport b
                    ) {
                        return a.getDate().compareTo(
                                b.getDate()
                        );
                    }
                }
        );

        displaySummary(
                report.getSummary()
        );

        displayRidesChart(
                dailyReports
        );

        displayDistanceChart(
                dailyReports
        );

        displayMoneyChart(
                dailyReports
        );

        showCharts();
    }

    private void displaySummary(RideSummary summary) {

        if (summary == null) {
            clearSummary();
            return;
        }

        summaryRides.setText(
                String.valueOf(
                        summary.getRideCount()
                )
        );

        summaryDistance.setText(
                String.format(
                        Locale.getDefault(),
                        "%.2f km",
                        summary.getTotalDistanceKm()
                )
        );

        summaryMoney.setText(
                String.format(
                        Locale.getDefault(),
                        "%.2f",
                        summary.getTotalPrice()
                )
        );
    }

    private void clearSummary() {

        summaryRides.setText("0");
        summaryDistance.setText("0.00 km");
        summaryMoney.setText("0.00");
    }

    private void displayRidesChart(
            List<DailyRideReport> reports
    ) {

        ArrayList<BarEntry> entries =
                new ArrayList<>();

        ArrayList<String> labels =
                new ArrayList<>();

        for (int i = 0; i < reports.size(); i++) {

            DailyRideReport report =
                    reports.get(i);

            entries.add(
                    new BarEntry(
                            i,
                            report.getRideCount()
                    )
            );

            labels.add(
                    formatChartDate(
                            report.getDate()
                    )
            );
        }

        BarDataSet dataSet =
                new BarDataSet(
                        entries,
                        "Rides"
                );

        BarData data =
                new BarData(dataSet);

        ridesChart.setData(data);

        configureChart(
                ridesChart,
                labels,
                "Number of rides"
        );
    }

    private void displayDistanceChart(
            List<DailyRideReport> reports
    ) {

        ArrayList<BarEntry> entries =
                new ArrayList<>();

        ArrayList<String> labels =
                new ArrayList<>();

        for (int i = 0; i < reports.size(); i++) {

            DailyRideReport report =
                    reports.get(i);

            entries.add(
                    new BarEntry(
                            i,
                            (float)
                                    report.getTotalDistanceKm()
                    )
            );

            labels.add(
                    formatChartDate(
                            report.getDate()
                    )
            );
        }

        BarDataSet dataSet =
                new BarDataSet(
                        entries,
                        "Distance (km)"
                );

        BarData data =
                new BarData(dataSet);

        distanceChart.setData(data);

        configureChart(
                distanceChart,
                labels,
                "Distance (km)"
        );
    }

    private void displayMoneyChart(
            List<DailyRideReport> reports
    ) {

        ArrayList<BarEntry> entries =
                new ArrayList<>();

        ArrayList<String> labels =
                new ArrayList<>();

        for (int i = 0; i < reports.size(); i++) {

            DailyRideReport report =
                    reports.get(i);

            entries.add(
                    new BarEntry(
                            i,
                            (float)
                                    report.getTotalPrice()
                    )
            );

            labels.add(
                    formatChartDate(
                            report.getDate()
                    )
            );
        }

        BarDataSet dataSet =
                new BarDataSet(
                        entries,
                        "Price"
                );

        BarData data =
                new BarData(dataSet);

        moneyChart.setData(data);

        configureChart(
                moneyChart,
                labels,
                "Price"
        );
    }

    private void configureChart(
            BarChart chart,
            ArrayList<String> labels,
            String description
    ) {

        chart.getDescription()
                .setText(description);

        chart.getXAxis()
                .setValueFormatter(
                        new IndexAxisValueFormatter(
                                labels
                        )
                );

        chart.getXAxis()
                .setGranularity(1f);

        chart.getXAxis()
                .setGranularityEnabled(true);

        chart.getXAxis()
                .setLabelRotationAngle(-45);

        chart.getAxisRight()
                .setEnabled(false);

        chart.setFitBars(true);

        chart.setNoDataText(
                "No report data available."
        );

        chart.invalidate();
    }

    private String formatChartDate(String date) {

        if (date == null) {
            return "";
        }

        try {

            SimpleDateFormat input =
                    new SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.US
                    );

            SimpleDateFormat output =
                    new SimpleDateFormat(
                            "dd.MM",
                            Locale.getDefault()
                    );

            return output.format(
                    input.parse(date)
            );

        } catch (ParseException e) {

            return date;
        }
    }

    private void hideCharts() {

        ridesChart.setVisibility(View.GONE);
        distanceChart.setVisibility(View.GONE);
        moneyChart.setVisibility(View.GONE);
    }

    private void showCharts() {

        ridesChart.setVisibility(View.VISIBLE);
        distanceChart.setVisibility(View.VISIBLE);
        moneyChart.setVisibility(View.VISIBLE);
    }
}