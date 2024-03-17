package com.example.strichliste;

import android.app.Application;

import java.util.ArrayList;

public class MyGlobalVariables extends Application {
    private ArrayList<String> gaesteListe;

    public ArrayList<String> getGaesteListe() {
        return gaesteListe;
    }
    public void setGaesteListe(ArrayList<String> gaesteListe) {
        this.gaesteListe = gaesteListe;
    }
}
