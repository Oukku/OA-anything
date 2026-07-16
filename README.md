# 企业级 OA 智能办公系统 (V2)

> Spring Boot 3 + Vue 2 + RAG-Anything 多模态知识库

## 项目简介

企业级 OA 办公自动化系统 V2，覆盖用户认证、员工/部门管理、公告发布、文件管理、工作日志等核心模块，并集成 **RAG-Anything** 多模态知识库，提供 PDF/Word/PPT/Excel 的智能问答能力。

## 技术栈

| 层级 | 技术 |
|---|---|
| 后端 | Spring Boot 3.2.5, MyBatis Plus 3.5.5, Java 17+, JWT, HikariCP, Knife4j |
| 前端 | Vue 2.7, Element UI 2.15, Vuex, Vue Router, Axios, ECharts |
| RAG 服务 | Python 3.11, FastAPI, LightRAG, RAG-Anything 1.3, MinerU 2.0, Torch |
| 存储 | MySQL 8, Redis 7, 本地文件存储, LightRAG NanoVectorDB |
| 工具 | Maven 3.9, Lombok 1.18, npm 10 |

## 项目结构

```
OA/
├── springboot/              # Java 后端 (Spring Boot)
│   ├── src/main/java/com/jlwl/   # 业务代码
│   ├── src/main/resources/       # 配置 + 静态资源 + admin 前端
│   ├── src/main/resources/admin/admin/  # Vue 前端
│   └── pom.xml
├── rag-service/             # Python RAG 微服务 (FastAPI)
│   ├── app/                 # 应用代码
│   ├── data/                # 文档存储
│   ├── storage/             # LightRAG 索引存储
│   └── requirements.txt
├── RAG-Anything-main/       # 第三方 RAG 框架源码
├── db-v2.sql                # 数据库初始化脚本
├── dev-start.ps1            # Windows 一键启动脚本
├── dev-start.sh             # Linux/macOS 启动脚本
└── DEPLOY.md                # 部署文档
```

## 快速开始

### 1. 准备环境

| 工具 | 版本 | 说明 |
|---|---|---|
| Java | 17+ | 后端 |
| Maven | 3.9+ | 构建 |
| Node.js | 18+ | 前端 |
| Python | 3.11 | RAG 服务 |
| MySQL | 8.0+ | 数据库 |

### 2. 一键启动 (Windows)

```powershell
.\dev-start.ps1
```

启动顺序：MySQL → 后端 (8080) → 前端 (8080 静态托管) → RAG 服务 (8001)

### 3. 手动启动

详见 [DEPLOY.md](./DEPLOY.md)

## 核心功能

### 1. 用户管理
- 登录 / Token 鉴权 / 退出
- 个人中心、修改密码

### 2. 部门与员工
- 部门树形结构
- 员工档案（增删改查）

### 3. 公告管理
- 发布 / 置顶 / 阅读
- 富文本内容

### 4. 文件管理
- 上传 / 下载 / 标签
- 配合 RAG 做内容检索

### 5. 工作日志
- 日志记录
- 配合 RAG 自动生成周报

### 6. RAG AI 智能助手 🆕
- 多模态文档解析 (PDF/Word/PPT/Excel/图片)
- 知识图谱检索（4 种查询模式）
- 图文表公式混合回答
- 视觉模型 (VLM) 增强

## API 文档

启动后访问：
- 后端 Knife4j: http://localhost:8080/springboot-oa-v2/doc.html
- RAG 服务 OpenAPI: http://localhost:8001/docs

## 验证测试

```powershell
# 全链路验证
.\rag-venv\Scripts\python.exe test-rag-quick.py
```

## 许可证

仅供学习使用。
