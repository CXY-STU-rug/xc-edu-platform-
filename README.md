# 学成在线 plus（xuecheng-plus）

基于 Spring Cloud Alibaba 的在线教育微服务实战项目，覆盖**课程管理、选课、支付、学习、媒资、搜索**全链路。

> 重构并修复 13 处部署/运行时缺陷，已在 Windows 11 + Docker Desktop 本机端到端跑通，并按 v2 路线持续演进。

---

## 技术栈

| 类别 | 选型 |
|---|---|
| 微服务框架 | Spring Boot 2.3.7 + Spring Cloud Hoxton + Spring Cloud Alibaba 2.2.6 |
| 服务治理 | Nacos（注册中心 + 配置中心，namespace=dev402） |
| 网关 | Spring Cloud Gateway（63010）|
| 鉴权 | OAuth2 Password Grant + JWT |
| 数据库 | MySQL 8.0（6 库分离：content / learning / media / orders / system / users） |
| ORM | MyBatis-Plus |
| 消息队列 | RabbitMQ（payresult_notify 等业务队列） |
| 任务调度 | xxl-job（课程发布静态化） |
| 全文检索 | Elasticsearch 7.12.1 + Kibana |
| 对象存储 | MinIO（mediafiles / video bucket，HTTP Range 流式传输） |
| 模板引擎 | Freemarker（页面静态化） |
| 第三方支付 | 支付宝 H5 Wap（沙箱） |
| 前端 | Vue 2 + ElementUI（教学机构后台）+ 静态多页门户 |

---

## 架构

```
                        ┌─────────────────────────────┐
                        │   nginx (portal-home:80)     │
                        │ www / file / learn / teacher │
                        │     / ucenter 五个虚拟主机   │
                        └──┬─────────┬─────────────────┘
                           │         │
            浏览器 ─→ 静态门户   反代  Vue SPA (portal:80)
                           │         │
                           ├──/api/──┴──→ Gateway (63010) ──┐
                           │                                │
                           └──/mediafiles/──→ MinIO          │
                                                            ▼
   ┌────────────┬────────────┬────────────┬────────────┬───────────┐
   │ auth-svc   │ content-api│ media-api  │ learning   │ orders    │
   │ (63070)    │ (63040)    │ (63050)    │ (63020)    │ (63030)   │
   └────────────┴────────────┴────────────┴────────────┴───────────┘
   ┌────────────┬────────────┐
   │ system-api │ search     │              ┌──────────────────┐
   │ (63110)    │ (63080)    │ ←─── ES ──→  │ Nacos / RabbitMQ │
   └────────────┴────────────┘              │ MySQL / xxl-job   │
                ↑                           │ MinIO / Redis     │
                └──── checkcode (63075) ─── └──────────────────┘
```

**控制面 / 数据面分离**：Java 服务只返回视频 URL 字符串；浏览器拿到 URL 后直接通过 nginx → MinIO 拉视频字节，后端 0 流量。

---

## 业务流程

```
浏览课程 ──→ 选课 ──┬─ 免费 → 直接 701001 + 写课程表 → 学习
                    │
                    └─ 收费 → 701002 待支付 → 下单 → 支付宝扫码 → 异步回调
                                                          │
                                                          ▼
                                          MQ paynotify_queue
                                                          │
                                                          ▼
                                  learning 消费 → 701001 + 写课程表 → 学习
```

**学习资格三态**：702001 正常学习 / 702002 未购或未支付 / 702003 已过期需续费。

---

## 模块一览

| 模块 | 端口 | 主要职责 |
|---|---|---|
| `xuecheng-plus-gateway` | 63010 | 路由、JWT 校验、白名单 |
| `xuecheng-plus-auth` | 63070 | OAuth2 服务、用户密码模式 |
| `xuecheng-plus-checkcode` | 63075 | 验证码生成 |
| `xuecheng-plus-content-api` | 63040 | 课程 CRUD、审核、发布、模板渲染 |
| `xuecheng-plus-media-api` | 63050 | 媒资上传（含分块）、Feign 给 content 调 |
| `xuecheng-plus-learning-api` | 63020 | 选课、我的课程表、获取视频地址 |
| `xuecheng-plus-orders-api` | 63030 | 订单、支付二维码、支付宝下单/回调 |
| `xuecheng-plus-system-api` | 63110 | 数据字典 |
| `xuecheng-plus-search` | 63080 | ES 课程检索 |

