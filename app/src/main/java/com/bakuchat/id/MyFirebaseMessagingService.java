/*
 * FirebaseMessagingService.java
 *
 * Nerima push notification langsung lewat FCM native, bukan lewat
 * Service Worker (sw.js). Ini yang bikin notifikasi jauh lebih tahan
 * dari battery optimization Android (Xiaomi/Oppo/Vivo dkk), karena
 * FCM native dijalankan sistem Android sendiri, bukan lewat proses
 * browser yang gampang dimatikan di background.
 */
package com.bakuchat.id;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "bakuchat_messages";

    // Dipanggil setiap kali ada token FCM baru (pertama install, atau saat
    // token di-refresh oleh sistem). Kita simpan ke SharedPreferences dulu;
    // LauncherActivity yang nanti kirim token ini ke backend (lewat web app),
    // supaya token terhubung ke akun user yang lagi login.
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        getSharedPreferences("bakuchat_prefs", MODE_PRIVATE)
                .edit()
                .putString("fcm_token", token)
                .putBoolean("fcm_token_synced", false) // tandai belum dikirim ke server
                .apply();
    }

    // Dipanggil setiap ada pesan masuk dari server (lewat Edge Function
    // Supabase yang kirim ke FCM). Kita pakai "data message" (bukan
    // "notification message" bawaan FCM) supaya bisa full kontrol
    // tampilan notif dan supaya tetap kepanggil walau app benar-benar
    // ditutup/killed.
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        String title = data.getOrDefault("title", "Baku Chat");
        String body = data.getOrDefault("body", "Ada pesan baru");
        String url = data.getOrDefault("url", "https://bayuptbga.github.io/yungz/index.html");

        showNotification(title, body, url);
    }

    private void showNotification(String title, String body, String url) {
        NotificationManager manager = getSystemService(NotificationManager.class);

        // Wajib buat notification channel di Android 8 (Oreo) ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pesan Chat",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifikasi pesan masuk Baku Chat");
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }

        // Tap notif -> buka LauncherActivity, lalu arahkan ke URL chat terkait
        Intent intent = new Intent(this, LauncherActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(Uri.parse(url));

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(android.R.drawable.ic_dialog_email) // GANTI ke ikon app kamu kalau ada, mis. R.drawable.ic_notification_icon
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build();

        manager.notify((int) System.currentTimeMillis(), notification);
    }
}
