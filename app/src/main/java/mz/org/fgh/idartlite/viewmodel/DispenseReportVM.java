package mz.org.fgh.idartlite.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import mz.org.fgh.idartlite.BR;
import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.base.SearchVM;
import mz.org.fgh.idartlite.model.Clinic;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.service.DispenseService;
import mz.org.fgh.idartlite.util.DateUtilitis;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.reports.DispenseReportActivity;

public class DispenseReportVM extends SearchVM<Dispense> {


    private DispenseService dispenseService;



    private String startDate;

    private String endDate;


    public DispenseReportVM(@NonNull Application application) {
        super(application);
        dispenseService = new DispenseService(application, getCurrentUser());


    }

    public List<Dispense> getDispensesByDates(Date startDate,Date endDate, long offset, long limit) throws SQLException {
        return dispenseService.getDispensesBetweenStartDateAndEndDateWithLimit(startDate, endDate,offset,limit);
    }



    @Override
    public void initSearch(){
        if(!Utilities.stringHasValue(startDate) || !Utilities.stringHasValue(endDate)) {
            Utilities.displayAlertDialog(getRelatedActivity(),getRelatedActivity().getString(R.string.start_end_date_is_mandatory)).show();
        }else {

            try {
                super.initSearch();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }


    public List<Dispense> doSearch(long offset, long limit) throws SQLException {

        return getDispensesByDates(DateUtilitis.createDate(startDate, DateUtilitis.DATE_FORMAT),DateUtilitis.createDate(endDate, DateUtilitis.DATE_FORMAT),offset,limit);
    }



    @Override
    public void displaySearchResults() {
        Utilities.hideSoftKeyboard(getRelatedActivity());

        getRelatedActivity().displaySearchResult();
    }




    public DispenseReportActivity getRelatedActivity() {
        return (DispenseReportActivity) super.getRelatedActivity();
    }

    @Bindable
    public Clinic getClinic(){
        return getCurrentClinic();
    }

    @Bindable
    public String getSearchParam() {
        return startDate;
    }

    public void setSearchParam(String searchParam) {
        this.startDate = searchParam;
        notifyPropertyChanged(BR.searchParam);
    }

    @Bindable
    public String getSearchParam2() {
        return endDate;
    }

    public void setSearchParam2(String searchParam) {
        this.endDate = searchParam;
        notifyPropertyChanged(BR.searchParam);
    }

}
