# EMQX-TopicX
一个专为EMQX设计的Topic管理增强服务，提供直观的Web界面来管理和监控MQTT Topic。

## 🚀 项目简介
为了解决目前生产环境中MQTT Topic管理混乱、难以维护和缺乏业务视角的问题，我自主开发并上线了一套 “EMQX Topic管理增强系统”。

该系统作为EMQX企业版功能的补充与增强，旨在实现对海量Topic的规范化、可视化和精细化管理。其核心价值与功能包括：

+ 全局视角，集中管理：系统可同步并展示现有EMQX集群的全量Topic信息，提供一个统一的控制面板，彻底改变了过去需要通过命令行或零散查询的被动状态。
+ 业务赋能，精细治理：创新性地引入了“业务分组” 和 “标签管理” 功能。允许我们不再从技术前缀（如 /device/001/temp），而是从业务逻辑（如“黄酒-传感器数据”）的维度对Topic进行归类、授权和审计，极大提升了管理效率。
+ 消息体规范与沉淀：支持为每个Topic配置和定义期望的消息体格式（Payload Schema）。这不仅为开发人员提供了明确的接口规范，减少了通信歧义，也为后续的数据质量稽核奠定了基础。



## ✨ 核心功能
### 📊 系统管理
+ **多系统支持**: 管理多个EMQX系统实例，支持不同环境的EMQX集群
+ **实时状态监控**: 实时显示系统在线状态、连接数、Topic数量
+ **连接测试**: 支持EMQX系统连接状态测试和响应时间监控
+ **系统健康检查**: 自动检测EMQX系统健康状态，提供详细的错误信息
+ **安全认证**: 支持EMQX API用户名密码认证，密码加密存储

