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
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FullscreenActivity extends AppCompatActivity {
    private Button addToFavoritesButton, btnSetWallpaper;
    private String wallpaperUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        // Toolbar'ı gizle
        getSupportActionBar().hide();

        // Görseli görüntüle
        ImageView imageView = findViewById(R.id.fullscreenImageView);
        wallpaperUrl = getIntent().getStringExtra("IMAGE_URL");

        // Glide ile duvar kağıdını yükle
        Glide.with(this)
                .load(wallpaperUrl)
                .into(imageView);

        // SharedPreferences nesnesini oluştur
        SharedPreferences sharedPreferences = getSharedPreferences("wallpaper_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Favoriye ekleme butonunu bul
        addToFavoritesButton = findViewById(R.id.addToFavoritesButton);
        // Duvar kağıdı yapma butonunu bul
        btnSetWallpaper = findViewById(R.id.btnSetWallpaper);

        // Favoriye ekleme işlemi
        addToFavoritesButton.setOnClickListener(v -> {
            Set<String> favoritesSet = sharedPreferences.getStringSet("favorites", new HashSet<>());
            if (favoritesSet.contains(wallpaperUrl)) {
                Toast.makeText(FullscreenActivity.this, "Bu duvar kağıdı zaten favorilerde!", Toast.LENGTH_SHORT).show();
            } else {
                favoritesSet.add(wallpaperUrl);  // Favoriye ekle
                editor.putStringSet("favorites", favoritesSet);
                editor.apply();  // Değişiklikleri kaydet
                Toast.makeText(FullscreenActivity.this, "Duvar kağıdı favorilere eklendi!", Toast.LENGTH_SHORT).show();
            }
        });

        // Duvar kağıdı olarak ayarlama işlemi
        btnSetWallpaper.setOnClickListener(v -> setWallpaper());
    }

    private void setWallpaper() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);

        Glide.with(this)
                .asBitmap()
                .load(wallpaperUrl)
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
}
