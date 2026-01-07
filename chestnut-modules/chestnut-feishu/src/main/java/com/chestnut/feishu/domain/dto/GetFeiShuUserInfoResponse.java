package com.chestnut.feishu.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * GetWechatUserInfoResponse
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class GetFeiShuUserInfoResponse {
    private Integer code;
    private String msg;
    private FeiShuUserInfo data;

    public record FeiShuUserInfo(
        String name, // 用户姓名
        String en_name, // 用户英文名
        String avatar_url, // 用户头像 原始大小
        String avatar_thumb, // 用户头像 72x72
        String avatar_middle, // 用户头像 240x240
        String avatar_big, // 用户头像 640x640
        String open_id, // 用户在应用内的唯一标识
        String union_id, // 用户对ISV的唯一标识，对于同一个ISV，用户在其名下所有应用的union_id相同
        String email, // 用户邮箱。邮箱信息为管理员导入的用户联系方式，未经过用户本人实时验证，不建议开发者直接将其作为业务系统的登录凭证。如使用，务必自行认证。
        String enterprise_email, // 企业邮箱，请先确保已在管理后台启用飞书邮箱服务
        String user_id, // 用户id
        String mobile, // 用户手机号。手机号信息为管理员导入的用户联系方式，未经过用户本人实时验证，不建议开发者直接将其作为业务系统的登录凭证。如使用，务必自行认证。
        String tenant_key, // 当前企业标识
        String employee_no // 用户工号
    ) {
    }
}
