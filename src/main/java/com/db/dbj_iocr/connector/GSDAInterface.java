package com.db.dbj_iocr.connector;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.db.dbj_iocr.utils.LoginUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kingdee.bos.webapi.client.K3CloudApiClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @ClassName GSDAInterface
 * @Author 夏俭琼
 * @Date 2020/4/29 9:34
 * <p>
 * 公司档案
 **/
@Component
public class GSDAInterface {
    /**
     * 公司档案 查询
     *
     * @return
     * @throws Exception
     */
    public static List<List<Object>> GSDAQuery() throws Exception {
        K3CloudApiClient client = LoginUtils.getK3CloudApiClient();
        if (client == null) {
            return null;
        } else {
            String json = "{\n" +
                    "    \"FormId\": \"k8f73d4e693e5454295ae6874bfe2d154\",\n" +
                    "    \"FieldKeys\": \"F_Mortage_Situ,F_RECEIPT_DATE,F_File_Sign,F_File_BorrowingDATE,F_Ereturn_Date,FID,FBillStatus,F_Mortage_Id\",\n" +
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
     * 公司档案 修改
     *
     * @return
     */

    @Scheduled(cron = "0 0 23 * * ?")
    public static String GSDAUpdate() throws Exception {
        String sFormId = "k8f73d4e693e5454295ae6874bfe2d154";
        int daySum = 0;
        List<List<Object>> gsdaQuery = GSDAInterface.GSDAQuery();
        if (gsdaQuery == null) {
            System.out.println("公司档案查询无结果");
        } else {
            for (List<Object> objectList : gsdaQuery) {
                //抵押情况
                String F_Mortage_Situ = String.valueOf(objectList.get(0));
                //收件日期 F_RECEIPT_DATE
                String F_RECEIPT_DATE = String.valueOf(objectList.get(1));
                // 档案借阅跟踪
                String F_File_Sign = String.valueOf(objectList.get(2));
//                 档案借阅日期
                String F_File_BorrowingDATE = String.valueOf(objectList.get(3));
                //  预计归还日期
                String F_Ereturn_Date = String.valueOf(objectList.get(4));
                // 实体主键
                String FID = String.valueOf(objectList.get(5));
                //  单据状态
                String FBillStatus = String.valueOf(objectList.get(6));
                //  抵押人身份证号
                String F_Mortage_Id = String.valueOf(objectList.get(7));
                if (F_RECEIPT_DATE != "null") {
                    daySum = GSDAInterface.daySum(F_RECEIPT_DATE);
                    int i = GSDAInterface.daysBetween(F_RECEIPT_DATE);
                    if (F_Mortage_Situ.equals("1") && daySum > 50 && !FBillStatus.equals("Z") && !F_Mortage_Situ.equals("2")) {
                        K3CloudApiClient client = LoginUtils.getK3CloudApiClient();
                        if (client != null) {
                            JSONObject json = new JSONObject();
                            json.put("Creator", "");
                            JSONArray array = new JSONArray();
                            json.put("NeedUpDateFields", array);
//                            json.put("NeedReturnFields", new String[]{"F_Mortage_Situ"});
//                            json.put("NeedUpDateFields", new String[]{"Model", "F_Mortage_Situ"});
                            JSONObject Model = new JSONObject();
                            Model.put("FID", FID);
                            Model.put("F_Mortage_Situ", "2");
                            json.put("Model", Model);
                            String result = client.save(sFormId, json.toString());
                            System.out.println("公司档案保存结果" + result);
                            JsonParser jp = new JsonParser();
                            //将json字符串转化成json对象
                            JsonObject jo = jp.parse(result).getAsJsonObject();
                            //获取city对应的值
                            String IsSuccess = jo.get("Result").getAsJsonObject().get("ResponseStatus")
                                    .getAsJsonObject().get("IsSuccess").getAsString();
                            if (IsSuccess.equals("true")) {
                                //注销登录 修改
                                ZXDLInterface.ZXDLUpdate(F_Mortage_Id);
                            }


                        }
                    }
                }
                if (F_Ereturn_Date != "null") {
                    GSDAInterface.GSDASaveJk(F_File_Sign, F_Ereturn_Date, sFormId, FID);
                }
            }
        }
        return null;
    }


    /**
     * 两个时间之间的天数差
     *
     * @param F_RECEIPT_DATE
     * @return
     */
    public static int daySum(String F_RECEIPT_DATE) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String word = F_RECEIPT_DATE.substring(0, F_RECEIPT_DATE.indexOf("T"));
        int days = 0;
        String format = df.format(new Date());
        if (word != null) {
            System.out.println(word);
            //现在
            Date fromDate1 = df.parse(format);
            long from1 = fromDate1.getTime();
            //过去
            Date toDate1 = df.parse(word);
            long to1 = toDate1.getTime();
            days = (int) ((from1 - to1) / (1000 * 60 * 60 * 24));
            System.out.println("两个时间之间的天数差为：" + days);
        }
        return days;
    }

    /**
     * 计算两个日期之间相差的天数
     *
     * @param F_RECEIPT_DATE 较小的时间
     * @return 相差天数
     * @throws ParseException
     */
    public static int daysBetween(String F_RECEIPT_DATE) throws ParseException {
        Calendar cal = Calendar.getInstance();
        int days = 0;
        long between_days = 0;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String format = df.format(new Date());
        String word = F_RECEIPT_DATE.substring(0, F_RECEIPT_DATE.indexOf("T"));
        if (word != null && word.length() > 0) {
            System.out.println(word);
            //现在
            Date bdate = df.parse(format);
            cal.setTime(bdate);
            long time2 = cal.getTimeInMillis();
            //过去
            Date smdate = df.parse(word);
            cal.setTime(smdate);
            long time1 = cal.getTimeInMillis();
            between_days = (time2 - time1) / (1000 * 3600 * 24);
        }
        return Integer.parseInt(String.valueOf(between_days));

    }

    /**
     * 预计与现在是否一样
     *
     * @param F_Ereturn_Date
     * @return
     * @throws Exception
     */
    public static Boolean YJJDaySum(String F_Ereturn_Date) throws Exception {
        if (F_Ereturn_Date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String f_Ereturn_Date = F_Ereturn_Date.substring(0, F_Ereturn_Date.indexOf("T"));
            if (f_Ereturn_Date != null && f_Ereturn_Date.length() > 0) {
                String format = sdf.format(new Date());
                // 预计归还日期
                Date fromDate1 = sdf.parse(f_Ereturn_Date);
                // 现在
                Date nowDate = sdf.parse(format);
                String from1 = String.valueOf(fromDate1.getTime());
                String to1 = String.valueOf(nowDate.getTime());
                int i = from1.compareTo(to1);
                System.out.println(i);
                if (i >= 0) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return null;
    }

    /**
     * 调用公司档案保存接口
     *
     * @param F_File_Sign
     * @param
     * @param F_Ereturn_Date
     * @param sFormId
     * @return
     * @throws Exception
     */
    public static String GSDASaveJk(String F_File_Sign, String F_Ereturn_Date, String sFormId, String FID) throws Exception {
        Boolean XTTime = GSDAInterface.YJJDaySum(F_Ereturn_Date);
        if (F_File_Sign.equals("1") && XTTime == true&& !F_File_Sign.equals("2")) {
            K3CloudApiClient client = LoginUtils.getK3CloudApiClient();
            if (client != null) {
                JSONObject json = new JSONObject();
                JSONArray array = new JSONArray();
                JSONObject Model = new JSONObject();
                json.put("Creator", "");
                json.put("NeedUpDateFields", array);
                Model.put("FID", FID);
                Model.put("F_File_Sign", "2");
                json.put("Model", Model);
                String result = client.save(sFormId, json.toString());
                System.out.println("公司档案保存结果2" + result);
            }
        }
        return null;
    }
}



