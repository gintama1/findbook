package com.example.spider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {
//    private static URL_SET url_dict;
//    private static WORD_SET word_dict;
private static final int NOT_NOTICE = 2;//如果勾选了不再询问
    private AlertDialog alertDialog;
    private AlertDialog mDialog;

    private List<String>  findall;

    private static SQLiteDatabase Url;                                      //存放文章标题、url、对应的id的数据库
    private static SQLiteDatabase Word;                                     //存放单词与含有该单词的id数组的数据库
    private static String url = "http://t.icesmall.cn/bookSpecial/special_book1/%E5%B0%8F%E8%AF%B4";

    private static Set<String> url_set = new HashSet<>();                    //存放当前未访问的url集合
    private static Set<String> visited= new HashSet<>();                    //已经访问过的url集合

    private static Integer id = 0;                                          //依次记录url对应的id号



    private final String DATABASE_PATH = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/databese";

    private final String URL_DATABASE_FILENAME = "url_dict.db";
    private final String WORD_DATABASE_FILENAME = "word_dict.db";



    private Button search;
    private Button build;
    private EditText editText;



    private RecyclerView recyclerView;
    private GeneralAdapter adapter;



    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myRequetPermission();

//        url_dict=new URL_SET(MainActivity.this,"url_dict",null,1);
//        word_dict=new WORD_SET(MainActivity.this,"word_dict",null,1);

//将数据库保存到本地
        if (!checkDataBase(URL_DATABASE_FILENAME)) {
            Url= URLopenDatabase(URL_DATABASE_FILENAME);
            Log.i("ssssssss", "开始llllllLlLl" + Url);

        }else{
            System.out.println("URL文件已存在");
            Url= URLopenDatabase(URL_DATABASE_FILENAME);
        }
        if (!checkDataBase(WORD_DATABASE_FILENAME)) {
            Word= WORDopenDatabase(WORD_DATABASE_FILENAME);
            Log.i("aaaaa", "开始aaaaaa" + Word);
        }else{
            System.out.println("WORD文件已存在");
            Word= WORDopenDatabase(WORD_DATABASE_FILENAME);
        }







        recyclerView=findViewById(R.id.show);
        //设置LayoutManager为LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));




        build=findViewById(R.id.build);
        build.setVisibility(View.INVISIBLE);                            //用于再建数据库的时候，才显示

        editText=findViewById(R.id.input);
        search=findViewById(R.id.search);






        build.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Android 4.0 之后不能在主线程中请求HTTP请求
                new Thread(new Runnable(){
                    @Override
                    public void run() {
//                        word_dict.getReadableDatabase().execSQL("delete from word_dict");
//                        url_dict.getReadableDatabase().execSQL("delete from url_dict");

                        Word.execSQL("delete from word_dict");
                        Url.execSQL("delete from url_dict");
                        getArticleListFromUrl(url);
                    }
                }).start();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                String content=editText.getText().toString();
                findall=get_score(content);
                if(findall!=null) {
                    int size = findall.size();



                    //设置Adaptero
                    List<String> _title=new ArrayList<>();
                    List<String> _url=new ArrayList<>();
                    for (int i=0;i<findall.size();i++){
                        String get= findall.get(i);
                        _title.add(get.split("::")[1]);
                        _url.add(get.split("::")[0]);
                    }


                    adapter= new GeneralAdapter(MainActivity.this,_title);
                    recyclerView.setAdapter(adapter);
                    adapter.setOnitemClickLintener(new GeneralAdapter.OnitemClick() {
                        @Override
                        public void onItemClick(int position) {
                            String get= findall.get(position);
                            String name=get.split("::")[1];
                            get=get.split("::")[0];

                            Bundle bundle=new Bundle();
                            bundle.putCharSequence("url",get);
                            bundle.putCharSequence("name",name);


                            //当点击时获取对应的url并将该url传入Chapter，再跳转到Chapter界面

                            Intent intent=new Intent(MainActivity.this,Chapter.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    });
                    recyclerView.addItemDecoration(new MyItemDivider(MainActivity.this,R.drawable.rv_main_item_divider));

                }


            }
        });








    }

    /**
     * 获取文章列表
     */
    public static void getArticleListFromUrl(String url) {
        url_set.add(url);



        while(!url_set.isEmpty()){

            Document doc = null;
            Iterator it = url_set.iterator(); url = (String) it.next();

            try {

                Connection conn = Jsoup.connect(url).timeout(50000);
                conn.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                conn.header("Accept-Encoding", "gzip, deflate, sdch");
                conn.header("Accept-Language", "zh-CN,zh;q=0.8");
                conn.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
                doc=conn.get();
                Thread.sleep(1000);

            } catch (Exception e) {

                e.printStackTrace();
                //System.out.println(url);
                url_set.remove(url);
                visited.add(url);
                continue;
            }
            if(!url.contains("/0.html")){
                String title=doc.getElementsByTag("title").toString();
                title=title.replace("<title>","");
                title=title.replace("</title>","");
                if(title.length()==0)
                {
                    System.out.println("redo");
                    continue;
                }
                dealTagLink(doc.getElementsByTag("a")); //找到所有a标签


                //System.out.println(url_set.toString());
                //getArticleFromUrl(url,doc.getElementsByTag("title").toString());
                url_set.remove(url);
                visited.add(url);
            }else{
                String title=doc.getElementsByTag("title").toString();
                title=title.replace("<title>","");
                title=title.replace("</title>","");
                if(title.length()==0)
                {
                    System.out.println("redo");
                    continue;
                }
                getArticleFromUrl(url,doc.getElementsByTag("title").toString());
                url_set.remove(url);
                visited.add(url);
            }
        }
    }
