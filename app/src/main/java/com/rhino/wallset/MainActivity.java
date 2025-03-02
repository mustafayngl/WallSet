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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WallpaperAdapter wallpaperAdapter;
    private List<Wallpaper> wallpapers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar'ı ayarlıyoruz
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Toolbar'ı ActionBar olarak set ediyoruz

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 sütunlu grid

        loadWallpapers(""); // Uygulama açıldığında duvar kağıtlarını yükleyelim
    }

    // Arama fonksiyonu
    private void loadWallpapers(String query) {
        Random random = new Random();
        int page = random.nextInt(100) + 1; // 1-100 arasında rastgele bir sayfa numarası

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

                    wallpaperAdapter = new WallpaperAdapter(MainActivity.this, wallpapers,
                            wallpaper -> {
                                Intent intent = new Intent(MainActivity.this, FullscreenActivity.class);
                                intent.putExtra("IMAGE_URL", wallpaper.getUrl());
                                startActivity(intent);
                            },
                            wallpaper -> {
                                SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(MainActivity.this);
                                sharedPreferencesHelper.removeFromFavorites(wallpaper.getUrl());
                                Toast.makeText(MainActivity.this, "Favorilerden çıkarıldı!", Toast.LENGTH_SHORT).show();
                            });

                    recyclerView.setAdapter(wallpaperAdapter);
                } else {
                    Toast.makeText(MainActivity.this, "API Hatası", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WallpaperResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Bağlantı Hatası", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

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
        } else {
            Toast.makeText(this, "Arama butonu bulunamadı!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Yenileniyor...", Toast.LENGTH_SHORT).show();
                loadWallpapers("");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
