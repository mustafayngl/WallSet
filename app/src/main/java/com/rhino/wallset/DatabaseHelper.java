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
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_NAME = "favorites";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PHOTOGRAPHER = "photographer";
    private static final String COLUMN_PHOTOGRAPHER_URL = "photographer_url";
    private static final String COLUMN_URL = "image_url";
    private static final String COLUMN_SRC_ORIGINAL = "src_original";
    private static final String COLUMN_SRC_LARGE = "src_large";
    private static final String COLUMN_SRC_MEDIUM = "src_medium";
    private static final String COLUMN_SRC_SMALL = "src_small";
    private static final String COLUMN_EMBEDDING = "embedding";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PHOTOGRAPHER + " TEXT,"
                + COLUMN_PHOTOGRAPHER_URL + " TEXT,"
                + COLUMN_URL + " TEXT UNIQUE,"
                + COLUMN_SRC_ORIGINAL + " TEXT,"
                + COLUMN_SRC_LARGE + " TEXT,"
                + COLUMN_SRC_MEDIUM + " TEXT,"
                + COLUMN_SRC_SMALL + " TEXT,"
                + COLUMN_EMBEDDING + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addToFavorites(Wallpaper wallpaper) {
        return addToFavoritesWithEmbedding(wallpaper, null);
    }

    public boolean addToFavoritesWithEmbedding(Wallpaper wallpaper, float[] embedding) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PHOTOGRAPHER, wallpaper.getPhotographer());
        values.put(COLUMN_PHOTOGRAPHER_URL, wallpaper.getPhotographerUrl());
        values.put(COLUMN_URL, wallpaper.getUrl());
        values.put(COLUMN_SRC_ORIGINAL, wallpaper.getSrc().getOriginal());
        values.put(COLUMN_SRC_LARGE, wallpaper.getSrc().getLarge());
        values.put(COLUMN_SRC_MEDIUM, wallpaper.getSrc().getMedium());
        values.put(COLUMN_SRC_SMALL, wallpaper.getSrc().getSmall());
        values.put(COLUMN_EMBEDDING, serializeEmbedding(embedding));
        long result = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return result != -1;
    }

    public boolean removeFromFavorites(String imageUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deleted = db.delete(TABLE_NAME, COLUMN_URL + "=?", new String[]{imageUrl});
        db.close();
        return deleted > 0;
    }

    public boolean isFavorite(String imageUrl) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID}, COLUMN_URL + "=?",
                new String[]{imageUrl}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public List<Wallpaper> getFavorites() {
        List<Wallpaper> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_ID, COLUMN_PHOTOGRAPHER, COLUMN_PHOTOGRAPHER_URL, COLUMN_URL,
                        COLUMN_SRC_ORIGINAL, COLUMN_SRC_LARGE, COLUMN_SRC_MEDIUM, COLUMN_SRC_SMALL},
                null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Wallpaper.Src src = new Wallpaper.Src();
                src.setOriginal(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SRC_ORIGINAL)));
                src.setLarge(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SRC_LARGE)));
                src.setMedium(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SRC_MEDIUM)));
                src.setSmall(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SRC_SMALL)));

                Wallpaper wallpaper = new Wallpaper(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTOGRAPHER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTOGRAPHER_URL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL)),
                        src
                );
                list.add(wallpaper);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    public float[] getEmbedding(String imageUrl) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_EMBEDDING}, COLUMN_URL + "=?",
                new String[]{imageUrl}, null, null, null);
        float[] result = null;
        if (cursor.moveToFirst()) {
            String embeddingStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMBEDDING));
            result = deserializeEmbedding(embeddingStr);
        }
        cursor.close();
        db.close();
        return result;
    }

    public float[] getEmbeddingForWallpaper(String imageUrl) {
        return getEmbedding(imageUrl);
    }

    private String serializeEmbedding(float[] embedding) {
        if (embedding == null || embedding.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) sb.append(",");
        }
        return sb.toString();
    }

    private float[] deserializeEmbedding(String str) {
        if (str == null || str.isEmpty()) return new float[0];
        String[] parts = str.split(",");
        float[] embedding = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                embedding[i] = Float.parseFloat(parts[i]);
            } catch (NumberFormatException e) {
                embedding[i] = 0f;
            }
        }
        return embedding;
    }
}
