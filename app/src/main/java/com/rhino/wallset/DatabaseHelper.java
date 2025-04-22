package com.rhino.wallset;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "wallpaper_favorites.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "favorites";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PHOTOGRAPHER = "photographer";
    private static final String COLUMN_PHOTOGRAPHER_URL = "photographer_url";
    private static final String COLUMN_URL = "image_url";
    private static final String COLUMN_SRC_ORIGINAL = "src_original";
    private static final String COLUMN_SRC_LARGE = "src_large";
    private static final String COLUMN_SRC_MEDIUM = "src_medium";
    private static final String COLUMN_SRC_SMALL = "src_small";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PHOTOGRAPHER + " TEXT,"
                + COLUMN_PHOTOGRAPHER_URL + " TEXT,"
                + COLUMN_URL + " TEXT UNIQUE,"
                + COLUMN_SRC_ORIGINAL + " TEXT,"
                + COLUMN_SRC_LARGE + " TEXT,"
                + COLUMN_SRC_MEDIUM + " TEXT,"
                + COLUMN_SRC_SMALL + " TEXT"
                + ")";
        db.execSQL(CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Favoriye ekle
    public boolean addToFavorites(Wallpaper wallpaper) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PHOTOGRAPHER, wallpaper.getPhotographer());
        values.put(COLUMN_PHOTOGRAPHER_URL, wallpaper.getPhotographerUrl());
        values.put(COLUMN_URL, wallpaper.getUrl());
        values.put(COLUMN_SRC_ORIGINAL, wallpaper.getSrc().getOriginal());
        values.put(COLUMN_SRC_LARGE, wallpaper.getSrc().getLarge());
        values.put(COLUMN_SRC_MEDIUM, wallpaper.getSrc().getMedium());
        values.put(COLUMN_SRC_SMALL, wallpaper.getSrc().getSmall());

        long result = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return result != -1;
    }

    // Favoriden çıkar
    public boolean removeFromFavorites(String imageUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(TABLE_NAME, COLUMN_URL + "=?", new String[]{imageUrl});
        db.close();
        return deletedRows > 0;
    }

    // Favori kontrolü
    public boolean isFavorite(String imageUrl) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID},
                COLUMN_URL + "=?", new String[]{imageUrl},
                null, null, null);

        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }

    // Favorileri listele
    public List<Wallpaper> getFavorites() {
        List<Wallpaper> favorites = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_ID, COLUMN_PHOTOGRAPHER, COLUMN_PHOTOGRAPHER_URL, COLUMN_URL,
                        COLUMN_SRC_ORIGINAL, COLUMN_SRC_LARGE, COLUMN_SRC_MEDIUM, COLUMN_SRC_SMALL},
                null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                // Src nesnesini oluşturuyoruz
                Wallpaper.Src src = new Wallpaper.Src(); // Wallpaper.Src olarak erişilmeli
                src.setOriginal(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SRC_ORIGINAL)));
                src.setLarge(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SRC_LARGE)));
                src.setMedium(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SRC_MEDIUM)));
                src.setSmall(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SRC_SMALL)));

                // Wallpaper nesnesi oluşturuluyor
                Wallpaper wallpaper = new Wallpaper(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTOGRAPHER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTOGRAPHER_URL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL)),
                        src
                );

                favorites.add(wallpaper);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return favorites;
    }
}
