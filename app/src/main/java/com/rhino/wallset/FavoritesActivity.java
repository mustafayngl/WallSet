package com.rhino.wallset;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView favoritesRecyclerView;
    private WallpaperAdapter adapter;
    private List<Wallpaper> wallpaperList;
    private ExecutorService executorService;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoritesRecyclerView.setHasFixedSize(true);

        dbHelper = new DatabaseHelper(this); // database bağlantısı
        executorService = Executors.newSingleThreadExecutor(); // arka plan için

        loadFavorites();
    }

    // Favori duvar kağıtlarını yükleyen fonksiyon
    private void loadFavorites() {
        executorService.execute(() -> {
            List<Wallpaper> favorites = dbHelper.getFavorites(); // Favori duvar kağıtlarını alıyoruz
            wallpaperList = new ArrayList<>(favorites);

            runOnUiThread(() -> {
                if (!wallpaperList.isEmpty()) {
                    adapter = new WallpaperAdapter(FavoritesActivity.this, wallpaperList,
                            wallpaper -> {
                                Intent intent = new Intent(FavoritesActivity.this, FullscreenActivity.class);
                                intent.putExtra("IMAGE_URL", wallpaper.getUrl());
                                startActivity(intent);
                            },
                            wallpaper -> removeFavorite(wallpaper));
                    favoritesRecyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(FavoritesActivity.this, getString(R.string.no_favorites), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Favoriden kaldırma işlemi
    private void removeFavorite(Wallpaper wallpaper) {
        executorService.execute(() -> {
            dbHelper.removeFromFavorites(wallpaper.getUrl()); // URL'yi al ve favorilerden kaldır
            wallpaperList.remove(wallpaper);

            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                if (wallpaperList.isEmpty()) {
                    Toast.makeText(FavoritesActivity.this, getString(R.string.all_favorites_removed), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        executorService.shutdown();
        super.onDestroy();
    }
}
