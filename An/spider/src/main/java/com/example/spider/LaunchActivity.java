package com.example.spider;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spider.launcher.LauncherView;

//软件开始的动画
public class LaunchActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        final LauncherView launcherView = (LauncherView) findViewById(R.id.view);
        //延时500ms后运行动画
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                launcherView.start();

            }
        },500);





//延时5000ms后跳转到搜索界面
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(LaunchActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        },4000);
    }
}
