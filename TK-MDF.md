# 📘 Knowledge Transfer: Dokumentasi Perubahan Proyek GitLab

> **Dokumen ini** berisi panduan untuk mendokumentasikan perubahan proyek bulanan melalui GitLab, mulai dari eksplorasi project hingga pengisian template dokumen.

---

## 🎯 Tujuan

Memastikan setiap perubahan proyek yang terjadi dalam satu bulan tercatat dengan baik ke dalam dokumen resmi, menggunakan template `.docx` yang telah tersedia.

---

## 🔄 Alur Kerja (Workflow)

```
Explore Projects (All Projects)
    └── Filter latest project (bulan ini)
            └── Cek CHANGELOG
                    ├── [Ada] → Catat perubahan -> Isi Template .docx
                    └── [Tidak Ada] → Cek Commit
                                        ├── [Commit jelas] → Isi Template .docx
                                        └── [Commit tidak jelas] → Buka Source Code
                                                └── Analisis perubahan
                                                        └── Isi Template .docx
```

---

## 📋 Langkah-Langkah Detail

### Langkah 1 — Buka GitLab & Eksplorasi Semua Project

1. Buka browser dan akses URL GitLab perusahaan.
2. Login menggunakan akun kamu.
3. Pada menu navigasi atas, klik **"Explore"** atau **"Explore Projects"**.
4. Pastikan filter menampilkan **"All Projects"** agar tidak ada project yang terlewat.

> 💡 **Tips:** Gunakan fitur pencarian atau filter grup jika jumlah project sangat banyak.

---

### Langkah 2 — Filter Project Bulan Ini

1. Di halaman daftar project, perhatikan kolom **"Last Activity"** atau **"Updated"**.
2. Identifikasi project yang memiliki aktivitas pada **bulan berjalan**.
3. Buat catatan atau list sementara project-project tersebut sebelum mulai memeriksa satu per satu.

> 💡 **Tips:** Kamu bisa sort project berdasarkan "Last Updated" untuk mempermudah identifikasi.

---

### Langkah 3 — Cek CHANGELOG

1. Masuk ke halaman project yang dituju.
2. Di halaman repository, cari file bernama:
   - `CHANGELOG.md`
   - `CHANGELOG`
   - `CHANGES.md`
   - `HISTORY.md`
3. Buka file tersebut dan cari entri yang sesuai dengan **bulan ini**.
4. Catat semua perubahan yang tercantum.

**Jika CHANGELOG ditemukan dan lengkap → Lanjut ke [Langkah 6](#langkah-6--isi-template-docx).**

**Jika CHANGELOG tidak ada atau tidak lengkap → Lanjut ke Langkah 4.**

---

### Langkah 4 — Cek Commit History

1. Di halaman project, klik menu **"Repository"** → **"Commits"**.
2. Filter commit berdasarkan rentang waktu bulan ini (gunakan filter tanggal jika tersedia).
3. Baca setiap **commit message** satu per satu.
4. Identifikasi commit yang relevan: fitur baru, perbaikan bug, perubahan konfigurasi, dsb.

**Contoh commit message yang jelas:**
```
feat: tambah fitur login SSO dengan Google
fix: perbaiki bug kalkulasi harga diskon
chore: update dependency react ke versi 18
```

**Jika commit message sudah jelas → Catat perubahan, lanjut ke [Langkah 6](#langkah-6--isi-template-docx).**

**Jika commit message tidak jelas (contoh: `update`, `fix`, `wip`) → Lanjut ke Langkah 5.**

---

### Langkah 5 — Analisis Source Code

Lakukan langkah ini jika commit message tidak memberikan informasi yang cukup.

1. Klik commit yang tidak jelas untuk melihat **diff (perbandingan kode)**.
2. Perhatikan:
   - File apa saja yang berubah?
   - Fungsi atau modul apa yang dimodifikasi?
   - Apakah ada penambahan, penghapusan, atau penggantian logika?
3. Jika perlu konteks lebih dalam, buka file terkait melalui **"Repository" → "Files"** dan baca bagian kode yang diubah.
4. Simpulkan perubahan dalam kalimat yang mudah dipahami.

> ⚠️ **Catatan:** Kamu tidak perlu memahami seluruh kodenya — fokus pada **apa yang berubah** dan **dampaknya**, bukan bagaimana cara kerjanya secara teknis.

---

### Langkah 6 — Isi Template .docx

Setelah semua informasi perubahan terkumpul, saatnya mengisi dokumen resmi.

1. Buka file template `.docx` yang sudah tersedia.
2. Isi setiap bagian sesuai informasi yang telah dikumpulkan:

| Field di Template | Sumber Informasi |
|---|---|
| Nama Project | Nama project di GitLab |
| Bulan / Periode | Bulan yang sedang didokumentasikan |
| Deskripsi Perubahan | Dari CHANGELOG / Commit / Source Code |
| Tipe Perubahan | Fitur baru, Bug fix, Improvement, dll. |
| Developer / Author | Dari informasi commit (author) |
| Tanggal Perubahan | Dari tanggal commit |

3. Simpan dokumen dengan format penamaan yang sesuai standar tim (tanyakan ke atasan jika belum tahu).

---

## ⚡ Ringkasan Cepat (Quick Reference)

| Situasi | Yang Harus Dilakukan |
|---|---|
| CHANGELOG tersedia & lengkap | Gunakan langsung isi CHANGELOG |
| CHANGELOG tidak ada | Cek commit history |
| Commit message jelas | Gunakan commit message sebagai referensi |
| Commit message tidak jelas | Buka diff / source code untuk analisis |
| Sudah dapat info perubahan | Isi template .docx |

---

## ❓ FAQ

**Q: Bagaimana jika satu project tidak ada perubahan sama sekali bulan ini?**
> Lewati project tersebut. Tidak perlu dibuat dokumentasi jika tidak ada aktivitas.

**Q: Bagaimana jika ada ratusan commit dalam satu bulan?**
> Fokus pada commit yang menyentuh **fitur utama atau perubahan signifikan**. Commit kecil seperti typo fix atau format code bisa diabaikan atau digabung menjadi satu poin.

**Q: Siapa yang harus saya tanya jika commit tidak bisa dipahami?**
> Hubungi langsung **developer yang membuat commit tersebut** (nama author tertera di setiap commit).

---

*Dokumen ini dibuat untuk keperluan transfer knowledge. Diperbarui sesuai kebutuhan.*