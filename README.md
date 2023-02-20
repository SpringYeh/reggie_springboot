# reggie_springboot

#### 描述
瑞吉外卖单体项目

#### 介绍
这是一个个人练手的Springboot项目，一个轻量级单体外卖服务系统，包括员工后台和用户移动端前台，功能包含登录登出、员工及用户管理、菜品及套餐展管理、购物车以及订单管理等模块。

#### 涉及技术
* springboot
* mybatis plus
* Redis
* Nginx

#### 项目优化
用Redis做缓存，用Nginx服务器存前端静态资源，反向代理，负载均衡，java代码部署到另一个服务器，并以此做MySQL主从复制实现读写分离。