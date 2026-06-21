package com.example.qurbancare;

import com.google.firebase.firestore.PropertyName;

public class Penerima {
    private String id; // ID Dokumen Firestore
    private String alamat;
    private String id_penerima;
    private String kategori;
    private String nama;
    private boolean status_ambil;
    private Object telepon; // Pakai Object agar angka di Firebase tidak crash

    public Penerima() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAlamat() { return alamat != null ? alamat : ""; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getId_penerima() { return id_penerima; }
    public void setId_penerima(String id_penerima) { this.id_penerima = id_penerima; }

    public String getKategori() { return kategori != null ? kategori : ""; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public String getNama() { return nama != null ? nama : ""; }
    public void setNama(String nama) { this.nama = nama; }

    @PropertyName("status_ambil")
    public boolean isStatus_ambil() { return status_ambil; }

    @PropertyName("status_ambil")
    public void setStatus_ambil(boolean status_ambil) { this.status_ambil = status_ambil; }

    public String getTelepon() {
        return telepon != null ? String.valueOf(telepon) : "";
    }
    public void setTelepon(Object telepon) { this.telepon = telepon; }
}