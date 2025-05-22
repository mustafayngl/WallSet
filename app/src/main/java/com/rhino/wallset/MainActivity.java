package com.rhino.wallset;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WallpaperAdapter wallpaperAdapter;
    private List<Wallpaper> wallpapers;
    private ExecutorService executorService;

    // 🔁 Yeni: Favorilerden dönüş için launcher
    private final ActivityResultLauncher<Intent> favoritesLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            loadWallpapers("");
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideSystemUI();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);

        executorService = Executors.newSingleThreadExecutor();
        loadWallpapers("");
    }

    private void hideSystemUI() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            View decorView = getWindow().getDecorView();
            WindowInsetsController controller = decorView.getWindowInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
    }

    private void loadWallpapers(String query) {
        executorService.execute(() -> {
            int page = new Random().nextInt(100) + 1;
            ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
            Call<WallpaperResponse> call = query.isEmpty()
                    ? apiService.getWallpapers(page, 10)
                    : apiService.searchWallpapers(query, page, 10);

            call.enqueue(new Callback<WallpaperResponse>() {
                @Override
                public void onResponse(Call<WallpaperResponse> call, Response<WallpaperResponse> response) {
                    if (response.body() == null || response.body().getPhotos() == null) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "No wallpapers found", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    wallpapers = response.body().getPhotos();
                    Collections.shuffle(wallpapers);

                    runOnUiThread(() -> {
                        wallpaperAdapter = new WallpaperAdapter(MainActivity.this, wallpapers,
                                wallpaper -> {
                                    Intent intent = new Intent(MainActivity.this, FullscreenActivity.class);
                                    intent.putExtra("IMAGE_URL", wallpaper.getUrl());
                                    startActivity(intent);
                                },
                                wallpaper -> {
                                    CohereEmbeddingHelper.getEmbedding(wallpaper.getUrl(), new CohereEmbeddingHelper.EmbeddingCallback() {
                                        @Override
                                        public void onSuccess(float[] embedding) {
                                            executorService.execute(() -> {
                                                DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
                                                dbHelper.addToFavoritesWithEmbedding(wallpaper, embedding);
                                                runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.added_to_favorites, Toast.LENGTH_SHORT).show());
                                            });
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            runOnUiThread(() ->
                                                    Toast.makeText(MainActivity.this, "AI failed: " + error, Toast.LENGTH_SHORT).show()
                                            );
                                        }
                                    });
                                });
                        recyclerView.setAdapter(wallpaperAdapter);
                    });
                }

                @Override
                public void onFailure(Call<WallpaperResponse> call, Throwable t) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.connection_error, Toast.LENGTH_SHORT).show());
                }
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.searchView);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("Search wallpapers...");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    loadWallpapers(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_favorites) {
            Intent intent = new Intent(this, FavoritesActivity.class);
            favoritesLauncher.launch(intent); // 🔁 Launcher ile başlat
            return true;
        } else if (id == R.id.action_refresh) {
            loadWallpapers("");
            Toast.makeText(this, "Wallpapers refreshed", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_toggle_theme) {
            SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
            boolean isDarkMode = prefs.getBoolean("dark_mode", false);
            boolean newMode = !isDarkMode;

            prefs.edit().putBoolean("dark_mode", newMode).apply();

            AppCompatDelegate.setDefaultNightMode(
                    newMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            Toast.makeText(this, "Theme changed. Restart app to fully apply.", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        executorService.shutdown();
        super.onDestroy();
    }
}
