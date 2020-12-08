package vn.poly.musicoffline.model;

public class Music {
    private String id, uri, title, artist, duration, idMemberPlayList;

    public Music(String id, String uri, String title, String artist, String duration) {
        this.id = id;
        this.uri = uri;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
    }

    public Music(String id, String uri, String title, String artist, String duration, String idMemberPlayList) {
        this.id = id;
        this.uri = uri;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.idMemberPlayList = idMemberPlayList;
    }

    public String getIdMemberPlayList() {
        return idMemberPlayList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
