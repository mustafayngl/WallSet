package com.rhino.wallset;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 sütunlu grid

        // Retrofit ile API'ye bağlanalım
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiService apiService = retrofit.create(ApiService.class);

        // API'yi çağırıyoruz
        Call<WallpaperResponse> call = apiService.getWallpapers(1, 10); // Sayfa 1 ve 10 duvar kağıdı isteği
        call.enqueue(new Callback<WallpaperResponse>() {
            @Override
            public void onResponse(@NonNull Call<WallpaperResponse> call, @NonNull Response<WallpaperResponse> response) {
                if (response.body() != null && response.body().getPhotos() != null) {
                    // API cevabını loglayalım
                    Log.d("Wallpapers", "API'den gelen cevap: " + response.body().toString());

                    List<Wallpaper> wallpapers = response.body().getPhotos();

                    // Duvar kağıtlarının sayısını log'la yazdıralım
                    Log.d("Wallpapers", "Duvar Kağıtları: " + wallpapers.size());

                    // Eğer veriler boşsa, bir log mesajı ekleyelim
                    if (wallpapers.isEmpty()) {
                        Log.d("Wallpapers", "Photos listesi boş.");
                        Toast.makeText(MainActivity.this, "Hiç duvar kağıdı bulunamadı!", Toast.LENGTH_SHORT).show();
                        return;
                    }

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
                    Log.e("Wallpapers", "API Hatası: " + response.code() + " - " + response.message());
                    Toast.makeText(MainActivity.this, "API Hatası", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WallpaperResponse> call, @NonNull Throwable t) {
                // Bağlantı hatası durumunda log ekleyelim
                Log.e("Wallpapers", "Bağlantı Hatası: " + t.getMessage());
                t.printStackTrace(); // Daha ayrıntılı hata bilgisi için stack trace ekleyelim
                Toast.makeText(MainActivity.this, "Bağlantı Hatası", Toast.LENGTH_SHORT).show();
            }
        });

        // Favoriler butonunu ekleyelim
        Button favoritesButton = findViewById(R.id.favoritesButton);
        favoritesButton.setOnClickListener(v -> {
            // Favoriler ekranına geçiş yapalım
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });
    }
}
