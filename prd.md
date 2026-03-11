JungleNav
Product Requirements Document (PRD)
Versi Sangat Lengkap / Engineering Blueprint

Metadata	Nilai
Produk	JungleNav – Sistem navigasi offline berbasis terrain, sensor fusion, dan dead reckoning
Platform awal	Android-first (Kotlin native), dengan opsi lapisan UI lintas platform di fase lanjutan
Jenis dokumen	PRD strategis + spesifikasi produk + blueprint teknis + dasar roadmap engineering
Bahasa	Indonesia
Tujuan	Dokumen acuan untuk perencanaan produk, desain arsitektur, dan implementasi MVP hingga produksi


Dokumen ini sengaja dibuat lebih lengkap daripada PRD biasa. Isinya bukan hanya kebutuhan produk, tetapi juga mencakup definisi sistem, arsitektur modul, risiko teknis, strategi akurasi, paket data offline, KPI, acceptance criteria, rencana fase pengembangan, hingga lampiran spesifikasi modul.
 
1. Ringkasan Eksekutif
                                                                                                                                                                                                                                                                                                         JungleNav adalah sistem navigasi lapangan yang dirancang untuk tetap berguna ketika konektivitas jaringan tidak tersedia dan akurasi GNSS memburuk akibat kanopi hutan, kontur medan, atau kondisi operasi yang menantang. Produk ini memadukan peta topografi offline, pembacaan sensor perangkat, estimasi posisi berbasis dead reckoning, dan koreksi posisi berbasis terrain untuk membantu pengguna mempertahankan orientasi, menavigasi waypoint, merekam jejak, serta kembali ke jalur atau titik awal.
                                                                                                                                                                                                                                                                                                         Berbeda dari aplikasi hiking umum yang umumnya berasumsi GNSS cukup baik dan data peta dapat diunduh sesuai kebutuhan, JungleNav dibangun sebagai sistem offline-first. Seluruh fungsi inti—menampilkan peta, memuat paket wilayah operasi, navigasi waypoint, merekam track, menilai kualitas posisi, dan menampilkan estimasi posisi ketika GNSS turun—harus tetap berjalan tanpa internet.
                                                                                                                                                                                                                                                                                                         Prinsip inti produk — JungleNav tidak menjanjikan posisi absolut selalu benar. Sistem harus menampilkan estimasi, confidence, dan kondisi navigasi secara jujur agar pengguna memahami tingkat kepercayaan hasil.

2. Latar Belakang dan Masalah yang Diselesaikan
   2.1 Kondisi medan
   Di hutan tropis, pegunungan berhutan, dan lembah sempit, performa GNSS dapat menurun karena multipath, kanopi lebat, dan geometri satelit yang kurang ideal. Pada saat yang sama, landmark visual sering terbatas, jalur tidak selalu jelas, dan pengguna tidak bisa bergantung pada infrastruktur internet.
   2.2 Masalah pengguna
   •	Sulit mempertahankan orientasi ketika peta digital ya guumum tidak menunjukkan informasi terrain secara memadai.
   •	Posisi GNSS dapat melompat atau memburuk ketika berada di bawah pohon lebat atau saat berpindah lembah/ridge.
   •	Saat sinyal GNSS hilang, aplikasi konvensional sering hanya membekukan posisi terakhir tanpa memberi estimasi gerak lanjutan.
   •	Track log sering sulit dipakai untuk retrace jika data tidak diberi status kualitas atau mode posisi.
   •	Operator lapangan membutuhkan antarmuka cepat, jelas, dan tidak bergantung pada sinkronisasi cloud.
   2.3 Opportunity produk
   Opportunity utama JungleNav adalah mengisi celah antara aplikasi peta offline umum dan sistem navigasi lapangan yang lebih tahan terhadap kondisi GNSS buruk. Nilai tambah produk terletak pada pemodelan kualitas posisi, integrasi terrain, serta pengalaman pengguna yang dirancang untuk konteks operasional, bukan konsumsi massal biasa.
