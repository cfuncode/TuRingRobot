package com.cjz.turingrobot;

import android.util.Log;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    private static final String USERID = "623125";
    private static final String KEY = "d1903503f2304b2ca3883f38b2a0238e";
    private static final String sendJson = "{\"reqType\": 0,\"perception\": {\"inputText\": {\"text\": \"%s\"},\"inputImage\": {\"url\": \"imageUrl\"},\"selfInfo\": {\"location\": {\"city\": \"北京\",\"province\": \"北京\",\"street\": \"信息路\"}}},\"userInfo\": {\"apiKey\": \""+KEY+"\",\"userId\": \""+USERID+"\"}}";
    private String getSendJson(String sendMsg){
        return String.format(sendJson,sendMsg);
    }

    @Test
    public void test(){
        System.out.println(getSendJson("666"));
    }
}