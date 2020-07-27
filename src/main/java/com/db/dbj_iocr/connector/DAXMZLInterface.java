package com.db.dbj_iocr.connector;

import com.db.dbj_iocr.utils.LoginUtils;
import kingdee.bos.webapi.client.K3CloudApiClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName DAXMZLInterface
 * @Author 夏俭琼
 * @Date 2020/4/29 16:18
 **/
@Component
public class DAXMZLInterface {
    /**
     * 档案项目资料 查
     *
     * @return
     * @throws Exception
     */
    public static List<List<Object>> DAXMZLQuery() throws Exception {
        K3CloudApiClient client = LoginUtils.getK3CloudApiClient();
        if (client == null) {
            return null;
        } else {
            String json = "{\n" +
                    "    \"FormId\": \"kaa3e54bec91d435baa2f62fe01ed17a1\",\n" +
                    "    \"FieldKeys\": \"F_File_Sign,F_File_BorrowingDATE,F_Ereturn_Date,FID\",\n" +
                    "    \"FilterString\": \"\",\n" +
                    "    \"OrderString\": \"\",\n" +
                    "    \"TopRowCount\": 0,\n" +
                    "    \"StartRow\": 0,\n" +
                    "    \"Limit\": 0\n" +
                    "}";
            List<List<Object>> list = client.executeBillQuery(json);
            System.out.println("档案项目资料"+list);
            if (list == null || list.size() == 0) {
                return null;
            }
            return list;
        }
    }

    /**
     * 档案项目资料 修改
     *
     * @return
     * @throws Exception 凌晨半
     */

    @Scheduled(cron = "0 0 23 * * ?")
    public static String DAXMZLUpdate() throws Exception {
        String sFormId = "kaa3e54bec91d435baa2f62fe01ed17a1";
        List<List<Object>> daxmzlQuery = DAXMZLInterface.DAXMZLQuery();
        if (daxmzlQuery != null) {
            String result = DAXMZLInterface.queryResult(daxmzlQuery, sFormId);
            System.out.println(result);
        }
        return null;
    }


    /**
     * 查询结果
     *
     * @param result
     * @param sFormId
     * @return
     * @throws Exception
     */
    public static String queryResult(List<List<Object>> result, String sFormId) throws Exception {
        if (result != null) {
            for (List<Object> objectList : result) {
                //档案借阅情况
                String F_File_Sign = String.valueOf(objectList.get(0));
                //档案借阅日期
                String F_File_BorrowingDATE = String.valueOf(objectList.get(1));
                //预计归还日期
                String F_Ereturn_Date = String.valueOf(objectList.get(2));
                String FID = String.valueOf(objectList.get(3));
                if (F_Ereturn_Date != "null") {
                    GSDAInterface.GSDASaveJk(F_File_Sign, F_Ereturn_Date, sFormId, FID);
                }
            }
        }
        return null;
    }
}