3. Visi Produk, Misi, dan Prinsip Desain
   3.1 Visi
   Menjadi sistem navigasi offline yang andal, transparan, dan efisien untuk lingkungan medan berat dengan konektivitas terbatas.
   3.2 Misi
   •	Membantu pengguna mempertahankan orientasi ketika GNSS dan internet tidak dapat diandalkan.
   •	Memberikan gambaran terrain dan konteks medan yang relevan untuk pengambilan keputusan lapangan.
   •	Menyediakan estimasi posisi yang lebih stabil melalui sensor fusion dan terrain-aware correction.
   •	Menyajikan status akurasi dan confidence secara jelas agar pengguna tidak salah menafsirkan posisi.
   3.3 Prinsip desain
   Prinsip	Implikasi produk
   Offline-first	Semua fungsi inti harus dapat dijalankan tanpa internet, termasuk peta, waypoint, track, dan status kualitas posisi.
   Honest confidence	UI wajib menampilkan mode posisi (GNSS / fused / DR) dan confidence, bukan hanya ikon lokasi.
   Terrain-aware	Produk harus memanfaatkan data kontur, elevasi, slope, ridge, valley, sungai, dan jalur untuk pemahaman medan.
   Low-friction	Aksi inti harus dapat dilakukan dalam beberapa tap, dengan mode layar lapangan dan visibilitas tinggi.
   Battery-aware	Sampling sensor, frekuensi update, dan rendering peta harus disesuaikan dengan mode operasi.

4. Sasaran Pengguna dan Persona
   4.1 Segmen utama
   Segmen	Kebutuhan utama	Karakteristik operasi
   SAR	Menemukan arah, waypoint, dan track kembali dalam kondisi tergesa	Butuh kejelasan, kecepatan, reliabilitas, dan retrace yang mudah
   Peneliti lapangan	Mencatat titik observasi, elevasi, serta jalur survei	Durasi panjang, minim daya, area terpencil
   Surveyor / ranger	Membawa peta wilayah, menandai objek, dan menavigasi antar titik	Pekerjaan berulang dan area luas
   Ekspedisi	Navigasi jalur, camp, sumber air, dan alternatif rute	Butuh konteks terrain dan kemudahan penggunaan
   Relawan bencana	Koordinasi lapangan dasar dan pemetaan area terdampak	Lingkungan dinamis, infrastruktur rusak

4.2 Persona
Persona A — Operator lapangan
Bergerak cepat, memakai sarung tangan, sering melihat layar singkat, lebih mementingkan arah, jarak, mode posisi, dan jejak balik daripada detail analitik rumit.
Persona B — Surveyor teknis
Butuh akurasi relatif baik, pencatatan titik/track, metadata elevasi, dan kemampuan ekspor data untuk analisis lanjutan.
Persona C — Tim kecil
Lebih mementingkan koordinasi waypoint dan konsistensi pemahaman medan antar anggota daripada UI yang kompleks.
5. Scope Produk
   5.1 In-scope (fase inti)
   •	Peta topografi offline berbasis paket wilayah.
   •	Waypoint management, navigasi ke waypoint, dan return to base.
   •	Track recording, retrace, playback sederhana, dan ekspor.
   •	Pembacaan GNSS + sensor perangkat untuk estimasi posisi.
   •	Dead reckoning jangka pendek ketika GNSS turun/hilang.
   •	Confidence score dan indikator mode navigasi.
   •	Terrain layer: contour, hillshade, elevasi, slope, sungai, jalur.
   •	Pengaturan battery modes dan sampling adaptif.
   5.2 Out-of-scope awal
   •	Perencanaan misi multi-user yang kompleks.
   •	Sinkronisasi cloud wajib sebagai dependency inti.
   •	Visual SLAM produksi penuh pada fase MVP.
   •	Routing turn-by-turn seperti navigasi kendaraan.
   •	Autopilot drone atau integrasi sensor eksternal tingkat lanjut.
   5.3 Fase lanjutan
   •	Mesh position sharing lokal.
   •	Marker tim dan sinkronisasi waypoint terdekat.
   •	Pengenalan mode gerak berbasis model kecil.
   •	Terrain risk engine yang lebih kaya.
   •	Visual correction berbasis kamera bila perangkat mendukung.
6. Tujuan Produk dan KPI
   Area	Target awal	Catatan
   Waktu buka aplikasi	< 2 detik ke map screen	Pada perangkat target kelas menengah
   Waktu muat paket wilayah	< 3 detik	Dari penyimpanan lokal
   Update posisi UI	1 Hz default	Dapat diturunkan pada mode hemat daya
   Daya tahan operasi	> 12 jam	Profil patroli standar tanpa layar terus menyala
   Track persistence	0 kehilangan titik saat aplikasi di-background	Dengan strategi penyimpanan lokal dan service yang benar
   DR jangka pendek	Masih usable selama 30–60 detik	Confidence harus menurun progresif
   Ekspor data	< 15 detik untuk track harian	GPX/KML/GeoJSON

Catatan: target akurasi absolut harus diperlakukan hati-hati karena sangat tergantung perangkat, posisi perangkat di tubuh, kualitas sensor, kondisi medan, dan durasi GNSS drop. Karena itu KPI tidak hanya berbicara tentang error meter, tetapi juga tentang usability, confidence presentation, dan kemampuan recovery.
7. Kebutuhan Fungsional Utama
   7.1 Map dan terrain
