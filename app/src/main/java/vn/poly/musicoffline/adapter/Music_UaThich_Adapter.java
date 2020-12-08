package vn.poly.musicoffline.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.List;

import vn.poly.musicoffline.R;
import vn.poly.musicoffline.model.Music;
import vn.poly.musicoffline.model.PlayList;
import vn.poly.musicoffline.sql.PlayList_Dao;

public class Music_UaThich_Adapter extends BaseAdapter {
    List<Music> musicList;
    List<PlayList> playLists;
    PlayList_Dao playList_dao;
    Context context;

    @Override
    public int getCount() {
        return musicList.size();
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
    public View getView(int position, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.view_music, null);

        ImageView img_view_music = view.findViewById(R.id.img_view_music);
        ImageView img_view_more_music = view.findViewById(R.id.img_view_more_music);
        TextView tv_view_baiHat_music = view.findViewById(R.id.tv_view_baiHat_music);
        TextView tv_view_ngheSi_music = view.findViewById(R.id.tv_view_ngheSi_music);
        TextView tv_view_thoiGian_music = view.findViewById(R.id.tv_view_thoiGian_music);

        // gán các giá trị cho view con trong layout
        Music music = musicList.get(position);
        tv_view_baiHat_music.setText(music.getTitle());
        tv_view_ngheSi_music.setText(music.getArtist());
        tv_view_thoiGian_music.setText(music.getDuration());

        // load ảnh trên 1 luồng khác
        AsyncTask<String, Void, Bitmap> asyncTask = new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... strings) {
                return getImage(music.getUri());
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (bitmap != null) {
                    img_view_music.setImageBitmap(bitmap);
                } else {
                    img_view_music.setImageResource(R.drawable.ic_no_music);
                }
            }
        }.execute(music.getUri());

        // khi nhấn vào img more
        img_view_more_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create popup menu
                PopupMenu popupMenu = new PopupMenu(view.getContext(), img_view_more_music);
                popupMenu.getMenuInflater().inflate(R.menu.menu_music, popupMenu.getMenu());
                // bắt sự kiện khi nhấn vào item ở menu
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.menu_add_danhSachPhat:


                                break;

                            case R.id.menu_delete_music:


                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        return view;
    }

    // hàm lấy ảnh từ uri bài hát
    public Bitmap getImage(String uri) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(uri);
        byte arr[] = mmr.getEmbeddedPicture();
        if (arr != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.length);
            return bitmap;
        }
        return null;
    }

}
