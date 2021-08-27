package com.lzx.routerdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lzx.router_annotation.Router;

@Router(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}