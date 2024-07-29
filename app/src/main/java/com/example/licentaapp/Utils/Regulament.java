package com.example.licentaapp.Utils;

public class Regulament {
    private String nume;
    private byte[] pdfData;

    public Regulament(String nume, byte[] pdfData) {
        this.nume = nume;
        this.pdfData = pdfData;
    }

    public String getNume() {
        return nume;
    }

    public byte[] getPdfData() {
        return pdfData;
    }
}
