# 教学机构门户（Vue 2 + TypeScript）

机构端后台 SPA，提供课程管理、媒资管理、课程发布等功能，配合后端网关使用。

## 本机开发运行

```sh
# 1. 安装依赖（Node 16）
npm install

# 2. 修改 .env，把 API 指向本地网关
# VUE_APP_SERVER_API_URL=http://localhost:63010/api

# 3. 启动开发服务器
npm run serve
# 浏览器打开 http://localhost:8080/
```

## 容器化构建（推荐）

本机无需安装 Node，多阶段构建（node:16-alpine 编译 → nginx:alpine 运行，镜像约 28MB）：

```sh
docker build -t xc-portal:dev .
docker run -d --name xc-portal -p 8601:80 xc-portal:dev
```

容器内 nginx 将 `/api/` 反代到宿主机网关 63010，详见仓库根目录 README。

## 环境配置

- `.env`：开发环境 API 地址与调试 Token
- `.env.prod`：生产构建配置（已指向本地部署域名 `51xuecheng.cn` 体系）
