package com.rhino.wallset;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder> {

    private final Context context;
    private final List<Wallpaper> wallpaperList;
    private final OnWallpaperClickListener wallpaperListener;
    private final OnRemoveClickListener removeListener;
    private final DatabaseHelper dbHelper;

    public WallpaperAdapter(Context context, List<Wallpaper> wallpaperList,
                            OnWallpaperClickListener wallpaperListener,
                            OnRemoveClickListener removeListener) {
        this.context = context;
        this.wallpaperList = wallpaperList;
        this.wallpaperListener = wallpaperListener;
        this.removeListener = removeListener;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public WallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wallpaper, parent, false);
        return new WallpaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WallpaperViewHolder holder, int position) {
        Wallpaper wallpaper = wallpaperList.get(position);

        Glide.with(context)
                .load(wallpaper.getUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.imageView);

        holder.imageView.setOnClickListener(v -> wallpaperListener.onWallpaperClick(wallpaper));

        updateFavoriteIcon(holder, wallpaper);

        holder.favoriteIcon.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;

            if (context instanceof MainActivity) {
                boolean isCurrentlyFavorite = dbHelper.isFavorite(wallpaper.getUrl());

                if (isCurrentlyFavorite) {
                    dbHelper.removeFromFavorites(wallpaper.getUrl());
                    runOnUiThreadSafe(() -> {
                        Toast.makeText(context, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
                        notifyItemChanged(adapterPosition);
                    });
                } else {
                    CohereEmbeddingHelper.getEmbedding(wallpaper.getUrl(), new CohereEmbeddingHelper.EmbeddingCallback() {
                        @Override
                        public void onSuccess(float[] embedding) {
                            dbHelper.addToFavoritesWithEmbedding(wallpaper, embedding);
                            runOnUiThreadSafe(() -> {
                                Toast.makeText(context, R.string.added_to_favorites, Toast.LENGTH_SHORT).show();
                                notifyItemChanged(adapterPosition);
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThreadSafe(() ->
                                    Toast.makeText(context, "Embedding error: " + error, Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                }

            } else if (context instanceof FavoritesActivity) {
                removeListener.onRemoveClick(wallpaper);
                runOnUiThreadSafe(() -> {
                    Toast.makeText(context, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
                    notifyItemRemoved(adapterPosition);
                });
            }
        });
    }

    private void updateFavoriteIcon(@NonNull WallpaperViewHolder holder, Wallpaper wallpaper) {
        boolean isFavorite = dbHelper.isFavorite(wallpaper.getUrl());
        holder.favoriteIcon.setImageResource(
                isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline
        );
    }

    private void runOnUiThreadSafe(Runnable action) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(action);
        } else {
            action.run(); // fallback
        }
    }

    @Override
    public int getItemCount() {
        return wallpaperList.size();
    }

    public static class WallpaperViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView favoriteIcon;

        public WallpaperViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.wallpaperImageView);
            favoriteIcon = itemView.findViewById(R.id.favoriteHeartIcon);
        }
    }

    public interface OnWallpaperClickListener {
        void onWallpaperClick(Wallpaper wallpaper);
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(Wallpaper wallpaper);
    }
}
