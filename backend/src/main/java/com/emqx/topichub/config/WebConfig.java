package com.emqx.topichub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Web配置类
 * 配置跨域访问和静态资源等Web相关设置
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置跨域访问
     * 允许前端应用访问后端API
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置静态资源处理
     * 提供前端静态文件服务
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置根路径静态资源映射 - 直接访问前端页面
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }

    /**
     * 配置视图控制器
     * 处理前端路由，将SPA路由请求转发到index.html
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 处理根路径请求
        registry.addViewController("/").setViewName("forward:/index.html");
        
        // 处理前端路由请求，都转发到index.html让前端路由处理
        registry.addViewController("/dashboard").setViewName("forward:/index.html");
        registry.addViewController("/dashboard/**").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/topics").setViewName("forward:/index.html");
        registry.addViewController("/topics/**").setViewName("forward:/index.html");
        registry.addViewController("/groups").setViewName("forward:/index.html");
        registry.addViewController("/groups/**").setViewName("forward:/index.html");
        registry.addViewController("/templates").setViewName("forward:/index.html");
        registry.addViewController("/templates/**").setViewName("forward:/index.html");
        registry.addViewController("/system").setViewName("forward:/index.html");
        registry.addViewController("/system/**").setViewName("forward:/index.html");
    }

    /**
     * 配置Jackson ObjectMapper
     * 设置全局时间格式化
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 配置LocalDateTime的序列化格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        
        mapper.registerModule(javaTimeModule);
        return mapper;
    }

    /**
     * 配置HTTP消息转换器
     * 使用自定义的ObjectMapper
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        converters.add(0, converter);
    }

}