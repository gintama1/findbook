package com.example.spider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spider.NewTextView.fzktjtView;
import com.example.spider.NewTextView.fzlxjtView;
import com.example.spider.NewTextView.hksnztView;
import com.example.spider.NewTextView.official_scriptView;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class Content extends AppCompatActivity {
private String url;
    private ProgressBar progressBar;
    private Handler mHandler;
    private String findall;
    private TextView textView;
    private int colors[]={R.color.background1,R.color.background2,R.color.background3,R.color.background4};
    private String text_Style[]={"fonts/hksnzt.ttf","fonts/official_script.ttf","fonts/fzktjt.ttf","fonts/fzlxtjt.ttf"};

    private SeekBar seekBar1;
    private ImageView background_1;
    private ImageView background_2;
    private ImageView background_3;
    private ImageView background_4;



    private hksnztView text_style1;
    private official_scriptView text_style2;
    private fzktjtView text_style3;
    private fzlxjtView text_style4;



    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        textView=findViewById(R.id.show);


        seekBar1=findViewById(R.id.seekbar1);
        background_1=findViewById(R.id.background_1);
        background_2=findViewById(R.id.background_2);
        background_3=findViewById(R.id.background_3);
        background_4=findViewById(R.id.background_4);

        background_1.setOnClickListener(background_color);
        background_2.setOnClickListener(background_color);
        background_3.setOnClickListener(background_color);
        background_4.setOnClickListener(background_color);

        text_style1=findViewById(R.id.text_style1);
        text_style2=findViewById(R.id.text_style2);
        text_style3=findViewById(R.id.text_style3);
        text_style4=findViewById(R.id.text_style4);


        text_style1.setOnClickListener(TextStyle);
        text_style2.setOnClickListener(TextStyle);
        text_style3.setOnClickListener(TextStyle);
        text_style4.setOnClickListener(TextStyle);

        //从asset 读取字体
        AssetManager mgr = getAssets();
        //根据路径得到Typeface
        Typeface tf = Typeface.createFromAsset(mgr, "fonts/hksnzt.ttf");//华康少女字体










        sp=getSharedPreferences("data",MODE_PRIVATE);
//阅读背景
        int color=sp.getInt("background",-1);
        if(color==-1) {
            textView.setBackgroundColor(getResources().getColor(R.color.background1));
        }
        else{
            textView.setBackgroundColor(getResources().getColor(colors[color]));
        }
        int text_style=sp.getInt("text_style",-1);
        if(text_style==-1){
            tf = Typeface.createFromAsset(mgr, "fonts/hksnzt.ttf");//华康少女字体
            textView.setTypeface(tf);
        }else{
            tf = Typeface.createFromAsset(mgr, text_Style[text_style]);//华康少女字体
            textView.setTypeface(tf);
        }

//字体大小
        int size=sp.getInt( "text_Size",-1);
        if(color==-1) {
            seekBar1.setProgress(0);
            textView.setTextSize(20);
        }
        else{

            seekBar1.setProgress(size-20);
            textView.setTextSize(size);
        }



        progressBar = (ProgressBar)findViewById(R.id.spin_kit);
        Sprite doubleBounce= new DoubleBounce();
        progressBar.setIndeterminateDrawable(doubleBounce);
        new Thread(new Runnable(){
            @Override
            public void run() {
                while(true){
                    Intent intent=getIntent();
                    Bundle bundle=intent.getExtras();
                    url=bundle.getString("url");
                    System.out.println("url  "+url);
                    //通过Chapter传入的url进行对该网页的进行爬虫处理
                    findall=getArticleListFromUrl(url);
                    Message msg=new Message();
                    if(findall!=null&&findall.length()!=0){
                        msg.what=0x111;
                        mHandler.sendMessage(msg);
                        break;
                    }
                }
            }
        }).start();

        mHandler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(msg.what==0x111){
                    textView.setText(findall);
                    progressBar.setVisibility(View.GONE);
                }
            }
        };

        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //获取编辑器
                SharedPreferences.Editor editor=sp.edit();
                //保存字体大小
                textView.setTextSize(20+i);
                editor.putInt("text_Size",20+i);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        final BottomSheetBehavior bottomSheetBehavior=BottomSheetBehavior.from(findViewById(R.id.design_bottom_sheet1));
        //设置默认先隐藏
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        findViewById(R.id.bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //根据状态不同显示隐藏
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        //设置监听事件
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                //拖动
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //状态变化
            }
        });



    }
    //通过Chapter传入的url进行对该网页爬虫处理 返回内容
    public static String getArticleListFromUrl(String url) {


        Document doc = null;


            try {

                Connection conn = Jsoup.connect(url).timeout(50000);
                conn.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                conn.header("Accept-Encoding", "gzip, deflate, sdch");
                conn.header("Accept-Language", "zh-CN,zh;q=0.8");
                conn.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
                doc = conn.get();
                Thread.sleep(1000);

            } catch (Exception e) {
                e.printStackTrace();
            }

        if(doc!=null){
            return dealTagLink(doc.getElementsByTag("div")); //找到所有a标签
        }else{
            System.out.println("doc==null");
            return null;
        }


    }

    private static String dealTagLink(Elements elements) {

        ArrayList<String > result=new ArrayList<>();
        for (Element element : elements) {
            String S_element=element.toString();
            String id = element.attr("id");

            if (id.equals("Content")) {
                S_element=S_element.replace("<p>","  ");
                S_element=S_element.replace("</p>","");
                S_element=S_element.replace("</div>","");
                S_element=S_element.replace("&nbsp;","");
                while (S_element.contains(">")) {
                    int start = S_element.indexOf(">");
                    S_element = S_element.substring(start + 1);
                }
                return S_element;
            }
        }
        return null;
    }
    public View.OnClickListener background_color=new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //获取编辑器
            SharedPreferences.Editor editor=sp.edit();
            switch (v.getId()){
                case R.id.background_1:
                    System.out.println("background1");
                    textView.setBackgroundColor(getResources().getColor(R.color.background1));
                    editor.putInt("background",0);
                    editor.commit();
                    break;
                case R.id.background_2:
                    textView.setBackgroundColor(getResources().getColor(R.color.background2));
                    System.out.println("background2");
                    editor.putInt("background",1);
                    editor.commit();
                    break;
                case R.id.background_3:
                    textView.setBackgroundColor(getResources().getColor(R.color.background3));
                    System.out.println("background3");
                    editor.putInt("background",2);
                    editor.commit();
                    break;
                case R.id.background_4:
                    textView.setBackgroundColor(getResources().getColor(R.color.background4));
                    System.out.println("background4");
                    editor.putInt("background",3);
                    editor.commit();
                    break;
            }
        }
    };

    public View.OnClickListener TextStyle=new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //从asset 读取字体
            AssetManager mgr = getAssets();
            Typeface tf;

            SharedPreferences.Editor editor=sp.edit();
            switch (v.getId()){
                case R.id.text_style1:

                     tf= Typeface.createFromAsset(mgr, text_Style[0]);//华康少女字体
                    textView.setTypeface(tf);
                    editor.putInt("text_style",0);
                    editor.commit();

                    break;
                case R.id.text_style2:

                    tf = Typeface.createFromAsset(mgr, text_Style[1]);//隶书字体
                    textView.setTypeface(tf);
                    editor.putInt("text_style",1);
                    editor.commit();
                    break;
                case R.id.text_style3:

                    tf = Typeface.createFromAsset(mgr, text_Style[2]);//方正卡通
                    textView.setTypeface(tf);
                    editor.putInt("text_style",2);
                    editor.commit();
                    break;
                case R.id.text_style4:

                    tf = Typeface.createFromAsset(mgr, text_Style[3]);//方正流行
                    textView.setTypeface(tf);
                    editor.putInt("text_style",3);
                    editor.commit();
                    break;
            }
        }
    };



}
