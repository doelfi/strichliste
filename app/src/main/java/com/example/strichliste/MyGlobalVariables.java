package com.example.strichliste;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

public class MyGlobalVariables extends Application {
    private String gast;
    private String getraenk;
    private List<String> gaesteListe = new ArrayList();
    private List<String> getraenkeListe = new ArrayList();
    public String getGast(){
        return gast;
    }
    public void setGast(String gast) {
        this.gast = gast;
    }
    public String getGetraenk() {
        return getraenk;
    }
    public void setGetraenk(String getraenk) {
        this.getraenk = getraenk;
    }
    public List<String> getGaesteListe() {
        return gaesteListe;
    }
    public void setGaesteListe(List<String> gaesteListe) {
        this.gaesteListe = gaesteListe;
    }
    public List<String> getGetraenkeListe() {
        return getraenkeListe;
    }
    public void setGetraenkeListe(List<String> getraenkeListe) {
        this.getraenkeListe = getraenkeListe;
    }
}
