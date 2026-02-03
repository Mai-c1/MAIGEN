-- 添加任务配置字段到 task 表
ALTER TABLE `task` 
ADD COLUMN `time_limit` INT DEFAULT 1000 COMMENT '时间限制(ms)',
ADD COLUMN `memory_limit` INT DEFAULT 256 COMMENT '内存限制(MB)';
