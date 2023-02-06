package com.itheima;

import org.junit.jupiter.api.Test;

public class UploadTest {
    @Test
    public void test1(){
        String originalFilename = "sdd.sdgsdi.kjk.jpeg";
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        System.out.println(suffix);
    }

    @Test
    public void test2(){
        System.out.println("测试中文");
    }
}
