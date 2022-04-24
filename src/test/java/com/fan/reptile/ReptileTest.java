package com.fan.reptile;

import cn.hutool.core.util.URLUtil;
import org.junit.jupiter.api.Test;

/**
 * @author zhang_fan
 * @since 2022/4/11 下午 04:30
 */
class ReptileTest {
    static String out = "D:\\develop\\IdeaProjects\\reptile\\src\\main\\resources\\web";
    static String staticHtml = "http://10.7.211.5:18081/#/home";
    static int depth = 1;

    @Test
    void test() {
        long start = System.currentTimeMillis();
        Reptile reptile = new Reptile(out, staticHtml, depth);
        reptile.start();
        System.out.println("耗时 = " + (System.currentTimeMillis() - start));
    }

    @Test
    void urlTest() {
        String url = URLUtil.completeUrl("http://10.7.211.5:18081/#/home", "123");
        System.out.println("url = " + url);
    }
}