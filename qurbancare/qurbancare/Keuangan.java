package com.example.qurbancare;

public class Keuangan {
    private String nama;
    private String paket;
    private String slot;
    private String status;
    private String tanggal;
    private String total_harga;
    private String metode;
    private String email;
    private String bukti;
    private String tipe; // INI YANG TADI HILANG

    public Keuangan() {} // Wajib untuk Firebase

    // --- GETTER (Untuk mengambil data) ---
    public String getTipe() { return tipe; } // INI YANG DICARI KEUANGANACTIVITY
    public String getNama() { return nama; }
    public String getPaket() { return paket; }
    public String getSlot() { return slot; }
    public String getStatus() { return status; }
    public String getTanggal() { return tanggal; }
    public String getTotal_harga() { return total_harga; }
    public String getMetode() { return metode; }
    public String getEmail() { return email; }
    public String getBukti() { return bukti; }

    // --- SETTER (Untuk mengisi data) ---
    public void setTipe(String tipe) { this.tipe = tipe; }
    public void setNama(String nama) { this.nama = nama; }
    public void setPaket(String paket) { this.paket = paket; }
    public void setSlot(String slot) { this.slot = slot; }
    public void setStatus(String status) { this.status = status; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }
    public void setTotal_harga(String total_harga) { this.total_harga = total_harga; }
    public void setMetode(String metode) { this.metode = metode; }
    public void setEmail(String email) { this.email = email; }
    public void setBukti(String bukti) { this.bukti = bukti; }
}