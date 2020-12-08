package vn.poly.musicoffline.manage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import vn.poly.musicoffline.MainActivity;
import vn.poly.musicoffline.R;

public class Chao_Activity extends AppCompatActivity {
    final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chao);

        // yêu cầu cấp quyền truy cập vào bộ nhớ
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
    }

    // lắng nghe quyền được cấp
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1500);
                        startActivity(new Intent(Chao_Activity.this, MainActivity.class));
                        finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
    }

}