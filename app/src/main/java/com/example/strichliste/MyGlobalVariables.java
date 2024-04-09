package com.example.strichliste;

import android.app.Application;

public class MyGlobalVariables extends Application {

        private String fileName;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }
