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

public class List_DanhSach_Adapter extends BaseAdapter {
    private Context context;
    private List<PlayList> playLists;

    public List_DanhSach_Adapter(Context context, List<PlayList> playLists) {
        this.context = context;
        this.playLists = playLists;
    }

    @Override
    public int getCount() { return playLists.size(); }

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
        view = LayoutInflater.from(context).inflate(R.layout.view_list_danh_sach, null);

        TextView tv_view_listDanhSach = view.findViewById(R.id.tv_view_listDanhSach);
        ImageView img_view_listDanhSach = view.findViewById(R.id.img_view_listDanhSach);

        tv_view_listDanhSach.setText(playLists.get(i).getName());
        img_view_listDanhSach.setImageResource(R.drawable.ic_no_music);

        return view;
    }

}
