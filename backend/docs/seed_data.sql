-- MAIGEN 系统演示数据种子脚本
-- 建议在 init.sql 执行后运行

USE maigen;

-- 1. 用户数据 (密码均为 123456 的 BCrypt 加密值: $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2)
INSERT INTO `user` (`username`, `password`, `email`, `nickname`, `avatar`, `bio`, `invitation_code`, `points`, `status`) VALUES
('admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'admin@maigen.com', '超级管理员', 'https://p1-arco.byteimg.com/tos-cn-i-uwbnlip3yd/3ee5f1341c7918341.png~tplv-uwbnlip3yd-webp.webp', '系统首席管理员', 'ADMIN888', 9999, 1),
('tester01', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'tester01@maigen.com', '深度测试员', 'https://p1-arco.byteimg.com/tos-cn-i-uwbnlip3yd/a8c099c27964681f4.png~tplv-uwbnlip3yd-webp.webp', '专注于生成各种边界条件的测试数据', 'TESTER01', 500, 1),
('user_demo', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'demo@maigen.com', '普通访客', NULL, '路过看看', 'WELCOME1', 100, 1),
('locked_user', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'locked@maigen.com', '被封禁用户', NULL, NULL, 'LOCKED99', 0, 0);

-- 为 admin 绑定管理员角色 (假设 role 表中管理员 ID 为 2)
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (1, 2);
-- 为其他用户绑定普通用户角色 (假设 ID 为 1)
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (2, 1), (3, 1), (4, 1);

-- 2. 邀请关系
INSERT INTO `invitation` (`inviter_id`, `invitee_id`, `invitation_code`, `status`) VALUES
(1, 2, 'ADMIN888', 1),
(1, 3, 'ADMIN888', 1);

-- 3. 积分流水
INSERT INTO `points_record` (`user_id`, `amount`, `source`, `related_id`, `description`) VALUES
(1, 10000, 'SYSTEM', NULL, '系统初始化奖励'),
(2, 50, 'REGISTER_REWARD', NULL, '新用户注册奖励'),
(2, 20, 'INVITE_REWARD', '3', '邀请用户 user_demo 奖励'),
(2, -5, 'TASK_CONSUME', '10001', '创建 AI 生成任务消耗');

-- 4. 任务监控数据
INSERT INTO `task` (`id`, `user_id`, `title`, `problem_description`, `standard_code`, `testcase_count`, `status`, `progress`, `total_points`) VALUES
(10001, 2, '快速排序基准测试', '生成 10 万个随机整数用于快排性能测试', 'void sort(int a[], int n)...', 10, 2, 100, 5),
(10002, 2, '图论连通性测试集', '生成包含 1000 个节点和 5000 条边的稀疏图', 'struct Edge { int u, v; }...', 5, 1, 45, 10),
(10003, 3, '二叉树平衡性验证', '生成深度为 20 的完全二叉树', 'class Node { int val; Node *l, *r; }...', 20, 0, 0, 5),
(10004, 1, '异常压力测试', '生成导致系统内存溢出的极端边界数据', 'malloc(1024*1024*1024)...', 1, 3, 15, 0);

-- 5. 生成的数据文件记录
INSERT INTO `generated_data` (`task_id`, `file_name`, `file_path`, `download_url`, `size`, `status`) VALUES
(10001, 'qsort_test_data.zip', '/data/qsort_test_data.zip', 'http://cdn.maigen.com/files/qsort_test_data.zip', 1048576, 1);

-- 6. 社区资源数据
INSERT INTO `category` (`name`, `description`) VALUES ('排序算法', '各种排序算法的测试数据'), ('图论', '最短路、最小生成树等图论数据'), ('动态规划', '背包问题、状态机等 DP 测试集');

INSERT INTO `community_content` (`user_id`, `title`, `description`, `data_file_path`, `category_id`, `status`, `view_count`, `download_count`, `like_count`, `rating_avg`, `points`) VALUES
(2, 'ACM 竞赛快排测试集', '包含大量重复元素、逆序序列等特殊情况', '/data/acm_qsort.zip', 1, 1, 1250, 45, 12, 4.8, 10),
(3, '最短路 Dijkstra 算法模板数据', '包含负权边（用于 SPFA 测试）和大规模稀疏图', '/data/shortest_path.zip', 2, 0, 89, 0, 0, 0.0, 5),
(1, 'LeetCode 周赛级别 DP 数据', '覆盖了最近一年周赛的典型动态规划题目', '/data/lc_dp.zip', 3, 1, 5600, 230, 156, 4.9, 20);

-- 7. 标签数据
INSERT INTO `tag` (`name`) VALUES ('ACM'), ('LeetCode'), ('边界测试'), ('高难度');
INSERT INTO `community_tag` (`community_id`, `tag_id`) VALUES (1, 1), (1, 3), (3, 2), (3, 4);

-- 8. 社区互动 (点赞、解锁)
INSERT INTO `community_like` (`user_id`, `community_id`) VALUES (1, 1), (3, 1);
INSERT INTO `community_unlock` (`user_id`, `community_id`) VALUES (1, 1), (2, 3);

-- 9. 操作日志
INSERT INTO `operation_log` (`user_id`, `module`, `operation`, `method`, `status`, `duration`) VALUES
(1, '用户管理', '修改用户状态', 'updateUserStatus', 1, 120),
(2, '任务管理', '提交生成任务', 'submitTask', 1, 450),
(1, '内容管理', '审核通过资源', 'auditContent', 1, 85);

-- 10. 全局统计初始化
UPDATE `statistics` SET `value` = '4' WHERE `code` = 'USER_TOTAL';
UPDATE `statistics` SET `value` = '4' WHERE `code` = 'TASK_TOTAL';
UPDATE `statistics` SET `value` = '3' WHERE `code` = 'COMMUNITY_CONTENT_TOTAL';
