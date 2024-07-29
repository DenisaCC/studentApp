package com.example.licentaapp.Utils;

public class UserProfessorModel {
    private String numeUtilizator;
    private String email;
    private String password;
    private int idProfessor;

    public UserProfessorModel(String numeUtilizator, String email, String password, int idProfessor) {
        this.numeUtilizator = numeUtilizator;
        this.email = email;
        this.password = password;
        this.idProfessor = idProfessor;
    }

    public String getNumeUtilizator() {
        return numeUtilizator;
    }

    public void setNumeUtilizator(String numeUtilizator) {
        this.numeUtilizator = numeUtilizator;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getIdProfessor() {
        return idProfessor;
    }

    public void setIdProfessor(int idProfessor) {
        this.idProfessor = idProfessor;
    }
}
