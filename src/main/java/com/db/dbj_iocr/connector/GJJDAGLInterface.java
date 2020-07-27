package com.db.dbj_iocr.connector;

import com.db.dbj_iocr.utils.LoginUtils;
import kingdee.bos.webapi.client.K3CloudApiClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName GJJDAGLInterface
 * @Author 夏俭琼
 * @Date 2020/4/30 9:34
 **/
@Component
public class GJJDAGLInterface {

    /**
     * 公积金档案 查
     *
     * @return
     * @throws Exception
     */
    public static List<List<Object>> GJJDAGLQuery() throws Exception {
        K3CloudApiClient client = LoginUtils.getK3CloudApiClient();
        if (client == null) {
            return null;
        } else {
            String json = "{\n" +
                    "    \"FormId\": \"k789ffd3b2be24b8586db79fdb207f3a4\",\n" +
                    "    \"FieldKeys\": \"F_File_Sign,F_File_BorrowingDATE,F_Ereturn_Date,FID\",\n" +
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
     * 公积金档案 修改
     *
     * @return
     * @throws Exception
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public static String GJJDAGLUpdate() throws Exception {
        String sFormId = "k789ffd3b2be24b8586db79fdb207f3a4";
        List<List<Object>> gjjdaglQuery = GJJDAGLInterface.GJJDAGLQuery();
        if (gjjdaglQuery != null) {
            DAXMZLInterface.queryResult(gjjdaglQuery, sFormId);
        }
        return null;
    }
}
