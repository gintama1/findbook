package com.example.spider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Wave;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


public class Chapter extends AppCompatActivity {
private TextView show;
private Button btn;


    private RecyclerView recyclerView;
    private GeneralAdapter adapter;

private static  String url;
private Handler mHandler;
 private    ProgressBar progressBar;
    private String[] ChapterUrl;
    private String[] ChapterTitle;
    private  static ArrayList<String>  findall;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter);
        show=findViewById(R.id.url);


        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        url=bundle.getString("url");

        String name=bundle.getString("name");
        int start=name.indexOf("《");
        int end=name.indexOf("》");
        //标题处理为 标题
        name=name.substring(start+1,end);

        progressBar = (ProgressBar)findViewById(R.id.spin_kit);
        Sprite wave = new Wave();
        progressBar.setIndeterminateDrawable(wave);


        recyclerView = findViewById(R.id.chapter);
//设置LayoutManager为LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(Chapter.this));





        new Thread(new Runnable(){
            @Override
            public void run() {
                while(true){
                Intent intent=getIntent();
                Bundle bundle=intent.getExtras();
                url=bundle.getString("url");
                System.out.println("url  "+url);
                //通过MainActivity传入的url进行对该网页进行爬虫处理
                findall=getArticleListFromUrl(url);
                Message msg=new Message();

                if(findall.size()!=0){
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

                    int size=findall.size();
                    String[] array = (String[])findall.toArray(new String[size]);
                    ChapterUrl=new String[array.length];
                    ChapterTitle=new String[array.length];
                    List<String> datas = new ArrayList<>();

                    for(int i=0;i<array.length;i++){
                        ChapterTitle[i]=array[i].split("-")[1];
                        ChapterUrl[i]=array[i].split("-")[0];
                        datas.add(array[i].split("-")[1]);
                    }



//设置Adapter
                    adapter= new GeneralAdapter(Chapter.this,datas);
                    recyclerView.setAdapter(adapter);

                    adapter.setOnitemClickLintener(new GeneralAdapter.OnitemClick() {
                        @Override
                        public void onItemClick(int position) {
                            String get= ChapterUrl[position];
                            System.out.println(get);

                            Bundle bundle=new Bundle();
                            bundle.putCharSequence("url",get);
                            //当点击时获取对应的url并将该url传入Content，再跳转到Content界面
                            Intent intent=new Intent(Chapter.this, Content.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    });

                    progressBar.setVisibility(View.GONE);

                }
            }
        };

        recyclerView.addItemDecoration(new MyItemDivider(this,R.drawable.rv_main_item_divider));



        show.setText(name);




    }


    //通过MainActivity传入的url进行对该网页的url爬虫处理 返回章节列表，格式为（"章节名-url链接"）
    public static ArrayList<String> getArticleListFromUrl(String url) {


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
    return dealTagLink(doc.getElementsByTag("a")); //找到所有a标签
    }else{
        System.out.println("doc==null");
        return null;
    }


}

    private static ArrayList<String > dealTagLink(Elements elements) {
        System.out.println("ArrayList<String > dealTagLink(Elements elements)");
        ArrayList<String > result=new ArrayList<>();
        for (Element element : elements) {
            String S_element=element.toString();
            System.out.println(S_element);
            int start=S_element.indexOf(">");
            int end=S_element.indexOf("</a>");
            String title=S_element.substring(start+1,end);
            System.out.println(title);
            String href = element.attr("href");
            String origin=url.replace("0.html","");
            origin=origin.replace("bookDir","book");
//            System.out.println("origin    "+origin);
//            System.out.println("href  "+href);
            if (href.contains(origin)) {
                result.add(href+"-"+title);
            }
        }
        return result;
    }



    //绘制间隔线
    class MyItemDivider extends RecyclerView.ItemDecoration {
        private Drawable mDrawable;

        public MyItemDivider(Context context, int resId) {
            mDrawable = context.getResources().getDrawable(resId);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDrawable.getIntrinsicHeight();
                mDrawable.setBounds(left, top, right, bottom);
                mDrawable.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(0, 0, 0, mDrawable.getIntrinsicWidth());
        }
    }

}
