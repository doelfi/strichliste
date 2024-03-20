package com.example.strichliste;

import android.app.Application;

import java.util.ArrayList;

public class MyGlobalVariables extends Application {
    private ArrayList<String> gaesteListe = new ArrayList<String>();
    private ArrayList<String> getraenkeListe = new ArrayList<String>();

    public ArrayList<String> getGaesteListe() {
        return gaesteListe;
    }
    public void setGaesteListe(ArrayList<String> gaesteListe) {
        this.gaesteListe = gaesteListe;
    }
    public ArrayList<String> getGetraenkeListe() {
        return getraenkeListe;
    }
    public void setGetraenkeListe(ArrayList<String> getraenkeListe) {
        this.getraenkeListe = getraenkeListe;
    }
}
