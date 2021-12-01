package com.fan.reptile;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.URLUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;

/**
 * @author zhangFan
 * @since 2021/12/1 下午 05:13
 */
public class Reptile {
    //静态资源输出路径
    static String out = "D:\\develop\\IdeaProjects\\reptile\\src\\main\\resources";
    //要爬取的静态页面url
    static URL index = URLUtil.url("http://192.168.189.132:80/index.php/login");

    public static void main(String[] args) throws IOException {
        Document document = Jsoup.connect(index.toString()).get();
        Elements scripts = document.select("script");
        for (Element script : scripts) {
            String src = script.attr("src");
            saveStaticResource(src);
        }

        Elements links = document.select("link");
        for (Element link : links) {
            String href = link.attr("href");
            saveStaticResource(href);
        }

        Elements imgs = document.select("img");
        for (Element img : imgs) {
            String src = img.attr("src");
            saveStaticResource(src);
        }
    }

    private static void saveStaticResource(String url) throws IOException {
        URL url1 = URLUtil.url(Reptile.index.getProtocol() + "://" + Reptile.index.getHost() + ":" + Reptile.index.getPort() + url);
        Document scriptDoc = Jsoup.connect(url1.toString()).get();
        String content = scriptDoc.body().html();
        //去除url中的参数
        String path = out + url1.getPath();
        FileUtil.writeString(content, FileUtil.file(path), scriptDoc.charset());
    }
}
