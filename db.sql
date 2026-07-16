/*
SQLyog Ultimate v11.3 (64 bit)
MySQL - 5.7.32-log : Database - springboot2142g
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`springboot2142g` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `springboot2142g`;

/*Table structure for table `cheliangxinxi` */

DROP TABLE IF EXISTS `cheliangxinxi`;

CREATE TABLE `cheliangxinxi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `cheliangmingcheng` varchar(200) NOT NULL COMMENT '车辆名称',
  `chepaihao` varchar(200) NOT NULL COMMENT '车牌号',
  `cheliangleixing` varchar(200) DEFAULT NULL COMMENT '车辆类型',
  `cheliangyanse` varchar(200) DEFAULT NULL COMMENT '车辆颜色',
  `tingfangweizhi` varchar(200) DEFAULT NULL COMMENT '停放位置',
  `cheliangxiangqing` longtext COMMENT '车辆详情',
  `shiyongzhuangkuang` varchar(200) DEFAULT NULL COMMENT '使用状况',
  `thumbsupnum` int(11) DEFAULT '0' COMMENT '赞',
  `crazilynum` int(11) DEFAULT '0' COMMENT '踩',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=utf8 COMMENT='车辆信息';

/*Data for the table `cheliangxinxi` */

insert  into `cheliangxinxi`(`id`,`addtime`,`cheliangmingcheng`,`chepaihao`,`cheliangleixing`,`cheliangyanse`,`tingfangweizhi`,`cheliangxiangqing`,`shiyongzhuangkuang`,`thumbsupnum`,`crazilynum`) values (61,'2021-05-19 14:54:56','车辆名称1','车牌号1','小轿车','车辆颜色1','停放位置1','车辆详情1','未使用',1,1);
insert  into `cheliangxinxi`(`id`,`addtime`,`cheliangmingcheng`,`chepaihao`,`cheliangleixing`,`cheliangyanse`,`tingfangweizhi`,`cheliangxiangqing`,`shiyongzhuangkuang`,`thumbsupnum`,`crazilynum`) values (62,'2021-05-19 14:54:56','车辆名称2','车牌号2','小轿车','车辆颜色2','停放位置2','车辆详情2','未使用',2,2);
insert  into `cheliangxinxi`(`id`,`addtime`,`cheliangmingcheng`,`chepaihao`,`cheliangleixing`,`cheliangyanse`,`tingfangweizhi`,`cheliangxiangqing`,`shiyongzhuangkuang`,`thumbsupnum`,`crazilynum`) values (63,'2021-05-19 14:54:57','车辆名称3','车牌号3','小轿车','车辆颜色3','停放位置3','车辆详情3','未使用',3,3);
insert  into `cheliangxinxi`(`id`,`addtime`,`cheliangmingcheng`,`chepaihao`,`cheliangleixing`,`cheliangyanse`,`tingfangweizhi`,`cheliangxiangqing`,`shiyongzhuangkuang`,`thumbsupnum`,`crazilynum`) values (64,'2021-05-19 14:54:57','车辆名称4','车牌号4','小轿车','车辆颜色4','停放位置4','车辆详情4','未使用',4,4);
insert  into `cheliangxinxi`(`id`,`addtime`,`cheliangmingcheng`,`chepaihao`,`cheliangleixing`,`cheliangyanse`,`tingfangweizhi`,`cheliangxiangqing`,`shiyongzhuangkuang`,`thumbsupnum`,`crazilynum`) values (65,'2021-05-19 14:54:57','车辆名称5','车牌号5','小轿车','车辆颜色5','停放位置5','车辆详情5','未使用',5,5);
insert  into `cheliangxinxi`(`id`,`addtime`,`cheliangmingcheng`,`chepaihao`,`cheliangleixing`,`cheliangyanse`,`tingfangweizhi`,`cheliangxiangqing`,`shiyongzhuangkuang`,`thumbsupnum`,`crazilynum`) values (66,'2021-05-19 14:54:57','车辆名称6','车牌号6','小轿车','车辆颜色6','停放位置6','车辆详情6','未使用',6,6);

/*Table structure for table `config` */

DROP TABLE IF EXISTS `config`;

CREATE TABLE `config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '配置参数名称',
  `value` varchar(100) DEFAULT NULL COMMENT '配置参数值',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COMMENT='配置文件';

/*Data for the table `config` */

