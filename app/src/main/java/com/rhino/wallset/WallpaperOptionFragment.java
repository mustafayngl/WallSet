package com.rhino.wallset;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;

public class WallpaperOptionFragment extends BottomSheetDialogFragment {

    private String wallpaperUrl;
    private ImageView previewImageView;

    public static WallpaperOptionFragment newInstance(String wallpaperUrl) {
        WallpaperOptionFragment fragment = new WallpaperOptionFragment();
        Bundle args = new Bundle();
        args.putString("WALLPAPER_URL", wallpaperUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallpaper_options, container, false);

        // Duvar kağıdı URL'sini al
        wallpaperUrl = getArguments().getString("WALLPAPER_URL");

        // Preview görseli
        previewImageView = view.findViewById(R.id.previewImageView);
        Glide.with(getContext())
                .load(wallpaperUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(previewImageView);

        // Seçenekler için butonlar
        Button btnHomeScreen = view.findViewById(R.id.btnHomeScreen);
        Button btnLockScreen = view.findViewById(R.id.btnLockScreen);
        Button btnBoth = view.findViewById(R.id.btnBoth);

        // Ana ekran için ayarlama
        btnHomeScreen.setOnClickListener(v -> setWallpaper(WallpaperManager.FLAG_SYSTEM));

        // Kilit ekranı için ayarlama
        btnLockScreen.setOnClickListener(v -> setWallpaper(WallpaperManager.FLAG_LOCK));

        // Her ikisi için de ayarlama
        btnBoth.setOnClickListener(v -> {
            setWallpaper(WallpaperManager.FLAG_SYSTEM);
            setWallpaper(WallpaperManager.FLAG_LOCK);
        });

        return view;
    }

    private void setWallpaper(int flag) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());

        Glide.with(getContext())
                .asBitmap()
                .load(wallpaperUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        try {
                            wallpaperManager.setBitmap(resource, null, false, flag);
                            Toast.makeText(getContext(), "Duvar kağıdı ayarlandı!", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(getContext(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }
}
