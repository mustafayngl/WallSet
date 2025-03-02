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
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> favorites = sharedPreferences.getStringSet(FAVORITES_KEY, new HashSet<>());

        favorites.add(imageUrl); // URL'yi favorilere ekle
        editor.putStringSet(FAVORITES_KEY, favorites);
        editor.apply();
    }

    // Favorilerden duvar kağıdını çıkar
    public void removeFromFavorites(String imageUrl) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> favorites = sharedPreferences.getStringSet(FAVORITES_KEY, new HashSet<>());

        favorites.remove(imageUrl); // URL'yi favorilerden çıkar
        editor.putStringSet(FAVORITES_KEY, favorites);
        editor.apply();
    }

    // Resmin favorilere eklenip eklenmediğini kontrol et
    public boolean isFavorite(String imageUrl) {
        Set<String> favorites = sharedPreferences.getStringSet(FAVORITES_KEY, new HashSet<>());
        return favorites.contains(imageUrl); // URL favorilerde varsa true döndür
    }

    // Favorilere eklenen duvar kağıtlarını al
    public String[] getFavorites() {
        Set<String> favorites = sharedPreferences.getStringSet(FAVORITES_KEY, new HashSet<>());
        return favorites.toArray(new String[0]);
    }
}
