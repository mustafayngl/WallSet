package com.rhino.wallset;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

import java.util.HashSet;
import java.util.Set;

public class FullscreenActivity extends AppCompatActivity {
    private Button addToFavoritesButton;
    private String wallpaperUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

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

        // Favoriye ekleme işlemi
        addToFavoritesButton.setOnClickListener(v -> {
            // Favorilerde olup olmadığını kontrol et
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
    }
}
