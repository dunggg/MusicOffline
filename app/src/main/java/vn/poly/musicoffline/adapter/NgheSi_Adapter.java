package vn.poly.musicoffline.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import vn.poly.musicoffline.R;

import java.util.List;

public class NgheSi_Adapter extends BaseAdapter {
    private Context context;
    private List<String> ngheSiList;
    Bitmap bitmap;
    int soLuongBaiHat;

    public NgheSi_Adapter(Context context, List<String> ngheSiList) {
        this.context = context;
        this.ngheSiList = ngheSiList;
    }

    @Override
    public int getCount() {
        return ngheSiList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = LayoutInflater.from(context).inflate(R.layout.view_nghe_si, null);

        ImageView img_view_ngheSi = view.findViewById(R.id.img_view_ngheSi);
        TextView tv_view_ngheSi = view.findViewById(R.id.tv_view_ngheSi);
        TextView tv_view_soLuong_ngheSi = view.findViewById(R.id.tv_view_soLuong_ngheSi);

        tv_view_ngheSi.setText(ngheSiList.get(position));

        // load ảnh trên 1 luồng khác
        AsyncTask<String, Void, Bitmap> asyncTask = new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... strings) {
                getImageArtist(strings[0]);
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (bitmap != null) {
                    img_view_ngheSi.setImageBitmap(bitmap);
                }
                tv_view_soLuong_ngheSi.setText(soLuongBaiHat + " bài hát");
            }
        }.execute(ngheSiList.get(position));

        return view;
    }

    private void getImageArtist(String artist) {
        bitmap = null;
        String selection = MediaStore.Audio.Media.ARTIST + "= '" + artist + "'";
        // danh sách các cột cần lấy
        String projection[] = {MediaStore.Audio.Media.DATA};

        // con trỏ truy cập vào file nhạc
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
        soLuongBaiHat = cursor.getCount();
        // vòng lặp sẽ dừng khi tìm thấy ảnh
        while (cursor.moveToNext()) {
            String uri = cursor.getString(0);
            if (getImage(uri) != null) {
                bitmap = getImage(uri);
                cursor.close();
                return;
            }
        }
        // đóng con trỏ
        cursor.close();
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