![](https://cdn.nlark.com/yuque/0/2025/png/661183/1756391207352-484296c4-5f1b-4a34-8d87-33951f896f26.png)

### 🏷️ Topic管理
+ **Topic同步**: 从EMQX系统自动同步Topic信息，支持增量更新
+ **Topic CRUD**: 完整的Topic创建、读取、更新、删除功能
+ **路径管理**: 支持MQTT Topic路径的层级展示和管理
+ **批量操作**: 支持批量添加标签、分配业务组、设置Payload模板等操作
+ **高级搜索**: 支持关键词搜索、标签筛选、业务组筛选、系统筛选
+ **数据导出**: 支持Topic数据的导出功能

![](https://cdn.nlark.com/yuque/0/2025/png/661183/1756391231148-9610ae23-2bce-41c2-9ac3-a1e4aaacb49d.png)

![](https://cdn.nlark.com/yuque/0/2025/png/661183/1756391277226-67076bd2-0510-4a7d-a88e-b87fd0fc9422.png)

### 🔖 标签系统
+ **彩色标签**: 为Topic添加彩色标签进行可视化分类
+ **标签管理**: 创建、编辑、删除标签，支持颜色自定义
+ **使用统计**: 显示标签使用次数和关联Topic数量
+ **批量标签**: 支持批量添加和移除标签操作
+ **删除保护**: 防止删除正在使用的标签



![](https://cdn.nlark.com/yuque/0/2025/png/661183/1756391344556-461623d9-3caf-4edc-bd63-8f5019b053cc.png)

### 👥 业务分组
+ **业务分组**: 按业务逻辑对Topic进行分组管理
+ **分组统计**: 显示每个业务组的Topic数量和使用情况
+ **批量分组**: 支持批量分配Topic到业务组
+ **分组搜索**: 支持按业务组筛选和搜索Topic

![](https://cdn.nlark.com/yuque/0/2025/png/661183/1756391300959-5b9584de-efd9-4d26-ae90-f7c0e05583c5.png)

### 📄 Payload模板管理
+ **模板库**: 提供丰富的Payload模板库，支持JSON、XML等格式
+ **模板分类**: 按业务组对模板进行分类管理
+ **模板收藏**: 支持收藏常用模板，快速访问
+ **使用统计**: 记录模板使用次数和最后使用时间
+ **模板复制**: 支持基于现有模板创建新模板
+ **批量操作**: 支持批量删除和管理模板

![](https://cdn.nlark.com/yuque/0/2025/png/661183/1756391379886-e07ac2f5-948f-4d28-91d8-7619cd1e24ff.png)

### 📈 数据统计与监控
+ **实时仪表板**: 显示Topic总数、系统状态、连接数等实时数据
+ **系统概览**: 提供多系统整体运行状况的可视化仪表板
+ **业务统计**: 显示业务组和标签的使用统计
+ **状态刷新**: 支持手动刷新系统状态和数据同步

![](https://cdn.nlark.com/yuque/0/2025/png/661183/1756391397249-5eca7cc2-f5c7-44d1-9662-4de1fdeba259.png)

### 👤 用户管理
+ **用户认证**: 基于Sa-Token的用户登录认证系统
+ **用户管理**: 支持用户的创建、编辑、删除和状态管理
+ **权限控制**: 基于角色的访问权限控制
+ **会话管理**: 安全的用户会话管理和自动登出

![](https://cdn.nlark.com/yuque/0/2025/png/661183/1756391434733-16dcf05d-4a30-44ca-bd99-3b8787d0b411.png)

## 🛠️ 技术架构
### 后端技术栈
+ **Spring Boot 3.2.0**: 现代化的Java Web框架，提供RESTful API服务
+ **MyBatis Plus 3.5.5**: 高效的ORM框架，简化数据库操作
+ **Sa-Token 1.39.0**: 轻量级权限认证框架，支持JWT和Redis会话
+ **MySQL 8.2.0**: 关系型数据库，存储Topic、用户、标签等核心数据
+ **Redis**: 缓存和会话存储，提升系统性能
+ **FastJSON 2.0.43**: 高性能JSON处理库
+ **Spring Boot Actuator**: 应用监控和健康检查
+ **Maven**: 项目构建和依赖管理

### 前端技术栈
+ **Vue 3**: 渐进式JavaScript框架，采用Composition API
+ **TypeScript**: 类型安全的JavaScript超集，提供更好的开发体验
+ **Element Plus 2.10.7**: 基于Vue 3的企业级组件库
+ **Vite 5.0.12**: 快速的前端构建工具，支持热更新
+ **Pinia 3.0.3**: Vue 3官方推荐的状态管理库
+ **Tailwind CSS 3.4.1**: 实用优先的CSS框架，快速构建现代UI
+ **Axios 1.11.0**: HTTP客户端，处理API请求
+ **Vue Router 4.2.5**: Vue.js官方路由管理器

### 部署技术
+ **Docker**: 容器化部署，支持单阶段构建
+ **OpenJDK 17**: Java运行时环境
+ **G1GC**: 垃圾收集器优化
+ **Health Check**: 容器健康检查机制

## 🚀 快速开始
### 环境要求
+ **Docker**: 20.10+ (推荐使用Docker部署)
+ **MySQL**: 8.0+ (存储应用数据)
+ **Redis**: 6.0+ (缓存和会话存储)
+ **EMQX**: 5.0+ (被管理的MQTT Broker)
+ **Java**: 17+ (本地开发需要)
+ **Node.js**: 18+ (前端开发需要)
+ **Maven**: 3.6+ (后端构建需要)

### 使用Docker部署（推荐）
1. **克隆项目**

```bash
git clone <repository-url>
cd EmqxTopicHub
```

2. **构建Docker镜像**

```bash
docker build -t emqx-topic-hub:latest .
```

3. **准备数据库**创建MySQL数据库：

```sql
CREATE DATABASE emqx_topic_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

4. **运行容器**

```bash
docker run -d \
  --name emqx-topic-hub \
  -p 8080:8080 \
  -v /host/logs:/app/logs \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://your-mysql-host:3306/emqx_topic_hub \
  -e SPRING_DATASOURCE_USERNAME=your-username \
  -e SPRING_DATASOURCE_PASSWORD=your-password \
  -e SPRING_DATA_REDIS_HOST=your-redis-host \
  -e SPRING_DATA_REDIS_PASSWORD=your-redis-password \
  emqx-topic-hub:latest
```

**说明**：

    - `-v /host/logs:/app/logs`：将宿主机的`/host/logs`目录挂载到容器的`/app/logs`目录，用于持久化日志文件
    - 请将`/host/logs`替换为您希望存储日志的实际路径
5. **访问应用**打开浏览器访问：`http://localhost:8080`默认登录账号：
    - 用户名：`admin`
    - 密码：`admin123`

### 本地开发部署
#### 后端开发
1. **环境要求**
    - JDK 17+
    - Maven 3.6+
    - MySQL 8.0+
    - Redis 6.0+
2. **配置数据库**修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/emqx_topic_hub
    username: your-username
    password: your-password
  data:
    redis:
      host: localhost
      port: 6379
      password: your-redis-password
```

3. **启动后端服务**

```bash
cd backend
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

#### 前端开发
1. **环境要求**
    - Node.js 18+
    - npm 或 yarn
2. **安装依赖**

```bash
cd frontend
npm install
```

3. **启动开发服务器**

```bash
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

## 📝 配置说明
### 环境变量
| 变量名 | 描述 | 默认值 |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | MySQL数据库连接URL | - |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | - |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | - |
| `SPRING_DATA_REDIS_HOST` | Redis主机地址 | localhost |
| `SPRING_DATA_REDIS_PORT` | Redis端口 | 6379 |
| `SPRING_DATA_REDIS_PASSWORD` | Redis密码 | - |
| `SERVER_PORT` | 后端服务端口 | 8080 |


### 数据库初始化
应用首次启动时会自动创建数据库表结构。如需手动初始化，可执行以下SQL脚本：

```sql
-- 创建用户表
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL UNIQUE,
  `password` varchar(255) NOT NULL,
  `email` varchar(100),
  `status` tinyint DEFAULT 1,
  `last_login_time` datetime,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`)
);

-- 插入默认管理员账号
INSERT INTO `user` (`username`, `password`, `email`) 
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', 'admin@example.com');
```

## 🔧 配置说明
### 后端配置 (application.yml)
```yaml
server:
  port: 8080

spring:
  application:
    name: emqx-topic-hub
  
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:emqx_topic_hub}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: ${REDIS_DB:0}
    timeout: 5000
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5

# MyBatis Plus配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# Sa-Token认证配置
sa-token:
  token-name: satoken
  timeout: 2592000          # token有效期30天
  activity-timeout: -1      # 无活动超时
  is-concurrent: true       # 允许并发登录
  is-share: true           # 共享token
  token-style: uuid        # token风格
  is-log: false            # 关闭日志
  jwt-secret-key: ${JWT_SECRET:emqx-topic-hub-secret-key}

# 应用自定义配置
app:
  emqx:
    default-timeout: 10000   # EMQX API默认超时时间(ms)
    max-retry: 3            # 最大重试次数
  
# 日志配置
logging:
  level:
    com.emqx.topic: INFO
    org.springframework.web: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 前端配置
#### 环境变量配置
**.env (开发环境)**

```bash
# API基础URL
VITE_API_BASE_URL=http://localhost:8080/api

# 应用信息
VITE_APP_TITLE=EMQX Topic Hub
VITE_APP_VERSION=1.0.0

# 开发模式配置
VITE_DEV_MODE=true
VITE_MOCK_API=false
```

**.env.production (生产环境)**

```bash
# 生产环境API地址
VITE_API_BASE_URL=/api

# 应用信息
VITE_APP_TITLE=EMQX Topic Hub
VITE_APP_VERSION=1.0.0

# 生产模式配置
VITE_DEV_MODE=false
VITE_ENABLE_ANALYTICS=true
```

#### Vite配置 (vite.config.ts)
```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['vue', 'vue-router', 'pinia'],
          element: ['element-plus']
        }
      }
    }
  }
})
```

## 📁 项目结构
```plain
EMQX-TopicX/
├── backend/                     # 后端Spring Boot服务
│   ├── src/main/java/com/emqx/topic/
│   │   ├── controller/          # REST API控制器
│   │   │   ├── AuthController.java      # 用户认证
│   │   │   ├── DashboardController.java # 仪表板数据
│   │   │   ├── GroupController.java     # 业务组管理
│   │   │   ├── PayloadTemplateController.java # Payload模板
│   │   │   ├── SystemController.java    # EMQX系统管理
│   │   │   ├── TagController.java       # 标签管理
│   │   │   ├── TopicController.java     # Topic管理
│   │   │   └── UserController.java      # 用户管理
│   │   ├── service/             # 业务逻辑层
│   │   │   ├── AuthService.java         # 认证服务
│   │   │   ├── DashboardService.java    # 仪表板服务
│   │   │   ├── EmqxService.java         # EMQX API集成
│   │   │   └── TopicService.java        # Topic业务逻辑
│   │   ├── mapper/              # MyBatis数据访问层
│   │   ├── entity/              # JPA实体类
│   │   ├── dto/                 # 数据传输对象
│   │   │   └── SystemManagementDTO.java
│   │   ├── config/              # 配置类
│   │   │   ├── SaTokenConfig.java       # 认证配置
│   │   │   └── WebConfig.java           # Web配置
│   │   └── TopicApplication.java        # 应用启动类
│   ├── src/main/resources/
│   │   ├── mapper/              # MyBatis XML映射文件
│   │   ├── schema.sql           # 数据库初始化脚本
│   │   └── application.yml      # 应用配置文件
│   └── pom.xml                  # Maven依赖管理
├── frontend/                    # 前端Vue 3应用
│   ├── src/
│   │   ├── components/          # 可复用组件
│   │   ├── views/               # 页面组件
│   │   │   ├── DashboardPage.vue        # 仪表板页面
│   │   │   ├── GroupManagement.vue      # 业务组管理
│   │   │   ├── PayloadTemplate.vue      # Payload模板管理
│   │   │   ├── TopicDetail.vue          # Topic详情页
│   │   │   └── TopicOverview.vue        # Topic总览页
│   │   ├── stores/              # Pinia状态管理
│   │   ├── api/                 # API接口定义
│   │   │   ├── auth.ts                  # 认证API
│   │   │   ├── group.ts                 # 业务组API
│   │   │   ├── payloadTemplate.ts       # 模板API
│   │   │   ├── system.ts                # 系统管理API
│   │   │   └── topic.ts                 # Topic API
│   │   ├── types/               # TypeScript类型定义
│   │   ├── router/              # Vue Router路由配置
│   │   └── main.ts              # 应用入口文件
│   ├── public/                  # 静态资源
│   ├── package.json             # 前端依赖配置
│   ├── vite.config.ts           # Vite构建配置
│   ├── tailwind.config.js       # Tailwind CSS配置
│   └── tsconfig.json            # TypeScript配置
├── docker-compose.yml           # Docker编排文件
├── Dockerfile                   # 多阶段Docker构建
├── .gitignore                   # Git忽略文件
└── README.md                    # 项目文档
```

## 🔧 使用指南
### 1. 系统管理
1. **添加EMQX系统**
    - 进入「系统管理」页面
    - 点击「添加系统」按钮
    - 填写EMQX系统信息：
        * 系统名称：便于识别的名称
        * API地址：EMQX REST API地址 (如: [http://localhost:18083](http://localhost:18083))
        * 用户名/密码：EMQX Dashboard的认证信息
        * 系统描述：可选的描述信息
    - 点击「测试连接」验证配置
    - 保存后系统会自动检测连接状态
2. **监控系统状态**
    - 实时查看系统在线/离线状态
    - 监控EMQX连接数和Topic数量
    - 查看系统响应时间和最后检查时间
    - 支持手动刷新系统状态

### 2. Topic管理
1. **同步Topic**
    - 在「Topic总览」页面选择EMQX系统
    - 点击「同步Topic」从EMQX系统获取最新Topic列表
    - 系统会自动识别新增、删除的Topic
2. **管理Topic**
    - 支持多条件搜索：关键词、业务组、标签、系统
    - 批量操作：
        * 批量分配业务组
        * 批量添加/移除标签
        * 批量设置Payload模板
    - 单个Topic操作：查看详情、编辑、删除
    - 数据导出：支持导出Topic列表为Excel或CSV
3. **Topic详情**
    - 查看Topic基本信息（名称、路径、所属业务、创建时间等）
    - 管理Topic标签：添加、移除标签
    - 查看关联的Payload模板

### 3. 业务管理
1. **业务组管理**
    - 创建业务组：设置名称和描述
    - 查看业务组统计：Topic数量、使用情况
    - 编辑和删除业务组
    - 搜索和筛选业务组
2. **标签管理**
    - 创建彩色标签：自定义标签名称和颜色
    - 查看标签使用统计：关联Topic数量
    - 编辑标签信息和颜色
    - 删除未使用的标签

### 4. Payload模板管理
1. **模板库使用**
    - 浏览预置的Payload模板库
    - 按业务组筛选模板
    - 收藏常用模板
    - 查看模板使用统计
2. **自定义模板**
    - 创建新的Payload模板
    - 支持JSON、XML等格式
    - 设置模板变量和参数
    - 复制现有模板进行修改
3. **模板应用**
    - 为Topic批量设置Payload模板
    - 在Topic详情中查看关联模板
    - 记录模板使用历史

### 5. 用户管理
1. **用户账号管理**
    - 管理员可创建、编辑、删除用户
    - 设置用户状态（启用/禁用）
    - 查看用户登录历史
2. **权限控制**
    - 基于Sa-Token的认证机制
    - 支持JWT令牌和Redis会话
    - 自动登出和会话过期处理

## 📚 API文档
### 认证相关
```http
# 用户登录
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}

# 用户登出
POST /api/auth/logout
Authorization: Bearer {token}

# 获取用户信息
GET /api/auth/userinfo
Authorization: Bearer {token}
```

### 系统管理
```http
# 获取系统列表
GET /api/systems

# 创建系统
POST /api/systems
{
  "name": "EMQX生产环境",
  "apiUrl": "http://localhost:18083",
  "username": "admin",
  "password": "public",
  "description": "生产环境EMQX集群"
}

# 测试系统连接
POST /api/systems/{id}/test

# 刷新系统状态
POST /api/systems/{id}/refresh
```

### Topic管理
```http
# 获取Topic列表
GET /api/topics?page=1&size=20&keyword=sensor&groupId=1&tagId=2

# 同步Topic
POST /api/topics/sync/{systemId}

# 批量操作Topic
POST /api/topics/batch
{
  "topicIds": [1, 2, 3],
  "operation": "addTags",
  "data": {
    "tagIds": [1, 2]
  }
}

# 导出Topic数据
GET /api/topics/export?format=excel&groupId=1
```

### Payload模板管理
```http
# 获取模板列表
GET /api/payload-templates?page=1&size=20&groupId=1&favorite=true

# 创建模板
POST /api/payload-templates
{
  "name": "传感器数据模板",
  "content": "{\"temperature\": 25.5, \"humidity\": 60}",
  "description": "温湿度传感器数据格式",
  "groupId": 1
}

# 收藏/取消收藏模板
POST /api/payload-templates/{id}/favorite

# 复制模板
POST /api/payload-templates/{id}/copy
```

## 🚨 常见问题
### Q: 无法连接到EMQX系统？
**A: 请按以下步骤排查：**

1. **检查EMQX服务状态**：确保EMQX服务正常运行
2. **验证API地址**：确认API地址格式正确（如：`http://192.168.1.100:18083`）
3. **检查认证信息**：确认用户名密码正确，建议使用EMQX Dashboard的管理员账号
4. **网络连通性**：测试网络连接，确保防火墙未阻止18083端口
5. **EMQX版本兼容性**：确保EMQX版本为5.0+，支持REST API v5

### Q: Topic同步失败？
**A: 可能的原因和解决方案：**

1. **权限不足**：确保使用的账号具有Topic管理权限
2. **API超时**：检查网络延迟，可在配置中增加超时时间
3. **EMQX系统负载过高**：等待系统负载降低后重试
4. **数据库连接问题**：检查MySQL和Redis连接状态

### Q: 前端页面无法加载？
**A: 请检查以下项目：**

1. **后端服务状态**：确认Spring Boot应用已启动（端口8080）
2. **API代理配置**：检查Vite代理配置是否正确
3. **浏览器控制台**：查看是否有JavaScript错误或网络请求失败
4. **CORS配置**：确认后端CORS配置允许前端域名访问

### Q: Docker部署失败？
**A: 常见解决方案：**

1. **端口冲突**：确保8080端口未被占用
2. **数据库连接**：检查MySQL和Redis容器是否正常启动
3. **环境变量**：确认docker-compose.yml中的环境变量配置正确
4. **镜像构建**：清理Docker缓存后重新构建：`docker-compose build --no-cache`

### Q: 用户登录失败？
**A: 排查步骤：**

1. **默认账号**：使用默认管理员账号 admin/admin123
2. **数据库初始化**：确认用户表已正确初始化
3. **Sa-Token配置**：检查Sa-Token配置和Redis连接
4. **密码加密**：确认密码加密方式与数据库中存储的一致

### Q: Payload模板无法使用？
**A: 检查要点：**

1. **JSON格式**：确保模板内容为有效的JSON格式
2. **权限问题**：确认用户有模板管理权限
3. **业务组关联**：检查模板是否正确关联到业务组
4. **模板状态**：确认模板未被删除或禁用

## 🔄 版本历史
### v1.0.0 (2025-08-28)
+ ✨ 初始版本发布
+ 🚀 支持多EMQX系统管理
+ 📊 Topic同步和管理功能
+ 🏷️ 标签和业务组管理
+ 📄 Payload模板管理
+ 👤 用户认证和权限控制
+ 🐳 Docker容器化部署

### 计划中的功能
+ 📈 Topic使用统计和分析
+ 🔔 系统告警和通知
+ 📱 移动端适配
+ 🌐 国际化支持
+ 🔌 插件系统
+ 📊 更丰富的数据可视化

## 🤝 贡献指南
我们欢迎所有形式的贡献！请遵循以下步骤：

### 开发环境搭建
1. **Fork项目**到你的GitHub账号
2. **克隆项目**到本地：

```bash
git clone https://github.com/your-username/EMQX-TopicX.git
cd EMQX-TopicX
```

3. **后端开发环境**：

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

4. **前端开发环境**：

```bash
cd frontend
npm install
npm run dev
```

### 提交规范
请使用以下格式提交代码：

```plain
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Type类型：**

+ `feat`: 新功能
+ `fix`: 修复bug
+ `docs`: 文档更新
+ `style`: 代码格式调整
+ `refactor`: 代码重构
+ `test`: 测试相关
+ `chore`: 构建过程或辅助工具的变动

**示例：**

```plain
feat(topic): 添加Topic批量导出功能

- 支持Excel和CSV格式导出
- 添加导出进度提示
- 优化大数据量导出性能

Closes #123
```

### Pull Request流程
1. 创建功能分支：`git checkout -b feature/your-feature-name`
2. 提交代码：`git commit -m "feat: your feature description"`
3. 推送分支：`git push origin feature/your-feature-name`
4. 创建Pull Request
5. 等待代码审查和合并

## 📞 支持与反馈
+ 🐛 **Bug报告**：[GitHub Issues](https://github.com/your-repo/EMQX-TopicX/issues)
+ 💡 **功能建议**：[GitHub Discussions](https://github.com/your-repo/EMQX-TopicX/discussions)

## 📄 许可证
MIT License - 详见 [LICENSE](LICENSE) 文件

本项目采用MIT许可证，您可以自由使用、修改和分发本软件。

