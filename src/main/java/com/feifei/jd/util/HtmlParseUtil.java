package com.feifei.jd.util;

import com.feifei.jd.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
@Component
public class HtmlParseUtil
{
   // public static void main(String[] args) throws IOException {
//        //获取请求：https://search.jd.com/Search?keyword=java
//        String url = "https://search.jd.com/Search?keyword=java";
//        //解析网页， 获取document对象节点
//        Document document = Jsoup.parse(new URL(url), 30000);
//        // 操作js   获取某一个 id元素
//        Element j_goodsList = document.getElementById("J_goodsList");
//        System.out.println(j_goodsList);
//        // 获取所有 li 元素
//        Elements li = j_goodsList.getElementsByTag("li");
//        System.out.println(li.size());
//        //获取元素内的数据
//        for (Element el : li) {
//            //关于这种图片特别多的网站，所有的图片都是延迟加载的，
//            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
//            String price = el.getElementsByClass("p-price").eq(0).text();
//            String title = el.getElementsByClass("p-name").eq(0).text();
//            System.out.println("===================");
//            System.out.println(img);
//            System.out.println(price);
//            System.out.println(title);
//        }
//        for (Content content : new HtmlParseUtil().parseJD("java")) {
//            System.out.println(content);
//        }
//    }

    //定义一个工具类
    public List<Content> parseJD(String keyword) throws IOException {
        List<Content> list = new ArrayList<>();
        //获取请求：https://search.jd.com/Search?keyword=java
        String url = "https://search.jd.com/Search?keyword="+keyword;
        //解析网页， 获取document对象节点
        Document document = Jsoup.parse(new URL(url), 30000);
        // 操作js   获取某一个 id元素
        Element j_goodsList = document.getElementById("J_goodsList");
//        System.out.println(j_goodsList);
        // 获取所有 li 元素
        Elements li = j_goodsList.getElementsByTag("li");
//        System.out.println(li.size());
        //获取元素内的数据
        for (Element el : li) {
            //关于这种图片特别多的网站，所有的图片都是延迟加载的，
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();
            Content content = new Content();
            content.setPrice(price);
            content.setImg(img);
            content.setTitle(title);
            list.add(content);
        }
        return  list;
    }
}
