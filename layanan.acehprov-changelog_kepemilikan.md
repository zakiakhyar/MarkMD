# Changelog — Fitur Kepemilikan Data Aplikasi

## Informasi Commit

| Item | Detail |
|------|--------|
| **Commit Hash** | `d0ac3ab2cdeac3a5da75c96158f2cfd46271318b` |
| **Branch** | `master` |
| **Author** | zaki_akhyar \<zaki.akhyar@acehprov.go.id\> |
| **Tanggal** | Senin, 4 Mei 2026 — 11:02 WIB |
| **Pesan Commit** | `feat: add kepemilikan field to DataApp model and form while hiding group field` |
| **File Berubah** | 3 file, 17 penambahan (+), 2 perubahan (-) |

---

## Ringkasan Perubahan

Perubahan ini menambahkan atribut **Kepemilikan** pada data aplikasi dan menyembunyikan field **Group** dari form input admin. Field Kepemilikan digunakan untuk mengklasifikasikan kepemilikan aplikasi berdasarkan instansi pemilik.

---

## Detail Perubahan per File

### 1. `resources/views/admin/dataapp/form.blade.php`

**Tujuan:** Menyembunyikan form Group dan menambahkan form Kepemilikan.

#### a) Menyembunyikan form Group
Kelas `d-none` (Bootstrap) ditambahkan pada `div` form-group field Group agar tidak tampil di UI, namun field tetap ada di DOM sehingga nilai default tidak hilang.

```diff
- <div class="form-group {{ $errors->has('group') ? 'has-error' : '' }}">
+ <div class="form-group {{ $errors->has('group') ? 'has-error' : '' }} d-none">
```

#### b) Menambahkan form Kepemilikan
Ditambahkan form-group baru berisi dropdown `select2` dengan tiga pilihan tetap:
- **Pemerintah Aceh**
- **Kementerian**
- **Lainnya**

```blade
<div class="form-group {{ $errors->has('kepemilikan') ? 'has-error' : '' }}">
    <label class="bs4-content-label tx-11 tx-medium tx-gray-600">Kepemilikan</label>
    {!! Form::select('kepemilikan', @$ls_kepemilikan ? $ls_kepemilikan : [], null, [
        'id' => 'kepemilikan',
        'class' => 'form-control select2',
        'placeholder' => 'Pilih Kepemilikan...',
    ]) !!}
    @if ($errors->has('kepemilikan'))
    <span class="badge badge-danger">{{ $errors->first('kepemilikan') }}</span>
    @endif
</div>
```

> Posisi: Setelah form Group (sebelumnya satu kolom dengan Tahun Rilis), di dalam `col-md-6` kiri bagian kolom kanan.

---

### 2. `app/Http/Controllers/Admin/DataAppController.php`

**Tujuan:** Menyediakan data pilihan Kepemilikan ke view melalui method `selectBoxs()`.

#### a) Penambahan variabel `$ls_kepemilikan`
```diff
+ $ls_kepemilikan = collect(['Pemerintah Aceh', 'Kementerian', 'Lainnya'])
+     ->mapWithKeys(function ($item) {
+         return [$item => $item];
+     });
```

#### b) Ditambahkan ke return `compact()`
```diff
- return compact('orgs', 'ls_kategori', 'ls_group', 'ls_tipe', 'ls_status', 'ls_spbe', 'ls_spbechild', 'ls_on_off_line');
+ return compact('orgs', 'ls_kategori', 'ls_group', 'ls_kepemilikan', 'ls_tipe', 'ls_status', 'ls_spbe', 'ls_spbechild', 'ls_on_off_line');
```

Data ini digunakan pada method `create()` dan `edit()` yang keduanya memanggil `selectBoxs()`.

---

### 3. `app/Models/DataApp.php`

**Tujuan:** Mendaftarkan kolom `kepemilikan` agar dapat diisi melalui mass assignment (Eloquent).

```diff
  protected $fillable = [
      ...
      'group',
+     'kepemilikan',
      'type',
      ...
  ];
```

> **Catatan:** Kolom `kepemilikan` juga perlu ditambahkan ke tabel database `data_apps` melalui migration. Migration belum dijalankan karena memerlukan akses ke container Docker (`docker exec layanan-v2 php artisan migrate`).

---

## Status Pekerjaan

| Komponen | Status |
|----------|--------|
| View — sembunyikan Group | ✅ Selesai |
| View — tambah form Kepemilikan | ✅ Selesai |
| Controller — tambah `ls_kepemilikan` | ✅ Selesai |
| Model — tambah `kepemilikan` di `$fillable` | ✅ Selesai |
| Database — migration kolom `kepemilikan` | ⏳ Menunggu dijalankan |

### Perintah Migration yang Perlu Dijalankan

```bash
docker exec layanan-v2 php artisan make:migration add_kepemilikan_to_data_apps_table --table=data_apps
# Edit file migration, lalu:
docker exec layanan-v2 php artisan migrate
```

Atau jika membuat migration manual, isi kolom:
```php
$table->string('kepemilikan')->nullable()->after('group');
```
