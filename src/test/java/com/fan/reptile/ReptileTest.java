package com.fan.reptile;

import cn.hutool.core.io.FileUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
    void copyTest() throws FileNotFoundException {
        long start = System.currentTimeMillis();
        FileInputStream in = new FileInputStream(out + "\\assets\\application-c713cd7dc8228e6522617974b28ae4b0aaee0c42a7301a1e586d0c82c34f20cd.css");
        FileUtil.writeFromStream(in, new File(out + "test.css"));
        System.out.println("耗时 = " + (System.currentTimeMillis() - start));
    }
}