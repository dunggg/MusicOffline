package vn.poly.musicoffline.fragment;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import vn.poly.musicoffline.MainActivity;
import vn.poly.musicoffline.R;
import vn.poly.musicoffline.adapter.Music_Adapter;
import vn.poly.musicoffline.model.Music;
import vn.poly.musicoffline.sql.Favorite_Dao;

public class UaThich_Fragment extends Fragment {
    TextView tv_soLuong_uaThich;
    ListView lv_uaThich;
    Favorite_Dao favorite_dao;
    List<Music> musicList;

    public UaThich_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ua_thich_, container, false);

        //ánh xạ
        tv_soLuong_uaThich = view.findViewById(R.id.tv_soLuong_uaThich);
        lv_uaThich = view.findViewById(R.id.lv_uaThich);

        //list
        musicList = new ArrayList<>();
        favorite_dao = new Favorite_Dao(getContext());

        // truy vấn trong nền và đổ giao diện ra
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                musicList = favorite_dao.getAllSongInFavorite();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                Music_Adapter music_adapter = new Music_Adapter(musicList, getContext());
                lv_uaThich.setAdapter(music_adapter);
            }
        }.execute();


        lv_uaThich.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.checkListMusic = musicList;
                Music_Fragment.positionBaiHat = position;
                MainActivity.playerMusicService.play(musicList.get(position), musicList);
            }
        });

        tv_soLuong_uaThich.setText(musicList.size() + " bài hát");

        return view;
    }
}