1.	Sistem harus memuat paket peta offline per wilayah operasi.
2.	Sistem harus menampilkan layer vector map, contour lines, hillshade, sungai, jalur, dan titik penting.
3.	Sistem harus mendukung zoom, pan, rotate, follow-user, dan north-up / heading-up.
4.	Sistem harus mendukung overlay waypoint, track, dan estimasi uncertainty sederhana.
5.	Sistem harus menampilkan elevasi lokasi dan slope indikatif dari data terrain.
      7.2 Waypoint
6.	Pengguna harus dapat membuat, memberi nama, dan mengelola waypoint secara offline.
7.	Pengguna harus dapat menavigasi ke waypoint dengan bearing, jarak, dan estimasi arah.
8.	Pengguna harus dapat memilih opsi return to base atau return to selected waypoint.
9.	Sistem harus mendukung impor/ekspor waypoint ke format umum.
      7.3 Track recording
10.	Sistem harus merekam track dengan timestamp dan status mode posisi.
11.	Sistem harus dapat me-resume perekaman setelah gangguan aplikasi yang tidak disengaja sejauh memungkinkan.
12.	Sistem harus menampilkan track aktif dengan perbedaan warna atau metadata kualitas posisi.
13.	Sistem harus menyediakan fitur retrace dan playback sederhana.
       7.4 Positioning
14.	Sistem harus membaca lokasi GNSS dan indikator kualitas dasarnya.
15.	Sistem harus menggabungkan accelerometer, gyroscope, magnetometer, dan barometer untuk estimasi fused position.
16.	Saat GNSS memburuk, sistem harus dapat beralih ke DR mode dengan confidence yang menurun.
17.	Saat GNSS kembali sehat, sistem harus melakukan re-lock dan koreksi posisi secara mulus.
       7.5 Operation modes
18.	Mode Patrol: update rutin, layar sesekali, fokus pada jejak dan waypoint.
19.	Mode Survey: metadata lebih kaya, sampling lebih sering, prioritas akurasi.
20.	Mode Battery Saver: update minim, rendering dan sampling terbatas.
21.	Mode Emergency: update dipercepat, prioritas status posisi dan return path.
8. Kebutuhan Non-Fungsional
   Kategori	Kebutuhan
   Reliability	Aplikasi harus tetap dapat dibuka dan menampilkan data lokal meskipun komponen positioning gagal sementara.
   Performance	Interaksi map harus terasa halus pada perangkat target kelas menengah.
   Storage	Paket wilayah harus dapat dihapus, dipindah, dan dikelola dengan jelas.
   Battery	Operasi latar belakang wajib dikendalikan melalui mode operasi dan adaptive sampling.
   Security	Data pengguna lokal dapat dienkripsi opsional; operasi inti tidak boleh mensyaratkan server.
   Transparency	UI harus menunjukkan mode posisi, confidence, usia lokasi, dan sumber posisi.
   Usability	Aksi inti tersedia dalam 1–3 tap dan tetap terbaca di bawah cahaya luar ruang.

9. Use Cases Utama
   UC-01 Navigasi ke waypoint
   Pengguna memilih waypoint target, masuk ke mode navigasi, melihat bearing, jarak, mode posisi, dan track real-time hingga tiba di sekitar titik.
   UC-02 Rekam jalur patroli
   Pengguna memulai track recording sebelum bergerak, berpindah antara area GNSS baik dan buruk, lalu menyimpan track lengkap untuk retrace/ekspor.
   UC-03 Kembali ke titik awal
   Saat orientasi menurun, pengguna menekan return to base dan mengikuti bearing/track balik yang sudah direkam.
   UC-04 Survey beberapa titik
   Pengguna menandai waypoint observasi, menambahkan metadata singkat, lalu mengekspor hasil di akhir hari.
   UC-05 Operasi hemat daya
   Pengguna beroperasi lebih dari setengah hari, sehingga mode Battery Saver digunakan sambil tetap menjaga fungsi navigasi dasar.
10. Definisi Sistem dan Arsitektur Tingkat Tinggi
    Secara konseptual, JungleNav dibagi menjadi enam lapisan: Presentation/UI, Map & Terrain, Positioning, Navigation Logic, Data & Storage, dan System Services. Pendekatan layered memudahkan pemisahan concern, pengujian per modul, dan peluang mengganti implementasi tanpa mengganggu keseluruhan produk.
    Lapisan	Fungsi	Contoh modul
    Presentation/UI	Map screen, status panel, controls, settings	Compose UI, theme, action sheet, overlays
    Map & Terrain	Render peta, style, contour, hillshade, terrain query	MapLibre wrapper, tile manager, DEM query
    Positioning	GNSS, sensor reading, fusion, DR	Location service, sensor manager, fusion engine
    Navigation Logic	Waypoint nav, retrace, route hints, confidence	Nav controller, waypoint engine, confidence model
    Data & Storage	DB lokal, file packages, export/import	SQLite/Room, file repository, GPX exporter
    System Services	Background service, permissions, battery policy	Foreground service, telemetry buffer, diagnostics

