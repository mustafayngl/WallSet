package com.rhino.wallset;

public class Wallpaper {
    private int id;
    private String photographer;
    private String photographer_url;
    private String url;
    private Src src;
    private float[] embedding;

    // Yapıcı: id, photographer, photographer_url, url ve src alır
    public Wallpaper(int id, String photographer, String photographer_url, String url, Src src) {
        this.id = id;
        this.photographer = photographer;
        this.photographer_url = photographer_url;
        this.url = url;
        this.src = src;
    }

    // src nesnesinden orijinal URL'yi döndüren metot
    public String getUrl() {
        return src != null ? src.getOriginal() : url; // Eğer src null ise, direkt url'yi döndür
    }

    // Getter metotları
    public int getId() {
        return id;
    }

    public String getPhotographer() {
        return photographer;
    }

    public String getPhotographerUrl() {
        return photographer_url;
    }

    public Src getSrc() {
        return src;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    // Src sınıfı
    public static class Src {
        private String original;
        private String large;
        private String medium;
        private String small;

        // Getter metotları
        public String getOriginal() {
            return original;
        }

        public String getLarge() {
            return large;
        }

        public String getMedium() {
            return medium;
        }

        public String getSmall() {
            return small;
        }

        public void setOriginal(String original) {
            this.original = original;
        }

        public void setLarge(String large) {
            this.large = large;
        }

        public void setMedium(String medium) {
            this.medium = medium;
        }

        public void setSmall(String small) {
            this.small = small;
        }
    }
}
