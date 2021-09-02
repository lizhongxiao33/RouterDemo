package com.lzx.order;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lzx.router_annotation.Parameter;
import com.lzx.router_annotation.Router;
import com.lzx.router_api.ParameterManager;
import com.lzx.router_api.RouterManager;

@Router(path = "/order/OrderActivity")
public class OrderActivity extends AppCompatActivity {

    @Parameter
    String name;

    @Parameter
    int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        ParameterManager.getInstance().loadParameter(this);
        Log.i("test", "OrderActivity  " + "name:" + name + "  age:" + age);
    }

    public void jumpToMain(View view) {
        RouterManager.getInstance()
                .build("/app/MainActivity")
                .navigation(this);
    }
}