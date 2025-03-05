package com.rhino.wallset;

import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

        // Favori ekleme işlemi
        addToFavoritesButton.setOnClickListener(v -> new Thread(() -> addToFavorites()).start());

        // Duvar kağıdı olarak ayarlama işlemi
        btnSetWallpaper.setOnClickListener(v -> setWallpaper());
    }

    // Favori ekleme işlemi (arka planda çalışıyor)
    private void addToFavorites() {
        Set<String> favoritesSet = sharedPreferences.getStringSet("favorites", new HashSet<>());
        Set<String> updatedFavorites = new HashSet<>(favoritesSet); // Mutable kopya oluştur

        if (updatedFavorites.contains(wallpaperUrl)) {
            runOnUiThread(() -> Toast.makeText(FullscreenActivity.this, "Bu duvar kağıdı zaten favorilerde!", Toast.LENGTH_SHORT).show());
        } else {
            updatedFavorites.add(wallpaperUrl);
            editor.putStringSet("favorites", updatedFavorites);
            editor.apply();
            runOnUiThread(() -> Toast.makeText(FullscreenActivity.this, "Duvar kağıdı favorilere eklendi!", Toast.LENGTH_SHORT).show());
        }
    }

    // Duvar kağıdını ayarlama işlemi
    private void setWallpaper() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);

        Glide.with(this)
                .asBitmap()
                .load(wallpaperUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA) // Hafıza optimizasyonu
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        try {
                            wallpaperManager.setBitmap(resource);
                            Toast.makeText(FullscreenActivity.this, "Duvar kağıdı ayarlandı!", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(FullscreenActivity.this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Glide.with(this).clear(imageView); // Daha güvenli
    }
}
