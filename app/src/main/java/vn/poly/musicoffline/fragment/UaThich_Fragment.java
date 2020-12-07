package vn.poly.musicoffline.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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

        musicList = favorite_dao.getAllSongInFavorite();
        Music_Adapter music_adapter = new Music_Adapter(musicList, getContext(), R.layout.view_music_nghesi);
        lv_uaThich.setAdapter(music_adapter);

        tv_soLuong_uaThich.setText(musicList.size()+" bài hát");

        return view;
    }
}