11. Arsitektur Navigasi
    11.1 State machine posisi
    Mesin navigasi harus bekerja sebagai state machine, bukan sekadar stream lokasi. Status minimum yang direkomendasikan:
    State	Deskripsi	Transisi masuk	Transisi keluar
    GNSS_LOCKED	GNSS sehat dan menjadi sumber utama	Akurasi, satelit, dan freshness memenuhi syarat	Masuk ke FUSED atau DEGRADED bila kualitas turun
    FUSED	GNSS masih ada tetapi dipadukan kuat dengan sensor	Saat GNSS cukup tetapi tidak stabil penuh	Kembali ke GNSS_LOCKED atau turun ke DR_ACTIVE
    DR_ACTIVE	Estimasi posisi lanjut berbasis sensor setelah GNSS drop	Kehilangan/penurunan GNSS signifikan	Confidence turun, lalu ke DR_LOW_CONF atau kembali ke FUSED
    DR_LOW_CONF	DR masih jalan tetapi ketidakpastian tinggi	Durasi DR melewati batas atau drift meningkat	Recovery ke FUSED/GNSS_LOCKED saat data membaik
    NO_FIX	Belum ada posisi yang layak	Aplikasi baru dibuka atau sensor tidak tersedia	Masuk ke state lain saat ada data cukup

11.2 Confidence model
Setiap solusi posisi harus memiliki confidence score 0–100 dan label kualitatif (Tinggi / Sedang / Rendah). Confidence dihitung dari kombinasi freshness lokasi, kualitas GNSS, stabilitas heading, durasi DR, kecocokan terrain, dan konsistensi gerak.
12. Desain Dead Reckoning
    12.1 Tujuan DR
    Dead reckoning digunakan untuk mempertahankan continuity posisi jangka pendek ketika GNSS turun. Tujuannya bukan menggantikan GNSS sepenuhnya, tetapi menjaga usability navigasi selama periode drop singkat hingga menengah.
    12.2 Komponen inti
    •	Step detection / motion event detection.
    •	Estimasi heading relatif dari gyroscope dengan koreksi periodik.
    •	Estimasi step length / displacement berbasis heuristik atau model kecil.
    •	Propagasi posisi dari last reliable fix.
    •	Confidence decay terhadap waktu dan ketidakstabilan sensor.
    12.3 Batasan
    DR berbasis ponsel akan terpengaruh oleh posisi perangkat (di tangan, saku, tas, rompi), variasi gaya berjalan, medan naik-turun, gangguan magnetik, dan kualitas sensor. Karena itu, PRD mewajibkan mekanisme penurunan confidence dan re-lock yang jelas.
    12.4 Acceptance criteria DR
22.	Saat GNSS drop <= 15 detik, indikator posisi tetap bergerak tanpa freeze total pada mayoritas skenario berjalan normal.
23.	Pada durasi DR yang lebih lama, confidence harus turun progresif dan terlihat jelas pada UI.
24.	Saat GNSS kembali sehat, koreksi posisi tidak boleh 'meloncat' secara brutal kecuali perbedaan memang sangat besar; transisi harus dihaluskan.
25.	Track log harus menyimpan metadata mode posisi untuk setiap titik.
13. Sensor Fusion Engine
    13.1 Sensor yang dipakai
    Sensor	Peran	Catatan implementasi
    GNSS	Posisi absolut, speed kasar, akurasi	Sumber acuan utama bila sehat
    Accelerometer	Deteksi gerak, langkah, perubahan percepatan	Harus difilter dari noise dan gravity
    Gyroscope	Perubahan orientasi / heading relatif	Baik untuk short-term stability
    Magnetometer	Arah absolut kasar	Rentan gangguan; dipakai hati-hati
    Barometer	Perubahan tekanan untuk estimasi elevasi relatif	Berguna untuk naik-turun dan validasi terrain

