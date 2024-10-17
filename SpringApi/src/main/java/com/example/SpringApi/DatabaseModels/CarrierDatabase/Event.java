package com.example.SpringApi.DatabaseModels.CarrierDatabase;

import java.util.Date;

public class Event {
    // primary key - auto increment
    private int eventsId;
    private String title;
    private Date startDate;
    private Date endDate;
    private boolean recurring;
    private String repeat;
    private Integer repeatInterval;
    private String repeatWeeklyday;
    private Integer repeatMonthlyon;
    private String repeatMonthlythe;
    private String repeatYearlyevery;
    private String repeatYearlythe;
    private Integer endRepeaton;
    private Date endRepeatafter;
    private boolean isDeleted;
    private boolean allday;
    private String selectedRadioMonthly;
    private String selectedRadioYearly;
    private String selectedReadioEnd;
    private boolean endRepeatNever;
    //Logging
    private String notes;
    private Date createdAt;
    // Fk's
    private int userId;
}