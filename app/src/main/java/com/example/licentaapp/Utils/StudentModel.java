package com.example.licentaapp.Utils;

public class StudentModel {
    private String id; // Adăugăm atributul pentru ID-ul studentului
    private String name;
    private int yearOfStudy;
    private String specialization;

    public StudentModel(String id, String name, int yearOfStudy, String specialization) {
        this.id = id;
        this.name = name;
        this.yearOfStudy = yearOfStudy;
        this.specialization = specialization;
    }

    public StudentModel( String name, int yearOfStudy, String specialization) {
        this.name = name;
        this.yearOfStudy = yearOfStudy;
        this.specialization = specialization;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getYearOfStudy() {
        return yearOfStudy;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setYearOfStudy(int yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    // Constructorul implicit, dacă este necesar
    public StudentModel() {
    }
}
