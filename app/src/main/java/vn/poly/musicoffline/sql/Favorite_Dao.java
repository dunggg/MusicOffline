package vn.poly.musicoffline.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import vn.poly.musicoffline.model.Music;

public class Favorite_Dao {
    Context context;

    public Favorite_Dao(Context context) {
        this.context = context;
    }

    // truyền vào string true hoặc false và id của bài hát
    public void update(String id, int isFavorite) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.IS_MUSIC, isFavorite);
        context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, MediaStore.Audio.Media._ID + "=?", new String[]{id});
    }

    // lấy tất cả bài hát trong yêu thích
    public List<Music> getAllSongInFavorite() {
        List<Music> musicList = new ArrayList<>();
        String projection[] = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST};

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.IS_MUSIC + "=?", new String[]{"2"}, null);
        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            String uri = cursor.getString(1);
            String title = cursor.getString(2);
            String artist = cursor.getString(3);
            int duration = MediaPlayer.create(context, Uri.parse(uri)).getDuration();
            musicList.add(new Music(id, uri, title, artist, convertDuration(duration)));
        }
        cursor.close();
        return musicList;
    }

    public boolean searchSongFavorite(String id) {
        // tìm kiếm bài hát theo id và đang nằm trong danh sách favorite
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Audio.Media._ID + "=? and " + MediaStore.Audio.Media.IS_MUSIC + "=?", new String[]{id, "2"}, null);
        if (cursor.getCount() != 0) {
            // nếu bài hát nằm trong favorite thì return về true
            cursor.close();
            return true;
        }
        // ngược lại return về false
        cursor.close();
        return false;
    }

    // chuyển đổi thời gian của bài hát vì mặc định thời gian của bài hát là kiểu int
    private String convertDuration(int duration) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        return simpleDateFormat.format(duration);
    }

}
