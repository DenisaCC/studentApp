package com.example.licentaapp.Utils;

public class UserStudentModel {
    private int nrMatricol;
    private String username;
    private String email;
    private String password;

    public UserStudentModel(int nrMatricol, String username, String email, String password) {
        this.nrMatricol = nrMatricol;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public int getNrMatricol() {
        return nrMatricol;
    }

    public void setNrMatricol(int nrMatricol) {
        this.nrMatricol = nrMatricol;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
}
