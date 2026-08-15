/*
 * MainActivity.java
 *
 * Pengganti LauncherActivity berbasis TWA. Ini pakai WebView native supaya
 * FLAG_SECURE (anti-screenshot) beneran diterapkan ke SELURUH layar app,
 * termasuk konten chat -- karena window-nya sepenuhnya milik app kita,
 * bukan window Chrome seperti di TWA.
 *
 * GANTI dulu nilai BASE_URL di bawah sebelum build.
 */
package com.bakuchat.id;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.CookieManager;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Diambil dari hostName + launchUrl di build.gradle TWA kamu yang lama.
    private static final String BASE_URL = "https://bayuptbga.github.io/yungz/index.html";

    private WebView webView;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ==== INTI PROTEKSI ANTI-SCREENSHOT ====
        // Karena ini window Activity milik app kita sendiri (bukan Chrome),
        // flag ini benar-benar berlaku ke seluruh konten yang ditampilkan,
        // termasuk saat WebView menampilkan halaman chat.
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        // Diperlukan supaya localStorage/IndexedDB punya app web kamu
        // (misal untuk auth Supabase) tetap persist antar sesi.
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Izinkan cookie persist -- penting untuk sesi login Supabase.
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        // Pastikan navigasi tetap di dalam WebView (tidak lempar ke Chrome luar)
        webView.setWebViewClient(new WebViewClient());

        // Diperlukan agar file input, alert(), dsb dari halaman web berfungsi normal
        webView.setWebChromeClient(new WebChromeClient());

        if (savedInstanceState == null) {
            webView.loadUrl(BASE_URL);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    // Tombol back Android navigasi mundur di riwayat WebView dulu,
    // baru keluar app kalau sudah di halaman paling awal.
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
