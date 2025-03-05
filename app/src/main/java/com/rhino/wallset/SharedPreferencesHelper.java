package com.rhino.wallset;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class SharedPreferencesHelper {

    private static final String PREF_NAME = "wallpaper_prefs";
    private static final String FAVORITES_KEY = "favorites";

    private SharedPreferences sharedPreferences;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Favoriye eklenen duvar kağıdını sakla
    public void addToFavorites(String imageUrl) {
        Set<String> favorites = new HashSet<>(sharedPreferences.getStringSet(FAVORITES_KEY, new HashSet<>()));

        if (!favorites.contains(imageUrl)) { // Eğer zaten favoride değilse ekle
            favorites.add(imageUrl);
            saveFavorites(favorites);
        }
    }

    // Favorilerden duvar kağıdını çıkar
    public void removeFromFavorites(String imageUrl) {
        Set<String> favorites = new HashSet<>(sharedPreferences.getStringSet(FAVORITES_KEY, new HashSet<>()));

        if (favorites.contains(imageUrl)) {
            favorites.remove(imageUrl);
            saveFavorites(favorites);
        }
    }

    // Resmin favorilere eklenip eklenmediğini kontrol et
    public boolean isFavorite(String imageUrl) {
        Set<String> favorites = sharedPreferences.getStringSet(FAVORITES_KEY, new HashSet<>());
        return favorites.contains(imageUrl);
    }

    // Favorilere eklenen duvar kağıtlarını al (String[] formatında)
    public String[] getFavorites() {
        Set<String> favorites = sharedPreferences.getStringSet(FAVORITES_KEY, new HashSet<>());
        return favorites.toArray(new String[0]); // List yerine String[] döndür
    }

    // Favori listesini güvenli şekilde SharedPreferences'e kaydet
    private void saveFavorites(Set<String> favorites) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(FAVORITES_KEY, new HashSet<>(favorites)); // Yeni Set oluşturup kaydet
        editor.apply();
    }
}
