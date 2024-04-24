package com.example.licentaapp.Utils;

public class Grade {
    private String discipline;
    private float grade;
    private String professor;
    private String date;

    public Grade(String discipline, float grade, String professor, String date) {
        this.discipline = discipline;
        this.grade = grade;
        this.professor = professor;
        this.date = date;
    }

    // Getters și setters pentru a accesa și a seta membrii privați ai clasei

    public String getDiscipline() {
        return discipline;
    }

    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }

    public float getGrade() {
        return grade;
    }

    public void setGrade(float grade) {
        this.grade = grade;
    }

    public String getProfessor() {
        return professor;
    }

    public void setProfessor(String professor) {
        this.professor = professor;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
