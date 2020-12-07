package vn.poly.musicoffline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import vn.poly.musicoffline.fragment.Music_Fragment;
import vn.poly.musicoffline.model.Music;

import java.util.ArrayList;
import java.util.List;

public class MyBroadcastReceiver extends BroadcastReceiver {

    public static final String RECEIVER_ACTION = "buttonclick";

    @Override
    public void onReceive(Context context, Intent intent) {
        // lấy giá trị của intent bắn ra
        // list xem đang phát ở  list nào
        List<Music> musicList = new ArrayList<>();
        musicList = MainActivity.checkListMusic;

        int id = intent.getIntExtra("idbutton", -1);
        switch (id) {

            case R.id.img_notificationPause:
                // ấn vào nút pause thì dừng
                MainActivity.playerMusicService.pause();
                break;

            case R.id.img_notificationPrev:
                // ấn vào nút prev
                // lấy vị trí của bài hát trừ đi 1
                Music_Fragment.positionBaiHat--;
                if (Music_Fragment.positionBaiHat < 0) {
                    Music_Fragment.positionBaiHat = musicList.size() - 1;
                }
                // truyền bài hát cần chạy
                MainActivity.playerMusicService.play(musicList.get(Music_Fragment.positionBaiHat),musicList);
                break;

            case R.id.img_notificationNext:
                // ấn vào button next
                // lấy vị trí của bài hát cộng 1
                Music_Fragment.positionBaiHat++;
                if (Music_Fragment.positionBaiHat == musicList.size()) {
                    Music_Fragment.positionBaiHat = 0;
                }
                // truyền bài hát cần chạy
                MainActivity.playerMusicService.play(musicList.get(Music_Fragment.positionBaiHat),musicList);
                break;

            case R.id.img_notificationClose:
                // khi nhấn vào dấu x trên thông báo
                MainActivity.playerMusicService.stop();
                break;

        }

    }
}