13.2 Strategi fusion
Strategi fusion dapat dimulai dengan pendekatan heuristik + filter klasik. Fase awal tidak wajib memakai model AI. Fusion engine harus mengeluarkan tiga hal: estimated position, heading estimate, dan confidence/context flags.
13.3 Output fusion engine
•	latitude, longitude, altitude estimate
•	heading estimate
•	speed estimate / movement state
•	confidence score
•	mode tag: GNSS / FUSED / DR
•	freshness / age of last reliable fix
•	diagnostic flags
14. Terrain Engine
    14.1 Peran terrain
    Terrain bukan sekadar layer visual. Terrain dipakai untuk tiga kelas fungsi: pemahaman medan, koreksi posisi, dan penilaian risiko jalur.
    14.2 Kapabilitas minimum
    •	Query elevasi pada posisi tertentu.
    •	Perhitungan slope lokal untuk area sekitar posisi.
    •	Render contour dan hillshade agar pengguna memahami bentuk medan.
    •	Identifikasi unsur linear penting seperti sungai/jalur bila tersedia.
    •	Terrain plausibility checks untuk menilai apakah posisi hasil estimasi masuk akal.
    14.3 Terrain plausibility examples
    Skenario	Respons sistem
    Posisi estimasi jatuh di badan sungai besar padahal pengguna bergerak seperti berjalan	Turunkan confidence; usulkan snap lemah ke jalur/tepi yang lebih masuk akal jika ada konteks cukup
    Barometer menunjukkan pendakian stabil tetapi track estimasi berada di area datar DEM	Tandai mismatch dan hindari confidence terlalu tinggi
    Heading berubah liar tanpa dukungan data gerak	Curigai gangguan magnetik / orientasi device berubah

15. Peta Offline dan Pipeline Data
    15.1 Komponen paket wilayah
    •	Vector tiles dasar (jalan, jalur, sungai, batas, titik penting).
    •	Contour lines dan hillshade.
    •	Metadata wilayah: nama, versi, cakupan bounding box, ukuran file, tanggal build.
    •	Opsional: raster cache tertentu bila diperlukan.
    15.2 Kebutuhan paket
    Atribut	Kebutuhan
    Ukuran	Harus dikelola agar tetap wajar per wilayah; pengguna harus melihat estimasi ukuran sebelum mengunduh/menyalin.
    Integritas	Paket harus diverifikasi sebelum diaktifkan.
    Versioning	Harus ada versi paket dan tanggal build.
    Delete/replace	Pengguna harus dapat menghapus atau mengganti paket dengan aman.
    Low-friction	Aktivasi paket tidak boleh memerlukan internet.

15.3 Pipeline build data
26.	Ambil data sumber geospasial wilayah target.
27.	Normalisasi dan filter layer yang relevan.
28.	Generate vector tiles dan terrain derivatives (contour/hillshade).
29.	Buat metadata paket dan checksum.
30.	Uji paket pada simulator/perangkat.
31.	Distribusikan sebagai bundle yang dapat dipasang lokal.
16. Manajemen Data, Database, dan File
    16.1 Database lokal
    Tabel	Isi	Keterangan
    waypoints	ID, nama, koordinat, elevasi, kategori, catatan, waktu dibuat	Entity titik pengguna
    tracks	ID, nama, waktu mulai/selesai, statistik ringkas, status	Satu entri per sesi track
    track_points	track_id, waktu, posisi, elevasi, heading, speed, mode, confidence	Menyimpan detail jalur
    map_packages	ID, nama, versi, ukuran, path, checksum, aktif/tidak	Inventaris paket wilayah
    settings	key-value konfigurasi	Pengaturan aplikasi dan mode
    diagnostics	event time, type, payload ringkas	Opsional untuk debug lokal

16.2 Kebijakan penyimpanan
•	Track aktif harus ditulis bertahap agar aman dari crash.
•	Data ekspor disimpan ke direktori yang jelas dan dapat dibagikan pengguna.
•	Paket wilayah harus terpisah dari database transaksi agar pengelolaannya mudah.
•	Semua operasi file yang lama harus memiliki progress state atau status yang dapat dibatalkan.
17. UX / UI Requirements
    17.1 Prinsip UX lapangan
    •	Kontras tinggi, mudah dibaca di luar ruang.
    •	Status kritis selalu terlihat: bearing, jarak, mode posisi, confidence, baterai.
    •	Aksi utama di map screen; minim perpindahan layar untuk operasi cepat.
    •	Kontrol besar dan tidak mengandalkan gesture rumit.
    •	Mode malam / low-light / red-mode dipertimbangkan.
    17.2 Layar utama
    Layar	Tujuan	Elemen wajib
    Map Screen	Pusat operasi	Peta, posisi, arah, status mode, tombol record, waypoint, package name
    Waypoint Detail	Kelola dan navigasi titik	Nama, koordinat, catatan, tombol navigate/edit/delete
    Track Screen	Kelola perekaman dan riwayat	Mulai/stop, daftar track, statistik ringkas, ekspor
    Package Manager	Kelola paket wilayah	Daftar paket, ukuran, versi, aktif/nonaktif, hapus/impor
    Diagnostics	Informasi teknis untuk validasi	Sensor state, mode posisi, usia fix, log ringkas
    Settings	Mode operasi dan konfigurasi	Battery mode, units, theme, export options

