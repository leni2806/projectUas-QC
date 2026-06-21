package com.example.qurbancare;

public class Panitia {
    // Variabel harus sama persis dengan nama field di Firestore
    private String nama, email, role, fotoUrl, bagian, password;

    public Panitia() {} // Konstruktor kosong wajib untuk Firebase

    public String getNama() {
        return nama;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public String getBagian() {
        return bagian;
    }

    // 🔥 Tambahkan ini supaya bisa cek password pas login manual
    public String getPassword() {
        return password;
    }
}