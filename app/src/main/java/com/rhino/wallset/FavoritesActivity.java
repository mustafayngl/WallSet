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
    private SharedPreferencesHelper preferencesHelper;
    private List<Wallpaper> wallpaperList;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoritesRecyclerView.setHasFixedSize(true); // Performans için ekledik

        preferencesHelper = new SharedPreferencesHelper(this);
        executorService = Executors.newSingleThreadExecutor(); // Arka plan işlemleri için

        loadFavorites();
    }

    // Favori duvar kağıtlarını yükleyen fonksiyon
    private void loadFavorites() {
        executorService.execute(() -> {
            String[] favorites = preferencesHelper.getFavorites();
            wallpaperList = new ArrayList<>();

            for (String url : favorites) {
                wallpaperList.add(new Wallpaper(url));
            }

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
                    Toast.makeText(FavoritesActivity.this, "Favori duvar kağıdınız yok!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Favoriden kaldırma işlemi
    private void removeFavorite(Wallpaper wallpaper) {
        executorService.execute(() -> {
            preferencesHelper.removeFromFavorites(wallpaper.getUrl());
            wallpaperList.remove(wallpaper);

            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                if (wallpaperList.isEmpty()) {
                    Toast.makeText(FavoritesActivity.this, "Tüm favoriler silindi!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        executorService.shutdown(); // Thread'leri serbest bırak
        super.onDestroy();
    }
}