17.3 Informasi yang harus selalu terlihat saat navigasi
•	Nama target / tujuan aktif.
•	Jarak ke tujuan.
•	Bearing atau arah target.
•	Mode posisi saat ini.
•	Confidence level.
•	Usia solusi posisi / freshness.
•	Status recording (jika aktif).
18. Mode Operasi dan Strategi Baterai
    18.1 Definisi mode
    Mode	Prioritas	Dampak teknis
    Patrol	Seimbang antara akurasi dan daya	Update reguler, perekaman kontinu, layar secukupnya
    Survey	Prioritas akurasi dan metadata	Sampling lebih tinggi, logging lebih detail
    Battery Saver	Prioritas daya	Frekuensi update diturunkan, visual non-esensial dikurangi
    Emergency	Prioritas continuity posisi dan orientasi	Foreground service kuat, status lebih menonjol

18.2 Strategi baterai
•	Adaptive sensor sampling berdasarkan movement state.
•	Throttle rendering saat layar tidak aktif.
•	Flush data track secara batch yang aman, bukan terlalu sering.
•	Pembatasan query terrain berat pada interval tertentu.
•	Kebijakan foreground/background yang eksplisit.
19. Permissions, Privacy, dan Security
    Karena aplikasi beroperasi di lapangan dan berpotensi menyimpan track sensitif, pendekatan privacy dan security harus konservatif. Operasi inti tidak boleh memerlukan akun. Pengguna harus dapat menjalankan aplikasi sepenuhnya secara lokal.
    Area	Kebijakan yang diinginkan
    Akun	Tidak wajib untuk fungsi inti
    Internet	Tidak wajib; boleh digunakan hanya untuk distribusi paket / update bila tersedia
    Lokasi	Diminta dengan penjelasan jelas mengapa dibutuhkan
    Penyimpanan	Pengguna mengetahui lokasi paket dan ekspor
    Enkripsi	Opsional untuk paket tertentu dan data track sensitif
    Telemetry	Default minimum / off untuk build lapangan; jika ada, harus bisa dimatikan

20. Observability, Diagnostics, dan Mode Debug
    Produk lapangan sulit divalidasi bila tidak memiliki observability yang cukup. Maka JungleNav memerlukan diagnostics mode yang aman namun berguna.
    •	Status sensor aktif/tidak aktif.
    •	Mode posisi saat ini dan alasan transisi.
    •	Usia lokasi terakhir dan confidence score.
    •	Statistik singkat perekaman track.
    •	Informasi paket wilayah aktif.
    •	Log ringkas event penting (mis. GNSS lost, DR entered, re-lock achieved).
21. Acceptance Criteria Produk
    ID	Acceptance criteria
    AC-01	Aplikasi dapat dibuka tanpa internet dan menampilkan paket wilayah aktif.
    AC-02	Pengguna dapat membuat, mengedit, dan menghapus waypoint seluruhnya secara offline.
    AC-03	Track recording dapat dimulai, dihentikan, dan disimpan lokal tanpa crash pada skenario dasar.
    AC-04	Saat GNSS turun, sistem masuk ke mode estimasi lanjutan dan menampilkan label mode yang benar.
    AC-05	Saat GNSS kembali memadai, sistem kembali ke solusi yang lebih andal dengan transisi yang halus.
    AC-06	Map screen tetap usable pada sinar luar ruang dan informasi inti terbaca.
    AC-07	Pengguna dapat mengekspor minimal satu track dan satu kumpulan waypoint ke format umum.

22. Risiko Teknis dan Strategi Mitigasi
    Risiko	Dampak	Mitigasi
    Drift dead reckoning	Posisi makin melenceng saat GNSS lama hilang	Confidence decay, re-lock smoothing, logging mode posisi, batas durasi DR efektif
    Gangguan magnetik	Heading salah	Prioritaskan gyro untuk short-term heading; gunakan magnetometer hati-hati
    Variasi device placement	Model langkah/perpindahan tidak konsisten	Uji banyak skenario pembawaan, heuristik adaptif
    Battery drain	Aplikasi tidak bertahan lama	Mode operasi, adaptive sampling, rendering throttle
    Paket peta terlalu besar	Storage cepat penuh	Wilayah modular, estimasi ukuran, delete flow yang jelas
    Crash saat background	Track hilang / perekaman putus	Foreground service, flush berkala, recovery state

