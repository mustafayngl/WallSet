package com.rhino.wallset;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
import retrofit2.Retrofit;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatDelegate;


public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WallpaperAdapter wallpaperAdapter;
    private List<Wallpaper> wallpapers;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // RecyclerView setup
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);

        executorService = Executors.newSingleThreadExecutor(); // Run API calls in background

        loadWallpapers("");
    }

    // Function to load wallpapers from the API
    private void loadWallpapers(String query) {
        executorService.execute(() -> {
            Random random = new Random();
            int page = random.nextInt(100) + 1;

            Retrofit retrofit = RetrofitClient.getRetrofitInstance();
            ApiService apiService = retrofit.create(ApiService.class);

            Call<WallpaperResponse> call = query.isEmpty()
                    ? apiService.getWallpapers(page, 10)
                    : apiService.searchWallpapers(query, page, 10);

            call.enqueue(new Callback<WallpaperResponse>() {
                @Override
                public void onResponse(Call<WallpaperResponse> call, Response<WallpaperResponse> response) {
                    if (response.body() != null && response.body().getPhotos() != null) {
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
                                        new Thread(() -> {
                                            DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
                                            dbHelper.removeFromFavorites(wallpaper.getUrl());

                                            runOnUiThread(() ->
                                                    Toast.makeText(MainActivity.this, R.string.favorites_removed, Toast.LENGTH_SHORT).show()
                                            );
                                        }).start();
                                    }
                            );
                            recyclerView.setAdapter(wallpaperAdapter);
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show());
                    }
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

        // Setup search
        MenuItem searchItem = menu.findItem(R.id.searchView);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
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
        switch (item.getItemId()) {
            case R.id.action_favorites:
                startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
                return true;
            case R.id.action_refresh:
                Toast.makeText(this, R.string.refreshing, Toast.LENGTH_SHORT).show();
                loadWallpapers("");
                return true;
            case R.id.action_toggle_theme:
                SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
                boolean isDarkMode = prefs.getBoolean("dark_mode", false);

                boolean newMode = !isDarkMode;
                prefs.edit().putBoolean("dark_mode", newMode).apply();

                AppCompatDelegate.setDefaultNightMode(
                        newMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onDestroy() {
        executorService.shutdown(); // Release threads
        super.onDestroy();
    }
}
