package com.example.spider;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

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

//没有用的代码
public class TestCrawler {

    private static String url = "http://t.icesmall.cn/bookSpecial/special_book1/%E5%B0%8F%E8%AF%B4";

    private static Set<String> url_set = new HashSet<>();
    private static Set<String> visited= new HashSet<>();
    private  static Map<Integer,String> url_map=new HashMap<>();
    private  static Map<String,String> word_map=new HashMap<>();

    private static URL_SET url_dict;
    private static WORD_SET word_dict;
    private static Integer id = 0;

    public static void main(String[] args) {


        getArticleListFromUrl(url);

        System.out.println(word_map.toString());
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
            Thread.sleep(100);

        } catch (Exception e) {

            e.printStackTrace();
            //System.out.println(url);
            url_set.remove(url);
            visited.add(url);
            continue;
        }
        if(!url.contains("0.html")){
        dealTagLink(doc.getElementsByTag("a")); //找到所有a标签
            //System.out.println(url_set.toString());
            getArticleFromUrl(url,doc.getElementsByTag("title").toString());
            url_set.remove(url);
            visited.add(url);
    }else{
            getArticleFromUrl(url,doc.getElementsByTag("title").toString());
            url_set.remove(url);
            visited.add(url);
        }
        }
}

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
        url_map.put(id,detailurl);

        for(String element:words){
            if(word_map.containsKey(element)){
                String num=word_map.get(element);
                num=num+" "+id.toString();
                word_map.put(element,num);
            }else{
                word_map.put(element,id.toString());
            }
        }


        id++;
    }



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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void get_score(String target){
        Map<Integer,Integer> score=new HashMap<Integer, Integer>();
        ArrayList<String> target_words=get_word(target);



        for(String word:target_words){
            if(word_map.containsKey(word)){
                String[] Snumber=word_map.get(word).split(" ");
                int []numbers=new int[Snumber.length];
                for(int i=0;i<Snumber.length;i++){
                    numbers[i]=Integer.parseInt(Snumber[i]);                    //含有该单词的id数组
                    if(target_words.contains(numbers[i])){
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
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).getKey() + ": " + list.get(i).getValue()+":"+url_map.get(list.get(i).getKey()));
        }


    }
}
