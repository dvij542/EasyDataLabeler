package com.example.easydatalabeler.data.model;

import com.example.easydatalabeler.dataLabeler.DataLabeler;
import com.example.easydatalabeler.dataScientist.DataScientist;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    String userId;
    String displayName;
    DataLabeler dataLabeler= null;
    DataScientist dataScientist=null;
    public LoggedInUser(String userId, String displayName, DataLabeler dataLabeler) {
        this.userId = userId;
        this.displayName = displayName;
        this.dataLabeler = dataLabeler;
    }
    public LoggedInUser(String userId, String displayName, DataScientist dataScientist) {
        this.userId = userId;
        this.displayName = displayName;
        this.dataScientist = dataScientist;
    }


    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public DataScientist getDataScientist(){
        return this.dataScientist;
    }
}
