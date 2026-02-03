-- 1. 移除 task 表中的 difficulty 字段
ALTER TABLE `task` DROP COLUMN `difficulty`;

-- 2. 确保 task_strategy 表结构正确（不含 code 字段）
-- 如果之前执行过含 code 的脚本，可以使用以下语句移除：
-- ALTER TABLE `task_strategy` DROP COLUMN `code`;

-- 3. 初始测试点策略
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
