package vn.poly.musicoffline.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.TextView;

import vn.poly.musicoffline.adapter.Music_Adapter;
import vn.poly.musicoffline.MainActivity;
import vn.poly.musicoffline.model.Music;
import vn.poly.musicoffline.R;

public class Music_Fragment extends Fragment {
    TextView tv_soLuong_music;
    ListView lv_frag_music;
    Music_Adapter musicAdapter;

    // biến để lưu vị trí của bài hát
    public static int positionBaiHat = -1;

    public Music_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music_, container, false);

        //ánh xạ
        tv_soLuong_music = view.findViewById(R.id.tv_soLuong_music);
        lv_frag_music = view.findViewById(R.id.lv_frag_music);

        tv_soLuong_music.setText(MainActivity.listSong.size() + " bài hát");

        // set adapter vào listview
        musicAdapter = new Music_Adapter(MainActivity.listSong, getContext());
        lv_frag_music.setAdapter(musicAdapter);

        lv_frag_music.setOnItemClickListener((parent, view1, position, id) -> {
            // nếu đang random thì tắt đi
            MainActivity.playerMusicService.checkRandom = false;
            Music music = MainActivity.listSong.get(position);
            positionBaiHat = position;
            MainActivity.playerMusicService.play(music, MainActivity.listSong);
            MainActivity.checkListMusic = MainActivity.listSong;
        });

        return view;
    }

}