/*
企业级OA系统 V2 数据库初始化脚本
仿钉钉架构 - 阶段1+2 (组织架构 + 权限底座)
兼容原springboot2142g核心业务表
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS `springboot_oa_v2`;
CREATE DATABASE `springboot_oa_v2` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `springboot_oa_v2`;

-- ==========================================================
-- 1. 兼容原系统核心表（保持原有14张表的字段结构不变）
-- ==========================================================

-- 1.1 用户表
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(100) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `role` varchar(100) DEFAULT '普通员工' COMMENT '角色',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

INSERT INTO `users` (`id`, `username`, `password`, `role`) VALUES (1, 'admin', 'admin', '超级管理员');

-- 1.2 业务表（简化字段，沿用原结构）
DROP TABLE IF EXISTS `tongxunlu`;
CREATE TABLE `tongxunlu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `xingming` varchar(200) NOT NULL,
  `nianling` varchar(200) DEFAULT NULL,
  `xingbie` varchar(200) DEFAULT NULL,
  `touxiang` varchar(200) DEFAULT NULL,
  `bumen` varchar(200) DEFAULT NULL,
  `shoujihao` varchar(200) DEFAULT NULL,
  `dizhi` varchar(200) DEFAULT NULL,
  `youxiang` varchar(200) DEFAULT NULL,
  `beizhu` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='通讯录';

DROP TABLE IF EXISTS `gonggaoxinxi`;
CREATE TABLE `gonggaoxinxi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `biaoti` varchar(200) NOT NULL,
  `leixing` varchar(200) NOT NULL,
  `fabushijian` datetime DEFAULT NULL,
  `gonggaoneirong` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='公告信息';

DROP TABLE IF EXISTS `gongzixinxi`;
CREATE TABLE `gongzixinxi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `mingcheng` varchar(200) NOT NULL,
  `yonghuming` varchar(200) NOT NULL,
  `xingming` varchar(200) DEFAULT NULL,
  `bumen` varchar(200) DEFAULT NULL,
  `zhiwei` varchar(200) DEFAULT NULL,
  `jibengongzi` int(11) DEFAULT 0,
  `jiabangongzi` int(11) DEFAULT 0,
  `fuli` int(11) DEFAULT 0,
  `shebao` int(11) DEFAULT 0,
  `koufei` int(11) DEFAULT 0,
  `shifagongzi` int(11) DEFAULT 0,
  `riqi` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='工资信息';

DROP TABLE IF EXISTS `gongzuorizhi`;
CREATE TABLE `gongzuorizhi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `biaoti` varchar(200) NOT NULL,
  `dengjishijian` datetime DEFAULT NULL,
  `gongzuoneirong` longtext,
  `yonghuming` varchar(200) DEFAULT NULL,
  `xingming` varchar(200) DEFAULT NULL,
  `bumen` varchar(200) DEFAULT NULL,
  `zhiwei` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='工作日志';

DROP TABLE IF EXISTS `kehuguanxi`;
CREATE TABLE `kehuguanxi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `kehuxingming` varchar(200) NOT NULL,
  `xingbie` varchar(200) DEFAULT NULL,
  `nianling` varchar(200) DEFAULT NULL,
  `shengri` date DEFAULT NULL,
  `gongsimingcheng` varchar(200) NOT NULL,
  `lianxidianhua` varchar(200) DEFAULT NULL,
  `youxiang` varchar(200) DEFAULT NULL,
  `hezuoneirong` longtext,
  `yonghuming` varchar(200) DEFAULT NULL,
  `xingming` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='客户关系';

DROP TABLE IF EXISTS `richenganpai`;
CREATE TABLE `richenganpai` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `bianhao` varchar(200) DEFAULT NULL,
  `mingcheng` varchar(200) NOT NULL,
  `leixing` varchar(200) DEFAULT NULL,
  `riqi` date DEFAULT NULL,
  `shijian` varchar(200) DEFAULT NULL,
  `richengneirong` longtext,
  `yonghuming` varchar(200) DEFAULT NULL,
  `xingming` varchar(200) DEFAULT NULL,
  `bumen` varchar(200) DEFAULT NULL,
  `zhiwei` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='日程安排';

DROP TABLE IF EXISTS `shangbankaoqin`;
CREATE TABLE `shangbankaoqin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `bianhao` varchar(200) DEFAULT NULL,
  `mingcheng` varchar(200) NOT NULL,
  `riqi` date DEFAULT NULL,
  `yonghuming` varchar(200) NOT NULL,
  `xingming` varchar(200) DEFAULT NULL,
  `bumen` varchar(200) DEFAULT NULL,
  `zhiwei` varchar(200) DEFAULT NULL,
  `zaotuicishu` varchar(200) NOT NULL DEFAULT '0',
  `chidaocishu` varchar(200) NOT NULL DEFAULT '0',
  `qingjiacishu` varchar(200) NOT NULL DEFAULT '0',
  `kuanggongcishu` varchar(200) NOT NULL DEFAULT '0',
  `userid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='上班考勤';

DROP TABLE IF EXISTS `cheliangxinxi`;
CREATE TABLE `cheliangxinxi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cheliangmingcheng` varchar(200) NOT NULL,
  `chepaihao` varchar(200) NOT NULL,
  `cheliangleixing` varchar(200) DEFAULT NULL,
  `cheliangyanse` varchar(200) DEFAULT NULL,
  `tingfangweizhi` varchar(200) DEFAULT NULL,
  `cheliangxiangqing` longtext,
  `shiyongzhuangkuang` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='车辆信息';

DROP TABLE IF EXISTS `wenjianxinxi`;
CREATE TABLE `wenjianxinxi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `biaoti` varchar(200) NOT NULL,
  `fujian` varchar(200) DEFAULT NULL,
  `wenjianneirong` longtext,
  `fabushijian` datetime DEFAULT NULL,
  `yonghuming` varchar(200) DEFAULT NULL,
  `xingming` varchar(200) DEFAULT NULL,
  `bumen` varchar(200) DEFAULT NULL,
  `zhiwei` varchar(200) DEFAULT NULL,
  `sfsh` varchar(200) DEFAULT '否',
  `shhf` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='文件信息';

DROP TABLE IF EXISTS `yonghu`;
CREATE TABLE `yonghu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `yonghuming` varchar(200) NOT NULL,
  `mima` varchar(200) NOT NULL,
  `xingming` varchar(200) DEFAULT NULL,
  `xingbie` varchar(200) DEFAULT NULL,
  `touxiang` varchar(200) DEFAULT NULL,
  `bumen` varchar(200) DEFAULT NULL,
  `zhiwei` varchar(200) DEFAULT NULL,
  `youxiang` varchar(200) DEFAULT NULL,
  `shouji` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_yonghuming` (`yonghuming`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户';

INSERT INTO `yonghu` (`yonghuming`, `mima`, `xingming`, `xingbie`, `bumen`, `zhiwei`) VALUES
('user1', '123456', '张三', '男', '技术部', '员工'),
('user2', '123456', '李四', '男', '技术部', '员工'),
('hr01', '123456', '王五', '女', '人力资源部', 'HR专员');

DROP TABLE IF EXISTS `config`;
CREATE TABLE `config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `value` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='配置文件';

-- ==========================================================
-- 2. 阶段1+2：组织架构与权限底座（核心新增表）
-- ==========================================================

-- 2.1 部门表（多层级树）
DROP TABLE IF EXISTS `dept`;
CREATE TABLE `dept` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `dept_name` varchar(100) NOT NULL COMMENT '部门名称',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父部门ID，0为根',
  `dept_path` varchar(500) DEFAULT NULL COMMENT '路径，逗号分隔，便于查询子树',
  `level` int(11) DEFAULT 1 COMMENT '层级，1为顶级',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `leader_id` bigint(20) DEFAULT NULL COMMENT '部门负责人employee_id',
  `dept_type` varchar(20) DEFAULT 'real' COMMENT 'real=实体部门，virtual=虚拟部门/项目组',
  `phone` varchar(50) DEFAULT NULL COMMENT '部门电话',
  `email` varchar(100) DEFAULT NULL COMMENT '部门邮箱',
  `status` tinyint(4) DEFAULT 1 COMMENT '1=启用，0=禁用',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '1=删除，0=正常',
  `create_by` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_leader_id` (`leader_id`),
  KEY `idx_dept_type` (`dept_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 2.2 岗位表
DROP TABLE IF EXISTS `position`;
CREATE TABLE `position` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `name` varchar(100) NOT NULL COMMENT '岗位名称',
  `code` varchar(50) DEFAULT NULL COMMENT '岗位编码',
  `level` int(11) DEFAULT 1 COMMENT '岗位级别（1-10，数值越大级别越高）',
  `sort_order` int(11) DEFAULT 0,
  `status` tinyint(4) DEFAULT 1,
  `del_flag` tinyint(4) DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

-- 2.3 员工主表（与yonghu并行，yonghu_id关联原登录账号）
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `yonghu_id` bigint(20) DEFAULT NULL COMMENT '关联原yonghu.id',
  `user_id` bigint(20) DEFAULT NULL COMMENT '关联users.id（管理员账号）',
  `emp_no` varchar(50) NOT NULL COMMENT '工号',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `english_name` varchar(50) DEFAULT NULL,
  `gender` tinyint(4) DEFAULT 0 COMMENT '0=未知，1=男，2=女',
  `birthday` date DEFAULT NULL,
  `id_card` varchar(50) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `avatar` varchar(500) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `position_id` bigint(20) DEFAULT NULL,
  `direct_manager_id` bigint(20) DEFAULT NULL COMMENT '直属上级employee_id',
  `employee_type` tinyint(4) DEFAULT 1 COMMENT '1=正式，2=试用，3=实习，4=兼职，5=离职',
  `entry_date` date DEFAULT NULL,
  `regular_date` date DEFAULT NULL COMMENT '转正日期',
  `leave_date` date DEFAULT NULL,
  `contract_start` date DEFAULT NULL,
  `contract_end` date DEFAULT NULL,
  `work_location` varchar(100) DEFAULT NULL,
  `status` tinyint(4) DEFAULT 1 COMMENT '1=在职，0=离职',
  `is_external` tinyint(4) DEFAULT 0 COMMENT '1=外部联系人',
  `del_flag` tinyint(4) DEFAULT 0,
  `create_by` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_no` (`emp_no`),
  KEY `idx_yonghu_id` (`yonghu_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_position_id` (`position_id`),
  KEY `idx_direct_manager` (`direct_manager_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_external` (`is_external`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工主表';

-- 2.4 员工扩展字段表
DROP TABLE IF EXISTS `employee_ext`;
CREATE TABLE `employee_ext` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `employee_id` bigint(20) NOT NULL,
  `field_key` varchar(50) NOT NULL COMMENT '字段key',
  `field_label` varchar(50) NOT NULL COMMENT '字段显示名',
  `field_value` text,
  `field_type` varchar(20) DEFAULT 'text' COMMENT 'text/number/date/select',
  `is_sensitive` tinyint(4) DEFAULT 0 COMMENT '1=敏感字段（薪资/手机等），需要权限才能看',
  `sort_order` int(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_field_key` (`field_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工扩展字段';

-- 2.5 角色表
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `code` varchar(50) NOT NULL COMMENT '角色编码：SUPER_ADMIN/HR_MANAGER/EMP/...',
  `remark` varchar(500) DEFAULT NULL,
  `data_scope` varchar(20) DEFAULT 'self' COMMENT '默认数据范围：self/dept/dept_children/all',
  `sort_order` int(11) DEFAULT 0,
  `status` tinyint(4) DEFAULT 1,
  `del_flag` tinyint(4) DEFAULT 0,
  `is_builtin` tinyint(4) DEFAULT 0 COMMENT '1=内置角色不可删',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 2.6 功能权限表
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) DEFAULT 0,
  `name` varchar(50) NOT NULL,
  `code` varchar(100) NOT NULL COMMENT '权限编码，如 org:dept:view',
  `type` varchar(20) DEFAULT 'menu' COMMENT 'menu=菜单，button=按钮，api=接口',
  `module` varchar(50) DEFAULT NULL COMMENT '所属模块',
  `path` varchar(200) DEFAULT NULL COMMENT '前端路由路径',
  `icon` varchar(50) DEFAULT NULL,
  `sort_order` int(11) DEFAULT 0,
  `status` tinyint(4) DEFAULT 1,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_code` (`code`),
  KEY `idx_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='功能权限表';

-- 2.7 角色-权限关联
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL,
  `permission_id` bigint(20) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联';

-- 2.8 用户-角色关联（支持多角色）
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT 'users.id或employee.id，根据user_type',
  `user_type` varchar(20) DEFAULT 'employee' COMMENT 'admin/employee',
  `role_id` bigint(20) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `user_type`, `role_id`),
  KEY `idx_user` (`user_id`, `user_type`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联';

-- 2.9 组织管理范围权限（子管理员边界）
DROP TABLE IF EXISTS `org_scope`;
CREATE TABLE `org_scope` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `user_type` varchar(20) DEFAULT 'employee',
  `dept_id` bigint(20) NOT NULL,
  `scope_type` varchar(20) NOT NULL COMMENT 'self=本人，dept=本部门，dept_children=本部门+下级，all=全部',
  `include_sub` tinyint(4) DEFAULT 0 COMMENT '是否包含子部门',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`, `user_type`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织管理范围';

-- 2.10 数据权限规则
DROP TABLE IF EXISTS `data_permission`;
CREATE TABLE `data_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL,
  `module` varchar(50) NOT NULL COMMENT '模块编码',
  `data_scope` varchar(20) DEFAULT 'self' COMMENT 'self/dept/dept_children/all',
  `column_permissions` json DEFAULT NULL COMMENT '列级权限JSON',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_module` (`role_id`, `module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据权限规则';

-- 2.11 资源独立权限（云盘/文档/合同等）
DROP TABLE IF EXISTS `resource_permission`;
CREATE TABLE `resource_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resource_type` varchar(30) NOT NULL COMMENT 'file/folder/form/contract',
  `resource_id` bigint(20) NOT NULL,
  `subject_type` varchar(20) NOT NULL COMMENT 'user/role/dept',
  `subject_id` bigint(20) NOT NULL,
  `permission_type` varchar(20) NOT NULL COMMENT 'read/write/admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resource` (`resource_type`, `resource_id`),
  KEY `idx_subject` (`subject_type`, `subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源独立权限';

-- 2.12 审计日志
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `username` varchar(100) DEFAULT NULL,
  `module` varchar(50) DEFAULT NULL,
  `action` varchar(50) DEFAULT NULL,
  `resource_type` varchar(50) DEFAULT NULL,
  `resource_id` varchar(100) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `old_value` text,
  `new_value` text,
  `ip` varchar(50) DEFAULT NULL,
  `user_agent` varchar(500) DEFAULT NULL,
  `success` tinyint(4) DEFAULT 1,
  `error_msg` varchar(2000) DEFAULT NULL,
  `cost_ms` bigint(20) DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_resource` (`resource_type`, `resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志';

-- 2.13 登录日志
DROP TABLE IF EXISTS `login_log`;
CREATE TABLE `login_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `username` varchar(100) DEFAULT NULL,
  `ip` varchar(50) DEFAULT NULL,
  `user_agent` varchar(500) DEFAULT NULL,
  `login_type` varchar(20) DEFAULT 'password' COMMENT 'password/sso/captcha',
  `success` tinyint(4) DEFAULT 1,
  `error_msg` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志';

-- 2.14 Token表（兼容原表+扩展user_type）
DROP TABLE IF EXISTS `token`;
CREATE TABLE `token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `user_type` varchar(20) DEFAULT 'admin' COMMENT 'admin/employee',
  `username` varchar(100) NOT NULL,
  `token` varchar(200) NOT NULL,
  `expire_time` datetime DEFAULT NULL,
  `ip` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token` (`token`),
  KEY `idx_user` (`user_id`, `user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token表';

-- ==========================================================
-- 3. 初始化数据：组织架构 + 角色 + 权限
-- ==========================================================

-- 3.1 部门（模拟钉钉多层级：集团->分公司->部门->小组）
INSERT INTO `dept` (`id`, `dept_name`, `parent_id`, `dept_path`, `level`, `sort_order`, `dept_type`, `phone`, `status`) VALUES
(1, '蓝湾科技集团', 0, '1', 1, 1, 'real', '010-88880000', 1),
(2, '北京总部', 1, '1,2', 2, 1, 'real', '010-88880001', 1),
(3, '上海分公司', 1, '1,3', 2, 2, 'real', '021-88880002', 1),
(4, '技术中心', 2, '1,2,4', 3, 1, 'real', NULL, 1),
(5, '人力资源部', 2, '1,2,5', 3, 2, 'real', NULL, 1),
(6, '财务部', 2, '1,2,6', 3, 3, 'real', NULL, 1),
(7, '前端组', 4, '1,2,4,7', 4, 1, 'real', NULL, 1),
(8, '后端组', 4, '1,2,4,8', 4, 2, 'real', NULL, 1),
(9, '销售部', 3, '1,3,9', 3, 1, 'real', NULL, 1),
(10, '数字化转型项目组', 2, '1,2,10', 3, 4, 'virtual', NULL, 1);

-- 3.2 岗位
INSERT INTO `position` (`id`, `name`, `code`, `level`, `sort_order`, `status`) VALUES
(1, '总裁', 'CEO', 10, 1, 1),
(2, '副总裁', 'VP', 9, 2, 1),
(3, '总监', 'DIRECTOR', 8, 3, 1),
(4, '经理', 'MANAGER', 7, 4, 1),
(5, '主管', 'SUPERVISOR', 6, 5, 1),
(6, '高级专员', 'SENIOR', 4, 6, 1),
(7, '专员', 'STAFF', 3, 7, 1),
(8, '助理', 'ASSISTANT', 2, 8, 1);

-- 3.3 员工（管理员和几个测试员工）
INSERT INTO `employee` (`id`, `user_id`, `yonghu_id`, `emp_no`, `real_name`, `gender`, `phone`, `email`, `dept_id`, `position_id`, `direct_manager_id`, `employee_type`, `entry_date`, `status`) VALUES
(1, 1, NULL, 'E0001', '系统管理员', 1, '13800000001', 'admin@bluewhale.com', 2, 1, NULL, 1, '2020-01-01', 1),
(2, NULL, 1, 'E0002', '张三', 1, '13800000002', 'zhangsan@bluewhale.com', 4, 7, NULL, 1, '2021-03-15', 1),
(3, NULL, 2, 'E0003', '李四', 1, '13800000003', 'lisi@bluewhale.com', 4, 7, NULL, 1, '2021-05-20', 1),
(4, NULL, 3, 'E0004', '王五', 2, '13800000004', 'wangwu@bluewhale.com', 5, 6, NULL, 1, '2020-08-10', 1);

-- 更新部门负责人
UPDATE `dept` SET `leader_id` = 1 WHERE `id` = 1;
UPDATE `dept` SET `leader_id` = 1 WHERE `id` = 2;
UPDATE `dept` SET `leader_id` = 4 WHERE `id` = 5;
UPDATE `dept` SET `leader_id` = 2 WHERE `id` = 4;
UPDATE `dept` SET `leader_id` = 2 WHERE `id` = 7;
UPDATE `dept` SET `leader_id` = 3 WHERE `id` = 8;

-- 员工扩展字段
INSERT INTO `employee_ext` (`employee_id`, `field_key`, `field_label`, `field_value`, `field_type`, `is_sensitive`, `sort_order`) VALUES
(1, 'education', '学历', '硕士', 'text', 0, 1),
(1, 'emergency_contact', '紧急联系人', '李某某 13800001234', 'text', 1, 2),
(2, 'education', '学历', '本科', 'text', 0, 1),
(2, 'bank_card', '银行卡', '6225****8888', 'text', 1, 2),
(2, 'salary', '月薪', '15000', 'number', 1, 3);

-- 3.4 角色（钉钉风格的预设角色）
INSERT INTO `role` (`id`, `name`, `code`, `remark`, `data_scope`, `sort_order`, `is_builtin`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有系统全部权限', 'all', 1, 1),
(2, 'HR管理员', 'HR_MANAGER', '负责组织架构与员工档案', 'all', 2, 1),
(3, '财务专员', 'FINANCE', '负责财务相关模块', 'all', 3, 1),
(4, '部门负责人', 'DEPT_LEADER', '管理部门员工与数据', 'dept_children', 4, 1),
(5, '普通员工', 'EMPLOYEE', '基础员工权限', 'self', 5, 1),
(6, '审计员', 'AUDITOR', '查看所有审计日志', 'all', 6, 1),
(7, 'IT管理员', 'IT_ADMIN', '系统与安全配置', 'all', 7, 1);

-- 3.5 权限树（菜单 + 按钮）
INSERT INTO `permission` (`id`, `parent_id`, `name`, `code`, `type`, `module`, `path`, `icon`, `sort_order`) VALUES
-- 一级菜单
(1, 0, '组织架构', 'org', 'menu', 'org', '/org', 'tree', 1),
(2, 0, '权限管理', 'permission', 'menu', 'sys', '/permission', 'lock', 2),
(3, 0, '审计中心', 'audit', 'menu', 'sys', '/audit', 'document', 3),
(4, 0, '个人中心', 'personal', 'menu', 'personal', '/personal', 'user', 99),

-- 组织架构子菜单
(10, 1, '部门管理', 'org:dept', 'menu', 'org', '/org/dept', 'tree-table', 1),
(11, 1, '员工管理', 'org:employee', 'menu', 'org', '/org/employee', 'peoples', 2),
(12, 1, '通讯录', 'org:contact', 'menu', 'org', '/org/contact', 'phone', 3),
(13, 1, '外部联系人', 'org:external', 'menu', 'org', '/org/external', 'user', 4),

-- 部门管理按钮
(100, 10, '查看部门', 'org:dept:view', 'button', 'org', NULL, NULL, 1),
(101, 10, '新增部门', 'org:dept:add', 'button', 'org', NULL, NULL, 2),
(102, 10, '编辑部门', 'org:dept:edit', 'button', 'org', NULL, NULL, 3),
(103, 10, '删除部门', 'org:dept:delete', 'button', 'org', NULL, NULL, 4),
(104, 10, '调整部门层级', 'org:dept:move', 'button', 'org', NULL, NULL, 5),

-- 员工管理按钮
(110, 11, '查看员工', 'org:employee:view', 'button', 'org', NULL, NULL, 1),
(111, 11, '新增员工', 'org:employee:add', 'button', 'org', NULL, NULL, 2),
(112, 11, '编辑员工', 'org:employee:edit', 'button', 'org', NULL, NULL, 3),
(113, 11, '删除员工', 'org:employee:delete', 'button', 'org', NULL, NULL, 4),
(114, 11, '导入员工', 'org:employee:import', 'button', 'org', NULL, NULL, 5),
(115, 11, '导出员工', 'org:employee:export', 'button', 'org', NULL, NULL, 6),
(116, 11, '调岗', 'org:employee:transfer', 'button', 'org', NULL, NULL, 7),
(117, 11, '离职', 'org:employee:leave', 'button', 'org', NULL, NULL, 8),

-- 通讯录按钮
(120, 12, '查看通讯录', 'org:contact:view', 'button', 'org', NULL, NULL, 1),
(121, 12, '查看手机号', 'org:contact:phone', 'button', 'org', NULL, NULL, 2),
(122, 12, '查看薪资', 'org:contact:salary', 'button', 'org', NULL, NULL, 3),

-- 外部联系人按钮
(130, 13, '查看外部联系人', 'org:external:view', 'button', 'org', NULL, NULL, 1),
(131, 13, '新增外部联系人', 'org:external:add', 'button', 'org', NULL, NULL, 2),
(132, 13, '编辑外部联系人', 'org:external:edit', 'button', 'org', NULL, NULL, 3),
(133, 13, '删除外部联系人', 'org:external:delete', 'button', 'org', NULL, NULL, 4),

-- 权限管理子菜单
(20, 2, '角色管理', 'sys:role', 'menu', 'sys', '/permission/role', 'peoples', 1),
(21, 2, '用户授权', 'sys:user-role', 'menu', 'sys', '/permission/user-role', 'user', 2),
(22, 2, '权限树', 'sys:perm-tree', 'menu', 'sys', '/permission/tree', 'tree', 3),

-- 角色管理按钮
(200, 20, '查看角色', 'sys:role:view', 'button', 'sys', NULL, NULL, 1),
(201, 20, '新增角色', 'sys:role:add', 'button', 'sys', NULL, NULL, 2),
(202, 20, '编辑角色', 'sys:role:edit', 'button', 'sys', NULL, NULL, 3),
(203, 20, '删除角色', 'sys:role:delete', 'button', 'sys', NULL, NULL, 4),
(204, 20, '分配权限', 'sys:role:assign-perm', 'button', 'sys', NULL, NULL, 5),

-- 审计子菜单
(30, 3, '操作日志', 'audit:op', 'menu', 'sys', '/audit/op', 'log', 1),
(31, 3, '登录日志', 'audit:login', 'menu', 'sys', '/audit/login', 'logininfor', 2),

-- 审计按钮
(300, 30, '查看操作日志', 'audit:op:view', 'button', 'sys', NULL, NULL, 1),
(301, 30, '导出日志', 'audit:op:export', 'button', 'sys', NULL, NULL, 2),
(310, 31, '查看登录日志', 'audit:login:view', 'button', 'sys', NULL, NULL, 1);

-- 3.6 角色-权限关联
-- 超级管理员拥有所有权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `permission`;

-- HR管理员：组织架构全部 + 角色查看
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 2, id FROM `permission` WHERE code IN (
  'org', 'org:dept', 'org:employee', 'org:contact', 'org:external',
  'org:dept:view', 'org:dept:add', 'org:dept:edit', 'org:dept:move', 'org:dept:delete',
  'org:employee:view', 'org:employee:add', 'org:employee:edit', 'org:employee:delete',
  'org:employee:import', 'org:employee:export', 'org:employee:transfer', 'org:employee:leave',
  'org:contact:view', 'org:contact:phone', 'org:contact:salary',
  'org:external:view', 'org:external:add', 'org:external:edit', 'org:external:delete',
  'sys:role', 'sys:role:view', 'sys:user-role'
);

-- 部门负责人：管理本部门员工
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 4, id FROM `permission` WHERE code IN (
  'org', 'org:dept', 'org:dept:view',
  'org:employee', 'org:employee:view', 'org:employee:edit', 'org:employee:transfer',
  'org:contact', 'org:contact:view', 'org:contact:phone',
  'personal'
);

-- 普通员工：仅看通讯录
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 5, id FROM `permission` WHERE code IN (
  'org:contact', 'org:contact:view',
  'personal'
);

-- 审计员：所有审计权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 6, id FROM `permission` WHERE code IN (
  'audit', 'audit:op', 'audit:login',
  'audit:op:view', 'audit:op:export', 'audit:login:view',
  'personal'
);

-- IT管理员：权限+审计
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 7, id FROM `permission` WHERE code IN (
  'permission', 'sys:role', 'sys:role:view', 'sys:role:add', 'sys:role:edit', 'sys:role:delete', 'sys:role:assign-perm',
  'sys:user-role', 'sys:perm-tree',
  'audit', 'audit:op', 'audit:login', 'audit:op:view', 'audit:login:view',
  'personal'
);

-- 财务专员：基础权限（此版本仅占位）
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 3, id FROM `permission` WHERE code IN (
  'org:contact', 'org:contact:view', 'org:contact:phone', 'org:contact:salary',
  'personal'
);

-- 3.7 用户-角色关联
INSERT INTO `user_role` (`user_id`, `user_type`, `role_id`) VALUES
(1, 'admin', 1),     -- 管理员账号 = 超级管理员
(1, 'employee', 1),
(2, 'employee', 5),  -- 张三 = 普通员工
(3, 'employee', 5),  -- 李四 = 普通员工
(4, 'employee', 4);  -- 王五 = 部门负责人

-- 3.8 组织管理范围
INSERT INTO `org_scope` (`user_id`, `user_type`, `dept_id`, `scope_type`, `include_sub`) VALUES
(4, 'employee', 5, 'dept_children', 1);

-- 3.9 数据权限规则
INSERT INTO `data_permission` (`role_id`, `module`, `data_scope`, `column_permissions`) VALUES
(1, '*', 'all', NULL),
(2, 'org', 'all', NULL),
(4, 'org:employee', 'dept_children', NULL),
(5, 'org:employee', 'self', '{"salary":"hidden","phone":"hidden","email":"hidden","idCard":"hidden","bankCard":"hidden"}'),
(5, 'org:contact', 'all', '{"salary":"hidden","phone":"hidden","idCard":"hidden","bankCard":"hidden"}'),
(3, 'org:contact', 'all', '{"salary":"hidden","idCard":"hidden","bankCard":"hidden"}');

-- 3.10 原业务表示例数据
INSERT INTO `tongxunlu` (`xingming`, `nianling`, `xingbie`, `touxiang`, `bumen`, `shoujihao`, `dizhi`, `youxiang`, `beizhu`) VALUES
('张三', '28', '男', 'http://localhost:8080/springboot-oa-v2/static/upload/tongxunlu_touxiang1.jpg', '技术部', '13800000002', '北京', 'zhangsan@bluewhale.com', '前端工程师'),
('李四', '30', '男', 'http://localhost:8080/springboot-oa-v2/static/upload/tongxunlu_touxiang2.jpg', '技术部', '13800000003', '北京', 'lisi@bluewhale.com', '后端工程师'),
('王五', '32', '女', 'http://localhost:8080/springboot-oa-v2/static/upload/tongxunlu_touxiang3.jpg', '人力资源部', '13800000004', '北京', 'wangwu@bluewhale.com', 'HR专员');

INSERT INTO `gonggaoxinxi` (`biaoti`, `leixing`, `fabushijian`, `gonggaoneirong`) VALUES
('欢迎使用企业级OA系统V2', '系统通知', NOW(), '本系统基于Spring Boot 3 + Vue 3重构，集成组织架构、四层权限、审计日志等功能。'),
('关于2026年春节放假安排', '行政通知', NOW(), '2026年春节放假时间为2月10日-2月17日，共8天。');

INSERT INTO `cheliangxinxi` (`cheliangmingcheng`, `chepaihao`, `cheliangleixing`, `cheliangyanse`, `tingfangweizhi`, `cheliangxiangqing`, `shiyongzhuangkuang`) VALUES
('公司用车1', '京A12345', '小轿车', '黑色', 'B1停车场', '奥迪A6L公务用车', '正常'),
('商务接待车', '京B88888', '商务车', '白色', 'B1停车场', '别克GL8', '正常');

INSERT INTO `config` (`name`, `value`) VALUES
('homepage', NULL),
('picture1', 'http://localhost:8080/springboot-oa-v2/static/upload/picture1.jpg'),
('picture2', 'http://localhost:8080/springboot-oa-v2/static/upload/picture2.jpg');

-- ==========================================================
-- 4. AI 中心：知识库 + 会话 + 配置（生产级）
-- ==========================================================

-- 4.1 知识库分类表
DROP TABLE IF EXISTS `kb`;
CREATE TABLE `kb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '知识库ID',
  `name` varchar(100) NOT NULL COMMENT '知识库名称',
  `code` varchar(50) DEFAULT NULL COMMENT '唯一编码',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` tinyint(4) DEFAULT 1 COMMENT '1=启用 0=禁用',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '1=删除 0=正常',
  `create_by` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kb_code` (`code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库分类表';

-- 4.2 知识库文档表
DROP TABLE IF EXISTS `kb_document`;
CREATE TABLE `kb_document` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `kb_id` bigint(20) NOT NULL COMMENT '所属知识库',
  `filename` varchar(255) NOT NULL COMMENT '原始文件名',
  `storage_path` varchar(500) DEFAULT NULL COMMENT '本地存储路径',
  `rag_doc_id` varchar(100) DEFAULT NULL COMMENT 'RAG-Anything 内部文档ID',
  `file_size` bigint(20) DEFAULT 0 COMMENT '文件大小字节',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件后缀',
  `status` tinyint(4) DEFAULT 1 COMMENT '1=已索引 0=处理中 -1=失败',
  `error_msg` varchar(1000) DEFAULT NULL,
  `del_flag` tinyint(4) DEFAULT 0,
  `create_by` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_kb_id` (`kb_id`),
  KEY `idx_status` (`status`),
  KEY `idx_rag_doc_id` (`rag_doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- 4.3 AI 会话表
DROP TABLE IF EXISTS `kb_session`;
CREATE TABLE `kb_session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `kb_id` bigint(20) DEFAULT NULL COMMENT '关联知识库分类',
  `title` varchar(200) DEFAULT '新会话' COMMENT '会话标题',
  `status` tinyint(4) DEFAULT 1 COMMENT '1=正常 0=归档',
  `del_flag` tinyint(4) DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_kb_id` (`kb_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 会话表';

-- 4.4 AI 消息表
DROP TABLE IF EXISTS `kb_message`;
CREATE TABLE `kb_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `session_id` bigint(20) NOT NULL COMMENT '所属会话',
  `role` varchar(20) NOT NULL COMMENT 'user/bot/system',
  `content` longtext NOT NULL,
  `mode` varchar(20) DEFAULT 'hybrid' COMMENT 'hybrid/local/global',
  `duration_ms` int(11) DEFAULT 0,
  `sources` json DEFAULT NULL COMMENT '引用来源文档ID列表',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 消息表';

-- 4.5 AI 配置表（全局配置，生产级 api_key 加密）
DROP TABLE IF EXISTS `ai_config`;
CREATE TABLE `ai_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `provider` varchar(50) DEFAULT 'openai' COMMENT 'openai/custom/minimax/ollama',
  `base_url` varchar(255) DEFAULT 'https://api.openai.com/v1' COMMENT 'API 基础地址',
  `model` varchar(100) DEFAULT 'gpt-3.5-turbo',
  `api_key` varchar(500) DEFAULT NULL COMMENT 'AES 加密后的 API Key',
  `temperature` decimal(3,2) DEFAULT 0.70 COMMENT '温度 0-2',
  `max_tokens` int(11) DEFAULT 2048,
  `top_k` int(11) DEFAULT 5 COMMENT '检索 Top-K',
  `mock_mode` tinyint(4) DEFAULT 0 COMMENT '1=MOCK 模式',
  `is_default` tinyint(4) DEFAULT 1 COMMENT '1=默认配置',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 配置表';

-- 初始化默认 AI 配置（MOCK 模式，避免无 Key 启动失败）
INSERT INTO `ai_config` (`provider`, `base_url`, `model`, `api_key`, `temperature`, `max_tokens`, `top_k`, `mock_mode`, `is_default`) VALUES
('mock', '', 'mock-llm', NULL, 0.70, 2048, 5, 1, 1);

-- 初始化示例知识库
INSERT INTO `kb` (`name`, `code`, `description`) VALUES
('公司制度', 'company_policy', '员工手册、考勤、报销等公司规章制度'),
('合同模板', 'contract_template', '各类合同、协议模板'),
('技术文档', 'tech_doc', '产品技术文档、API 文档、开发规范');

SET FOREIGN_KEY_CHECKS = 1;
