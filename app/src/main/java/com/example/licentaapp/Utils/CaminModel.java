package com.example.licentaapp.Utils;

public class CaminModel {
    private int idCamin;
    private String numeCamin;
    private String adresa;
    private String administrator;
    private String email;
    private String nrTelefon;
    private int capacitate;
    public CaminModel(int idCamin, String numeCamin, String adresa, String administrator, String email, String nrTelefon, int capacitate) {
        this.idCamin = idCamin;
        this.numeCamin = numeCamin;
        this.adresa = adresa;
        this.administrator = administrator;
        this.email = email;
        this.nrTelefon = nrTelefon;
        this.capacitate = capacitate;
    }

    public int getIdCamin() {
        return idCamin;
    }

    public void setIdCamin(int idCamin) {
        this.idCamin = idCamin;
    }

    public String getNumeCamin() {
        return numeCamin;
    }

    public void setNumeCamin(String numeCamin) {
        this.numeCamin = numeCamin;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public String getAdministrator() {
        return administrator;
    }

    public void setAdministrator(String administrator) {
        this.administrator = administrator;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNrTelefon() {
        return nrTelefon;
    }

    public void setNrTelefon(String nrTelefon) {
        this.nrTelefon = nrTelefon;
    }

    public int getCapacitate() {
        return capacitate;
    }

    public void setCapacitate(int capacitate) {
        this.capacitate = capacitate;
    }
}