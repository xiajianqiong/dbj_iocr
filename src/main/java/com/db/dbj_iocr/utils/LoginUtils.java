package com.db.dbj_iocr.utils;


import kingdee.bos.webapi.client.K3CloudApiClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class LoginUtils {


    private LoginUtils() {
    }

    private static String K3CloudURL = "http://192.168.1.121/k3cloud/";
//    private static String dbId = "5da7cab67045eb";// 5e358a1ba7a9ea（正式账套id）
    private static String dbId = "5e90171e9155a6";
    private static String uid = "Administrator";
    private static String pwd = "888888";
    private static int lang = 2052;
    private static K3CloudApiClient apiClient = null;

    @Cacheable(value = "getK3CloudApiClient", key = "k1")
    public static K3CloudApiClient getK3CloudApiClient() throws Exception {
        if (apiClient == null) {
            apiClient = new K3CloudApiClient(K3CloudURL);
            if (apiClient.login(dbId, uid, pwd, lang)) {
                System.out.println(apiClient);
                return apiClient;
            } else {
                return null;
            }
        } else {
            return apiClient;
        }
    }
}
