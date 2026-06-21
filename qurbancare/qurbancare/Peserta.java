package com.example.qurbancare;

public class Peserta {
    private String id; // Untuk menyimpan ID dokumen Firestore (penting buat update status)
    private String nama, email, paket, slot, status;

    public Peserta() {} // Wajib ada untuk Firebase

    public Peserta(String id, String nama, String email, String paket, String slot, String status) {
        this.id = id;
        this.nama = nama;
        this.email = email;
        this.paket = paket;
        this.slot = slot;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNama() { return nama; }
    public String getEmail() { return email; }
    public String getPaket() { return paket; }
    public String getSlot() { return slot; }
    public String getStatus() { return status; }
}