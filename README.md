# EMQX Topic Hub

一个专为EMQX设计的Topic管理增强服务，提供直观的Web界面来管理和监控MQTT Topic。

## 🚀 项目简介

EMQX Topic Hub是一个现代化的MQTT Topic管理平台，旨在简化EMQX系统的Topic管理工作。通过友好的Web界面，用户可以轻松地创建、编辑、删除和监控MQTT Topic，同时支持多EMQX系统管理、标签分类、业务分组等高级功能。

## ✨ 核心功能

### 📊 系统管理
- **多系统支持**: 管理多个EMQX系统实例
- **实时状态监控**: 实时显示系统在线状态、连接数、Topic数量
- **系统健康检查**: 自动检测EMQX系统健康状态
- **安全认证**: 支持用户名密码认证，密码加密存储

### 🏷️ Topic管理
- **Topic CRUD**: 完整的Topic创建、读取、更新、删除功能
- **路径管理**: 支持MQTT Topic路径的层级管理
- **批量操作**: 支持批量添加标签、分配业务组等操作
- **搜索过滤**: 支持关键词搜索、标签筛选、业务组筛选

### 🔖 标签系统
- **标签分类**: 为Topic添加彩色标签进行分类
- **标签管理**: 创建、编辑、删除标签
- **使用统计**: 显示标签使用次数
- **删除保护**: 防止删除正在使用的标签

### 👥 业务分组
- **分组管理**: 按业务逻辑对Topic进行分组
- **层级结构**: 支持业务组的层级管理
- **权限控制**: 基于业务组的访问权限控制

### 📈 数据统计
- **实时统计**: 显示Topic总数、连接数等实时数据
- **系统概览**: 提供系统整体运行状况的仪表板
- **历史趋势**: 记录和展示历史数据趋势

## 🛠️ 技术架构

### 后端技术栈
- **Spring Boot 3.2.0**: 现代化的Java Web框架
- **MyBatis Plus 3.5.5**: 高效的ORM框架
- **Sa-Token**: 轻量级权限认证框架
- **MySQL 8.0**: 关系型数据库
- **Redis**: 缓存和会话存储
- **Maven**: 项目构建和依赖管理

### 前端技术栈
- **Vue 3**: 渐进式JavaScript框架
- **TypeScript**: 类型安全的JavaScript超集
- **Element Plus**: 基于Vue 3的组件库
- **Vite**: 快速的前端构建工具
- **Pinia**: Vue 3状态管理库
- **Tailwind CSS**: 实用优先的CSS框架

### 部署技术
- **Docker**: 容器化部署
- **Nginx**: 反向代理和静态文件服务
- **Multi-stage Build**: 优化的Docker构建策略

## 🚀 快速开始

### 环境要求

- **Docker**: 20.10+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **EMQX**: 5.0+

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

3. **准备数据库**
   
   创建MySQL数据库：
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

5. **访问应用**
   
   打开浏览器访问：`http://localhost:8080`
   
   默认登录账号：
   - 用户名：`admin`
   - 密码：`admin123`

### 本地开发部署

#### 后端开发

1. **环境要求**
   - JDK 17+
   - Maven 3.6+
   - MySQL 8.0+
   - Redis 6.0+

2. **配置数据库**
   
   修改 `backend/src/main/resources/application.yml`：
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
|--------|------|--------|
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

## 🔧 使用指南

### 1. 系统管理

1. **添加EMQX系统**
   - 进入「系统管理」页面
   - 点击「添加系统」
   - 填写EMQX系统信息（名称、URL、用户名、密码）
   - 系统会自动检测连接状态

2. **监控系统状态**
   - 实时查看系统在线状态
   - 监控连接数和Topic数量
   - 查看系统健康状态

### 2. Topic管理

1. **创建Topic**
   - 进入「Topic管理」页面
   - 点击「新建Topic」
   - 填写Topic信息（名称、路径、所属系统等）
   - 可添加标签和分配业务组

2. **管理Topic**
   - 支持搜索、筛选Topic
   - 批量操作（添加标签、分配组等）
   - 编辑和删除Topic

### 3. 标签和分组

1. **标签管理**
   - 创建彩色标签
   - 为Topic分配标签
   - 查看标签使用统计

2. **业务分组**
   - 创建业务组
   - 按业务逻辑组织Topic
   - 支持层级结构

## 🤝 贡献指南

欢迎提交Issue和Pull Request来帮助改进项目！

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系我们

如有问题或建议，请通过以下方式联系：

- 提交 [Issue](../../issues)
- 发送邮件至：support@example.com

---

**EMQX Topic Hub** - 让MQTT Topic管理更简单！ 🚀
