package com.db.dbj_iocr.connector;

import com.db.dbj_iocr.utils.LoginUtils;
import kingdee.bos.webapi.client.K3CloudApiClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName BDCDYInterface
 * @Author 夏俭琼
 * @Date 2020/4/30 9:20
 **/
@Component
public class BDCDYInterface {
    /**
     *  不动产抵押档案 查
     *
     * @return
     * @throws Exception
     */
    public static List<List<Object>> BDCDYQuery() throws Exception {
        K3CloudApiClient client = LoginUtils.getK3CloudApiClient();
        if (client == null) {
            return null;
        } else {
            String json = "{\n" +
                    "    \"FormId\": \"kd4eedf150ae7463e931384b3a203c174\",\n" +
                    "    \"FieldKeys\": \"F_File_Sign,F_File_BorrowingDATE,F_Ereturn_Date,FID\",\n" +
                    "    \"FilterString\": \"\",\n" +
                    "    \"OrderString\": \"\",\n" +
                    "    \"TopRowCount\": 0,\n" +
                    "    \"StartRow\": 0,\n" +
                    "    \"Limit\": 0\n" +
                    "}";
            List<List<Object>> list = client.executeBillQuery(json);
            System.out.println("不动产抵押档案查询"+list);
            if (list == null || list.size() == 0) {
                return null;
            }
            return list;
        }
    }

    /**
     *  不动产抵押档案 修改
     * @return
     * @throws Exception
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public static String BDCDYUpdate() throws Exception {
        String sFormId = "kd4eedf150ae7463e931384b3a203c174";
        List<List<Object>> bdcdyQuery = BDCDYInterface.BDCDYQuery();
        if (bdcdyQuery != null) {
            DAXMZLInterface.queryResult(bdcdyQuery, sFormId);
        }
        return null;
    }


}

