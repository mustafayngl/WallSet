package com.rhino.wallset;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView favoritesRecyclerView;
    private WallpaperAdapter adapter;
    private SharedPreferencesHelper preferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        preferencesHelper = new SharedPreferencesHelper(this);
        String[] favorites = preferencesHelper.getFavorites();

        List<Wallpaper> wallpaperList = new ArrayList<>();
        for (String url : favorites) {
            Wallpaper wallpaper = new Wallpaper(url);
            wallpaperList.add(wallpaper);
        }

        if (!wallpaperList.isEmpty()) {
            adapter = new WallpaperAdapter(FavoritesActivity.this, wallpaperList,
                    wallpaper -> {
                        Intent intent = new Intent(FavoritesActivity.this, FullscreenActivity.class);
                        intent.putExtra("IMAGE_URL", wallpaper.getUrl());
                        startActivity(intent);
                    },
                    wallpaper -> {
                        preferencesHelper.removeFromFavorites(wallpaper.getUrl());
                        wallpaperList.remove(wallpaper);
                        adapter.notifyDataSetChanged();
                    });
            favoritesRecyclerView.setAdapter(adapter);
        } else {
            Toast.makeText(this, "Favori duvar kağıdınız yok!", Toast.LENGTH_SHORT).show();
        }
    }
}
