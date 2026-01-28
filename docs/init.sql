-- MAIGEN 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS maigen DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE maigen;

-- 1. 用户管理模块

-- 1.1 用户表
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
CREATE TABLE IF NOT EXISTS `user_role` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.5 角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permission` (
  `role_id` BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  PRIMARY KEY (`role_id`, `permission_id`),
  FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.6 邀请表
CREATE TABLE IF NOT EXISTS `invitation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `inviter_id` BIGINT NOT NULL,
  `invitee_id` BIGINT NOT NULL,
  `invitation_code` VARCHAR(32) NOT NULL,
  `status` TINYINT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`inviter_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`invitee_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 积分管理模块

-- 2.1 积分记录表
CREATE TABLE IF NOT EXISTS `points_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `amount` INT NOT NULL,
  `source` VARCHAR(50) NOT NULL,
  `related_id` VARCHAR(50) DEFAULT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.2 积分规则表
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

-- 3. 任务管理模块

-- 3.1 任务表
CREATE TABLE IF NOT EXISTS `task` (
  `id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `problem_description` TEXT NOT NULL,
  `standard_code` TEXT NOT NULL,
  `testcase_count` INT NOT NULL,
  `status` TINYINT DEFAULT 0,
  `progress` INT DEFAULT 0,
  `total_points` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `expired_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.2 测试点表
CREATE TABLE IF NOT EXISTS `task_testcase` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL,
  `testcase_index` INT NOT NULL,
  `strategy_id` BIGINT DEFAULT NULL,
  `parameters` JSON DEFAULT NULL,
  `status` TINYINT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`task_id`) REFERENCES `task` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.3 测试点策略表
CREATE TABLE IF NOT EXISTS `task_strategy` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.4 任务执行日志表
CREATE TABLE IF NOT EXISTS `task_execution_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL,
  `ai_role` VARCHAR(50) NOT NULL,
  `ai_response` TEXT DEFAULT NULL,
  `status` TINYINT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`task_id`) REFERENCES `task` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 数据管理模块

-- 4.1 生成数据表
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
  PRIMARY KEY (`id`),
  FOREIGN KEY (`task_id`) REFERENCES `task` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 社区管理模块

-- 5.1 社区内容表
CREATE TABLE IF NOT EXISTS `community_content` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `description` TEXT NOT NULL,
  `data_file_path` VARCHAR(255) NOT NULL,
  `status` TINYINT DEFAULT 0,
  `view_count` INT DEFAULT 0,
  `download_count` INT DEFAULT 0,
  `points` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5.2 社区内容解锁表
