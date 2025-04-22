package com.rhino.wallset;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class FullscreenActivity extends AppCompatActivity {

    private Button addToFavoritesButton, btnSetWallpaper;
    private String wallpaperUrl;
    private ImageView imageView;
    private DatabaseHelper dbHelper; // SharedPreferences yerine bu!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        // Toolbar'ı gizle
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        imageView = findViewById(R.id.fullscreenImageView);
        wallpaperUrl = getIntent().getStringExtra("IMAGE_URL");

        Glide.with(this)
                .load(wallpaperUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(0.1f)
                .priority(Priority.HIGH)
                .override(800, 600)
                .into(imageView);

        dbHelper = new DatabaseHelper(this); // Veritabanını başlat

        addToFavoritesButton = findViewById(R.id.addToFavoritesButton);
        btnSetWallpaper = findViewById(R.id.btnSetWallpaper);

        updateFavoriteButton(); // Başlangıçta buton durumunu ayarla

        addToFavoritesButton.setOnClickListener(v -> new Thread(() -> toggleFavorite()).start());

        btnSetWallpaper.setOnClickListener(v -> showSetWallpaperDialog());
    }

    private void updateFavoriteButton() {
        // Wallpaper nesnesi oluşturuluyor
        Wallpaper wallpaper = new Wallpaper(0, "", "", wallpaperUrl, new Wallpaper.Src());

        boolean isFavorite = dbHelper.isFavorite(wallpaperUrl);
        runOnUiThread(() -> {
            if (isFavorite) {
                addToFavoritesButton.setText("Favorilerden Çıkar");
            } else {
                addToFavoritesButton.setText("Favorilere Ekle");
            }
        });
    }

    private void toggleFavorite() {
        // Wallpaper nesnesi oluşturuluyor
        Wallpaper wallpaper = new Wallpaper(0, "", "", wallpaperUrl, new Wallpaper.Src());

        boolean isFavorite = dbHelper.isFavorite(wallpaperUrl);
        if (isFavorite) {
            dbHelper.removeFromFavorites(wallpaperUrl);
            runOnUiThread(() -> {
                Toast.makeText(this, "Duvar kağıdı favorilerden çıkarıldı!", Toast.LENGTH_SHORT).show();
                updateFavoriteButton();
            });
        } else {
            dbHelper.addToFavorites(wallpaper); // Wallpaper nesnesi ekleniyor
            runOnUiThread(() -> {
                Toast.makeText(this, "Duvar kağıdı favorilere eklendi!", Toast.LENGTH_SHORT).show();
                updateFavoriteButton();
            });
        }
    }

    private void showSetWallpaperDialog() {
        WallpaperOptionFragment fragment = WallpaperOptionFragment.newInstance(wallpaperUrl);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.show(transaction, "WallpaperOptionFragment");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Glide.with(this).clear(imageView);
    }
}