insert  into `config`(`id`,`name`,`value`) values (1,'picture1','http://localhost:8080/springboot2142g/upload/picture1.jpg');
insert  into `config`(`id`,`name`,`value`) values (2,'picture2','http://localhost:8080/springboot2142g/upload/picture2.jpg');
insert  into `config`(`id`,`name`,`value`) values (3,'picture3','http://localhost:8080/springboot2142g/upload/picture3.jpg');
insert  into `config`(`id`,`name`,`value`) values (6,'homepage',NULL);

/*Table structure for table `gonggaoxinxi` */

DROP TABLE IF EXISTS `gonggaoxinxi`;

CREATE TABLE `gonggaoxinxi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `biaoti` varchar(200) NOT NULL COMMENT '标题',
  `leixing` varchar(200) NOT NULL COMMENT '类型',
  `fabushijian` datetime DEFAULT NULL COMMENT '发布时间',
  `gonggaoneirong` longtext COMMENT '公告内容',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8 COMMENT='公告信息';

/*Data for the table `gonggaoxinxi` */

insert  into `gonggaoxinxi`(`id`,`addtime`,`biaoti`,`leixing`,`fabushijian`,`gonggaoneirong`) values (21,'2021-05-19 14:54:56','标题1','类型1','2021-05-19 14:54:56','公告内容1');
insert  into `gonggaoxinxi`(`id`,`addtime`,`biaoti`,`leixing`,`fabushijian`,`gonggaoneirong`) values (22,'2021-05-19 14:54:56','标题2','类型2','2021-05-19 14:54:56','公告内容2');
insert  into `gonggaoxinxi`(`id`,`addtime`,`biaoti`,`leixing`,`fabushijian`,`gonggaoneirong`) values (23,'2021-05-19 14:54:56','标题3','类型3','2021-05-19 14:54:56','公告内容3');
insert  into `gonggaoxinxi`(`id`,`addtime`,`biaoti`,`leixing`,`fabushijian`,`gonggaoneirong`) values (24,'2021-05-19 14:54:56','标题4','类型4','2021-05-19 14:54:56','公告内容4');
insert  into `gonggaoxinxi`(`id`,`addtime`,`biaoti`,`leixing`,`fabushijian`,`gonggaoneirong`) values (25,'2021-05-19 14:54:56','标题5','类型5','2021-05-19 14:54:56','公告内容5');
insert  into `gonggaoxinxi`(`id`,`addtime`,`biaoti`,`leixing`,`fabushijian`,`gonggaoneirong`) values (26,'2021-05-19 14:54:56','标题6','类型6','2021-05-19 14:54:56','公告内容6');

/*Table structure for table `gongzixinxi` */

DROP TABLE IF EXISTS `gongzixinxi`;

CREATE TABLE `gongzixinxi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mingcheng` varchar(200) NOT NULL COMMENT '名称',
  `yonghuming` varchar(200) NOT NULL COMMENT '用户名',
  `xingming` varchar(200) DEFAULT NULL COMMENT '姓名',
  `bumen` varchar(200) DEFAULT NULL COMMENT '部门',
  `zhiwei` varchar(200) DEFAULT NULL COMMENT '职位',
  `jibengongzi` int(11) NOT NULL COMMENT '基本工资',
  `jiabangongzi` int(11) NOT NULL COMMENT '加班工资',
  `fuli` int(11) NOT NULL COMMENT '福利',
  `shebao` int(11) NOT NULL COMMENT '社保',
  `koufei` int(11) NOT NULL COMMENT '扣费',
  `shifagongzi` int(11) NOT NULL COMMENT '实发工资',
  `riqi` date DEFAULT NULL COMMENT '日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=107 DEFAULT CHARSET=utf8 COMMENT='工资信息';

/*Data for the table `gongzixinxi` */

insert  into `gongzixinxi`(`id`,`addtime`,`mingcheng`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`jibengongzi`,`jiabangongzi`,`fuli`,`shebao`,`koufei`,`shifagongzi`,`riqi`) values (101,'2021-05-19 14:54:57','名称1','用户名1','姓名1','部门1','职位1',1,1,1,1,1,1,'2021-05-19');
insert  into `gongzixinxi`(`id`,`addtime`,`mingcheng`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`jibengongzi`,`jiabangongzi`,`fuli`,`shebao`,`koufei`,`shifagongzi`,`riqi`) values (102,'2021-05-19 14:54:57','名称2','用户名2','姓名2','部门2','职位2',2,2,2,2,2,2,'2021-05-19');
insert  into `gongzixinxi`(`id`,`addtime`,`mingcheng`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`jibengongzi`,`jiabangongzi`,`fuli`,`shebao`,`koufei`,`shifagongzi`,`riqi`) values (103,'2021-05-19 14:54:57','名称3','用户名3','姓名3','部门3','职位3',3,3,3,3,3,3,'2021-05-19');
insert  into `gongzixinxi`(`id`,`addtime`,`mingcheng`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`jibengongzi`,`jiabangongzi`,`fuli`,`shebao`,`koufei`,`shifagongzi`,`riqi`) values (104,'2021-05-19 14:54:57','名称4','用户名4','姓名4','部门4','职位4',4,4,4,4,4,4,'2021-05-19');
insert  into `gongzixinxi`(`id`,`addtime`,`mingcheng`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`jibengongzi`,`jiabangongzi`,`fuli`,`shebao`,`koufei`,`shifagongzi`,`riqi`) values (105,'2021-05-19 14:54:57','名称5','用户名5','姓名5','部门5','职位5',5,5,5,5,5,5,'2021-05-19');
insert  into `gongzixinxi`(`id`,`addtime`,`mingcheng`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`jibengongzi`,`jiabangongzi`,`fuli`,`shebao`,`koufei`,`shifagongzi`,`riqi`) values (106,'2021-05-19 14:54:57','名称6','用户名6','姓名6','部门6','职位6',6,6,6,6,6,6,'2021-05-19');

/*Table structure for table `gongzuorizhi` */

DROP TABLE IF EXISTS `gongzuorizhi`;

CREATE TABLE `gongzuorizhi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `biaoti` varchar(200) NOT NULL COMMENT '标题',
  `dengjishijian` datetime DEFAULT NULL COMMENT '登记时间',
  `gongzuoneirong` longtext COMMENT '工作内容',
  `yonghuming` varchar(200) DEFAULT NULL COMMENT '用户名',
  `xingming` varchar(200) DEFAULT NULL COMMENT '姓名',
  `bumen` varchar(200) DEFAULT NULL COMMENT '部门',
  `zhiwei` varchar(200) DEFAULT NULL COMMENT '职位',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8 COMMENT='工作日志';

