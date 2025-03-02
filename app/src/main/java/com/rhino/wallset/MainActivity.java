package com.rhino.wallset;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WallpaperAdapter wallpaperAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar'ı ayarlıyoruz
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Toolbar'ı ActionBar olarak set ediyoruz

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 sütunlu grid

        // Retrofit ile API'ye bağlanalım
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiService apiService = retrofit.create(ApiService.class);

        // API'yi çağırıyoruz
        Call<WallpaperResponse> call = apiService.getWallpapers(1, 10); // Sayfa 1 ve 10 duvar kağıdı isteği
        call.enqueue(new Callback<WallpaperResponse>() {
            @Override
            public void onResponse(Call<WallpaperResponse> call, Response<WallpaperResponse> response) {
                if (response.body() != null && response.body().getPhotos() != null) {
                    List<Wallpaper> wallpapers = response.body().getPhotos();

                    // RecyclerView Adapter'ini ayarla
                    wallpaperAdapter = new WallpaperAdapter(MainActivity.this, wallpapers,
                            wallpaper -> {
                                // Kullanıcı bir duvar kağıdına tıklarsa FullscreenActivity aç
                                Intent intent = new Intent(MainActivity.this, FullscreenActivity.class);
                                intent.putExtra("IMAGE_URL", wallpaper.getUrl());
                                startActivity(intent);
                            },
                            wallpaper -> {
                                // Favorilerden çıkarma işlemi
                                SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(MainActivity.this);
                                sharedPreferencesHelper.removeFromFavorites(wallpaper.getUrl());
                                Toast.makeText(MainActivity.this, "Favorilerden çıkarıldı!", Toast.LENGTH_SHORT).show();
                            });

                    recyclerView.setAdapter(wallpaperAdapter); // Adapter’i RecyclerView’e bağla

                } else {
                    // API başarısızsa hata mesajı göster
                    Toast.makeText(MainActivity.this, "API Hatası", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WallpaperResponse> call, Throwable t) {
                // Bağlantı hatası durumunda log ekleyelim
                Toast.makeText(MainActivity.this, "Bağlantı Hatası", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Menü öğelerini ekle
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Menü öğeleri için tıklama işlemi
        switch (item.getItemId()) {
            case R.id.action_favorites:
                // Favoriler butonuna tıklanmış
                Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_refresh:
                // Yenile butonuna tıklanmış
                Toast.makeText(this, "Yeniliyor...", Toast.LENGTH_SHORT).show();
                // Yenileme işlemi yapılacak
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
