package vn.poly.musicoffline.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import vn.poly.musicoffline.R;
import vn.poly.musicoffline.manage.DanhSach_Activity;
import vn.poly.musicoffline.model.Music;
import vn.poly.musicoffline.model.PlayList;
import vn.poly.musicoffline.sql.PlayList_Dao;

public class Music_NgheSi_Adapter extends BaseAdapter {
    List<Music> musicList;
    List<PlayList> playLists;
    PlayList_Dao playList_dao;
    Context context;

    public Music_NgheSi_Adapter(Context context, List<Music> musicList) {
        this.context = context;
        this.musicList = musicList;
        playList_dao = new PlayList_Dao(context);
    }

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
        view = LayoutInflater.from(context).inflate(R.layout.view_music_nghesi, null);

        ImageView img_view_music_ngheSi = view.findViewById(R.id.img_view_music_ngheSi);
        ImageView img_view_more_music_ngheSi = view.findViewById(R.id.img_view_more_music_ngheSi);
        TextView tv_view_baiHat_music_ngheSi = view.findViewById(R.id.tv_view_baiHat_music_ngheSi);
        TextView tv_view_ngheSi_music_ngheSi = view.findViewById(R.id.tv_view_ngheSi_music_ngheSi);
        TextView tv_view_thoiGian_music_ngheSi = view.findViewById(R.id.tv_view_thoiGian_music_ngheSi);

        // gán các giá trị cho view
        Music music = musicList.get(position);
        tv_view_baiHat_music_ngheSi.setText(music.getTitle());
        tv_view_ngheSi_music_ngheSi.setText(music.getArtist());
        tv_view_thoiGian_music_ngheSi.setText(music.getDuration());

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
                    img_view_music_ngheSi.setImageBitmap(bitmap);
                } else {
                    img_view_music_ngheSi.setImageResource(R.drawable.ic_no_music);
                }
            }
        }.execute(music.getUri());

        // khi nhấn vào img more
        img_view_more_music_ngheSi.setOnClickListener(view12 -> {
            // create popup menu
            PopupMenu popupMenu = new PopupMenu(view12.getContext(), img_view_more_music_ngheSi);
            popupMenu.getMenuInflater().inflate(R.menu.menu_add_folder, popupMenu.getMenu());
            // bắt sự kiện khi nhấn vào item ở menu
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.menu_add_folder:
                        AlertDialog.Builder builder = new AlertDialog.Builder(viewGroup.getContext());
                        View viewa1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dialog_menu_danh_sach_phat, null);
                        builder.setView(viewa1);

                        ListView lv_dialog_menu_danhSachPhat = viewa1.findViewById(R.id.lv_dialog_menu_danhSachPhat);

                        Dialog dialog = builder.create();

                        playLists = new ArrayList<>();
                        playLists = playList_dao.getAllPlayList();

                        DanhSach_Adapter_DiaLog arrayAdapter = new DanhSach_Adapter_DiaLog(context, playLists);
                        lv_dialog_menu_danhSachPhat.setAdapter(arrayAdapter);

                        // nhấn vào playlist để thêm bài hát
                        lv_dialog_menu_danhSachPhat.setOnItemClickListener((parent, view1, i, id) -> {
                            playList_dao.addTrackToPlaylist(context, musicList.get(position).getId(), Long.parseLong(playLists.get(position).getId()));
                            DanhSach_Activity.musicList.add(music);
                            Toast.makeText(context, "Thêm vào danh sách thành công", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        });

                        dialog.show();

                        break;
                }
                return false;
            });
            popupMenu.show();
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
