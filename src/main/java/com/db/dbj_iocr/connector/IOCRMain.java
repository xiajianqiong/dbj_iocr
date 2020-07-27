package com.db.dbj_iocr.connector;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.db.dbj_iocr.entity.JSONInfo;
import com.db.dbj_iocr.entity.OAuthInfo;
import com.db.dbj_iocr.utils.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kingdee.bos.webapi.client.K3CloudApiClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName ocrMain
 * @Author 夏俭琼
 * @Date 2020/4/22 9:44
 **/
@Component
public class IOCRMain {
    /**
     * 获取token
     *
     * @return
     */
    public static String getToken() {
        // 百度云 获取Access Token
        String accessTokenUrl = "https://aip.baidubce.com/oauth/2.0/token?client_id=";
        String apiKey = "MgL4b1zqKKf7WVHqYepwE28R";
        String secretKey = "SUyzgWwrQSOOHdXfQ8hNhRLHlEZzAqcF";

        String tokenUrl = accessTokenUrl + apiKey +
                "&client_secret=" + secretKey +
                "&grant_type=client_credentials";
        RestTemplate restTemplate = new RestTemplate();
        String str = restTemplate.getForObject(tokenUrl, String.class);

        //JSON 转实体
        ObjectMapper objectMapper = new ObjectMapper();
        String accessToken = null;
        try {
            OAuthInfo oAuthInfo = objectMapper.readValue(str, OAuthInfo.class);
            accessToken = oAuthInfo.getAccess_token();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return accessToken;
    }


    /**
     * 每五秒执行一次
     */
   @Scheduled(cron = "0/2 * * * * ?")
    public static void iocrMain() {
        Pattern pattern = Pattern.compile("[0-9]*");
        JsonParser jsonParser = new JsonParser();
        String[] imgStrs = {"jpg", "png"};
        // 获取目录下的图片
        File dir = new File("D:\\img");
        File[] files = dir.listFiles();
        if (files.length != 0) {
            //获取JSON路径
            String recogniseUrl = "https://aip.baidubce.com/rest/2.0/solution/v1/iocr/recognise";
            for (int i = 0; i < files.length; i++) {
                //过滤非图片
                String fileType = files[i].getName().substring(files[i].getName().lastIndexOf('.') + 1, files[i].getName().length());
                for (int t = 0; t < imgStrs.length; t++) {
                    if (imgStrs[t].equals(fileType.toLowerCase())) {
                        // 图片路径
                        String filePath = files[i].getPath();
                        System.out.println(filePath);
                        // 请求模板识别
                        String result = null;
                        try {
                            byte[] imgData = FileUtil.readFileByBytes(filePath);
                            String imgStr = Base64Util.encode(imgData);
                            // 请求分类器参数
                            String classifierParams = "classifierId=4&image=" + URLEncoder.encode(imgStr, "UTF-8");
                            // 获取token
                            String accessToken = IOCRMain.getToken();
                            if (null != accessToken) {
                                //请求分类器识别
                                result = HttpUtil.post(recogniseUrl, accessToken, classifierParams);
                                System.out.println("分类器" + result);
                            }
                        // json数组存到list中
                        List<JSONInfo> list = new ArrayList<>();
//                        try {
                            if (null != result) {
                                //获取JSON 内容
                                JSONObject jsonobject = JSONObject.parseObject(result);
                                String data = jsonobject.getString("data");
                                JSONObject dataJsono = JSONObject.parseObject(data);
                                String ret = dataJsono.getString("ret");
                                List<JSONObject> jsonObjectList = JSON.parseArray(ret, JSONObject.class);
                                // 遍历json数组并取出想要的数据
                                for (JSONObject jsonObject2 : jsonObjectList) {
                                    String word_name = jsonObject2.getString("word_name");
                                    String word = jsonObject2.getString("word");
                                    JSONInfo jsonInfo = new JSONInfo();
                                    jsonInfo.setWord(word);
                                    jsonInfo.setWord_name(word_name);
                                    list.add(jsonInfo);
                                }
                            }
                            System.out.println(list);
                            //业务id
                            String sFormId = "k864ae4760c414adaab3f92fdf7b531b4";
                            K3CloudApiClient client = null;
                            client = LoginUtils.getK3CloudApiClient();
                            if (null != client) {
                                JSONObject json = new JSONObject();
                                json.put("Creator", "");
                                JSONArray array = new JSONArray();
                                json.put("NeedUpDateFields", array);
                                JSONObject Model = new JSONObject();
                                //获取JSON 内容
                                for (int j = 0; j < list.size(); j++) {
                                    JSONInfo s = list.get(j);
                                    if (StringUtils.equals("借款人姓名", s.getWord_name())) {
                                        Model.put("F_Name_Text", s.getWord());
                                    }
                                    if (StringUtils.equals("借款人婚姻状况", s.getWord_name())) {
                                        Model.put("F_Mal_Status", s.getWord());
                                    }
                                    if (StringUtils.equals("借款人联系电话", s.getWord_name())) {
                                        Model.put("F_Tel_Nume", s.getWord());
                                    }
                                    if (StringUtils.equals("借款人工作单位", s.getWord_name())) {
                                        Model.put("F_Work_Place", s.getWord());
                                    }
                                    if (StringUtils.equals("借款人身份证", s.getWord_name())) {
                                        Model.put("F_Ident_Card1", s.getWord());
                                    }
                                    if (StringUtils.equals("借款人月收入", s.getWord_name())) {
                                        Model.put("F_Monthly_Pro1", s.getWord());
                                    }
                                    if (StringUtils.equals("借款人性别", s.getWord_name())) {
                                        Model.put("F_Gender_Text", s.getWord());
                                    }
                                    if (StringUtils.equals("借款人公积金起缴时间", s.getWord_name())) {
                                        Model.put("F_borrow_start", s.getWord());
                                    }
                                    if (StringUtils.equals("借款人公积金缴存账号", s.getWord_name())) {
                                        Model.put("F_borrower_Account", s.getWord());
                                    }
                                    if (StringUtils.equals("邮编", s.getWord_name())) {
                                        Model.put("F_Zip_Code", s.getWord());
                                    }
                                    if (StringUtils.equals("借款人公积金缴存余额", s.getWord_name())) {
                                        Model.put("F_Borrowremain_sum", s.getWord());
                                    }
                                    if (StringUtils.equals("借款人现详细住址", s.getWord_name())) {
                                        Model.put("F_Borrow_Address", s.getWord());
                                    }
                                    if (StringUtils.equals("配偶姓名", s.getWord_name())) {
                                        Model.put("F_Spouse_Name", s.getWord());
                                    }
                                    if (StringUtils.equals("配偶联系电话", s.getWord_name())) {
                                        Model.put("F_Spouse_Tel", s.getWord());
                                    }
                                    if (StringUtils.equals("配偶身份证", s.getWord_name())) {
                                        Model.put("F_Spouse_Id", s.getWord());
                                    }
                                    if (StringUtils.equals("配偶公积金起缴时间", s.getWord_name())) {
                                        Model.put("F_Spouse_Sta", s.getWord());
                                    }
                                    if (StringUtils.equals("配偶公积金缴存账号", s.getWord_name())) {
                                        Model.put("F_Spouse_Account", s.getWord());
                                    }
                                    if (StringUtils.equals("配偶工作单位", s.getWord_name())) {
                                        Model.put("F_Spouse_WorkPlace ", s.getWord());
                                    }
                                    if (StringUtils.equals("购房详细地址", s.getWord_name())) {
                                        Model.put("F_Detailed_Adderss", s.getWord());
                                    }
                                    if (StringUtils.equals("房屋类型", s.getWord_name())) {
                                        Model.put("F_room_Way1", s.getWord());
                                    }
                                    if (StringUtils.equals("合同登记号", s.getWord_name())) {
                                        Model.put("F_Contract_ResNo", s.getWord());
                                    }

                                    if (StringUtils.equals("公积金贷款第", s.getWord_name())) {
                                        Model.put("F_Loan_Time", s.getWord());
                                    }
                                    if (StringUtils.equals("配偶公积金缴存余额", s.getWord_name())) {
                                        Model.put("F_Souseremain_sum1", s.getWord());
                                    }
                                    if (StringUtils.equals("房屋评估价值", s.getWord_name())) {
                                        Model.put("F_House_Worth", s.getWord());
                                    }
                                    if (StringUtils.equals("售房单位", s.getWord_name())) {
                                        Model.put("F_Sale_Unit", s.getWord());
                                    }
                                    if (StringUtils.equals("房屋总价", s.getWord_name())) {
                                        Model.put("F_Total_Price", s.getWord());
                                    }
                                    if (StringUtils.equals("建筑面积", s.getWord_name())) {
                                        String sb = s.getWord();
                                        boolean contains = sb.contains("m");
                                        if (contains) {
                                            String word = sb.substring(0, sb.indexOf("m"));
                                            System.out.println(word);
                                            Model.put("F_Covered_Area", word);
                                        } else {
                                            Model.put("F_Covered_Area", sb);
                                        }
                                    }
                                    if (StringUtils.equals("单价", s.getWord_name())) {
                                        String sb = s.getWord();
                                        boolean contains = sb.contains("元");
                                        if (contains) {
                                            String word = sb.substring(0, sb.indexOf("元"));
                                            System.out.println(word);
                                            Model.put("F_Unit_price", word);
                                        } else {
                                            Model.put("F_Unit_price", sb);
                                        }
                                    }
                                    if (StringUtils.equals("首付款金额", s.getWord_name())) {
                                        String sb = s.getWord();
                                        boolean contains = sb.contains("元");
                                        if (contains) {
                                            String word = sb.substring(0, sb.indexOf("元"));
                                            Model.put("F_Frist_Pay", word);
                                        } else {
                                            Model.put("F_Frist_Pay", sb);
                                        }
                                    }
                                    if (StringUtils.equals("商贷剩余本金", s.getWord_name())) {
                                        Model.put("F_Remaining_Principal", s.getWord());
                                    }
                                    if (StringUtils.equals("申请公积金借款金额", s.getWord_name())) {
                                        String word = s.getWord();
                                        Matcher isNum = pattern.matcher(word);
                                        if (!isNum.matches()) {
                                            //大写中文
                                            Model.put("F_Applica_Amoutcom", s.getWord());
                                            // 公积金借款金额  转数字
                                            BigDecimal F_APPLICA_AMOUTCOM1 = ChineseAmountUtil.chinese2Number(s.getWord());
                                            Model.put("F_APPLICA_AMOUTCOM1", String.valueOf(F_APPLICA_AMOUTCOM1));
                                        } else {
                                            Model.put("F_APPLICA_AMOUTCOM1", s.getWord());
                                        }
                                    }
                                    if (StringUtils.equals("申请商业借款金额", s.getWord_name())) {
                                        String word = s.getWord();
                                        Matcher isNum = pattern.matcher(word);
                                        if (!isNum.matches()) {
                                            //大写中文
                                            Model.put("F_Applica_Amoutpub", s.getWord());
                                            // 商业借款金额  转数字
                                            BigDecimal F_APPLICA_AMOUTPUB1 = ChineseAmountUtil.chinese2Number(s.getWord());
                                            Model.put("F_APPLICA_AMOUTPUB1", String.valueOf(F_APPLICA_AMOUTPUB1));
                                        } else {
                                            Model.put("F_APPLICA_AMOUTPUB1", s.getWord());
                                        }
                                    }
                                    if (StringUtils.equals("公积金月还款本息", s.getWord_name())) {
                                        Model.put("F_Public_Repayment", s.getWord());
                                    }
                                    if (StringUtils.equals("商业贷款月还款本息", s.getWord_name())) {
                                        Model.put("F_Com_Repayment", s.getWord());
                                    }
                                    if (StringUtils.equals("贷款期限", s.getWord_name())) {
                                        String sb = s.getWord();
                                        boolean contains = sb.contains("年");
                                        if (contains) {
                                            String word = sb.substring(0, sb.indexOf("年"));
                                            Model.put("F_Loan_Term", word);
                                        } else {
                                            Model.put("F_Loan_Term", sb);
                                        }
                                    }
                                    if (StringUtils.equals("贷款担保类型", s.getWord_name())) {
                                        Model.put("F_Guarantee_Type", s.getWord());
                                    }
                                    if (StringUtils.equals("申请人及配偶月收入之和", s.getWord_name())) {
                                        Model.put("F_Income_Sum", s.getWord());
                                    }
                                    if (StringUtils.equals("公积金贷款利率", s.getWord_name())) {
                                        Model.put("F_ZFDB_Rate", s.getWord());
                                    }
                                    if (StringUtils.equals("还款方式", s.getWord_name())) {
                                        Model.put("F_Payment_Way", s.getWord());
                                    }
                                    System.out.println(s.getWord_name() + "  " + s.getWord());
                                }
                                // 申请商业借款金额(大写)
                                String F_Applica_Amoutpub1 = Model.getString("F_APPLICA_AMOUTPUB1");
                                if (F_Applica_Amoutpub1 == null || F_Applica_Amoutpub1.equals("0.00")) {
                                    Model.put("F_Loan_Way", "2");
                                } else {
                                    Model.put("F_Loan_Way", "1");
                                }
                                json.put("Model", Model);
                                System.out.println("当前时间:"+ new Date());
                                String res = client.save(sFormId, json.toString());
                                //将json字符串转化成json对象
                                JsonObject jo = jsonParser.parse(res).getAsJsonObject();
                                String IsSuccess = jo.get("Result").getAsJsonObject().get("ResponseStatus")
                                        .getAsJsonObject().get("IsSuccess").getAsString();
                                System.out.println(IsSuccess);
                                if (IsSuccess.equals("true")) {
                                    files[i].delete();
                                    System.out.println("业务受理单保存成功"+ res);
                                }else {
                                    System.out.println("业务受理单接口调用错误"+ res);
                                }
                            }
                        } catch (Exception e) {
                            files[i].delete();
                            e.printStackTrace();
                            System.out.println("百度识别异常或找到模板但无法识别");
                        }
                    }

                }
            }
        } else {
            System.out.println("此文件此时，无图片");
        }
    }


}