/*Data for the table `gongzuorizhi` */

insert  into `gongzuorizhi`(`id`,`addtime`,`biaoti`,`dengjishijian`,`gongzuoneirong`,`yonghuming`,`xingming`,`bumen`,`zhiwei`) values (81,'2021-05-19 14:54:57','标题1','2021-05-19 14:54:57','工作内容1','用户名1','姓名1','部门1','职位1');
insert  into `gongzuorizhi`(`id`,`addtime`,`biaoti`,`dengjishijian`,`gongzuoneirong`,`yonghuming`,`xingming`,`bumen`,`zhiwei`) values (82,'2021-05-19 14:54:57','标题2','2021-05-19 14:54:57','工作内容2','用户名2','姓名2','部门2','职位2');
insert  into `gongzuorizhi`(`id`,`addtime`,`biaoti`,`dengjishijian`,`gongzuoneirong`,`yonghuming`,`xingming`,`bumen`,`zhiwei`) values (83,'2021-05-19 14:54:57','标题3','2021-05-19 14:54:57','工作内容3','用户名3','姓名3','部门3','职位3');
insert  into `gongzuorizhi`(`id`,`addtime`,`biaoti`,`dengjishijian`,`gongzuoneirong`,`yonghuming`,`xingming`,`bumen`,`zhiwei`) values (84,'2021-05-19 14:54:57','标题4','2021-05-19 14:54:57','工作内容4','用户名4','姓名4','部门4','职位4');
insert  into `gongzuorizhi`(`id`,`addtime`,`biaoti`,`dengjishijian`,`gongzuoneirong`,`yonghuming`,`xingming`,`bumen`,`zhiwei`) values (85,'2021-05-19 14:54:57','标题5','2021-05-19 14:54:57','工作内容5','用户名5','姓名5','部门5','职位5');
insert  into `gongzuorizhi`(`id`,`addtime`,`biaoti`,`dengjishijian`,`gongzuoneirong`,`yonghuming`,`xingming`,`bumen`,`zhiwei`) values (86,'2021-05-19 14:54:57','标题6','2021-05-19 14:54:57','工作内容6','用户名6','姓名6','部门6','职位6');

/*Table structure for table `kehuguanxi` */

DROP TABLE IF EXISTS `kehuguanxi`;