//从对应的html页面中在获取为访问的url界面
    private static void dealTagLink(Elements elements) {

        for (Element element : elements) {
            //System.out.println(element.toString());
            String S_element=element.toString();
            String pattern="[\\u4E00-\\u9FA5]+";
            Pattern.matches(pattern, S_element);
            String href = element.attr("href");
            String href2 = element.attr("target");
            String href3=element.attr("title");
            String href4=element.attr("class");

            //System.out.println(href);


            if (href.contains("http://t.icesmall.cn")) {
                if (!visited.contains(href) && !url_set.contains(href)) {
                    //System.out.println(pre + href + "        " + href3);
                    url_set.add(href);
                }
            }
        }


    }

    /**
     * 获取文章内容
     * @param detailurl
     * @param title
     */
    public static void getArticleFromUrl(String detailurl,String title) {
        //System.out.println(title);
        title=title.replace("<title>","");
        title=title.replace("</title>","");
        ArrayList<String> words=get_word(title);
        System.out.println(id+" "+detailurl+"           "+title+"                "+ words.toString());

//        url_insert(url_dict.getReadableDatabase(),String.valueOf(id),detailurl,title);
//
//        for(String element:words){
//            if(isExist(word_dict.getReadableDatabase(),element)){
//                String num=getids(word_dict.getReadableDatabase(),element);
//                num=num+" "+id.toString();
//                Update(word_dict.getReadableDatabase(),element,num);
//            }else{
//                word_insert(word_dict.getReadableDatabase(),element,id.toString());
//            }
//        }


        url_insert(Url,String.valueOf(id),detailurl,title);

        for(String element:words){
            if(isExist(Word,element)){
                String num=getids(Word,element);
                num=num+" "+id.toString();
                Update(Word,element,num);
            }else{
                word_insert(Word,element,id.toString());
            }
        }

        id++;
    }


