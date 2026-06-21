package com.example.qurbancare;

import com.google.firebase.firestore.PropertyName;

public class Distribusi {
    private String id_penerima, nama, kategori, alamat, paket, petugas, berat;
    private Object status_ambil;

    // Konstruktor kosong wajib untuk Firebase
    public Distribusi() {}

    public String getId_penerima() { return id_penerima; }
    public void setId_penerima(String id_penerima) { this.id_penerima = id_penerima; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getPaket() { return paket; }
    public void setPaket(String paket) { this.paket = paket; }

    public String getPetugas() { return petugas; }
    public void setPetugas(String petugas) { this.petugas = petugas; }

    // 🔥 REVISI BERAT: Menggunakan String.valueOf agar aman jika di Firebase tipe datanya Number
    public String getBerat() {
        if (berat == null) return "0";
        // Membersihkan karakter non-angka agar bisa dihitung secara matematis di Activity
        return String.valueOf(berat).replaceAll("[^0-9]", "");
    }
    public void setBerat(String berat) { this.berat = berat; }

    // 🔥 REVISI STATUS: Getter status yang aman untuk pengecekan "Selesai" atau "Proses"
    @PropertyName("status_ambil")
    public String getStatus_ambil() {
        if (status_ambil == null) return "Belum Ambil";
        return String.valueOf(status_ambil);
    }

    @PropertyName("status_ambil")
    public void setStatus_ambil(Object status_ambil) {
        this.status_ambil = status_ambil;
    }
}