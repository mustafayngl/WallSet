package com.rhino.wallset;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView favoritesRecyclerView;
    private WallpaperAdapter adapter;
    private List<Wallpaper> wallpaperList;
    private List<Wallpaper> allFavorites;
    private ExecutorService executorService;
    private DatabaseHelper dbHelper;
    private SearchView semanticSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites); // ✅ Set content before hiding UI
        hideSystemUI();

        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoritesRecyclerView.setHasFixedSize(true);

        semanticSearchView = findViewById(R.id.semanticSearchView);
        dbHelper = new DatabaseHelper(this);
        executorService = Executors.newSingleThreadExecutor();

        setupSearch();
        loadFavorites();
    }

    private void hideSystemUI() {
        Window window = getWindow();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
            View decorView = window.getDecorView();
            WindowInsetsController controller = decorView.getWindowInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    private void setupSearch() {
        semanticSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                runSemanticSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void runSemanticSearch(String query) {
        CohereEmbeddingHelper.getEmbedding(query, new CohereEmbeddingHelper.EmbeddingCallback() {
            @Override
            public void onSuccess(float[] queryEmbedding) {
                executorService.execute(() -> {
                    List<ScoredWallpaper> scoredList = new ArrayList<>();
                    for (Wallpaper wp : allFavorites) {
                        float[] titleEmbedding = dbHelper.getEmbeddingForWallpaper(wp.getUrl());
                        if (titleEmbedding != null) {
                            float similarity = computeCosineSimilarity(queryEmbedding, titleEmbedding);
                            scoredList.add(new ScoredWallpaper(wp, similarity));
                        }
                    }

                    Collections.sort(scoredList, Comparator.comparingDouble(w -> -w.similarity));
                    wallpaperList.clear();
                    for (ScoredWallpaper sw : scoredList) {
                        wallpaperList.add(sw.wallpaper);
                    }

                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(FavoritesActivity.this, "AI search failed: " + error, Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private float computeCosineSimilarity(float[] a, float[] b) {
        float dot = 0f;
        float normA = 0f;
        float normB = 0f;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    private void loadFavorites() {
        executorService.execute(() -> {
            allFavorites = dbHelper.getFavorites();
            wallpaperList = new ArrayList<>(allFavorites);

            runOnUiThread(() -> {
                if (!wallpaperList.isEmpty()) {
                    adapter = new WallpaperAdapter(FavoritesActivity.this, wallpaperList,
                            wallpaper -> {
                                Intent intent = new Intent(FavoritesActivity.this, FullscreenActivity.class);
                                intent.putExtra("IMAGE_URL", wallpaper.getUrl());
                                startActivity(intent);
                            },
                            this::removeFavorite);
                    favoritesRecyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(FavoritesActivity.this, getString(R.string.no_favorites), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void removeFavorite(Wallpaper wallpaper) {
        executorService.execute(() -> {
            dbHelper.removeFromFavorites(wallpaper.getUrl());
            wallpaperList.remove(wallpaper);
            allFavorites.remove(wallpaper);

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

    // Helper class for ranking
    static class ScoredWallpaper {
        Wallpaper wallpaper;
        float similarity;

        ScoredWallpaper(Wallpaper w, float s) {
            this.wallpaper = w;
            this.similarity = s;
        }
    }
}
