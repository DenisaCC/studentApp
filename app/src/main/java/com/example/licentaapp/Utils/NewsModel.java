package com.example.licentaapp.Utils;

public class NewsModel {
    private int id;
    private String title;
    private String date;
    private String description;
    private byte[] pdfData;

    public NewsModel(int id, String title, String date, String description, byte[] pdfData) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.description = description;
        this.pdfData = pdfData;
    }

    public int getId() {
        return id;
    }
    public byte[] getPdfData() {
        return pdfData;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }
}
