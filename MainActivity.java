package ru.roadlauncher;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements LocationListener {
    private final int BLUE = Color.rgb(54, 184, 255);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(155, 170, 190);
    private TextView clock, date, speed, gps;
    private LocationManager locationManager;
    private final Handler handler = new Handler();
    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            Date now = new Date();
            clock.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now));
            date.setText(new SimpleDateFormat("EEEE, d MMMM", new Locale("ru")).format(now));
            handler.postDelayed(this, 1000);
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        buildScreen();
        handler.post(clockTick);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        requestGps();
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextColor(color); t.setTextSize(size);
        t.setGravity(Gravity.CENTER);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        return l;
    }

    private LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(w, h);
    }

    private Button tile(String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(WHITE);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(21, 34, 51)));
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private void buildScreen() {
        LinearLayout root = column();
        root.setPadding(18, 18, 18, 16);
        root.setBackgroundColor(Color.rgb(7, 11, 18));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setOrientation(LinearLayout.HORIZONTAL);
        clock = text("--:--", 34, WHITE, true);
        date = text("", 13, MUTED, false);
        LinearLayout left = column();
        left.setGravity(Gravity.CENTER_VERTICAL);
        left.addView(clock); left.addView(date);
        top.addView(left, new LinearLayout.LayoutParams(0, -2, 1));
        TextView weather = text("ПОГОДА\\nОткрой приложение", 12, MUTED, false);
        top.addView(weather, lp(130, 54));
        root.addView(top, lp(-1, 76));

        TextView label = text("ROAD  •  DRIVE MODE", 12, BLUE, true);
        LinearLayout.LayoutParams labelLp = lp(-1, 36);
        labelLp.topMargin = 8;
        root.addView(label, labelLp);

        LinearLayout gauge = column();
        gauge.setBackgroundTintList(null);
        TextView caption = text("ТЕКУЩАЯ СКОРОСТЬ", 12, MUTED, true);
        gauge.addView(caption, lp(-1, 28));
        speed = text("0", 92, WHITE, true);
        gauge.addView(speed, lp(-1, 112));
        TextView unit = text("км/ч", 20, BLUE, true);
        gauge.addView(unit, lp(-1, 30));
        gps = text("Ожидание GPS…", 12, MUTED, false);
        gauge.addView(gps, lp(-1, 28));
        LinearLayout.LayoutParams gaugeLp = lp(-1, 0);
        gaugeLp.weight = 1;
        root.addView(gauge, gaugeLp);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        Button nav = tile("➤  Яндекс Навигатор", () -> launchApp(
            "ru.yandex.yandexnavi", "yandexnavi://"));
        Button music = tile("♫  Яндекс Музыка", () -> launchApp(
            "ru.yandex.music", "https://music.yandex.ru"));
        row1.addView(nav, new LinearLayout.LayoutParams(0, 68, 1));
        row1.addView(music, new LinearLayout.LayoutParams(0, 68, 1));
        root.addView(row1, lp(-1, 72));

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.addView(tile("⏮", () -> media(KeyEvent.KEYCODE_MEDIA_PREVIOUS)),
            new LinearLayout.LayoutParams(0, 56, 1));
        controls.addView(tile("▶ / ⏸", () -> media(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)),
            new LinearLayout.LayoutParams(0, 56, 1));
        controls.addView(tile("⏭", () -> media(KeyEvent.KEYCODE_MEDIA_NEXT)),
            new LinearLayout.LayoutParams(0, 56, 1));
        LinearLayout.LayoutParams controlsLp = lp(-1, 60);
        controlsLp.topMargin = 6;
        root.addView(controls, controlsLp);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.addView(tile("☎  Телефон", () -> startActivity(
            new Intent(Intent.ACTION_DIAL, Uri.parse("tel:")))),
            new LinearLayout.LayoutParams(0, 58, 1));
        row2.addView(tile("Bluetooth", () -> startActivity(
            new Intent(Settings.ACTION_BLUETOOTH_SETTINGS))),
            new LinearLayout.LayoutParams(0, 58, 1));
        root.addView(row2, lp(-1, 62));

        TextView foot = text("ROAD LAUNCHER  •  BETA 0.1", 10, MUTED, false);
        root.addView(foot, lp(-1, 24));
        setContentView(root);
    }

    private void launchApp(String packageName, String fallback) {
        Intent i = getPackageManager().getLaunchIntentForPackage(packageName);
        try {
            if (i != null) startActivity(i);
            else startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fallback)));
        } catch (Exception e) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fallback))); }
            catch (Exception ignored) {}
        }
    }

    private void media(int keyCode) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        sendBroadcast(intent);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keyCode));
        sendBroadcast(intent);
    }

    private void requestGps() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            gps.setText("Разреши геолокацию для спидометра");
            return;
        }
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, this);
                Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (last != null) onLocationChanged(last);
            } else gps.setText("Включи геолокацию");
        } catch (SecurityException ignored) {}
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] p, int[] r) {
        super.onRequestPermissionsResult(requestCode, p, r);
        if (requestCode == 10) requestGps();
    }

    @Override public void onLocationChanged(Location loc) {
        if (loc.hasSpeed()) speed.setText(String.valueOf(Math.round(loc.getSpeed() * 3.6f)));
        gps.setText("GPS • точность " + Math.round(loc.getAccuracy()) + " м");
    }
    @Override public void onProviderEnabled(String p) {}
    @Override public void onProviderDisabled(String p) { if (gps != null) gps.setText("GPS выключен"); }
    @Override public void onStatusChanged(String p, int s, Bundle b) {}
    @Override protected void onResume() { super.onResume(); if (locationManager != null) requestGps(); }
    @Override protected void onDestroy() { super.onDestroy(); handler.removeCallbacks(clockTick); }
}
