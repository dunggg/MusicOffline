package vn.poly.musicoffline.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import vn.poly.musicoffline.R;
import vn.poly.musicoffline.model.PlayList;

public class DanhSach_Adapter_DiaLog extends BaseAdapter {
    private Context context;
    private List<PlayList> playLists;

    public DanhSach_Adapter_DiaLog(Context context, List<PlayList> playLists) {
        this.context = context;
        this.playLists = playLists;
    }

    @Override
    public int getCount() {
        return playLists.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.test_view, null);

        TextView tv_test = view.findViewById(R.id.tv_test);
        ImageView img_test = view.findViewById(R.id.img_test);

        tv_test.setText(playLists.get(i).getName());
        img_test.setImageResource(R.drawable.ic_no_music);

        return view;
    }

}
