package com.lzx.routerdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lzx.router_annotation.Router;
import com.lzx.router_api.RouterManager;

@Router(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("test", "/app/MainActivity");
    }

    public void jumpToOrder(View view) {
        Log.i("test", "jumpToOrder: ");
        RouterManager.getInstance()
                .build("/order/OrderActivity")
                .withString("name", "lzx")
                .withInt("age", 18)
                .navigation(this);
    }
}