CREATE TABLE `kehuguanxi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `kehuxingming` varchar(200) NOT NULL COMMENT '客户姓名',
  `xingbie` varchar(200) DEFAULT NULL COMMENT '性别',
  `nianling` varchar(200) DEFAULT NULL COMMENT '年龄',
  `shengri` date DEFAULT NULL COMMENT '生日',
  `gongsimingcheng` varchar(200) NOT NULL COMMENT '公司名称',
  `lianxidianhua` varchar(200) DEFAULT NULL COMMENT '联系电话',
  `youxiang` varchar(200) DEFAULT NULL COMMENT '邮箱',
  `hezuoneirong` longtext COMMENT '合作内容',
  `yonghuming` varchar(200) DEFAULT NULL COMMENT '用户名',
  `xingming` varchar(200) DEFAULT NULL COMMENT '姓名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8 COMMENT='客户关系';

/*Data for the table `kehuguanxi` */

insert  into `kehuguanxi`(`id`,`addtime`,`kehuxingming`,`xingbie`,`nianling`,`shengri`,`gongsimingcheng`,`lianxidianhua`,`youxiang`,`hezuoneirong`,`yonghuming`,`xingming`) values (31,'2021-05-19 14:54:56','客户姓名1','男','年龄1','2021-05-19','公司名称1','13823888881','773890001@qq.com','合作内容1','用户名1','姓名1');
insert  into `kehuguanxi`(`id`,`addtime`,`kehuxingming`,`xingbie`,`nianling`,`shengri`,`gongsimingcheng`,`lianxidianhua`,`youxiang`,`hezuoneirong`,`yonghuming`,`xingming`) values (32,'2021-05-19 14:54:56','客户姓名2','男','年龄2','2021-05-19','公司名称2','13823888882','773890002@qq.com','合作内容2','用户名2','姓名2');
insert  into `kehuguanxi`(`id`,`addtime`,`kehuxingming`,`xingbie`,`nianling`,`shengri`,`gongsimingcheng`,`lianxidianhua`,`youxiang`,`hezuoneirong`,`yonghuming`,`xingming`) values (33,'2021-05-19 14:54:56','客户姓名3','男','年龄3','2021-05-19','公司名称3','13823888883','773890003@qq.com','合作内容3','用户名3','姓名3');
insert  into `kehuguanxi`(`id`,`addtime`,`kehuxingming`,`xingbie`,`nianling`,`shengri`,`gongsimingcheng`,`lianxidianhua`,`youxiang`,`hezuoneirong`,`yonghuming`,`xingming`) values (34,'2021-05-19 14:54:56','客户姓名4','男','年龄4','2021-05-19','公司名称4','13823888884','773890004@qq.com','合作内容4','用户名4','姓名4');
insert  into `kehuguanxi`(`id`,`addtime`,`kehuxingming`,`xingbie`,`nianling`,`shengri`,`gongsimingcheng`,`lianxidianhua`,`youxiang`,`hezuoneirong`,`yonghuming`,`xingming`) values (35,'2021-05-19 14:54:56','客户姓名5','男','年龄5','2021-05-19','公司名称5','13823888885','773890005@qq.com','合作内容5','用户名5','姓名5');
insert  into `kehuguanxi`(`id`,`addtime`,`kehuxingming`,`xingbie`,`nianling`,`shengri`,`gongsimingcheng`,`lianxidianhua`,`youxiang`,`hezuoneirong`,`yonghuming`,`xingming`) values (36,'2021-05-19 14:54:56','客户姓名6','男','年龄6','2021-05-19','公司名称6','13823888886','773890006@qq.com','合作内容6','用户名6','姓名6');

/*Table structure for table `richenganpai` */

DROP TABLE IF EXISTS `richenganpai`;

CREATE TABLE `richenganpai` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `bianhao` varchar(200) DEFAULT NULL COMMENT '编号',
  `mingcheng` varchar(200) NOT NULL COMMENT '名称',
  `leixing` varchar(200) DEFAULT NULL COMMENT '类型',
  `riqi` date DEFAULT NULL COMMENT '日期',
  `shijian` varchar(200) DEFAULT NULL COMMENT '时间',
  `richengneirong` longtext COMMENT '日程内容',
  `yonghuming` varchar(200) DEFAULT NULL COMMENT '用户名',
  `xingming` varchar(200) DEFAULT NULL COMMENT '姓名',
  `bumen` varchar(200) DEFAULT NULL COMMENT '部门',
  `zhiwei` varchar(200) DEFAULT NULL COMMENT '职位',
  PRIMARY KEY (`id`),
  UNIQUE KEY `bianhao` (`bianhao`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8 COMMENT='日程安排';

/*Data for the table `richenganpai` */

insert  into `richenganpai`(`id`,`addtime`,`bianhao`,`mingcheng`,`leixing`,`riqi`,`shijian`,`richengneirong`,`yonghuming`,`xingming`,`bumen`,`zhiwei`) values (51,'2021-05-19 14:54:56','编号1','名称1','工作安排','2021-05-19','时间1','日程内容1','用户名1','姓名1','部门1','职位1');
insert  into `richenganpai`(`id`,`addtime`,`bianhao`,`mingcheng`,`leixing`,`riqi`,`shijian`,`richengneirong`,`yonghuming`,`xingming`,`bumen`,`zhiwei`) values (52,'2021-05-19 14:54:56','编号2','名称2','工作安排','2021-05-19','时间2','日程内容2','用户名2','姓名2','部门2','职位2');
insert  into `richenganpai`(`id`,`addtime`,`bianhao`,`mingcheng`,`leixing`,`riqi`,`shijian`,`richengneirong`,`yonghuming`,`xingming`,`bumen`,`zhiwei`) values (53,'2021-05-19 14:54:56','编号3','名称3','工作安排','2021-05-19','时间3','日程内容3','用户名3','姓名3','部门3','职位3');
insert  into `richenganpai`(`id`,`addtime`,`bianhao`,`mingcheng`,`leixing`,`riqi`,`shijian`,`richengneirong`,`yonghuming`,`xingming`,`bumen`,`zhiwei`) values (54,'2021-05-19 14:54:56','编号4','名称4','工作安排','2021-05-19','时间4','日程内容4','用户名4','姓名4','部门4','职位4');
insert  into `richenganpai`(`id`,`addtime`,`bianhao`,`mingcheng`,`leixing`,`riqi`,`shijian`,`richengneirong`,`yonghuming`,`xingming`,`bumen`,`zhiwei`) values (55,'2021-05-19 14:54:56','编号5','名称5','工作安排','2021-05-19','时间5','日程内容5','用户名5','姓名5','部门5','职位5');
insert  into `richenganpai`(`id`,`addtime`,`bianhao`,`mingcheng`,`leixing`,`riqi`,`shijian`,`richengneirong`,`yonghuming`,`xingming`,`bumen`,`zhiwei`) values (56,'2021-05-19 14:54:56','编号6','名称6','工作安排','2021-05-19','时间6','日程内容6','用户名6','姓名6','部门6','职位6');

/*Table structure for table `shangbankaoqin` */

DROP TABLE IF EXISTS `shangbankaoqin`;

CREATE TABLE `shangbankaoqin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `bianhao` varchar(200) DEFAULT NULL COMMENT '编号',
  `mingcheng` varchar(200) NOT NULL COMMENT '名称',
  `riqi` date DEFAULT NULL COMMENT '日期',
  `yonghuming` varchar(200) NOT NULL COMMENT '用户名',
  `xingming` varchar(200) DEFAULT NULL COMMENT '姓名',
  `bumen` varchar(200) DEFAULT NULL COMMENT '部门',
  `zhiwei` varchar(200) DEFAULT NULL COMMENT '职位',
  `zaotuicishu` varchar(200) NOT NULL COMMENT '早退次数',
  `chidaocishu` varchar(200) NOT NULL COMMENT '迟到次数',
  `qingjiacishu` varchar(200) NOT NULL COMMENT '请假次数',
  `kuanggongcishu` varchar(200) NOT NULL COMMENT '旷工次数',
  `userid` bigint(20) DEFAULT NULL COMMENT '用户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `bianhao` (`bianhao`)
) ENGINE=InnoDB AUTO_INCREMENT=97 DEFAULT CHARSET=utf8 COMMENT='上班考勤';