---

## 快速启动

### 前置依赖
- Docker Desktop（Windows / WSL2）
- JDK 1.8.0_202
- Maven 3.8+
- MySQL 8（本机 3306）

### 步骤

```bash
# 1. 启动基础设施容器
cd xc
docker compose up -d

# 2. 导入 Nacos 配置（带 tenant=dev402）
bash import-nacos-config.sh

# 3. Maven 编译微服务（首次）
cd xuecheng-plus-project && mvn clean package -DskipTests

# 4. 启动 Java 微服务（v2.0 修复模板加载后，content-api 可直接以 fat jar 启动）
cd .. && bash start-services.sh

# 5. 配置 hosts（管理员）
# 127.0.0.1 www.51xuecheng.cn file.51xuecheng.cn learn.51xuecheng.cn teacher.51xuecheng.cn ucenter.51xuecheng.cn
```

### 访问入口

| URL | 说明 |
|---|---|
| `http://www.51xuecheng.cn/` | 学员主门户（静态多页 SSI） |
| `http://teacher.51xuecheng.cn/` | 教学机构后台（Vue SPA） |
| `http://learn.51xuecheng.cn/` | 学习/播放页 |
| `http://file.51xuecheng.cn/` | 媒资静态页/视频流（反代 MinIO） |
| `http://127.0.0.1:8848/nacos` | Nacos 控制台（nacos/nacos） |
| `http://127.0.0.1:8088/xxl-job-admin` | xxl-job 调度（admin/123456） |
| `http://127.0.0.1:9001` | MinIO 控制台（minioadmin/minioadmin） |
| `http://127.0.0.1:5601` | Kibana |

---

## 已修复的项目缺陷（13 项）

部署过程中发现并修复/绕过的项目缺陷清单：

### 构建/依赖
1. `xuecheng-plus-system-api` 缺 `spring-cloud-starter-alibaba-nacos-discovery/-config`，服务起来不注册
2. 7 个 `*-api` 入口模块缺 `spring-boot-maven-plugin`，只生成 thin jar 无主清单
3. Nacos 配置 POST 必须带 `tenant=dev402`，否则落 public 命名空间

### 运行时
4. **`CoursePublishServiceImpl`** 用 `new File(getResource("/").getPath() + "/templates/")` 装载 Freemarker 模板，IDE OK 但 fat jar 启动报 `FileNotFoundException`
   → **v2.0 根治：** 改用 `setClassForTemplateLoading` 走类加载器读模板，fat jar 可直接启动，不再需要 exploded 目录绕过
5. xxl-job 执行器组需手动创建，且 admin 跑 docker 容器内，自动注册的宿主机 IP 不可达 → 改 `addressType=1` 手动录入 `host.docker.internal:8999`
6. **Feign + Hystrix 默认 1s 超时**，文件上传操作实际已成功但返回 null 被误判为失败
   → **v2.0 根治：** `feign-dev.yaml` 配齐三层超时（Hystrix 30s ≥ Ribbon 3s+25s ≥ Feign），外层不再先于内层熔断
7. **`course_template.ftl:183`** 用 `${secondNode.teachplanMedia.teachplanId!''}` 处理空值，但 `teachplanMedia` 本身为 null 时 `!''` 防不住 → 加括号 `${(...)!''}`

### 业务/数据
8. **`MyCourseTablesServiceImpl.addChargeCoruse`** 幂等只查 `status=701002`，忽略已购成功的 701001，会产生脏的重复待支付记录
9. **`xc_orders.order_detail NOT NULL no default`** 但 `saveXcOrders` 不一定填充 → schema 加默认 `''` 绕过
10. `AddOrderDto.orderDetail` 不允许 null，前端必须传 goods 列表 JSON 字符串
11. **portal 容器 Vue dist** 用 `.env.prod` 打包，硬编码了已下线的远程演示服务器地址 → 早期用 sed 批量替换 dist 绕过；**v2.0 根治：** `.env.prod` 源头改为本地部署地址（`/api` 相对路径 + `51xuecheng.cn` 域），重新构建即生效

