package mz.org.fgh.idartlite.view.reports;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.highsoft.highcharts.Common.HIChartsClasses.HIChart;
import com.highsoft.highcharts.Common.HIChartsClasses.HIDataLabels;
import com.highsoft.highcharts.Common.HIChartsClasses.HIOptions;
import com.highsoft.highcharts.Common.HIChartsClasses.HIPie;
import com.highsoft.highcharts.Common.HIChartsClasses.HIPlotOptions;
import com.highsoft.highcharts.Common.HIChartsClasses.HITitle;
import com.highsoft.highcharts.Common.HIChartsClasses.HITooltip;
import com.highsoft.highcharts.Core.HIChartView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.base.BaseActivity;
import mz.org.fgh.idartlite.base.BaseViewModel;
import mz.org.fgh.idartlite.util.DateUtilitis;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.viewmodel.DispensedDrugsReportVM;

public class DispensedDrugsReportActivity extends BaseActivity {

    private String start;
    private String end;
    private HIChartView chartView;
    private EditText edtStart;
    private EditText edtEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispensed_drugs_report);

        chartView = findViewById(R.id.hc_view);

        chartView.setVisibility(View.GONE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        edtStart = findViewById(R.id.start);
        edtEnd = findViewById(R.id.end);
        ImageView search = findViewById(R.id.buttonSearch);

        edtStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mYear, mMonth, mDay;

                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(DispensedDrugsReportActivity.this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        edtStart.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                        start =dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        edtEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mYear, mMonth, mDay;

                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(DispensedDrugsReportActivity.this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        edtEnd.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                        end = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utilities.stringHasValue(start) || !Utilities.stringHasValue(end) ){
                    Utilities.displayAlertDialog(DispensedDrugsReportActivity.this, "Por favor indicar o período por analisar!").show();
                }else
                if (DateUtilitis.dateDiff(DateUtilitis.createDate(end, DateUtilitis.DATE_FORMAT), DateUtilitis.createDate(start, DateUtilitis.DATE_FORMAT), DateUtilitis.DAY_FORMAT) < 0){
                    Utilities.displayAlertDialog(DispensedDrugsReportActivity.this, "A data inicio deve ser menor que a data fim.").show();
                }else
                if ((int) (DateUtilitis.dateDiff(DateUtilitis.getCurrentDate(), DateUtilitis.createDate(start, DateUtilitis.DATE_FORMAT), DateUtilitis.DAY_FORMAT)) < 0){
                    Utilities.displayAlertDialog(DispensedDrugsReportActivity.this, "A data inicio deve ser menor que a data corrente.").show();
                }
                else
                if ((int) DateUtilitis.dateDiff(DateUtilitis.getCurrentDate(), DateUtilitis.createDate(end, DateUtilitis.DATE_FORMAT), DateUtilitis.DAY_FORMAT) < 0){
                    Utilities.displayAlertDialog(DispensedDrugsReportActivity.this, "A data fim deve ser menor que a data corrente.").show();
                }else {
                    Utilities.hideSoftKeyboard(DispensedDrugsReportActivity.this);
                    generateGraph(start, end);
                }
            }
        });
    }

    private void generateGraph(String start, String end) {

        start = edtStart.getText().toString();
        end = edtEnd.getText().toString();

        Map<String, Double> map = new HashMap<>();
        try {
            map = getRelatedViewModel().search(DateUtilitis.createDate(start, DateUtilitis.DATE_FORMAT), DateUtilitis.createDate(end, DateUtilitis.DATE_FORMAT));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (map == null || map.isEmpty() || map.size() == 0){
            Utilities.displayAlertDialog(DispensedDrugsReportActivity.this, "Não foram encontrados registos para o período informado!").show();
            return;
        }

        HIOptions options = new HIOptions();

        HIChart chart = new HIChart();
        chart.setType("pie");
        chart.setBackgroundColor(null);
        chart.setPlotBorderWidth(null);
        chart.setPlotShadow(false);
        options.setChart(chart);

        HITitle title = new HITitle();
        title.setText("Percentagem de Aviamentos por Regime, "+start+" à "+end);
        options.setTitle(title);

        HITooltip tooltip = new HITooltip();
        tooltip.setPointFormat("{series.name}: <b>{point.percentage:.1f}%</b>");
        options.setTooltip(tooltip);

        HIPlotOptions plotOptions = new HIPlotOptions();
        plotOptions.setPie(new HIPie());
        plotOptions.getPie().setAllowPointSelect(true);
        plotOptions.getPie().setCursor("pointer");
        plotOptions.getPie().setDataLabels(new HIDataLabels());
        plotOptions.getPie().getDataLabels().setEnabled(false);
        plotOptions.getPie().setShowInLegend(true);
        options.setPlotOptions(plotOptions);

        HIPie series1 = new HIPie();
        series1.setName("Regime");

        List<Map<String, Object>> dataMapList = new ArrayList<>();

        for (Map.Entry<String, Double> entry : map.entrySet()) {
            HashMap<String, Object> m = new HashMap<>();
            m.put("name", entry.getKey());
            m.put("y", entry.getValue());
            if (dataMapList.size() == 1){
                m.put("sliced", true);
                m.put("selected", true);
            }
            dataMapList.add(m);
        }

        series1.setData(new ArrayList<>(dataMapList));

        options.setSeries(new ArrayList<>(Arrays.asList(series1)));

        chartView.setOptions(options);

        chartView.reload();

        chartView.setVisibility(View.VISIBLE);
    }

    @Override
    public DispensedDrugsReportVM getRelatedViewModel() {
        return (DispensedDrugsReportVM) super.getRelatedViewModel();
    }

    @Override
    public BaseViewModel initViewModel() {
        return new ViewModelProvider(this).get(DispensedDrugsReportVM.class);
    }
}