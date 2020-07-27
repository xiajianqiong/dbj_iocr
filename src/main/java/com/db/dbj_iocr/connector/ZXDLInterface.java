package com.db.dbj_iocr.connector;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.db.dbj_iocr.utils.LoginUtils;
import kingdee.bos.webapi.client.K3CloudApiClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName ZXInterface
 * @Author 夏俭琼
 * @Date 2020/5/12 9:18
 **/
@Component
public class ZXDLInterface {
    /**
     * 注销登录业务 查
     *
     * @return
     * @throws Exception
     */
    public static List<List<Object>> ZXDLQuery() throws Exception {
        K3CloudApiClient client = LoginUtils.getK3CloudApiClient();
        if (client == null) {
            return null;
        } else {
            String json = "{\n" +
                    "    \"FormId\": \"k2c4da824dd2b45ac9ae27d0c74ac6e0b\",\n" +
                    "    \"FieldKeys\": \"FID,F_MORTAGE_SITU,F_Mortage_Id\",\n" +
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
     * 注销登录 修改
     *
     * @return
     */
    public static String ZXDLUpdate(String F_Mortage_Id) throws Exception {
        String result = null;
        String sFormId = "k8f73d4e693e5454295ae6874bfe2d154";
        List<List<Object>> zxdlQuery = ZXDLInterface.ZXDLQuery();
        if (zxdlQuery == null) {
            System.out.println("注销登录业务查询无结果");
        } else {
            for (List<Object> objectList : zxdlQuery) {
                // 实体主键
                String FID = String.valueOf(objectList.get(0));
                //抵押情况
                String F_MORTAGE_SITU = String.valueOf(objectList.get(1));
                //抵押人身份证号
                String ZXDL_F_Mortage_Id = String.valueOf(objectList.get(2));
                if (F_MORTAGE_SITU != "null" && F_Mortage_Id.equals(ZXDL_F_Mortage_Id) && !F_MORTAGE_SITU.equals("2")) {
                    K3CloudApiClient client = LoginUtils.getK3CloudApiClient();
                    if (client != null) {
                        JSONObject json = new JSONObject();
                        JSONArray array = new JSONArray();
                        json.put("Creator", "");
                        json.put("NeedUpDateFields", array);
                        JSONObject Model = new JSONObject();
                        Model.put("FID", FID);
                        Model.put("F_MORTAGE_SITU", "2");
                        json.put("Model", Model);
                        result = client.save(sFormId, json.toString());
                        System.out.println("注销登录保存结果" + result);
                    }
                }
            }
        }
        return result;
    }
}