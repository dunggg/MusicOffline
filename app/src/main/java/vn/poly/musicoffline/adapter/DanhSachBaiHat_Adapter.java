package vn.poly.musicoffline.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import vn.poly.musicoffline.R;
import vn.poly.musicoffline.model.Music;

public class DanhSachBaiHat_Adapter extends BaseAdapter {
    List<Music> musicList;

    public DanhSachBaiHat_Adapter(List<Music> musicList) {
        this.musicList = musicList;
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
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_danh_sach_bai_hat, null);

        ImageView img_view_danhSach = view.findViewById(R.id.img_view_danhSach);
        ImageView img_view_delete_danhSach = view.findViewById(R.id.img_view_delete_danhSach);
        TextView tv_view_music_danhSach = view.findViewById(R.id.tv_view_music_danhSach);
        TextView tv_view_ngheSi_danhSach = view.findViewById(R.id.tv_view_ngheSi_danhSach);

        // gán các giá trị cho view con trong layout
        Music music = musicList.get(position);
        tv_view_music_danhSach.setText(music.getTitle());
        tv_view_ngheSi_danhSach.setText(music.getArtist());

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
                    img_view_danhSach.setImageBitmap(bitmap);
                } else {
                    img_view_danhSach.setImageResource(R.drawable.ic_no_music);
                }
            }
        }.execute(music.getUri());

        img_view_delete_danhSach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //code
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
