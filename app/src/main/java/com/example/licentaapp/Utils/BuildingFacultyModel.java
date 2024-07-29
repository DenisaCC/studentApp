package com.example.licentaapp.Utils;

public class BuildingFacultyModel {
    private int idCorpFacultate;
    private String numeCorpFacultate;
    private String adresa;

    public BuildingFacultyModel(int idCorpFacultate, String numeCorpFacultate, String adresa) {
        this.idCorpFacultate = idCorpFacultate;
        this.numeCorpFacultate = numeCorpFacultate;
        this.adresa = adresa;
    }

    public int getIdCorpFacultate() {
        return idCorpFacultate;
    }

    public String getNumeCorpFacultate() {
        return numeCorpFacultate;
    }

    public String getAdresa() {
        return adresa;
    }
}
