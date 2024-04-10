package com.example.strichliste;

import android.app.Application;

import java.nio.file.Path;

public class MyGlobalVariables extends Application {

        private Path fileName;

        public Path getFileName() {
            return fileName;
        }

        public void setFileName(Path fileName) {
            this.fileName = fileName;
        }
    }
