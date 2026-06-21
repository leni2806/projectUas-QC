package com.example.qurbancare;

public class Pekurban {
        String nama, jenis;
        public Pekurban() {} // Required for Firestore
        public Pekurban(String nama, String jenis) { this.nama = nama; this.jenis = jenis; }
        public String getNama() { return nama; }
        public String getJenis() { return jenis; }
    }

