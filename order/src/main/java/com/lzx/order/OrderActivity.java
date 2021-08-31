package com.lzx.order;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lzx.router_annotation.Router;
import com.lzx.router_api.RouterManager;

@Router(path = "/order/OrderActivity")
public class OrderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("test", "/order/OrderActivity");
    }

    public void jumpToMain(View view) {
        RouterManager.getInstance()
                .build("/app/MainActivity")
                .navigation(this);
    }
}