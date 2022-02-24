package com.fan.reptile;

import cn.hutool.core.io.FileUtil;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author zhangFan
 * @since 2022/2/24 下午 01:40
 */
public class HttpUnitCraw {
    public static void main(String[] args) throws Exception {
        HttpUnitCraw crawl = new HttpUnitCraw();
        String url = "http://192.168.1.18/top_index.asp";
        System.out.println("----------------------抓取页面时解析js-------------------");
        crawl.crawlPageWithAnalyseJs(url);
    }

    /**
     * 功能描述：抓取页面时并解析页面的js
     * @param url 页面url
     * @throws Exception 异常
     */
    public void crawlPageWithAnalyseJs(String url) throws Exception{
        // 1.创建连接client
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        // 2.设置连接的相关选项
        // 需要解析css
        webClient.getOptions().setCssEnabled(true);
        // 需要解析js
        webClient.getOptions().setJavaScriptEnabled(true);
        // 解析js出错时不抛异常
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        // 超时时间  ms
        webClient.getOptions().setTimeout(30000);
        // 3.抓取页面
        HtmlPage page = webClient.getPage(url);
        // 等侍js脚本执行完成
        webClient.waitForBackgroundJavaScript(30000);
        // 4.保存页面
        page.save(FileUtil.file("D:\\develop\\IdeaProjects\\reptile\\src\\main\\resources\\page"));
        // 5.关闭模拟的窗口
        webClient.close();
    }
}