/*Data for the table `shangbankaoqin` */

insert  into `shangbankaoqin`(`id`,`addtime`,`bianhao`,`mingcheng`,`riqi`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`zaotuicishu`,`chidaocishu`,`qingjiacishu`,`kuanggongcishu`,`userid`) values (91,'2021-05-19 14:54:57','编号1','名称1','2021-05-19','用户名1','姓名1','部门1','职位1','早退次数1','迟到次数1','请假次数1','旷工次数1',1);
insert  into `shangbankaoqin`(`id`,`addtime`,`bianhao`,`mingcheng`,`riqi`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`zaotuicishu`,`chidaocishu`,`qingjiacishu`,`kuanggongcishu`,`userid`) values (92,'2021-05-19 14:54:57','编号2','名称2','2021-05-19','用户名2','姓名2','部门2','职位2','早退次数2','迟到次数2','请假次数2','旷工次数2',2);
insert  into `shangbankaoqin`(`id`,`addtime`,`bianhao`,`mingcheng`,`riqi`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`zaotuicishu`,`chidaocishu`,`qingjiacishu`,`kuanggongcishu`,`userid`) values (93,'2021-05-19 14:54:57','编号3','名称3','2021-05-19','用户名3','姓名3','部门3','职位3','早退次数3','迟到次数3','请假次数3','旷工次数3',3);
insert  into `shangbankaoqin`(`id`,`addtime`,`bianhao`,`mingcheng`,`riqi`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`zaotuicishu`,`chidaocishu`,`qingjiacishu`,`kuanggongcishu`,`userid`) values (94,'2021-05-19 14:54:57','编号4','名称4','2021-05-19','用户名4','姓名4','部门4','职位4','早退次数4','迟到次数4','请假次数4','旷工次数4',4);
insert  into `shangbankaoqin`(`id`,`addtime`,`bianhao`,`mingcheng`,`riqi`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`zaotuicishu`,`chidaocishu`,`qingjiacishu`,`kuanggongcishu`,`userid`) values (95,'2021-05-19 14:54:57','编号5','名称5','2021-05-19','用户名5','姓名5','部门5','职位5','早退次数5','迟到次数5','请假次数5','旷工次数5',5);
insert  into `shangbankaoqin`(`id`,`addtime`,`bianhao`,`mingcheng`,`riqi`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`zaotuicishu`,`chidaocishu`,`qingjiacishu`,`kuanggongcishu`,`userid`) values (96,'2021-05-19 14:54:57','编号6','名称6','2021-05-19','用户名6','姓名6','部门6','职位6','早退次数6','迟到次数6','请假次数6','旷工次数6',6);

