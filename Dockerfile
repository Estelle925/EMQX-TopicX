# 单阶段构建 Dockerfile - 使用本地构建的前后端文件
# 运行时镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制本地构建的前端产物
COPY frontend/dist ./static

# 复制本地构建的后端JAR包
COPY backend/target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs

# 设置环境变量
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"
ENV SPRING_PROFILES_ACTIVE=prod
ENV LOG_PATH=/app/logs

# 暴露端口
EXPOSE 8080

# 启动命令
CMD java $JAVA_OPTS \
    -Dspring.datasource.url=${SPRING_DATASOURCE_URL} \
    -Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
    -Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD} \
    -Dspring.data.redis.host=${SPRING_DATA_REDIS_HOST:-localhost} \
    -Dspring.data.redis.port=${SPRING_DATA_REDIS_PORT:-6379} \
    -Dspring.data.redis.password=${SPRING_DATA_REDIS_PASSWORD} \
    -Dlogging.file.path=${LOG_PATH} \
    -jar app.jar

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1