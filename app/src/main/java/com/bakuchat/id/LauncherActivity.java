/*
 * LauncherActivity.java -- versi paling lengkap untuk TWA (Trusted Web Activity)
 *
 * PENTING, BACA DULU:
 * Flag/proteksi di file ini HANYA berlaku untuk window LauncherActivity itu
 * sendiri -- yaitu splash screen singkat sebelum TWA/Chrome mengambil alih,
 * dan tampilan thumbnail di recent-apps switcher (karena itu snapshot dari
 * window LauncherActivity, bukan window Chrome).
 *
 * Screenshot/recording di LAYAR CHAT SEBENARNYA (setelah TWA aktif) TIDAK
 * ikut terproteksi oleh file ini, karena saat itu yang render adalah window
 * milik Chrome/Custom Tabs provider, bukan window app ini. Ini keterbatasan
 * arsitektur TWA, bukan sesuatu yang bisa diperbaiki lewat kode di sini.
 * Kalau butuh proteksi penuh, satu-satunya jalan adalah WebView native
 * (lihat MainActivity.java yang sudah dibuat sebelumnya).
 *
 * Copyright 2020 Google Inc. (base class asli)
 * Licensed under the Apache License, Version 2.0
 */
package com.bakuchat.id;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

public class LauncherActivity
        extends com.google.androidbrowserhelper.trusted.LauncherActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ==== 1. FLAG_SECURE ====
        // Blokir screenshot & screen recording untuk window LauncherActivity
        // ini (splash), dan sembunyikan preview-nya di recent-apps switcher.
        // Lihat catatan di atas soal keterbatasannya.
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        // ==== 2. Orientasi ====
        // Setting orientation crash di Android 8.0 Oreo ke bawah karena
        // background transparan splash. Hanya di-set di Oreo ke atas.
        // Ref: https://github.com/GoogleChromeLabs/bubblewrap/issues/496
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-assert FLAG_SECURE setiap kali activity kembali ke foreground
        // (misal setelah user sempat pindah ke app lain lalu balik lagi).
        // Beberapa kondisi bisa membuat window flags perlu di-set ulang.
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );
    }

    @Override
    protected Uri getLaunchingUrl() {
        // Get the original launch Url. Tidak dimodifikasi -- placeholder
        // resmi dari template Bubblewrap kalau nanti perlu inject query
        // param tambahan sebelum TWA dibuka.
        return super.getLaunchingUrl();
    }
}
