package com.fan.reptile;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author zhangFan
 * @since 2021/12/1 下午 05:13
 */
public class Reptile {
    //静态资源输出路径
    static String out = "E:\\Workspaces\\reptile\\src\\main\\resources";
    //要爬取的静态页面url
    static URL index;

    static {
        try {
            index = new URL("http://10.7.212.226/drupal/user/login");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        // 保存静态页面html
        saveStaticResource(index.toString());
        // 保存页面中引用的js，css，img等
        Document document = Jsoup.newSession().url(index).get();
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
        if (StrUtil.isEmpty(url)) {
            return;
        }
        URL url1;
        try {
            url1 = new URL(url);
        } catch (MalformedURLException e) {
            String protocol = Reptile.index.getProtocol();
            int defaultPort = "https".equals(protocol) ? 443 : 80;
            int port = Reptile.index.getPort();
            port = port == -1 ? defaultPort : port;
            url1 = new URL(protocol + "://" + Reptile.index.getHost() + ":" + port + url);
        }
        System.out.println("开始获取：" + url1);
        URLConnection urlConnection = url1.openConnection();
        InputStream is = null;
        try {
            is = urlConnection.getInputStream();
        } catch (IOException e) {
            System.out.println("连接失败！");
            return;
        }
        //去除url中的参数
        String path = out + url1.getPath();
        FileUtil.writeFromStream(is, FileUtil.file(path));
    }
}
