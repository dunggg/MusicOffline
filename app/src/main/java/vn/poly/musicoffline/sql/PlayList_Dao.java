package vn.poly.musicoffline.sql;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import vn.poly.musicoffline.MainActivity;
import vn.poly.musicoffline.model.Music;
import vn.poly.musicoffline.model.PlayList;

public class PlayList_Dao {
    Context context;
    private final Uri EXTERNAL_CONTENT_URI = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;

    public PlayList_Dao(Context context) {
        this.context = context;
    }

    // lấy all danh sách phát
    public List<PlayList> getAllPlayList() {
        List<PlayList> playLists = new ArrayList<>();
        String projection[] = {
                MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME
        };
        Cursor cursor = context.getContentResolver().query(EXTERNAL_CONTENT_URI, projection, null, null, null);
        int b = cursor.getCount();
        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            String name = cursor.getString(1);
            playLists.add(new PlayList(id, name));
        }
        cursor.close();
        return playLists;
    }

    // insert playlist
    public void insertPlayList(Long id, String name) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists._ID, id);
        values.put(MediaStore.Audio.Playlists.NAME, name);
        context.getContentResolver().insert(EXTERNAL_CONTENT_URI, values);
    }

    // insert bài hát vào playlis theo id playlist
    public void addTrackToPlaylist(Context context, String audio_id,
                                   long playlist_id) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlist_id);
        Cursor cursor = resolver.query(uri, new String[]{"count(*)"}, null, null, null);
        cursor.moveToFirst();
        int last = cursor.getInt(0);
        cursor.close();
        ContentValues value = new ContentValues();
        // play_order thứ tự của bài hát trong danh sách
        value.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, ++last);
        value.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audio_id);

        resolver.insert(uri, value);
    }

    // lấy tất cả bài hát nằm trong list
    public List<Music> getAllSongInPlayList(String idPlayList) {
        List<Music> musicList = new ArrayList<>();
        String projection[] = {
                MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.Audio.Playlists.Members._ID
        };
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Playlists.Members.getContentUri(
                "external", Long.parseLong(idPlayList)), projection, null, null, null);
        String audio_id;
        String idMemberPlayList;
        while (cursor.moveToNext()) {
            audio_id = cursor.getString(0);
            idMemberPlayList = cursor.getString(1);

            // lấy bài hát trùng với bài hát đã lưu vào playlist
            // danh sách những cột cần lấy dữ liệu
            String projection1[] = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST
            };

            // con trỏ truy cập vào file nhạc lấy bài hát theo audio id
            Cursor cursorMusic = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection1, MediaStore.Audio.Media._ID + "=?", new String[]{audio_id}, null);

            cursorMusic.moveToFirst();
            // vòng lặp sẽ dừng khi hết bản ghi
            if (cursorMusic.getCount() != 0) {
                String id = cursor.getString(0);
                String uri = cursorMusic.getString(1);
                String title = cursorMusic.getString(2);
                String artist = cursorMusic.getString(3);
                int duration = MediaPlayer.create(context, Uri.parse(uri)).getDuration();
                musicList.add(new Music(id, uri, title, artist, convertDuration(duration), idMemberPlayList));
            }
            // đóng con trỏ
            cursorMusic.close();
        }
        cursor.close();
        return musicList;
    }

    // xóa bài hát khỏi playlist
    public void deletSongInPlayList(String idMember, String idPlayList) {
        context.getContentResolver().delete(MediaStore.Audio.Playlists.Members.getContentUri(
                "external", Long.parseLong(idPlayList)), MediaStore.Audio.Playlists.Members._ID + "=?", new String[]{idMember});
    }

    // xóa playlist theo id
    public void deletePlayList(String idPlayList) {
        context.getContentResolver().delete(EXTERNAL_CONTENT_URI, MediaStore.Audio.Playlists._ID + "=?", new String[]{idPlayList});
    }

    // update PlayList
    public void updatePlayList(String idPlayList, String name) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.NAME, name);
        context.getContentResolver().update(EXTERNAL_CONTENT_URI, values, MediaStore.Audio.Playlists._ID + "=?", new String[]{idPlayList});
    }

    // kiểm tra xem tên của playlist đã tồn tại hay chưa
    public boolean checkNamePlayList(String name) {
        Cursor cursor = context.getContentResolver().query(EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Playlists.NAME + "=?", new String[]{name}, null);
        if (cursor.getCount() == 0) {
            // nếu mà tên chưa tồn tại return true
            cursor.close();
            return true;
        }
        // ngược lại return false
        cursor.close();
        return false;
    }

    // kiểm tra xem tên và id của playlist đã tồn tại hay chưa
    public boolean checkPlayList(String name, String id) {
        Cursor cursor = context.getContentResolver().query(EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Playlists.NAME + "=? or " + MediaStore.Audio.Playlists._ID + "=?", new String[]{name, id}, null);
        if (cursor.getCount() == 0) {
            // nếu mà tên và id chưa tồn tại return true
            cursor.close();
            return true;
        }
        // nếu tên hoặc id đã tồn tại return false
        cursor.close();
        return false;
    }

    // chuyển đổi thời gian của bài hát vì mặc định thời gian của bài hát là kiểu int
    private String convertDuration(int duration) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        return simpleDateFormat.format(duration);
    }

}
