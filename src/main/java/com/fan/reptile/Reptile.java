package com.fan.reptile;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhangFan
 * @since 2022/02/14 下午 05:13
 */
public class Reptile {
    /**
     * 静态资源输出路径
     */
    static String out = "D:\\develop\\IdeaProjects\\reptile\\src\\main\\resources";
    /**
     * 要爬取的静态页面url
     */
    static String staticHtml = "https://www.freebuf.com/vuls/289282.html";
    static URL index;

    static Pattern pattern = Pattern.compile("url\\(([^)]*)\\)");

    static String rootDir = "/";
    static String currentDir = "./";
    static String parentDir = "../";

    public static void main(String[] args) throws IOException {
        index = new URL(staticHtml);
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
            if (url.startsWith(rootDir)) {
                //根目录
                String path = index.getPath();
                url = staticHtml.replace(path, url);
            } else if (url.startsWith(currentDir)) {
                //当前目录
                url = staticHtml.replaceAll("/[^/]*$", url.replace("./", "/"));
            } else if (url.startsWith(parentDir)) {
                //上级目录
                url = staticHtml.replaceAll("/[^/]*/[^/]*$", url.replace("../", "/"));
            } else {
                url = staticHtml.replaceAll("/[^/]*$", "/" + url);
            }
            url1 = new URL(url);
        }
        System.out.println("开始获取：" + url1);
        URLConnection urlConnection = url1.openConnection();
        InputStream is;
        try {
            is = urlConnection.getInputStream();
        } catch (IOException e) {
            System.out.println("连接失败！");
            return;
        }
        //去除url中的参数
        String path = out + url1.getPath();
        File file = FileUtil.file(path);
        FileUtil.writeFromStream(is, file);
        String contentEncoding = urlConnection.getContentEncoding();
        if (contentEncoding == null) {
            contentEncoding = "GBK";
        }
        Charset charset = CharsetUtil.charset(contentEncoding);
        String content = FileUtil.readString(file, charset);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String group = matcher.group(1);
            if (group.startsWith("'") || group.startsWith("\"")) {
                group = group.substring(1, group.length() - 1);
            }
            if (group.startsWith("../")) {
                String s = url1.toString().replaceAll("/[^/]*/[^/]*$", group.replace("../", "/"));
                if (!s.isEmpty()) {
                    saveStaticResource(s);
                }
            } else if (group.startsWith("./")) {
                String s = url1.toString().replaceAll("/[^/]*$", group.replace("./", "/"));
                if (!s.isEmpty()) {
                    saveStaticResource(s);
                }
            } else if (group.startsWith("/")) {
                String s = staticHtml.replace(index.getPath(), group);
                if (!s.isEmpty()) {
                    saveStaticResource(s);
                }
            } else {
                System.out.println("group = " + group);
            }
        }
    }
}
