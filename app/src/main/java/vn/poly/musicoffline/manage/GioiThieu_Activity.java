package vn.poly.musicoffline.manage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import vn.poly.musicoffline.MainActivity;
import vn.poly.musicoffline.R;

public class GioiThieu_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gioi_thieu_);
        toolbar_gioiThieu();
    }

    // hàm toolbar xử lý chức năng quay lại và tìm kiếm
    public void toolbar_gioiThieu() {
        Toolbar toolbar = findViewById(R.id.toolbar_gioiThieu);
        toolbar.setTitle("Giới thiệu");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationOnClickListener(view -> startActivity(new Intent(getBaseContext(), MainActivity.class)));
    }

}