23. Rencana Pengembangan Bertahap
    23.1 Phase 0 — Discovery & validation
    •	Validasi use case inti dan perangkat target.
    •	Eksperimen sensor logging dan quality benchmarks sederhana.
    •	Proof of concept render peta offline + package manager.
    23.2 Phase 1 — MVP offline navigation
    •	Map screen + package manager.
    •	Waypoint CRUD + navigate to waypoint.
    •	Track recording dasar.
    •	Status GNSS dasar dan mode operasi awal.
    23.3 Phase 2 — Position fusion
    •	Integrasi sensor pipeline.
    •	State machine posisi.
    •	Dead reckoning jangka pendek.
    •	Confidence model awal.
    •	Diagnostics screen.
    23.4 Phase 3 — Terrain-aware system
    •	Terrain query + contour/hillshade matang.
    •	Plausibility checks terrain.
    •	Smoothing re-lock dan heuristik perbaikan posisi.
    •	Battery optimization lebih matang.
    23.5 Phase 4 — Advanced field capabilities
    •	Team markers lokal / mesh sederhana.
    •	Risk layer dan route advisory ringan.
    •	Model kecil untuk motion classification / stride adaptation.
    •	Eksplorasi visual correction bila realistis.
24. Rekomendasi Stack Teknis
    Komponen	Pilihan utama	Alasan
    Platform	Android native (Kotlin)	Akses sensor, background service, dan tuning sistem paling kuat
    UI	Jetpack Compose	Modern, cepat dikembangkan, cocok untuk state-driven UI
    Map engine	MapLibre Native	Render map modern dan fleksibel untuk paket offline
    DB	SQLite / Room	Stabil untuk data lokal transaksional
    Package storage	File-based bundles + metadata DB	Mudah dikelola dan diganti
    Sensor stack	Android Sensor APIs + custom fusion layer	Kontrol tinggi pada sampling dan event
    Export	GPX / KML / GeoJSON	Interoperabilitas dasar

Catatan implementasi — Jika tim ingin UI lintas platform, pendekatan yang dianjurkan adalah Flutter atau lapisan serupa hanya untuk presentasi, sedangkan navigation engine tetap native.

25. Struktur Repositori yang Direkomendasikan
    Struktur berikut tidak wajib, tetapi dianjurkan agar domain navigasi, data, dan UI tidak tercampur.

junglenav/
├─ app/
├─ core/
│   ├─ common/
│   ├─ model/
│   └─ utils/
├─ feature/
│   ├─ map/
│   ├─ waypoint/
│   ├─ track/
│   ├─ navigation/
│   ├─ package_manager/
│   └─ diagnostics/
├─ engine/
│   ├─ positioning/
│   ├─ fusion/
│   ├─ dead_reckoning/
│   └─ terrain/
├─ data/
│   ├─ db/
│   ├─ repository/
│   ├─ export/
│   └─ packages/
├─ system/
│   ├─ permissions/
│   ├─ battery/
│   └─ background/
└─ docs/

26. Spesifikasi Modul Rinci
    26.1 Map Module
    •	Memuat style peta dan paket wilayah aktif
    •	Mengelola overlay user position, waypoint, track, dan contour visibility
    •	Menyediakan query koordinat layar ke geometri peta
    26.2 Waypoint Module
    •	CRUD waypoint
    •	Kategori dan metadata ringan
    •	Import/export titik
    26.3 Track Module
    •	Lifecycle recording
    •	Segmentasi track
    •	Statistik dasar: jarak, durasi, elevasi gain
    •	Playback sederhana
    26.4 Positioning Module
    •	Abstraksi GNSS
    •	Sensor subscriptions
    •	Output posisi terstandar
    26.5 Fusion Module
    •	Menggabungkan data posisi dan sensor
    •	Menghasilkan confidence, mode, dan flags
    •	Mengelola transisi state posisi
    26.6 Terrain Module
    •	Query elevasi/slope
    •	Plausibility checks
    •	Penyedia layer terrain untuk UI
    26.7 Package Manager Module
    •	Inventaris paket
    •	Validasi checksum
    •	Aktivasi/penghapusan paket
    26.8 Diagnostics Module
    •	Status teknis
    •	Event log ringkas
    •	Export debug bundle opsional
