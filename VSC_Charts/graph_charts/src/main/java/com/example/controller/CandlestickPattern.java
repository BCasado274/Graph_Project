package com.example.controller;

import java.util.Date;

public class CandlestickPattern {
    private String name;
    private Date date;

    public CandlestickPattern(String name, Date date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }
}