/*Table structure for table `token` */

DROP TABLE IF EXISTS `token`;

CREATE TABLE `token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userid` bigint(20) NOT NULL COMMENT '用户id',
  `username` varchar(100) NOT NULL COMMENT '用户名',
  `tablename` varchar(100) DEFAULT NULL COMMENT '表名',
  `role` varchar(100) DEFAULT NULL COMMENT '角色',
  `token` varchar(200) NOT NULL COMMENT '密码',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `expiratedtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '过期时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='token表';

/*Data for the table `token` */

insert  into `token`(`id`,`userid`,`username`,`tablename`,`role`,`token`,`addtime`,`expiratedtime`) values (1,1,'abo','users','管理员','5dkkmppugxu6hcolknvjhfjxtmkbfef2','2021-05-19 14:56:39','2021-05-19 15:56:40');

/*Table structure for table `tongxunlu` */

DROP TABLE IF EXISTS `tongxunlu`;

CREATE TABLE `tongxunlu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `xingming` varchar(200) NOT NULL COMMENT '姓名',
  `nianling` varchar(200) DEFAULT NULL COMMENT '年龄',
  `xingbie` varchar(200) DEFAULT NULL COMMENT '性别',
  `touxiang` varchar(200) DEFAULT NULL COMMENT '头像',
  `bumen` varchar(200) DEFAULT NULL COMMENT '部门',
  `shoujihao` varchar(200) DEFAULT NULL COMMENT '手机号',
  `dizhi` varchar(200) DEFAULT NULL COMMENT '地址',
  `youxiang` varchar(200) DEFAULT NULL COMMENT '邮箱',
  `beizhu` longtext COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8 COMMENT='通讯录';

/*Data for the table `tongxunlu` */

