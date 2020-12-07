package vn.poly.musicoffline.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import vn.poly.musicoffline.MainActivity;
import vn.poly.musicoffline.adapter.NgheSi_Adapter;
import vn.poly.musicoffline.manage.NgheSi_Activity;
import vn.poly.musicoffline.model.Music;
import vn.poly.musicoffline.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NgheSi_Fragment extends Fragment {
    ListView lv_frag_ngheSi;
    List<String> ngheSiList;
    NgheSi_Adapter ngheSi_adapter;

    public NgheSi_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nghe_si_, container, false);

        //ánh xạ
        lv_frag_ngheSi = view.findViewById(R.id.lv_frag_ngheSi);

        // load data trên 1 luồng khác
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                ngheSiList = new ArrayList<>();
                getAllNgheSi();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                ngheSi_adapter = new NgheSi_Adapter(getContext(), ngheSiList);
                lv_frag_ngheSi.setAdapter(ngheSi_adapter);
            }
        }.execute();

        lv_frag_ngheSi.setOnItemClickListener((adapterView, view1, i, l) -> {
            Intent intent = new Intent(getContext(), NgheSi_Activity.class);
            intent.putExtra("artist", ngheSiList.get(i));
            startActivity(intent);
        });

        return view;
    }

    private void getAllNgheSi() {
        Set<String> ngheSiSet = new HashSet<>();
        for (Music music : MainActivity.listSong) {
            ngheSiSet.add(music.getArtist());
        }
        ngheSiList.addAll(ngheSiSet);
    }

}