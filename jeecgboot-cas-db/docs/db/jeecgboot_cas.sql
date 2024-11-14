

CREATE TABLE `sys_user` (
  `id` varchar(32) NOT NULL COMMENT '主键id',
  `username` varchar(100) default NULL COMMENT '登录账号',
  `realname` varchar(100) default NULL COMMENT '真实姓名',
  `password` varchar(255) default NULL COMMENT '密码',
  `salt` varchar(45) default NULL COMMENT 'md5密码盐',
  `avatar` varchar(255) default NULL COMMENT '头像',
  `birthday` datetime default NULL COMMENT '生日',
  `sex` tinyint(1) default '0' COMMENT '性别(0-默认未知,1-男,2-女)',
  `email` varchar(45) default NULL COMMENT '电子邮件',
  `phone` varchar(45) default NULL COMMENT '电话',
  `org_code` varchar(64) default NULL COMMENT '机构编码',
  `status` tinyint(1) default NULL COMMENT '性别(1-正常,2-冻结)',
  `del_flag` tinyint(1) default NULL COMMENT '删除状态(0-正常,1-已删除)',
  `activiti_sync` tinyint(1) default NULL COMMENT '同步工作流引擎(1-同步,0-不同步)',
  `create_by` varchar(32) default NULL COMMENT '创建人',
  `create_time` datetime default NULL COMMENT '创建时间',
  `update_by` varchar(32) default NULL COMMENT '更新人',
  `update_time` datetime default NULL COMMENT '更新时间',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `index_user_name` USING BTREE (`username`),
  KEY `index_user_status` USING BTREE (`status`),
  KEY `index_user_del_flag` USING BTREE (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

