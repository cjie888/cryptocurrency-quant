package com.cjie.cryptocurrency.quant.api.huobi.util;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Map;

public class HuobiUtil {

    private static final Gson gson = new Gson();

    public static String toJson(Object src){
        return gson.toJson(src);
    }

    public static <T> T fromJson(String json,Class<T> clazz) {
        return gson.fromJson(json,clazz);
    }

    public static <T> T fromJson(String json, Type typeOfT){
        return gson.fromJson(json,typeOfT);
    }

    public static String hashMac256(String message, String secret) {

        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(message.getBytes()));
            return hash;
        }
        catch (Exception e){
            throw new RuntimeException("Unable to sign message.", e);
        }
    }

    // 加密
    public static String encode(String str) {
        String result = null;
        try {
            org.apache.commons.codec.binary.Base64.encodeBase64String(str.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    // 解密
    public static String decode(String s) {
        String result = null;
        try {
            result = new String(org.apache.commons.codec.binary.Base64.decodeBase64(s),"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static String buildQuery(Map<String, String> map) throws UnsupportedEncodingException {
        if(map == null || map.isEmpty()){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e:map.entrySet()){
            if(first){
                first = false;
            }else {
                sb.append("&");
            }
            sb.append( e.getKey() ).append("=").append( urlEncode(e.getValue()) );
        }
        return sb.toString();
    }

    public static String urlEncode(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s,"utf-8");
    }

    public static String uncompress(byte[] bytes) throws IOException {
        InputStream fin = new ByteArrayInputStream(bytes);
        BufferedInputStream in = new BufferedInputStream(fin);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
        final byte[] buffer = new byte[256];
        int n = 0;
        while (-1 != (n = gzIn.read(buffer))) {
            out.write(buffer, 0, n);
        }
        out.close();
        gzIn.close();
        return out.toString("utf-8");
    }

}
