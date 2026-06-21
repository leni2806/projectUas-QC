package com.example.qurbancare;

import com.google.firebase.firestore.PropertyName;

public class Qurban {
    private String nama;
    private String berat;
    private String gambar;
    private String gambar_detail;
    private String jenis;
    private String deskripsi;
    private String umur;             // 🔥 Revisi: Tambah Umur
    private String jenis_kelamin;     // 🔥 Revisi: Tambah Jenis Kelamin
    private long harga;
    private int slot_terisi;
    private int total_slot;

    public Qurban() {}

    // Getter Standar
    public String getNama() { return nama; }
    public long getHarga() { return harga; }
    public String getBerat() { return berat; }
    public String getGambar() { return gambar; }
    public String getJenis() { return jenis; }

    // 🔥 Getter Revisi: Umur
    public String getUmur() { return umur; }

    // 🔥 Getter Revisi: Jenis Kelamin
    @PropertyName("jenis_kelamin")
    public String getJenis_kelamin() { return jenis_kelamin; }

    @PropertyName("deskripsi")
    public String getDeskripsi() { return deskripsi; }

    @PropertyName("gambar_detail")
    public String getGambarDetail() { return gambar_detail; }

    @PropertyName("slot_terisi")
    public int getSlot_terisi() { return slot_terisi; }

    @PropertyName("total_slot")
    public int getTotal_slot() {
        return total_slot;
    }

    // --- LOGIKA TAMBAHAN UNTUK KAMBING (REVISI DOSEN) ---

    /**
     * Fungsi bantuan untuk mengecek apakah ini tipe urunan (Sapi)
     * atau mandiri (Kambing) sesuai revisi dosen.
     */
    public boolean isUrunan() {
        // Jika jenisnya "Kambing", maka bukan urunan (false)
        return jenis != null && !jenis.equalsIgnoreCase("Kambing");
    }
}