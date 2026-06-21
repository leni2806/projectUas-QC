package com.example.qurbancare;

public class TransparansiModel {
    private String nama, slot, paket, gambar, gender, umur, berat, harga;

    public TransparansiModel(String nama, String slot, String paket, String gambar, String gender, String umur, String berat, String harga) {
        this.nama = nama;
        this.slot = slot;
        this.paket = paket;
        this.gambar = gambar;
        this.gender = gender;
        this.umur = umur;
        this.berat = berat;
        this.harga = harga;
    }

    public String getNama() { return nama; }
    public String getSlot() { return slot; }
    public String getPaket() { return paket; }
    public String getGambar() { return gambar; }
    public String getGender() { return gender; }
    public String getUmur() { return umur; }
    public String getBerat() { return berat; }
    public String getHarga() { return harga; }
}