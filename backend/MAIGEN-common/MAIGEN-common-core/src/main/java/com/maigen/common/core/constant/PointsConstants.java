package com.maigen.common.core.constant;

/**
 * 积分规则常量
 * 暂时替代数据库配置，用于快速开发
 */
public class PointsConstants {

    /**
     * 注册奖励
     */
    public static final int REGISTER_REWARD = 50;

    /**
     * 邀请奖励 (邀请人)
     */
    public static final int INVITATION_REWARD_INVITER = 20;

    /**
     * 邀请奖励 (被邀请人)
     */
    public static final int INVITATION_REWARD_INVITEE = 5;

    /**
     * 生成数据消耗
     */
    public static final int GENERATE_TASK_COST = 5;

    /**
     * 下载社区数据消耗
     */
    public static final int DOWNLOAD_COMMUNITY_DATA_COST = 10;

    /**
     * 社区数据被下载奖励 (作者)
     */
    public static final int DOWNLOAD_REWARD_AUTHOR = 5;

    /**
     * 每日签到奖励
     */
    public static final int SIGN_IN_REWARD = 5;

    /**
     * 广告激励奖励
     */
    public static final int AD_REWARD = 2;

    /**
     * 社区分享奖励 (审核通过)
     */
    public static final int COMMUNITY_SHARE_REWARD = 5;

    // --- 变动来源 ---
    public static final String SOURCE_TASK_CREATE = "TASK_CREATE";
    public static final String SOURCE_TASK_REFUND = "TASK_REFUND";
    public static final String SOURCE_REGISTER = "REGISTER";
    public static final String SOURCE_INVITE = "INVITE";
    public static final String SOURCE_SIGN_IN = "SIGN_IN";
    public static final String SOURCE_AD_REWARD = "AD_REWARD";
    public static final String SOURCE_COMMUNITY_SHARE = "COMMUNITY_SHARE";
    public static final String SOURCE_ADMIN_ADJUST = "ADMIN_ADJUST";
}
