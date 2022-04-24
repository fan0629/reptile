package com.fan.reptile;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhangFan
 * @since 2022/02/14 下午 05:13
 */
@Slf4j
public class Reptile {
    /**
     * 静态资源输出路径
     */
    private final String out;
    /**
     * 要爬取的静态页面url
     */
    private String staticHtml;
    /**
     * 爬取深度: 0为首页，1，2，3
     */
    private final int depth;

    Pattern pattern = Pattern.compile("url\\(([^)]*)\\)");
    String rootDir = "/";

    private final Set<String> urlSet = new HashSet<>();
    private URL targetUrl;
    @Getter
    private final String host;
    @Getter
    private final int port;
    @Getter
    private final String path;
    @Getter
    private boolean success = false;

    public Reptile(String outPath, String staticHtml, int depth) {
        this.out = outPath;
        this.staticHtml = staticHtml;
        this.depth = depth;
        try {
            targetUrl = new URL(staticHtml);
            host = targetUrl.getHost();
            port = targetUrl.getPort() == -1 ? targetUrl.getDefaultPort() : targetUrl.getPort();
            path = targetUrl.getPath();
        } catch (MalformedURLException e) {
            log.error("地址不合法");
            throw new RuntimeException("地址不合法");
        }
        trustEveryone();
    }

    public void start() {
        saveDocument(staticHtml, 1);
    }

    private void saveDocument(String url, int dep) {
        List<Future<?>> resultList = new ArrayList<>();
        URL index;
        try {
            index = new URL(url);
        } catch (MalformedURLException e) {
            log.error("地址不合法");
            return;
        }
        // 保存静态页面html
        File file = saveStaticResource(index.toString());
        if (file == null) {
            return;
        }
        // 保存页面中引用的js，css，img等
        Document document;
        try {
            document = Jsoup.parse(file, CharsetUtil.GBK);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Element base = document.selectFirst("base");
        if (base != null) {
            String baseHref = base.attr("href");
            staticHtml = restoreFullUrl(baseHref, staticHtml);
        }

        Elements scripts = document.select("script");
        for (Element script : scripts) {
            String src = script.attr("src");
            if (!src.isEmpty()) {
                Future<?> future = ReptileTaskExecutor.submit(() -> saveStaticResource(restoreFullUrl(src, url)));
                resultList.add(future);
            }
        }

        Elements links = document.select("link");
        for (Element link : links) {
            String href = link.attr("href");
            if (!href.isEmpty()) {
                Future<?> future = ReptileTaskExecutor.submit(() -> saveStaticResource(restoreFullUrl(href, url)));
                resultList.add(future);
            }
        }

        Elements imgs = document.select("img");
        for (Element img : imgs) {
            String src = img.attr("src");
            if (!src.isEmpty()) {
                Future<?> future = ReptileTaskExecutor.submit(() -> saveStaticResource(restoreFullUrl(src, url)));
                resultList.add(future);
            }
            String dataSrc = img.attr("data-src");
            if (!dataSrc.isEmpty()) {
                Future<?> future = ReptileTaskExecutor.submit(() -> saveStaticResource(restoreFullUrl(dataSrc, url)));
                resultList.add(future);
            }
        }

        if (dep++ < this.depth) {
            log.info("depth = " + dep);
            Elements a = document.select("a");
            for (Element element : a) {
                String href = element.attr("href");
                String fullUrl = restoreFullUrl(href, url);
                if (!fullUrl.isEmpty() && !fullUrl.equals(staticHtml)) {
                    int finalDep = dep;
                    Future<?> future = ReptileTaskExecutor.submit(() -> saveDocument(fullUrl, finalDep));
                    resultList.add(future);
                }
            }
        }
        resultList.stream().filter(Objects::nonNull).forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        success = true;
    }

    private File saveStaticResource(String url) {
        List<Future<?>> resultList = new ArrayList<>();
        long isStart = System.currentTimeMillis();
        if (StrUtil.isEmpty(url)) {
            return null;
        }
        URL url1;
        try {
            url1 = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        if (!url1.getHost().equals(host)) {
            return null;
        }
        if (urlSet.contains(url1.getPath())) {
            return null;
        }
        urlSet.add(url1.getPath());
        log.info("开始获取：" + url1);
        InputStream is;
        URLConnection urlConnection;
        try {
            urlConnection = url1.openConnection();
            is = urlConnection.getInputStream();
        } catch (IOException e) {
            log.error("连接失败！");
            return null;
        }
        //去除url中的参数
        String urlPath = url1.getPath();
        if (rootDir.equals(urlPath)) {
            urlPath = "/index.html";
        }
        if (urlPath.endsWith("/")) {
            urlPath = urlPath.substring(0, urlPath.length() - 1) + ".html";
        }
        String path = out + urlPath;
        File file = FileUtil.file(path);
        FileUtil.writeFromStream(is, file);
        long isEnd = System.currentTimeMillis();
        log.info("读写资源耗时 = " + (isEnd - isStart));
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
            String fullUrl = restoreFullUrl(group, urlPath);
            if (!fullUrl.isEmpty()) {
                Future<?> future = ReptileTaskExecutor.submit(() -> saveStaticResource(fullUrl));
                resultList.add(future);
            }
        }
        resultList.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        return file;
    }

    private String restoreFullUrl(String relativePath, String baseUrl) {
        if (relativePath.contains("data:image/png;base64,") || relativePath.contains("data:image/gif;base64,")) {
            return "";
        }
        return URLUtil.completeUrl(String.valueOf(baseUrl), relativePath);
    }

    /**
     * 信任任何站点，实现https页面的正常访问
     */
    public static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }
}