### 检索
12. **ES `course-publish` 索引** `mtName/stName` 字段是 `text` 类型，搜索代码做 aggregation 时报 `illegal_argument_exception`
    → **v2.0 根治：** 新增 `CourseIndexInitializer`，search 启动时自动按正确 mapping 建索引（`mtName/stName` 用 keyword 主字段 + keyword 子字段，同时满足 terms 聚合与 `.keyword` 精确过滤），不再依赖手工 PUT mapping
13. content-service 发布课程时不会自动同步到 ES（saveCourseIndex 在 xxl-job 任务链第二阶段，被第一阶段静态化失败连坐）
    → **v2.0 根治：** 第 4、6 两项修复后，xxl-job 三阶段任务链（静态化 → ES 索引 → Redis 缓存）可完整走通

---

## 设计要点

### 控制面 / 数据面分离
视频/静态页流量绕过 Java 后端：

```
浏览器 → file.51xuecheng.cn (nginx) → MinIO (Range 请求)
        ↑
        learning-api 只返回 URL 字符串
```

Java 服务集群只承载请求/鉴权/业务逻辑，不参与文件传输——单实例可承载更高并发。

### 页面静态化
课程发布走 xxl-job 异步任务：
```
publish() → 写 mq_message → xxl-job 扫表 → CoursePublishTask
   ├─ Freemarker 渲染 → 临时文件
   ├─ Feign 调 media-api → MinIO 上传 mediafiles/course/{id}.html
   ├─ saveCourseIndex → ES
   └─ saveCourseCache → Redis
```
学员浏览课程详情走 nginx 直读 MinIO 的静态 HTML，**完全不打 Java**。

### 可靠消息
`mq_message` 表 + 阶段状态（stage_state1/2/3/4）支持任务断点续跑，xxl-job 周期扫表重试失败任务。

---

## v2 演进路线

- [x] **v2.0 缺陷根治（2026-06）**：FreeMarker fat-jar 加载、Feign 三层超时、ES 索引自动初始化、支付回调地址配置化、清理死代码与演示脚手架
- [ ] **v2.0-5**：课程发布第三阶段 Redis 缓存预热（现仅有注释占位，未实现）
- [ ] **v2.1 工程质量**：全局异常 + JSR303 参数校验补全、`@Idempotent` 幂等注解组件（Redis setnx）、MDC traceId 跨服务日志串联
- [ ] **v2.2 组件升级**：Hystrix → Sentinel 熔断迁移（Hystrix 已停止维护）
- [ ] **v2.3 新功能**：限时秒杀课（Redis Lua 原子扣减 + MQ 异步落库）

## 待完善

- [ ] 支付宝沙箱真实对接（当前用 SQL+MQ 模拟回调）
- [ ] 直播课（teachmode=200003）业务实现
- [ ] 笔记 / 评论 / 资料下载后端接口
- [ ] 课程审核员角色 r_002 完整流程
- [ ] CI/CD + k8s 部署
- [ ] 监控接入（Prometheus / SkyWalking）

---

## 目录结构

```
xc/
├── docker-compose.yml         # 8 个基础设施容器
├── nginx-portal-home.conf     # 5 个虚拟主机配置
├── nacos-config/              # Nacos 配置导出/导入脚本
├── start-services.sh          # 启动 9 个 Java 微服务
├── portal-static/             # 学员主门户静态资源（多页 SSI）
├── portal-vue-dist/           # 教学机构 Vue dist（已修死链 + 持久化挂载）
├── project-xczx2-portal-vue-ts/  # Vue 源码
└── xuecheng-plus-project/     # Spring Cloud 项目
    ├── xuecheng-plus-gateway/
    ├── xuecheng-plus-auth/
    ├── xuecheng-plus-content/
    ├── xuecheng-plus-media/
    ├── xuecheng-plus-learning/
    ├── xuecheng-plus-orders/
    ├── xuecheng-plus-system/
    ├── xuecheng-plus-search/
    ├── xuecheng-plus-checkcode/
    ├── xuecheng-plus-base/        # 公共 base
    ├── xuecheng-plus-message-sdk/ # 可靠消息框架
    └── xuecheng-plus-third-party/ # 阿里云/七牛/支付宝 SDK 封装
```

---

## 许可

仅作学习交流用途。
