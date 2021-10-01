package com.zxj.customlayout;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_view).setOnClickListener(v -> {
            startActivity(new Intent(this, LayoutViewActivity.class));
        });

        findViewById(R.id.btn_group).setOnClickListener(v -> {
            startActivity(new Intent(this, LayoutGroupActivity.class));
        });
        startActivity(new Intent(this, LayoutGroupActivity.class));
    }
}