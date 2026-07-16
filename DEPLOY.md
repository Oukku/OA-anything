# 部署文档 (DEPLOY.md)

> 完整部署操作手册 — Windows / Linux 双平台

## 目录

1. [环境要求](#1-环境要求)
2. [数据库初始化](#2-数据库初始化)
3. [后端部署](#3-后端部署)
4. [前端部署](#4-前端部署)
5. [RAG 服务部署](#5-rag-服务部署)
6. [一键启动](#6-一键启动)
7. [验证测试](#7-验证测试)
8. [常见问题](#8-常见问题)

---

## 1. 环境要求

### 1.1 硬件
- CPU: 2 核以上
- 内存: 8GB+ (RAG 索引时建议 16GB+)
- 硬盘: 20GB+

### 1.2 软件版本

| 工具 | 最低版本 | 推荐版本 | 验证命令 |
|---|---|---|---|
| Java | 17 | 21 | `java -version` |
| Maven | 3.8 | 3.9 | `mvn -v` |
| Node.js | 18 | 20 | `node -v` |
| npm | 9 | 10 | `npm -v` |
| Python | 3.10 | 3.11 | `python --version` |
| MySQL | 8.0 | 8.0+ | `mysql --version` |
| Redis | 6.0 | 7.0+ | `redis-cli --version` |

### 1.3 第三方账号
- **OpenAI API Key**（或兼容接口如 DeepSeek、Qwen）— 用于 RAG 的 LLM 和 Embedding

---

## 2. 数据库初始化

### 2.1 创建数据库

```bash
mysql -uroot -p
```

```sql
CREATE DATABASE springboot_oa_v2 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2.2 导入表结构与初始数据

```bash
mysql -uroot -p springboot_oa_v2 < db-v2.sql
```

> db-v2.sql 包含所有业务表（用户、部门、员工、公告、文件、日志）以及默认账号 admin / 123456

### 2.3 验证

```sql
USE springboot_oa_v2;
SHOW TABLES;
SELECT * FROM users WHERE username = 'admin';
```

---

## 3. 后端部署

### 3.1 修改配置

编辑 [springboot/src/main/resources/application-dev.yml](springboot/src/main/resources/application-dev.yml)：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/springboot_oa_v2?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码   # ← 改这里
```

### 3.2 构建

```bash
cd springboot
mvn clean package -DskipTests
```

产物：`target/springboot2142g-0.0.1-SNAPSHOT.jar`

### 3.3 启动

```bash
# 方式 1：直接运行
java -jar target/springboot2142g-0.0.1-SNAPSHOT.jar

# 方式 2：Maven 插件
mvn spring-boot:run

# 方式 3：后台运行 (Linux)
nohup java -jar target/springboot2142g-0.0.1-SNAPSHOT.jar > logs/app.log 2>&1 &
```

### 3.4 验证

```bash
curl http://localhost:8080/springboot-oa-v2/users/session-test
# 期望: {"code":0,"msg":"session-test"}
```

API 文档：http://localhost:8080/springboot-oa-v2/doc.html

---

## 4. 前端部署

### 4.1 安装依赖

```bash
cd springboot/src/main/resources/admin/admin
npm install
```

### 4.2 修改后端地址

编辑 `vue.config.js`：

```js
module.exports = {
  publicPath: '/springboot-oa-v2/',
  devServer: {
    port: 8080,
    proxy: {
      '/springboot-oa-v2': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/rag': {
        target: 'http://localhost:8001',
        changeOrigin: true
      }
    }
  }
}
```

### 4.3 启动开发服务器

```bash
npm run serve
```

访问：http://localhost:8080/springboot-oa-v2/admin/

### 4.4 生产构建

```bash
npm run build
# 产物: dist/
```

把 `dist/` 部署到 Nginx，配置：

```nginx
server {
  listen 80;
  server_name oa.example.com;
  location / {
    root /opt/oa/dist;
    try_files $uri $uri/ /index.html;
  }
  location /springboot-oa-v2/ {
    proxy_pass http://127.0.0.1:8080;
  }
  location /rag/ {
    proxy_pass http://127.0.0.1:8001;
  }
}
```

---

## 5. RAG 服务部署

### 5.1 创建虚拟环境

```bash
python -m venv rag-venv
# Windows
.\rag-venv\Scripts\activate
# Linux/macOS
source rag-venv/bin/activate
```

### 5.2 安装依赖

```bash
cd rag-service
pip install -r requirements.txt
```

> 注：torch 包约 1.5GB，安装需 5-10 分钟

### 5.3 配置环境变量

复制 `.env.example` 为 `.env`：

```bash
cp .env.example .env
```

填入：

```ini
# LLM 配置（OpenAI 兼容）
OPENAI_API_KEY=sk-xxxxxxxxxxxx
OPENAI_BASE_URL=https://api.openai.com/v1
LLM_MODEL=gpt-4o-mini
EMBEDDING_MODEL=text-embedding-3-small
EMBEDDING_DIM=1536

# 服务端口
RAG_SERVICE_PORT=8001

# 文档解析
PARSER=mineru
PARSE_METHOD=auto
```

**国内网络环境**：可使用以下兼容服务：
- DeepSeek: `OPENAI_BASE_URL=https://api.deepseek.com/v1`, `LLM_MODEL=deepseek-chat`
- 通义千问: `OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1`, `LLM_MODEL=qwen-plus`
- 月之暗面: `OPENAI_BASE_URL=https://api.moonshot.cn/v1`, `LLM_MODEL=moonshot-v1-8k`

> 留空 `OPENAI_API_KEY` 即自动启用 **mock 模式**（占位 LLM，仅用于接口联调）

### 5.4 启动

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8001
```

### 5.5 验证

```bash
curl http://localhost:8001/health
# 期望: {"status":"ok","rag_initialized":true,...}

curl http://localhost:8001/api/v1/chat/health
# 期望: 三件套 (llm/vision/embedding) 全部 ok
```

API 文档：http://localhost:8001/docs

---

## 6. 一键启动

### Windows (PowerShell)

```powershell
# 首次运行：初始化数据库 + 启动所有服务
.\dev-start.ps1

# 仅启动后端
mvn spring-boot:run

# 仅启动前端
cd springboot\src\main\resources\admin\admin
npm run serve

# 仅启动 RAG
cd rag-service
..\rag-venv\Scripts\python.exe -m uvicorn app.main:app --port 8001
```

### Linux / macOS (Bash)

```bash
./dev-start.sh
```

---

## 7. 验证测试

### 7.1 自动化测试

```bash
# Python 全链路测试
.\rag-venv\Scripts\python.exe test-rag-quick.py
```

期望输出：
- 1. 健康: 200
- 2. AI 三件套: 全部 ok
- 3. LLM 调用: 200 (返回 mock 或真实回答)
- 4. Embedding: 200 (1536 维)
- 5. 上传文档: 200 (状态 indexed)
- 6. RAG 问答: 200 (有或无上下文都算通)

### 7.2 手动测试 curl

```bash
# 1. LLM 调用
curl -X POST http://localhost:8001/api/v1/chat/raw \
  -H "Content-Type: application/json" \
  -d '{"prompt":"你好","system":"你是助手"}'

# 2. 上传文档
curl -X POST http://localhost:8001/api/v1/documents/upload \
  -F "file=@rag-service/data/test-oa-handbook.txt"

# 3. RAG 问答
curl -X POST http://localhost:8001/api/v1/query \
  -H "Content-Type: application/json" \
  -d '{"question":"年假怎么请","mode":"hybrid","kbId":"default"}'
```

### 7.3 前端功能

- 登录：admin / 123456
- 顶部菜单：首页 / 用户管理 / 公告 / 文件 / 工作日志 / AI 助手

---

## 8. 常见问题

### Q1: 后端启动报 Lombok 错误
**A:** Java 版本不匹配。Lombok 1.18.34 + Java 17+ 即可。`java -version` 检查。

### Q2: 前端 npm install 卡死
**A:** 用国内镜像：
```bash
npm config set registry https://registry.npmmirror.com
npm install
```

### Q3: RAG 报 `expected string or bytes-like object, got 'NoneType'`
**A:** 通常是 mock 模式 + 真实文档混合导致，请确认 `OPENAI_API_KEY` 状态一致。

### Q4: RAG 报 `Embedding dimension mismatch`
**A:** 配置的 `EMBEDDING_DIM` 与实际模型不符。OpenAI text-embedding-3-small 是 1536，bge-large 是 1024。

### Q5: 端口被占用
**A:** 修改端口：
- 后端：`application-dev.yml` 的 `server.port`
- 前端：`vue.config.js` 的 `devServer.port`
- RAG：`uvicorn` 的 `--port`

### Q6: RAG 服务内存占用大
**A:** 正常。LightRAG 默认使用进程内 NanoVectorDB，索引千级文档约 2-4GB。生产可换成 PostgreSQL + pgvector 或 Qdrant。

---

## 9. 服务端口速查

| 服务 | 端口 | URL |
|---|---|---|
| MySQL | 3306 | - |
| Redis | 6379 | - |
| 后端 API | 8080 | http://localhost:8080/springboot-oa-v2/ |
| 前端开发 | 8080 | http://localhost:8080/springboot-oa-v2/admin/ |
| RAG 服务 | 8001 | http://localhost:8001/ |
| Knife4j | 8080 | http://localhost:8080/springboot-oa-v2/doc.html |
| RAG OpenAPI | 8001 | http://localhost:8001/docs |

---

## 10. 目录挂载建议 (生产)

| 目录 | 内容 | 备份频率 |
|---|---|---|
| `db-v2.sql` | 数据库脚本 | 随版本 |
| `rag-service/data/` | 上传文档 | 每日 |
| `rag-service/storage/` | LightRAG 索引 | 每周 |
| `springboot/logs/` | 后端日志 | 每日切割 |

---

部署中遇到问题，开 issue 或联系开发者。