CREATE TABLE IF NOT EXISTS `community_unlock` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `community_id` BIGINT NOT NULL,
  `unlock_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_community` (`user_id`, `community_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`community_id`) REFERENCES `community_content` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 系统管理模块

-- 6.1 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `value` VARCHAR(255) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6.2 统计数据表
CREATE TABLE IF NOT EXISTS `statistics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `value` VARCHAR(50) NOT NULL,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入初始数据

-- 1. 初始角色
INSERT INTO `role` (`name`, `description`) VALUES
('普通用户', '系统默认角色，拥有基础权限'),
('管理员', '系统管理员，拥有所有权限')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- 2. 初始权限
INSERT INTO `permission` (`code`, `name`, `description`) VALUES
-- 用户管理权限
('user:view', '查看用户', '查看用户信息'),
('user:create', '创建用户', '创建新用户'),
('user:edit', '编辑用户', '编辑用户信息'),
('user:delete', '删除用户', '删除用户'),
('user:disable', '禁用用户', '禁用/启用用户'),
('user:reset-password', '重置密码', '重置用户密码'),
('user:assign-role', '分配角色', '为用户分配角色'),
('user:view-points', '查看积分', '查看用户积分'),
('user:edit-points', '编辑积分', '修改用户积分'),
-- 任务管理权限
('task:view', '查看任务', '查看任务信息'),
('task:create', '创建任务', '创建新任务'),
('task:edit', '编辑任务', '编辑任务信息'),
('task:delete', '删除任务', '删除任务'),
('task:cancel', '取消任务', '取消执行中的任务'),
('task:retry', '重试任务', '重试失败的任务'),
('task:view-log', '查看任务日志', '查看任务执行日志'),
('task:view-resource', '查看任务资源', '查看任务使用的资源'),
-- 社区管理权限
('community:view', '查看社区内容', '查看社区分享的内容'),
('community:share', '分享社区内容', '分享内容到社区'),
('community:download', '下载社区内容', '下载社区分享的内容'),
('community:approve', '审核社区内容', '审核社区分享的内容'),
('community:delete', '删除社区内容', '删除社区分享的内容'),
('community:view-reward', '查看社区奖励', '查看社区分享的奖励'),
-- 系统管理权限
('system:config', '系统配置', '修改系统配置'),
('system:view-log', '查看系统日志', '查看系统运行日志'),
('system:view-resource', '查看系统资源', '查看系统资源使用情况'),
('system:manage-role', '管理角色', '创建/编辑/删除角色'),
('system:manage-permission', '管理权限', '分配权限'),
('system:data-statistics', '数据统计', '查看系统数据统计'),
('system:backup', '数据备份', '备份系统数据'),
('system:restore', '数据恢复', '恢复系统数据'),
-- 资源管理权限
('resource:view', '查看资源', '查看系统资源'),
('resource:delete', '删除资源', '删除系统资源'),
('resource:manage-storage', '管理存储', '管理系统存储')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `description` = VALUES(`description`);

-- 3. 角色权限关联
-- 普通用户权限
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
((SELECT `id` FROM `role` WHERE `name` = '普通用户'), (SELECT `id` FROM `permission` WHERE `code` = 'user:view')),
((SELECT `id` FROM `role` WHERE `name` = '普通用户'), (SELECT `id` FROM `permission` WHERE `code` = 'task:view')),
((SELECT `id` FROM `role` WHERE `name` = '普通用户'), (SELECT `id` FROM `permission` WHERE `code` = 'task:create')),
((SELECT `id` FROM `role` WHERE `name` = '普通用户'), (SELECT `id` FROM `permission` WHERE `code` = 'task:edit')),
((SELECT `id` FROM `role` WHERE `name` = '普通用户'), (SELECT `id` FROM `permission` WHERE `code` = 'task:delete')),
((SELECT `id` FROM `role` WHERE `name` = '普通用户'), (SELECT `id` FROM `permission` WHERE `code` = 'task:cancel')),
((SELECT `id` FROM `role` WHERE `name` = '普通用户'), (SELECT `id` FROM `permission` WHERE `code` = 'task:retry')),
((SELECT `id` FROM `role` WHERE `name` = '普通用户'), (SELECT `id` FROM `permission` WHERE `code` = 'community:view')),
((SELECT `id` FROM `role` WHERE `name` = '普通用户'), (SELECT `id` FROM `permission` WHERE `code` = 'community:share')),
((SELECT `id` FROM `role` WHERE `name` = '普通用户'), (SELECT `id` FROM `permission` WHERE `code` = 'community:download'))
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`), `permission_id` = VALUES(`permission_id`);

-- 管理员权限（所有权限）
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT (SELECT `id` FROM `role` WHERE `name` = '管理员'), `id`
FROM `permission`
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`), `permission_id` = VALUES(`permission_id`);

-- 4. 初始积分规则
INSERT INTO `points_rule` (`code`, `name`, `value`, `description`) VALUES
('REGISTER_REWARD', '注册奖励', 50, '新用户注册奖励积分'),
('INVITE_REWARD', '邀请奖励', 20, '邀请新用户注册奖励积分'),
('INVITEE_REWARD', '被邀请奖励', 5, '被邀请注册奖励积分'),
('TASK_CONSUME', '任务消耗', 5, '创建任务消耗积分'),
('COMMUNITY_DOWNLOAD_CONSUME', '社区下载消耗', 10, '下载社区内容消耗积分'),
('COMMUNITY_SHARE_REWARD', '社区分享奖励', 5, '社区内容被下载奖励积分')
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

-- 6. 初始测试点策略
INSERT INTO `task_strategy` (`name`, `description`) VALUES
('基础随机', '生成基础随机测试数据'),
('边界极值', '生成边界极值测试数据'),
('顺序特征', '生成顺序特征测试数据'),
('复杂度边界', '生成复杂度边界测试数据'),
('特殊结构', '生成特殊结构测试数据'),
('数据分布', '生成数据分布测试数据'),
('对抗性', '生成对抗性测试数据'),
('组合特征', '生成组合特征测试数据')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

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

-- 测试点表索引
CREATE INDEX IF NOT EXISTS `idx_task_testcase_task_id` ON `task_testcase` (`task_id`);
CREATE INDEX IF NOT EXISTS `idx_task_testcase_status` ON `task_testcase` (`status`);

-- 任务执行日志表索引
CREATE INDEX IF NOT EXISTS `idx_task_execution_log_task_id` ON `task_execution_log` (`task_id`);
CREATE INDEX IF NOT EXISTS `idx_task_execution_log_status` ON `task_execution_log` (`status`);

-- 邀请表索引
CREATE INDEX IF NOT EXISTS `idx_invitation_inviter_id` ON `invitation` (`inviter_id`);
CREATE INDEX IF NOT EXISTS `idx_invitation_invitee_id` ON `invitation` (`invitee_id`);
CREATE INDEX IF NOT EXISTS `idx_invitation_status` ON `invitation` (`status`);

-- 提交所有更改
COMMIT;

-- 显示创建结果
SELECT 'MAIGEN 数据库初始化完成！' AS `result`;