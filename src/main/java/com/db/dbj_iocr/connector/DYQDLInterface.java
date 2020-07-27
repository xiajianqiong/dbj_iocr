package com.db.dbj_iocr.connector;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.db.dbj_iocr.utils.LoginUtils;
import kingdee.bos.webapi.client.K3CloudApiClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName DYQDLInterface
 * @Author 夏俭琼
 * @Date 2020/4/29 14:27
 * 抵押权登录
 **/
@Component
public class DYQDLInterface {
    /**
     * 抵押权登录 查询
     *
     * @return
     * @throws Exception
     */
    public static List<List<Object>> DYQDLQuery() throws Exception {
        K3CloudApiClient client = LoginUtils.getK3CloudApiClient();
        if (client == null) {
            return null;
        } else {
            String json = "{\n" +
                    "    \"FormId\": \"k88d0c0fe378f4f52b562a0490fe12f15\",\n" +
                    "    \"FieldKeys\": \"F_Mortgage_Situation,F_sign_DATE,FID\",\n" +
                    "    \"FilterString\": \"\",\n" +
                    "    \"OrderString\": \"\",\n" +
                    "    \"TopRowCount\": 0,\n" +
                    "    \"StartRow\": 0,\n" +
                    "    \"Limit\": 0\n" +
                    "}";
            List<List<Object>> list = client.executeBillQuery(json);
            System.out.println(list);
            if (list == null || list.size() == 0) {
                return null;
            }
            return list;
        }
    }

    /**
     * 抵押权登录 修改
     *
     * @return
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public static String DYQDLUpdate() throws Exception {
        K3CloudApiClient client = LoginUtils.getK3CloudApiClient();
        String sFormId = "k88d0c0fe378f4f52b562a0490fe12f15";
        List<List<Object>> dyqdlQuery = DYQDLInterface.DYQDLQuery();
        int daySum = 0;
        String result = null;
        if (dyqdlQuery == null) {
            System.out.println("抵押权登录查询无结果");
            return null;
        } else {
            for (List<Object> objectList : dyqdlQuery) {
                //抵押情况
                String F_Mortgage_Situstion = String.valueOf(objectList.get(0));
                // 递交抵押登记日期
                String F_sign_DATE = String.valueOf(objectList.get(1));
                String FID = String.valueOf(objectList.get(2));
                if (F_sign_DATE != "null") {
                    daySum = GSDAInterface.daySum(F_sign_DATE);
                    if (F_Mortgage_Situstion.equals("1") && daySum > 30 || F_Mortgage_Situstion.equals(" ") && !F_Mortgage_Situstion.equals("2")) {
                        if (client == null) {
                            return null;
                        } else {
                            JSONObject json = new JSONObject();
                            JSONArray array = new JSONArray();
                            json.put("Creator", "");
                            json.put("NeedUpDateFields", array);
                            JSONObject Model = new JSONObject();
                            Model.put("FID", FID);
                            Model.put("F_Mortgage_Situation", "2");
                            json.put("Model", Model);
                            result = client.save(sFormId, json.toString());
                            System.out.println("抵押权登录保存结果" + result);
                        }
                    }
                }
            }
            return null;
        }

    }
}