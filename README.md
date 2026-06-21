# QurbanCare – Berbagi di Hari yang Mulia

**QurbanCare** adalah aplikasi manajemen ibadah kurban berbasis Android yang dirancang untuk mempermudah koordinasi antara pekurban, panitia, dan penerima manfaat. Aplikasi ini mengintegrasikan sistem manajemen hewan, pelacakan distribusi, dan transparansi data dalam satu platform yang *user-friendly* dengan hak akses berbasis peran (*Role-Based Access Control*).

## 📑 Daftar Isi
* [Fitur Utama](#-fitur-utama)
* [Hak Akses Pengguna (Role Management)](#-hak-akses-pengguna-role-management)
* [User Experience (UX) Video](#-user-experience-ux-video)
* [Spesifikasi Desain](#-spesifikasi-desain)
* [Teknologi yang Digunakan](#-teknologi-yang-digunakan)
* [Antarmuka Pengguna (UI) & Alur Aplikasi](#-antarmuka-pengguna-ui--alur-aplikasi)
* [Manajemen Proyek](#-manajemen-proyek)

---

## 🚀 Fitur Utama
Aplikasi ini mencakup beberapa modul manajemen utama:
* **Manajemen & Penimbangan Hewan Qurban**: Memantau data sapi dan kambing (berat, umur, jenis kelamin, dan harga), serta fitur kalkulasi otomatis berat hewan untuk menentukan jumlah porsi daging yang siap dibagikan.
* **Sistem Slot Kurban**: Fitur pengambilan slot kurban patungan (misal: 1 sapi untuk 7 orang) dengan progres pembiayaan yang transparan.
* **Sistem Validasi QR Code**: Penerima manfaat mendapatkan QR Code unik sesuai nama mereka. Panitia cukup memindai (*scan*) QR Code tersebut untuk mengubah status distribusi menjadi otomatis tervalidasi "Terambil".
* **Integrasi Google Maps**: Fitur pemetaan lokasi untuk melacak distribusi daging kurban atau lokasi titik penyembelihan secara *real-time*.
* **Laporan Keuangan & Validasi**: Pencatatan transaksi masuk, upload bukti pembayaran, dan sistem validasi pembayaran oleh Admin/Panitia.
* **Tanya QurbanCare AI**: Asisten pintar berbasis AI untuk menjawab informasi seputar kurban.
* **Monitor Keramaian**: Integrasi kamera untuk memantau kepadatan di lokasi penyembelihan.

## 👥 Hak Akses Pengguna (Role Management)
Aplikasi ini mendukung tiga peran pengguna dengan hak akses yang disesuaikan:
1. **Guest (Belum Login)**: Dapat melihat daftar peserta yang berkurban (transparansi data) tanpa harus mendaftar, sebagai opsi bagi pengguna yang hanya ingin memantau informasi.
2. **Peserta (Pekurban)**: Mengakses Dashboard Utama untuk memilih hewan, mengambil slot kurban, melakukan pembayaran, serta mengunggah bukti bayar.
3. **Admin & Panitia**: Memiliki hak akses penuh (*Full Access*) untuk mengelola seluruh data sistem, melakukan validasi pembayaran peserta, serta memindai QR Code penerima manfaat di lokasi distribusi.

## 🎥 User Experience (UX) Video
Untuk melihat simulasi penggunaan aplikasi dan alur *User Experience* secara lengkap, silakan tonton video berikut:
* **Demo Aplikasi QurbanCare**: [https://youtube.com/shorts/OTzNAEWE850?feature=share](https://youtube.com/shorts/OTzNAEWE850?feature=share)

## 🎨 Spesifikasi Desain
Berdasarkan standar UI/UX yang telah ditetapkan dalam prototipe:
* **Tipografi**: Menggunakan font **Poppins** untuk semua elemen teks.
* **Palet Warna**:
    * `#1E7A4C` / `#2FA36B` (Warna Primer - Hijau Islami)
    * `#F4C542` (Warna Aksen - Kuning Emas)
    * `#F5F1E8` (Warna Latar Konten)
* **Komponen**: Penggunaan *Card Rectangle* dengan *Corner Radius* 20 untuk estetika modern.

## 🛠 Teknologi yang Digunakan
* **Platform**: Android Studio
* **Bahasa**: Java
* **Database**: Google Firebase / Firestore (Real-time data & Auth)
* **API & Libraries**: Groq API (ChatAiActivity), Google Maps API, ZXing QR Code Scanner.

## 📱 Antarmuka Pengguna (UI) & Alur Aplikasi

* **Splash Screen**: Menampilkan pesan "Berbagi di Hari yang Mulia".
* **Gerbang Autentikasi**:
    * **Opsi Guest**: Tombol langsung untuk melihat daftar orang yang berkurban tanpa login.
    * **Login & Registrasi**: Form autentikasi bagi Peserta dan Panitia/Admin untuk masuk ke sistem.
* **Dashboard Peserta**: Beranda bersih pasca-login yang berisi menu navigasi cepat ke fitur Pilih Hewan, Progres Slot, Modul Pembayaran, dan Fitur Maps.
* **Dashboard Admin/Panitia**: Antarmuka khusus yang dilengkapi dengan tombol *Scanner QR* dan tabel manajemen data untuk validasi pembayaran serta pemantauan status distribusi "Sudah Diambil".

## 📊 Manajemen Proyek
Proyek ini dikembangkan dengan metodologi **SCRUM**. Seluruh dokumentasi tugas dan *sprint* dikelola melalui tautan di bawah ini:
* **Link ClickUp**: [https://sharing.clickup.com/90181803761/l/h/6-901817840240-1/ed4c470e05a5606](https://sharing.clickup.com/90181803761/l/h/6-901817840240-1/ed4c470e05a5606)
* **Portal Academic**: [MEGAH Pelita Bangsa](https://megah.pelitabangsa.ac.id/)

---
*Dikembangkan oleh Leni QurbanCare – Teknik Informatika, Universitas Pelita Bangsa.*
