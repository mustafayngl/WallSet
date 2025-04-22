package com.rhino.wallset;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder> {
    private Context context;
    private List<Wallpaper> wallpaperList;
    private OnWallpaperClickListener wallpaperListener;
    private OnRemoveClickListener removeListener;
    private DatabaseHelper dbHelper;

    public WallpaperAdapter(Context context, List<Wallpaper> wallpaperList, OnWallpaperClickListener wallpaperListener, OnRemoveClickListener removeListener) {
        this.context = context;
        this.wallpaperList = wallpaperList;
        this.wallpaperListener = wallpaperListener;
        this.removeListener = removeListener;
        dbHelper = new DatabaseHelper(context); // DatabaseHelper initialization
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

        boolean isFavorite = dbHelper.isFavorite(wallpaper.getUrl()); // Using DatabaseHelper

        if (context instanceof MainActivity) {
            holder.favoriteButton.setVisibility(View.VISIBLE); // "Favoriye Ekle" button visible
            holder.removeButton.setVisibility(View.GONE); // "Favorilerden Çıkar" button hidden

            holder.favoriteButton.setText(isFavorite ? "Favorilerden Çıkar" : "Favorilere Ekle");

            holder.favoriteButton.setOnClickListener(v -> {
                if (isFavorite) {
                    dbHelper.removeFromFavorites(wallpaper.getUrl());
                    holder.favoriteButton.setText("Favorilere Ekle"); // Update UI immediately
                    Toast.makeText(context, "Favorilerden çıkarıldı!", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.addToFavorites(wallpaper); // Adding Wallpaper object directly
                    holder.favoriteButton.setText("Favorilerden Çıkar"); // Update UI immediately
                    Toast.makeText(context, "Favorilere eklendi!", Toast.LENGTH_SHORT).show();
                }

                // Notify the adapter to update the list
                notifyItemChanged(position); // Refresh UI for this particular item
            });
        } else if (context instanceof FavoritesActivity) {
            holder.favoriteButton.setVisibility(View.GONE); // "Favoriye Ekle" button hidden
            holder.removeButton.setVisibility(View.VISIBLE); // "Favorilerden Çıkar" button visible

            holder.removeButton.setOnClickListener(v -> {
                removeListener.onRemoveClick(wallpaper);
                Toast.makeText(context, "Favorilerden çıkarıldı!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return wallpaperList.size();
    }

    public static class WallpaperViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        Button favoriteButton; // Favorilere ekle/çıkart button
        Button removeButton; // Favorilerden çıkar button

        public WallpaperViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.wallpaperImageView);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            removeButton = itemView.findViewById(R.id.removeFromFavoritesButton);
        }
    }

    public interface OnWallpaperClickListener {
        void onWallpaperClick(Wallpaper wallpaper);
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(Wallpaper wallpaper);
    }
}