27. API Internal / Contract Antar Modul
    Producer	Consumer	Contract inti
    Positioning Module	Fusion Module	Raw location samples, speed, accuracy, timestamp
    Sensor Module	Fusion Module	Accel/gyro/mag/baro samples dengan timestamp
    Fusion Module	Navigation UI	Estimated position, mode, confidence, heading
    Terrain Module	Fusion / UI	Elevation, slope, terrain checks
    Track Module	Storage / Export	Track session + points + metadata mode
    Package Manager	Map Module	Path paket aktif, style resources, region metadata

28. Testing Strategy
    28.1 Unit tests
    •	Perhitungan confidence dan state transitions.
    •	Serialisasi/deserialisasi waypoint, track, dan package metadata.
    •	Heuristik movement state dasar.
    •	Ekspor GPX/KML/GeoJSON.
    28.2 Integration tests
    •	Track recording dari start sampai export.
    •	Aktivasi/deaktivasi paket wilayah.
    •	Transisi GNSS -> DR -> GNSS.
    •	Map screen dengan overlay aktif.
    28.3 Field tests
    •	Jalur terbuka, semi-kanopi, dan kanopi berat.
    •	Perangkat di tangan, saku, tas, dan mount.
    •	Medan datar, menanjak, menurun, dan area dekat sungai.
    •	Durasi operasi pendek dan panjang.
    28.4 Test data yang perlu direkam
    •	Sensor logs tersinkronisasi waktu.
    •	GNSS metadata minimal.
    •	Posisi referensi bila tersedia.
    •	Event user actions dan mode operasi.
    •	Baterai dan temperatur perangkat.
29. Rollout Plan
    Rollout disarankan bertahap: internal alpha, field alpha terbatas, beta lapangan dengan instrumen log, lalu produksi terbatas. Setiap tahap harus memiliki exit criteria yang jelas, terutama terkait crash rate, konsumsi daya, kejelasan UX, dan kualitas transisi mode posisi.
    Tahap	Tujuan	Exit criteria minimum
    Internal alpha	Validasi arsitektur dan dasar navigasi	Tidak ada crash blocker pada use case inti
    Field alpha	Uji perangkat nyata dan logging	Track recording stabil, package manager matang
    Beta	Uji keandalan transisi mode dan battery	Mode posisi dan confidence dipahami pengguna
    Production limited	Deploy terbatas pada pengguna nyata	Issue kritis terkendali dan docs siap

30. Future Vision
    Setelah fondasi offline navigation stabil, JungleNav dapat berkembang menjadi platform awareness lapangan yang lebih luas: marker tim lokal, paket misi, layer ancaman/risiko, sinkronisasi opportunistic, dan integrasi sensor tambahan. Namun dokumen ini menekankan bahwa nilai produk paling besar tetap berasal dari fondasi yang kokoh: peta offline yang baik, UI lapangan yang jujur, dan positioning engine yang transparan.
    Lampiran A — Daftar Istilah
    Istilah	Arti dalam konteks dokumen
    GNSS	Sumber posisi satelit secara umum
    DR	Dead reckoning; estimasi posisi lanjutan ketika GNSS buruk/hilang
    Confidence	Skor kepercayaan terhadap solusi posisi saat ini
    Terrain plausibility	Penilaian apakah posisi/gerak masuk akal terhadap kondisi medan
    Package	Bundle peta wilayah offline beserta metadata dan layer terrain
    Retrace	Mengikuti kembali track yang telah direkam

Lampiran B — Backlog Fitur Prioritas
Prioritas	Fitur	Alasan
P0	Map screen offline stabil	Fondasi seluruh aplikasi
P0	Waypoint + navigate	Use case inti
P0	Track recording aman	Keselamatan dan retrace
P1	Confidence model dasar	Transparansi dan usability
P1	DR jangka pendek	Diferensiasi utama
P1	Package manager yang matang	Operasional offline
P2	Terrain plausibility	Peningkatan akurasi/konteks
P2	Diagnostics mode	Validasi lapangan
P3	Mesh local markers	Kolaborasi tim kecil
P3	Model motion classification	Optimasi langkah/gerak

Lampiran C — Definition of Done (per fitur)
Map package feature
•	Dapat impor/aktifkan/hapus paket
•	Metadata tampil benar
•	Map screen dapat render paket tanpa internet
Waypoint navigation
•	CRUD titik berjalan
•	Arah dan jarak tampil benar
•	Status tujuan aktif jelas
Track recording
•	Start/stop/save berjalan
•	Data tersimpan lokal
•	Export minimal satu format sukses
Position mode engine
•	State transisi benar
•	Confidence tampil
•	Recovery ke GNSS mulus pada skenario dasar
 
Akhir dokumen
PRD ini dirancang sebagai dokumen kerja. Ia dapat dijadikan basis untuk roadmap, desain teknis, struktur repositori, dan sprint plan implementasi.