insert  into `tongxunlu`(`id`,`addtime`,`xingming`,`nianling`,`xingbie`,`touxiang`,`bumen`,`shoujihao`,`dizhi`,`youxiang`,`beizhu`) values (41,'2021-05-19 14:54:56','姓名1','年龄1','男','http://localhost:8080/springboot2142g/upload/tongxunlu_touxiang1.jpg','部门1','13823888881','地址1','773890001@qq.com','备注1');
insert  into `tongxunlu`(`id`,`addtime`,`xingming`,`nianling`,`xingbie`,`touxiang`,`bumen`,`shoujihao`,`dizhi`,`youxiang`,`beizhu`) values (42,'2021-05-19 14:54:56','姓名2','年龄2','男','http://localhost:8080/springboot2142g/upload/tongxunlu_touxiang2.jpg','部门2','13823888882','地址2','773890002@qq.com','备注2');
insert  into `tongxunlu`(`id`,`addtime`,`xingming`,`nianling`,`xingbie`,`touxiang`,`bumen`,`shoujihao`,`dizhi`,`youxiang`,`beizhu`) values (43,'2021-05-19 14:54:56','姓名3','年龄3','男','http://localhost:8080/springboot2142g/upload/tongxunlu_touxiang3.jpg','部门3','13823888883','地址3','773890003@qq.com','备注3');
insert  into `tongxunlu`(`id`,`addtime`,`xingming`,`nianling`,`xingbie`,`touxiang`,`bumen`,`shoujihao`,`dizhi`,`youxiang`,`beizhu`) values (44,'2021-05-19 14:54:56','姓名4','年龄4','男','http://localhost:8080/springboot2142g/upload/tongxunlu_touxiang4.jpg','部门4','13823888884','地址4','773890004@qq.com','备注4');
insert  into `tongxunlu`(`id`,`addtime`,`xingming`,`nianling`,`xingbie`,`touxiang`,`bumen`,`shoujihao`,`dizhi`,`youxiang`,`beizhu`) values (45,'2021-05-19 14:54:56','姓名5','年龄5','男','http://localhost:8080/springboot2142g/upload/tongxunlu_touxiang5.jpg','部门5','13823888885','地址5','773890005@qq.com','备注5');
insert  into `tongxunlu`(`id`,`addtime`,`xingming`,`nianling`,`xingbie`,`touxiang`,`bumen`,`shoujihao`,`dizhi`,`youxiang`,`beizhu`) values (46,'2021-05-19 14:54:56','姓名6','年龄6','男','http://localhost:8080/springboot2142g/upload/tongxunlu_touxiang6.jpg','部门6','13823888886','地址6','773890006@qq.com','备注6');

/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(100) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `role` varchar(100) DEFAULT '管理员' COMMENT '角色',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='用户表';

/*Data for the table `users` */

insert  into `users`(`id`,`username`,`password`,`role`,`addtime`) values (1,'abo','abo','管理员','2021-05-19 14:54:57');

/*Table structure for table `wenjianxinxi` */

DROP TABLE IF EXISTS `wenjianxinxi`;