//实现分词操作
    public static  ArrayList<String> get_word(String content){
        StringReader sr=new StringReader(content);
        IKSegmenter ik=new IKSegmenter(sr, true);
        ArrayList<String> a_l=new ArrayList<>();
        Lexeme lex=null;
        try {
            while((lex=ik.next())!=null){
                a_l.add(lex.getLexemeText());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return a_l;
    }

//对要搜索的输入字符串进行分词，并在url_dict找到相应的id数组，在对不同id的相关程度进行打分再排序
//返回的格式未List，元素格式为 URL+"::"+Title
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<String>get_score(String target){
        Map<Integer,Integer> score=new HashMap<Integer, Integer>();
        ArrayList<String> target_words=get_word(target);



//        for(String word:target_words){
//            if(isExist(word_dict.getReadableDatabase(),word)){
//                String[] Snumber=getids(word_dict.getReadableDatabase(),word).split(" ");
//                int []numbers=new int[Snumber.length];
//                for(int i=0;i<Snumber.length;i++){
//                    numbers[i]=Integer.parseInt(Snumber[i]);                    //含有该单词的id数组
//                    if(target_words.contains(numbers[i])){
//                        int value=score.get(numbers[i]);
//                        value++;
//                        score.put(numbers[i],value);
//
//                    }else{
//                        score.put(numbers[i],1);
//                    }
//                }
//
//            }
//        }

        for(String word:target_words){
            System.out.println("8888888888888888888888888888888888888888888"+word);
            if(isExist(Word,word)){
                String[] Snumber=getids(Word,word).split(" ");
                int []numbers=new int[Snumber.length];
                for(int i=0;i<Snumber.length;i++){

                    numbers[i]=Integer.parseInt(Snumber[i]);                    //含有该单词的id数组
                    if(score.get(numbers[i])!=null){
                        System.out.println("numbers[i]"+numbers[i]);
                        int value=score.get(numbers[i]);
                        value++;
                        score.put(numbers[i],value);

                    }else{
                        score.put(numbers[i],1);
                    }
                }

            }
        }



        System.out.println("*******************************************");
        System.out.println(score.toString());
        System.out.println("*******************************************");

        List<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer, Integer>>(score.entrySet()); //转换为list
        list.sort(new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        ArrayList<String> result=new ArrayList<>();
//        for (int i = 0; i < list.size(); i++) {
//            String URL=url_query_id(url_dict.getReadableDatabase(),String.valueOf(list.get(i).getKey()));
//            String Title=title_query_id(url_dict.getReadableDatabase(),String.valueOf(list.get(i).getKey()));
//            if(!URL.contains("/0.html")){
//                continue;
//            }
//            String element=URL+"::"+Title;
//            result.add(element);
//        }




                for (int i = 0; i < list.size(); i++) {
            String URL=url_query_id(Url,String.valueOf(list.get(i).getKey()));
            String Title=title_query_id(Url,String.valueOf(list.get(i).getKey()));
            if(!URL.contains("/0.html")){
                continue;
            }
            String element=URL+"::"+Title;

            result.add(element);
        }

    return result;

    }
    //url_dict中插入元素
    private static void url_insert(SQLiteDatabase sqLiteDatabase, String id, String detailurl,String title){
        ContentValues values=new ContentValues();
        values.put("id",id);
        values.put("url",detailurl);
        values.put("title",title);
        sqLiteDatabase.insert("url_dict",null,values);

    }
    //word_dict中插入元素
    private static void word_insert(SQLiteDatabase sqLiteDatabase, String word, String id){
        ContentValues values=new ContentValues();
        values.put("word",word);
        values.put("id_set",id);
        sqLiteDatabase.insert("word_dict",null,values);

    }
    //word_dict判断是否存在word
private static Boolean isExist(SQLiteDatabase sqLiteDatabase,String word){
        Cursor cursor=sqLiteDatabase.query("word_dict",null,"word=?",new String[]{word},null,null,null);
        if(cursor.getCount()==0){cursor.close();return false;}
        cursor.close();
        return true;
    }
    //word_dict通过word获得id数组（为String类型（"id1 id2 id3"））
private static String getids(SQLiteDatabase sqLiteDatabase,String word){
        Cursor cursor=sqLiteDatabase.query("word_dict",null,"word=?",new String[]{word},null,null,null);
        cursor.moveToNext();
        String a=cursor.getString(1);
        cursor.close();
        return a;
    }
    //word_dict通过word对相应的id数组进行更新
private static void Update(SQLiteDatabase sqLiteDatabase,String word,String ids){
        ContentValues contentValues=new ContentValues();
        contentValues.put("id_set",ids);
        sqLiteDatabase.update("word_dict",contentValues,"word=?",new String[]{word});
    }
    //word_dict输出所有的元素（元素输出格式为（word  id_set））
private static ArrayList<Map<String,String>>query(SQLiteDatabase sqLiteDatabase){
        Cursor cursor=sqLiteDatabase.query("word_dict",null,null,null,null,null,null);
        ArrayList<Map<String,String>> resulte=new ArrayList<>();
        while(cursor.moveToNext()){
            Map<String,String> map=new HashMap<>();
            map.put("word",cursor.getString(0));
            map.put("id_set",cursor.getString(1));
            resulte.add(map);
        }
        cursor.close();
        return resulte;
    }

    //url_dict 通过id 获取对应的url链接
private static String url_query_id(SQLiteDatabase sqLiteDatabase,String id){
    Cursor cursor=sqLiteDatabase.query("url_dict",null,"id=?",new String[]{id},null,null,null);
    cursor.moveToNext();
    String result=cursor.getString(1);
    cursor.close();
    return result;
    }
    //url_dict 通过id 获取对应的标题
    private static String title_query_id(SQLiteDatabase sqLiteDatabase,String id){
        Cursor cursor=sqLiteDatabase.query("url_dict",null,"id=?",new String[]{id},null,null,null);
        cursor.moveToNext();
        String result=cursor.getString(2);
        cursor.close();
        return result;
    }



//判断本地是否有对应的数据库文件
    public boolean checkDataBase(String DATABASE_FILENAME) {
        SQLiteDatabase checkDB = null;
        try {
            String databaseFilename = DATABASE_PATH + "/" + DATABASE_FILENAME;
            checkDB = SQLiteDatabase.openDatabase(databaseFilename, null,
                    SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
                e.printStackTrace();
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }



    //在指定的本地文件夹中创建对应的URL数据库文件
    public SQLiteDatabase URLopenDatabase(String DATABASE_FILENAME) {
        SQLiteDatabase database = null;
        try {
            // 获得dictionary.db文件的绝对路径
            String databaseFilename = DATABASE_PATH + "/" + DATABASE_FILENAME;
            File dir = new File(DATABASE_PATH);

            if (!dir.exists())
                dir.mkdir();

            if(dir.exists()){
                System.out.println("文件创建成功");
            }else{
                System.out.println("文件创建失败");
            }
            // 如果在/sdcard/dictionary目录中不存在
            // dictionary.db文件，则从res\raw目录中复制这个文件到
            // SD卡的目录（/sdcard/dictionary）
            if (!(new File(databaseFilename)).exists()) {
                // 获得封装dictionary.db文件的InputStream对象
                InputStream is =MainActivity.this.getResources()
                        .openRawResource(R.raw.url_dict);
                FileOutputStream fos = new FileOutputStream(databaseFilename);
                byte[] buffer = new byte[7168];
                int count = 0;

                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
            // 打开/sdcard/dictionary目录中的dictionary.db文件
            database = SQLiteDatabase.openOrCreateDatabase(databaseFilename,
                    null);
            return database;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return database;

    }
    //在指定的本地文件夹中创建对应的WORD数据库文件
    public SQLiteDatabase WORDopenDatabase(String DATABASE_FILENAME) {
        SQLiteDatabase database = null;
        try {
            // 获得dictionary.db文件的绝对路径
            String databaseFilename = DATABASE_PATH + "/" + DATABASE_FILENAME;
            File dir = new File(DATABASE_PATH);
            if (!dir.exists())
                dir.mkdir();
            // 如果在/sdcard/dictionary目录中不存在
            // dictionary.db文件，则从res\raw目录中复制这个文件到
            // SD卡的目录（/sdcard/dictionary）
            if (!(new File(databaseFilename)).exists()) {
                // 获得封装dictionary.db文件的InputStream对象
                InputStream is =MainActivity.this.getResources()
                        .openRawResource(R.raw.word_dict);
                FileOutputStream fos = new FileOutputStream(databaseFilename);
                byte[] buffer = new byte[7168];
                int count = 0;

                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
            // 打开/sdcard/dictionary目录中的dictionary.db文件
            database = SQLiteDatabase.openOrCreateDatabase(databaseFilename,
                    null);
            return database;
        } catch (Exception e) {
        }
        return database;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(url_dict!=null){
//            url_dict.close();
//        }
//        if(word_dict!=null){
//            word_dict.close();
//        }
    }

//申请获取权限
    private void myRequetPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PERMISSION_GRANTED) {//选择了“始终允许”
                    Toast.makeText(this, "" + "权限" + permissions[i] + "申请成功", Toast.LENGTH_SHORT).show();
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])){//用户选择了禁止不再询问

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("permission")
                                .setMessage("点击允许才可以使用app")
                                .setPositiveButton("去允许", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (mDialog != null && mDialog.isShowing()) {
                                            mDialog.dismiss();
                                        }
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);//注意就是"package",不用改成自己的包名
                                        intent.setData(uri);
                                        startActivityForResult(intent, NOT_NOTICE);
                                    }
                                });
                        mDialog = builder.create();
                        mDialog.setCanceledOnTouchOutside(false);
                        mDialog.show();



                    }else {//选择禁止
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("permission")
                                .setMessage("点击允许才可以使用app")
                                .setPositiveButton("去允许", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (alertDialog != null && alertDialog.isShowing()) {
                                            alertDialog.dismiss();
                                        }
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.INTERNET,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                                    }
                                });
                        alertDialog = builder.create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                    }

                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==NOT_NOTICE){
            myRequetPermission();//由于不知道是否选择了允许所以需要再次判断
        }
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
