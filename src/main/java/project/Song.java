package project;

class Song {
    private String id;
    private String name;
    private String artist;
    private String previewUrl;
    private String imageUrl;
    private long duration;
    private String albumName;
    private boolean isLiked;


    public Song() {

    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getArtist() { return artist; }
    public String getPreviewUrl() { return previewUrl; }
    public String getImageUrl() { return imageUrl; }
    public long getDuration() { return duration; }
    public String getAlbumName() { return albumName; }
    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { this.isLiked = liked; }

    public String getDurationFormatted() {
        long minutes = duration / 60000;
        long seconds = (duration % 60000) / 1000;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public String toString() {
        return artist + " - " + name + " (" + getDurationFormatted() + ")";
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }
}

