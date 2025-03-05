package com.rhino.wallset;

import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FullscreenActivity extends AppCompatActivity {
    private Button addToFavoritesButton, btnSetWallpaper;
    private String wallpaperUrl;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        // Toolbar'ı gizle
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Görseli görüntüle
        imageView = findViewById(R.id.fullscreenImageView);
        wallpaperUrl = getIntent().getStringExtra("IMAGE_URL");

        Glide.with(this)
                .load(wallpaperUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Önceden yüklenmiş resimleri önbellekte tut
                .thumbnail(0.1f) // Önce düşük çözünürlüklü göster
                .priority(Priority.HIGH) // Öncelikli yükleme
                .override(800, 600) // Büyük resimleri sıkıştırarak yükle
                .into(imageView);

        // SharedPreferences nesnesini oluştur
        sharedPreferences = getSharedPreferences("wallpaper_prefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Butonları bul
        addToFavoritesButton = findViewById(R.id.addToFavoritesButton);
        btnSetWallpaper = findViewById(R.id.btnSetWallpaper);

        // Favori durumu kontrol et ve butonu ona göre ayarla
        updateFavoriteButton();

        // Favori ekleme işlemi
        addToFavoritesButton.setOnClickListener(v -> new Thread(() -> toggleFavorite()).start());

        // Duvar kağıdını ayarlama işlemi
        btnSetWallpaper.setOnClickListener(v -> showSetWallpaperDialog());
    }

    // Favori durumu kontrol et ve butonu güncelle
    private void updateFavoriteButton() {
        Set<String> favoritesSet = sharedPreferences.getStringSet("favorites", new HashSet<>());
        if (favoritesSet.contains(wallpaperUrl)) {
            addToFavoritesButton.setText("Favorilerden Çıkar");
        } else {
            addToFavoritesButton.setText("Favorilere Ekle");
        }
    }

    // Favoriye ekleme/çıkarma işlemi
    private void toggleFavorite() {
        Set<String> favoritesSet = sharedPreferences.getStringSet("favorites", new HashSet<>());
        Set<String> updatedFavorites = new HashSet<>(favoritesSet); // Mutable kopya oluştur

        if (updatedFavorites.contains(wallpaperUrl)) {
            updatedFavorites.remove(wallpaperUrl); // Favorilerden çıkar
            runOnUiThread(() -> Toast.makeText(FullscreenActivity.this, "Duvar kağıdı favorilerden çıkarıldı!", Toast.LENGTH_SHORT).show());
        } else {
            updatedFavorites.add(wallpaperUrl); // Favorilere ekle
            runOnUiThread(() -> Toast.makeText(FullscreenActivity.this, "Duvar kağıdı favorilere eklendi!", Toast.LENGTH_SHORT).show());
        }

        editor.putStringSet("favorites", updatedFavorites);
        editor.apply();

        runOnUiThread(this::updateFavoriteButton); // Butonu güncelle
    }

    // Duvar kağıdını ayarlamak için seçenekleri gösteren BottomSheetDialogFragment
    private void showSetWallpaperDialog() {
        WallpaperOptionFragment fragment = WallpaperOptionFragment.newInstance(wallpaperUrl);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.show(transaction, "WallpaperOptionFragment");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Glide.with(this).clear(imageView); // Daha güvenli
    }
}
