package com.rhino.wallset;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class FullscreenActivity extends AppCompatActivity {

    private ImageButton btnFavorite;
    private ImageView imageView;
    private String wallpaperUrl;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        dbHelper = new DatabaseHelper(this);
        imageView = findViewById(R.id.fullscreenImageView);
        btnFavorite = findViewById(R.id.btnFavorite);
        wallpaperUrl = getIntent().getStringExtra("IMAGE_URL");

        if (wallpaperUrl == null || wallpaperUrl.isEmpty()) {
            Toast.makeText(this, "Invalid wallpaper URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Glide.with(this)
                .load(wallpaperUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(0.1f)
                .priority(Priority.HIGH)
                .override(800, 600)
                .into(imageView);

        updateFavoriteIcon();

        btnFavorite.setOnClickListener(v -> toggleFavorite());

        findViewById(R.id.btnSetWallpaper).setOnClickListener(v -> showSetWallpaperDialog());
    }

    private void updateFavoriteIcon() {
        boolean isFavorite = dbHelper.isFavorite(wallpaperUrl);
        btnFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        btnFavorite.setContentDescription(getString(isFavorite ? R.string.remove_from_favorites : R.string.add_to_favorites));
    }

    private void toggleFavorite() {
        Wallpaper wallpaper = new Wallpaper(0, "", "", wallpaperUrl, new Wallpaper.Src());
        boolean isFavorite = dbHelper.isFavorite(wallpaperUrl);

        if (isFavorite) {
            dbHelper.removeFromFavorites(wallpaperUrl);
            Toast.makeText(this, getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
            updateFavoriteIcon();
        } else {
            CohereEmbeddingHelper.getEmbedding(wallpaperUrl, new CohereEmbeddingHelper.EmbeddingCallback() {
                @Override
                public void onSuccess(float[] embedding) {
                    dbHelper.addToFavoritesWithEmbedding(wallpaper, embedding);
                    runOnUiThread(() -> {
                        Toast.makeText(FullscreenActivity.this, getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
                        updateFavoriteIcon();
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() ->
                            Toast.makeText(FullscreenActivity.this, "Failed to add with AI: " + error, Toast.LENGTH_SHORT).show()
                    );
                }
            });
        }
    }

    private void showSetWallpaperDialog() {
        WallpaperOptionFragment fragment = WallpaperOptionFragment.newInstance(wallpaperUrl);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.show(transaction, "WallpaperOptionFragment");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Glide.with(this).clear(imageView);
    }
}
