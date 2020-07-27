package com.db.dbj_iocr.entity;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName OAuthInfo
 * @Author 夏俭琼
 * @Date 2020/4/15 9:09
 **/
@Data
public class OAuthInfo {
    /**
     * 全局token
     */
    private String access_token;
    /**
     *  刷新token
     */
    private String refresh_token;
    /**
     * Access Token的有效期(秒为单位，一般为1个月)；
     */
    private String expires_in;
    private String scope;
    private String session_key;
    private String session_secret;

}
