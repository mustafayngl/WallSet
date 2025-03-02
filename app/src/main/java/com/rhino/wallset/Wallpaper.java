package com.rhino.wallset;

public class Wallpaper {
    private int id;
    private String photographer;
    private String photographer_url;
    private String url;
    private Src src;

    // Yapıcı, url parametresi alır
    public Wallpaper(String url) {
        this.url = url;
    }

    // src nesnesinden orijinal URL'yi döndüren metot
    public String getUrl() {
        // src nesnesi null ise url değerini döndür
        return src != null ? src.getOriginal() : url;  // Eğer src null ise, direkt url'yi döndür
    }

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

    // Src sınıfı
    public static class Src {
        private String original;
        private String large;
        private String medium;
        private String small;

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
    }
}