CREATE TABLE `wenjianxinxi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `biaoti` varchar(200) NOT NULL COMMENT '标题',
  `fujian` varchar(200) DEFAULT NULL COMMENT '附件',
  `wenjianneirong` longtext COMMENT '文件内容',
  `fabushijian` datetime DEFAULT NULL COMMENT '发布时间',
  `yonghuming` varchar(200) DEFAULT NULL COMMENT '用户名',
  `xingming` varchar(200) DEFAULT NULL COMMENT '姓名',
  `bumen` varchar(200) DEFAULT NULL COMMENT '部门',
  `zhiwei` varchar(200) DEFAULT NULL COMMENT '职位',
  `sfsh` varchar(200) DEFAULT '否' COMMENT '是否审核',
  `shhf` longtext COMMENT '审核回复',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=utf8 COMMENT='文件信息';

/*Data for the table `wenjianxinxi` */

insert  into `wenjianxinxi`(`id`,`addtime`,`biaoti`,`fujian`,`wenjianneirong`,`fabushijian`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`sfsh`,`shhf`) values (71,'2021-05-19 14:54:57','标题1','','文件内容1','2021-05-19 14:54:57','用户名1','姓名1','部门1','职位1','是','');
insert  into `wenjianxinxi`(`id`,`addtime`,`biaoti`,`fujian`,`wenjianneirong`,`fabushijian`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`sfsh`,`shhf`) values (72,'2021-05-19 14:54:57','标题2','','文件内容2','2021-05-19 14:54:57','用户名2','姓名2','部门2','职位2','是','');
insert  into `wenjianxinxi`(`id`,`addtime`,`biaoti`,`fujian`,`wenjianneirong`,`fabushijian`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`sfsh`,`shhf`) values (73,'2021-05-19 14:54:57','标题3','','文件内容3','2021-05-19 14:54:57','用户名3','姓名3','部门3','职位3','是','');
insert  into `wenjianxinxi`(`id`,`addtime`,`biaoti`,`fujian`,`wenjianneirong`,`fabushijian`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`sfsh`,`shhf`) values (74,'2021-05-19 14:54:57','标题4','','文件内容4','2021-05-19 14:54:57','用户名4','姓名4','部门4','职位4','是','');
insert  into `wenjianxinxi`(`id`,`addtime`,`biaoti`,`fujian`,`wenjianneirong`,`fabushijian`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`sfsh`,`shhf`) values (75,'2021-05-19 14:54:57','标题5','','文件内容5','2021-05-19 14:54:57','用户名5','姓名5','部门5','职位5','是','');
insert  into `wenjianxinxi`(`id`,`addtime`,`biaoti`,`fujian`,`wenjianneirong`,`fabushijian`,`yonghuming`,`xingming`,`bumen`,`zhiwei`,`sfsh`,`shhf`) values (76,'2021-05-19 14:54:57','标题6','','文件内容6','2021-05-19 14:54:57','用户名6','姓名6','部门6','职位6','是','');

/*Table structure for table `yonghu` */

DROP TABLE IF EXISTS `yonghu`;

CREATE TABLE `yonghu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `yonghuming` varchar(200) NOT NULL COMMENT '用户名',
  `mima` varchar(200) NOT NULL COMMENT '密码',
  `xingming` varchar(200) DEFAULT NULL COMMENT '姓名',
  `xingbie` varchar(200) DEFAULT NULL COMMENT '性别',
  `touxiang` varchar(200) DEFAULT NULL COMMENT '头像',
  `bumen` varchar(200) DEFAULT NULL COMMENT '部门',
  `zhiwei` varchar(200) DEFAULT NULL COMMENT '职位',
  `youxiang` varchar(200) DEFAULT NULL COMMENT '邮箱',
  `shouji` varchar(200) DEFAULT NULL COMMENT '手机',
  PRIMARY KEY (`id`),
  UNIQUE KEY `yonghuming` (`yonghuming`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COMMENT='用户';

/*Data for the table `yonghu` */

insert  into `yonghu`(`id`,`addtime`,`yonghuming`,`mima`,`xingming`,`xingbie`,`touxiang`,`bumen`,`zhiwei`,`youxiang`,`shouji`) values (11,'2021-05-19 14:54:56','1','1','姓名1','男','http://localhost:8080/springboot2142g/upload/yonghu_touxiang1.jpg','部门1','职位1','773890001@qq.com','13823888881');
insert  into `yonghu`(`id`,`addtime`,`yonghuming`,`mima`,`xingming`,`xingbie`,`touxiang`,`bumen`,`zhiwei`,`youxiang`,`shouji`) values (12,'2021-05-19 14:54:56','用户2','123456','姓名2','男','http://localhost:8080/springboot2142g/upload/yonghu_touxiang2.jpg','部门2','职位2','773890002@qq.com','13823888882');
insert  into `yonghu`(`id`,`addtime`,`yonghuming`,`mima`,`xingming`,`xingbie`,`touxiang`,`bumen`,`zhiwei`,`youxiang`,`shouji`) values (13,'2021-05-19 14:54:56','用户3','123456','姓名3','男','http://localhost:8080/springboot2142g/upload/yonghu_touxiang3.jpg','部门3','职位3','773890003@qq.com','13823888883');
insert  into `yonghu`(`id`,`addtime`,`yonghuming`,`mima`,`xingming`,`xingbie`,`touxiang`,`bumen`,`zhiwei`,`youxiang`,`shouji`) values (14,'2021-05-19 14:54:56','用户4','123456','姓名4','男','http://localhost:8080/springboot2142g/upload/yonghu_touxiang4.jpg','部门4','职位4','773890004@qq.com','13823888884');
insert  into `yonghu`(`id`,`addtime`,`yonghuming`,`mima`,`xingming`,`xingbie`,`touxiang`,`bumen`,`zhiwei`,`youxiang`,`shouji`) values (15,'2021-05-19 14:54:56','用户5','123456','姓名5','男','http://localhost:8080/springboot2142g/upload/yonghu_touxiang5.jpg','部门5','职位5','773890005@qq.com','13823888885');
insert  into `yonghu`(`id`,`addtime`,`yonghuming`,`mima`,`xingming`,`xingbie`,`touxiang`,`bumen`,`zhiwei`,`youxiang`,`shouji`) values (16,'2021-05-19 14:54:56','用户6','123456','姓名6','男','http://localhost:8080/springboot2142g/upload/yonghu_touxiang6.jpg','部门6','职位6','773890006@qq.com','13823888886');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
