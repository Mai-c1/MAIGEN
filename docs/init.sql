-- MAIGEN 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS maigen DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE maigen;

-- 1. 用户管理模块

-- 1.1 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  `nickname` VARCHAR(50) NOT NULL,
  `avatar` VARCHAR(255) DEFAULT NULL,
  `invitation_code` VARCHAR(32) NOT NULL,
  `points` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_invitation_code` (`invitation_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.2 角色表
DROP TABLE IF EXISTS `role`;
CREATE TABLE IF NOT EXISTS `role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.3 权限表
DROP TABLE IF EXISTS `permission`;
CREATE TABLE IF NOT EXISTS `permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.4 用户角色关联表
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE IF NOT EXISTS `user_role` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.5 角色权限关联表
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE IF NOT EXISTS `role_permission` (
  `role_id` BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  PRIMARY KEY (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.6 邀请表
DROP TABLE IF EXISTS `invitation`;
CREATE TABLE IF NOT EXISTS `invitation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `inviter_id` BIGINT NOT NULL,
  `invitee_id` BIGINT NOT NULL,
  `invitation_code` VARCHAR(32) NOT NULL,
  `status` TINYINT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 积分管理模块

-- 2.1 积分记录表
DROP TABLE IF EXISTS `points_record`;
CREATE TABLE IF NOT EXISTS `points_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `amount` INT NOT NULL,
  `source` VARCHAR(50) NOT NULL,
  `related_id` VARCHAR(50) DEFAULT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.2 积分规则表
DROP TABLE IF EXISTS `points_rule`;
CREATE TABLE IF NOT EXISTS `points_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `value` INT NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.3 每日签到表
DROP TABLE IF EXISTS `user_sign_in`;
CREATE TABLE IF NOT EXISTS `user_sign_in` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `sign_in_date` DATE NOT NULL,
  `points_reward` INT NOT NULL,
  `continuous_days` INT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `sign_in_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 任务管理模块

-- 3.1 任务表
DROP TABLE IF EXISTS `task`;
CREATE TABLE IF NOT EXISTS `task` (
  `id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `problem_description` TEXT NOT NULL,
  `standard_code` TEXT NOT NULL,
  `testcase_count` INT NOT NULL,
  `time_limit` INT DEFAULT 0,
  `memory_limit` INT DEFAULT 0,
  `workflow_id` BIGINT DEFAULT NULL,
  `status` TINYINT DEFAULT 0,
  `progress` INT DEFAULT 0,
  `retry_count` INT DEFAULT 0,
  `error_message` TEXT DEFAULT NULL,
  `total_points` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `expired_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.2 AI工作流模块

-- 3.2.1 工作流方案表
DROP TABLE IF EXISTS `ai_workflow`;
CREATE TABLE IF NOT EXISTS `ai_workflow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `is_visible` TINYINT(1) DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.2.2 工作流步骤表
DROP TABLE IF EXISTS `ai_workflow_step`;
CREATE TABLE IF NOT EXISTS `ai_workflow_step` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `workflow_id` BIGINT NOT NULL,
  `step_order` INT NOT NULL,
  `role_name` VARCHAR(50) NOT NULL,
  `system_prompt` TEXT NOT NULL,
  `user_prompt_template` TEXT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_workflow_id` (`workflow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.2.3 任务执行日志表
DROP TABLE IF EXISTS `task_execution_log`;
CREATE TABLE IF NOT EXISTS `task_execution_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL,
  `step_order` INT NOT NULL,
  `role_name` VARCHAR(50) NOT NULL,
  `prompt_snapshot` LONGTEXT,
  `ai_response` LONGTEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 4. 数据管理模块

-- 4.1 生成数据表
DROP TABLE IF EXISTS `generated_data`;
CREATE TABLE IF NOT EXISTS `generated_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL,
  `file_name` VARCHAR(100) NOT NULL,
  `file_path` VARCHAR(255) NOT NULL,
  `download_url` VARCHAR(255) NOT NULL,
  `size` BIGINT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `expired_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 社区管理模块

-- 5.1 社区内容表
DROP TABLE IF EXISTS `community_content`;
CREATE TABLE IF NOT EXISTS `community_content` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `description` TEXT NOT NULL,
  `data_file_path` VARCHAR(255) NOT NULL,
  `category_id` BIGINT DEFAULT NULL,
  `status` TINYINT DEFAULT 0,
  `view_count` INT DEFAULT 0,
  `download_count` INT DEFAULT 0,
  `like_count` INT DEFAULT 0,
  `rating_avg` DECIMAL(3,2) DEFAULT 0.00,
  `rating_count` INT DEFAULT 0,
  `points` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5.2 社区内容解锁表
DROP TABLE IF EXISTS `community_unlock`;
CREATE TABLE IF NOT EXISTS `community_unlock` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `community_id` BIGINT NOT NULL,
  `unlock_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_community` (`user_id`, `community_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5.3 分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5.4 标签表
DROP TABLE IF EXISTS `tag`;
CREATE TABLE IF NOT EXISTS `tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5.5 内容标签关联表
DROP TABLE IF EXISTS `community_tag`;
CREATE TABLE IF NOT EXISTS `community_tag` (
  `community_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  PRIMARY KEY (`community_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5.6 点赞表
DROP TABLE IF EXISTS `community_like`;
CREATE TABLE IF NOT EXISTS `community_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `community_id` BIGINT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_community` (`user_id`, `community_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5.7 评分表
DROP TABLE IF EXISTS `community_rating`;
CREATE TABLE IF NOT EXISTS `community_rating` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `community_id` BIGINT NOT NULL,
  `score` TINYINT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_community` (`user_id`, `community_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 系统管理模块

-- 6.1 系统配置表
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `value` LONGTEXT NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6.2 统计数据表
DROP TABLE IF EXISTS `statistics`;
CREATE TABLE IF NOT EXISTS `statistics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `value` VARCHAR(50) NOT NULL,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6.3 操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL,
  `module` VARCHAR(50) NOT NULL,
  `operation` VARCHAR(100) NOT NULL,
  `method` VARCHAR(255) DEFAULT NULL,
  `params` TEXT DEFAULT NULL,
  `ip` VARCHAR(50) DEFAULT NULL,
  `status` TINYINT DEFAULT 1,
  `error_msg` TEXT DEFAULT NULL,
  `duration` BIGINT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入初始数据

-- 1. 初始角色
INSERT INTO `role` (`name`, `description`) VALUES
('user', '系统默认角色，拥有基础权限'),
('admin', '系统管理员，拥有所有权限')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- 2. 初始权限
INSERT INTO `permission` (`code`, `name`, `description`) VALUES
-- 管理员入口
('admin:dashboard', '管理后台', '进入管理后台主页'),

-- 用户管理 (User)
('user:view', '查看用户', '查看用户信息列表'),
('user:create', '创建用户', '创建新用户'),
('user:edit', '编辑用户', '编辑用户信息'),
('user:delete', '删除用户', '删除单个用户'),
('user:batch-delete', '批量删除用户', '批量删除多个用户'),
('user:status:update', '状态管理', '禁用/启用用户状态'),
('user:password:reset', '重置密码', '重置用户登录密码'),
('user:role:assign', '分配角色', '为用户分配系统角色'),
('user:points:view', '查看积分', '查看用户积分余额'),
('user:points:edit', '手动调账', '人工修改用户积分'),

-- 任务管理 (Task)
('task:view', '查看任务', '查看生成任务列表/详情'),
('task:create', '创建任务', '创建新的数据生成任务'),
('task:edit', '编辑任务', '修改任务执行参数'),
('task:delete', '删除任务', '删除任务记录'),
('task:batch-delete', '批量删除任务', '批量删除多条任务'),
('task:cancel', '取消任务', '中止正在运行的任务'),
('task:retry', '重试任务', '重试已失败的任务'),
('task:export', '导出数据', '导出生成的测试数据包'),
('task:log:view', '查看日志', '查看任务运行详细日志'),
('task:resource:view', '查看资源', '查看任务占用的沙箱资源'),

-- AI 治理 (AI)
('ai:model:view', '模型查看', '查看已接入的 AI 模型列表'),
('ai:model:manage', '模型管理', '管理 AI 模型接入配置'),
('ai:prompt:view', '提示词查看', '查看内置 Prompt 模板'),
('ai:prompt:manage', '提示词管理', '编辑与发布 Prompt 策略'),
('ai:usage:view', '消耗统计', '查看 AI Token 消耗报告'),

-- 社区生态 (Community)
('community:view', '内容查看', '浏览广场分享内容'),
('community:share', '内容分享', '发布题目与数据到社区'),
('community:unlock', '内容解锁', '消耗积分解锁社区资源'),
('community:approve', '内容审核', '审核用户发布的内容'),
('community:delete', '内容删除', '删除违规分享内容'),
('community:comment:create', '发表评论', '在社区内容下评论'),
('community:comment:delete', '删除评论', '管理社区评论内容'),
('community:report:view', '查看举报', '查看社区举报列表'),
('community:report:handle', '处理举报', '处理违规内容举报'),
('community:tag:manage', '标签管理', '管理社区内容标签'),

-- 财务管理 (Points)
('points:record:view', '账务流水', '查看全站积分变动明细'),
('points:rule:manage', '积分规则', '配置积分获取与消耗规则'),

-- 系统运维 (System)
('system:config:view', '查看配置', '查看系统运行参数'),
('system:config:edit', '修改配置', '修改系统全局配置'),
('system:role:view', '查看角色', '查看系统角色定义'),
('system:role:manage', '角色管理', '增删改系统角色及权限'),
('system:permission:view', '查看权限', '查看系统权限码定义'),
('system:log:operation', '操作审计', '查看系统操作审计日志'),
('system:log:login', '登录审计', '查看用户登录历史'),
('system:monitor:server', '性能监控', '查看服务器实时运行状态'),
('system:monitor:queue', '队列监控', '监控消息队列堆积情况'),
('system:notice:publish', '发布通知', '向全站用户发布通知公告'),
('system:backup', '数据备份', '手动执行数据库备份'),
('system:restore', '数据恢复', '从备份中恢复系统数据'),

-- 资源管理 (Resource)
('resource:view', '查看资源', '浏览系统存储中的文件'),
('resource:delete', '删除资源', '彻底删除存储中的物理文件'),
('resource:storage:manage', '存储管理', '管理 MinIO/本地存储配置')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `description` = VALUES(`description`);

-- 3. 角色权限关联
-- 普通用户权限
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
((SELECT `id` FROM `role` WHERE `name` = 'user'), (SELECT `id` FROM `permission` WHERE `code` = 'user:view')),
((SELECT `id` FROM `role` WHERE `name` = 'user'), (SELECT `id` FROM `permission` WHERE `code` = 'task:view')),
((SELECT `id` FROM `role` WHERE `name` = 'user'), (SELECT `id` FROM `permission` WHERE `code` = 'task:create')),
((SELECT `id` FROM `role` WHERE `name` = 'user'), (SELECT `id` FROM `permission` WHERE `code` = 'task:edit')),
((SELECT `id` FROM `role` WHERE `name` = 'user'), (SELECT `id` FROM `permission` WHERE `code` = 'task:delete')),
((SELECT `id` FROM `role` WHERE `name` = 'user'), (SELECT `id` FROM `permission` WHERE `code` = 'task:cancel')),
((SELECT `id` FROM `role` WHERE `name` = 'user'), (SELECT `id` FROM `permission` WHERE `code` = 'task:retry')),
((SELECT `id` FROM `role` WHERE `name` = 'user'), (SELECT `id` FROM `permission` WHERE `code` = 'community:view')),
((SELECT `id` FROM `role` WHERE `name` = 'user'), (SELECT `id` FROM `permission` WHERE `code` = 'community:share')),
((SELECT `id` FROM `role` WHERE `name` = 'user'), (SELECT `id` FROM `permission` WHERE `code` = 'community:unlock')),
((SELECT `id` FROM `role` WHERE `name` = 'user'), (SELECT `id` FROM `permission` WHERE `code` = 'community:comment:create'))
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`), `permission_id` = VALUES(`permission_id`);

-- 管理员权限（所有权限）
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT (SELECT `id` FROM `role` WHERE `name` = 'admin'), `id`
FROM `permission`
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`), `permission_id` = VALUES(`permission_id`);

-- 4. 初始积分规则
INSERT INTO `points_rule` (`code`, `name`, `value`, `description`) VALUES
('REGISTER_REWARD', '注册奖励', 50, '新用户注册奖励积分'),
('INVITE_REWARD', '邀请奖励', 20, '邀请新用户注册奖励积分'),
('INVITEE_REWARD', '被邀请奖励', 5, '被邀请注册奖励积分'),
('TASK_CONSUME', '任务消耗', 5, '创建任务消耗积分'),
  ('COMMUNITY_DOWNLOAD_CONSUME', '社区下载消耗', 10, '下载社区内容消耗积分'),
  ('COMMUNITY_SHARE_REWARD', '社区分享奖励', 5, '社区内容被下载奖励积分'),
  ('SIGN_IN_REWARD', '每日签到奖励', 5, '每日签到奖励积分'),
  ('AD_REWARD', '广告激励奖励', 2, '观看广告奖励积分')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`), `description` = VALUES(`description`);

-- 5. 初始系统配置
INSERT INTO `system_config` (`code`, `name`, `value`, `description`) VALUES
('TASK_MAX_EXECUTION_TIME', '任务最大执行时间', '1800', '单个任务最大执行时间（秒）'),
('DATA_RETENTION_DAYS', '数据保留天数', '7', '生成的数据保留天数'),
('DOWNLOAD_URL_EXPIRE_HOURS', '下载链接过期时间', '24', '下载链接过期时间（小时）'),
('MAX_TESTCASE_COUNT', '最大测试点数量', '20', '单个任务最大测试点数量'),
('AI_MAX_RETRY_TIMES', 'AI最大重试次数', '3', 'AI调用最大重试次数'),
('EMAIL_VERIFY_CODE_EXPIRE_MINUTES', '邮箱验证码过期时间', '5', '邮箱验证码过期时间（分钟）'),
('EMAIL_VERIFY_CODE_SEND_INTERVAL', '邮箱验证码发送间隔', '60', '邮箱验证码发送间隔（秒）')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`), `description` = VALUES(`description`);

-- 6. 初始AI工作流
INSERT INTO `ai_workflow` (`id`, `name`, `description`, `is_visible`) VALUES
(1, '标准Python算法生成', '适用于标准算法竞赛题目的数据生成，使用 Python Cyaron 库', 1);

INSERT INTO `ai_workflow_step` (`workflow_id`, `step_order`, `role_name`, `system_prompt`, `user_prompt_template`) VALUES
(1, 1, '代码生成专家', 
'你是一个资深的算法竞赛命题专家。\n你的任务是根据用户提供的题目描述，编写基于 Python Cyaron 库的数据生成脚本。\n\n【核心库规范 (参考 Cyaron Wiki)】\n1. 基础结构：\n   import os\n   from cyaron import *\n   if not os.path.exists(''./data/''): os.makedirs(''./data/'')\n2. 文件命名：\n   使用 `io = IO(file_prefix="", test_data_number=i, path=''./data/'')` \n   生成 ./data/1.in, ./data/2.in 等。\n3. 核心 API 推荐：\n   - 图论：Graph.graph(n, m, weight_limit=(l, r), self_loop=False)\n   - 树：Graph.tree(n, chain=0.3, spider=0.2)\n   - 序列/数组：Vector.random(size, [(min, max)])\n   - 随机数/字符：randint(l, r), String.random(len, charset)\n\n【编写准则 - 解决幻觉问题】\n1. **动态识别规模**：请仔细分析题目中“数据范围”部分提到的核心变量（可能是 n, m, k, len, t 等）。不要生搬硬套 N 或 M，如果题目是字符串题，请关注长度约束；如果题目是多组询问，请关注询问次数。\n2. **阶梯式构造**：生成的 N 组数据应包含从“最小约束（如 n=1）”到“最大满额约束”的平滑过渡。利用循环变量 `i` 动态计算当前测试点的规模。\n3. **合法性检查**：确保生成的逻辑符合题目逻辑（如：生成树时节点数必须大于0；生成不重复序列时范围必须足够）。\n\n【约束要求】\n1. 仅生成输入文件 (.in)，无需生成输出文件。\n2. 返回格式必须是纯 JSON 字符串，禁止包含 Markdown 标签。\n\n【返回 JSON 结构】\n{\n  "code": "Python 代码字符串",\n  "explanation": "简要说明识别到了哪些关键规模参数，以及如何进行阶梯式构造的。"\n}',
'题目名称：{title}\n题目描述：{description}\n标准代码：{standardCode}\n测试用例数量：{testcaseCount}\n时间限制：{timeLimit}ms\n空间限制：{memoryLimit}MB\n\n请基于以上信息生成对应的 Cyraon 脚本。');

-- 7. 初始统计数据
INSERT INTO `statistics` (`code`, `name`, `value`) VALUES
('USER_TOTAL', '总用户数', '0'),
('TASK_TOTAL', '总任务数', '0'),
('TASK_SUCCESS', '成功任务数', '0'),
('TASK_FAILED', '失败任务数', '0'),
('COMMUNITY_CONTENT_TOTAL', '社区内容总数', '0'),
('COMMUNITY_DOWNLOAD_TOTAL', '社区下载总数', '0'),
('POINTS_ISSUED_TOTAL', '发放积分总数', '0'),
('POINTS_CONSUMED_TOTAL', '消耗积分总数', '0')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);

-- 创建索引以提高查询性能
-- 用户表索引
CREATE INDEX IF NOT EXISTS `idx_user_status` ON `user` (`status`);

-- 任务表索引
CREATE INDEX IF NOT EXISTS `idx_task_user_id` ON `task` (`user_id`);
CREATE INDEX IF NOT EXISTS `idx_task_status` ON `task` (`status`);
CREATE INDEX IF NOT EXISTS `idx_task_created_at` ON `task` (`created_at`);

-- 积分记录表索引
CREATE INDEX IF NOT EXISTS `idx_points_record_user_id` ON `points_record` (`user_id`);
CREATE INDEX IF NOT EXISTS `idx_points_record_created_at` ON `points_record` (`created_at`);

-- 社区内容表索引
CREATE INDEX IF NOT EXISTS `idx_community_content_user_id` ON `community_content` (`user_id`);
CREATE INDEX IF NOT EXISTS `idx_community_content_status` ON `community_content` (`status`);
CREATE INDEX IF NOT EXISTS `idx_community_content_created_at` ON `community_content` (`created_at`);

-- 社区解锁表索引
CREATE INDEX IF NOT EXISTS `idx_community_unlock_community_id` ON `community_unlock` (`community_id`);

-- 生成数据表索引
CREATE INDEX IF NOT EXISTS `idx_generated_data_task_id` ON `generated_data` (`task_id`);
CREATE INDEX IF NOT EXISTS `idx_generated_data_status` ON `generated_data` (`status`);
CREATE INDEX IF NOT EXISTS `idx_generated_data_expired_at` ON `generated_data` (`expired_at`);

-- 邀请表索引
CREATE INDEX IF NOT EXISTS `idx_invitation_inviter_id` ON `invitation` (`inviter_id`);
CREATE INDEX IF NOT EXISTS `idx_invitation_invitee_id` ON `invitation` (`invitee_id`);
CREATE INDEX IF NOT EXISTS `idx_invitation_status` ON `invitation` (`status`);

-- 提交所有更改
COMMIT;

-- 显示创建结果
SELECT 'MAIGEN 数据库初始化完成！' AS `result`;