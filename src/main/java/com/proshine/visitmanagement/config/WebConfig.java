package com.proshine.visitmanagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.http.CacheControl;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Web配置类
 *
 * @author System
 * @since 2024-01-01
 */
@Configuration
@EnableWebMvc
@RequiredArgsConstructor
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.file.upload-path:./uploads}")
    private String uploadPath;

    @Value("${app.file.export-path:./exports}")
    private String exportPath;

    /**
     * 配置跨域请求处理
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传文件访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS));

        // 导出文件访问
        registry.addResourceHandler("/exports/**")
                .addResourceLocations("file:" + exportPath + "/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS));

        // 静态资源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS));

        // Webjars资源
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS));
    }

    /**
     * 配置路径匹配
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 配置路径匹配规则
        configurer.setUseTrailingSlashMatch(true)
                .setUseSuffixPatternMatch(false)
                .setUrlPathHelper(null);
    }

    /**
     * 配置内容协商
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorParameter(false)
                .favorPathExtension(false)
                .ignoreAcceptHeader(false)
                .defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .mediaType("json", org.springframework.http.MediaType.APPLICATION_JSON)
                .mediaType("xml", org.springframework.http.MediaType.APPLICATION_XML);
    }

    /**
     * 配置HTTP消息转换器
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 添加JSON转换器
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper()));
    }

    /**
     * 配置格式化器
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 日期时间格式化
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        registrar.setTimeFormatter(DateTimeFormatter.ofPattern("HH:mm:ss"));
        registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        registrar.registerFormatters(registry);
    }

    /**
     * 配置视图控制器
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 错误页面
        registry.addViewController("/error").setViewName("error");
        registry.addViewController("/404").setViewName("error/404");
        registry.addViewController("/500").setViewName("error/500");
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 性能监控拦截器
        registry.addInterceptor(new PerformanceInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**", "/uploads/**", "/exports/**", "/webjars/**");

        // 请求日志拦截器
        registry.addInterceptor(new RequestLoggingInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/actuator/**");

        // API版本控制拦截器
        registry.addInterceptor(new ApiVersionInterceptor())
                .addPathPatterns("/api/**");
    }

    /**
     * 配置异步支持
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(30000); // 30秒超时
        configurer.setTaskExecutor(asyncTaskExecutor());
    }

    /**
     * 异步任务执行器 - 修复返回类型为AsyncTaskExecutor
     */
    @Bean
    public AsyncTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("web-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * ObjectMapper配置
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册Java 8时间模块
        mapper.registerModule(new JavaTimeModule());

        // 配置序列化
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);

        // 配置反序列化
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        // 配置包含策略
        mapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);

        // 设置日期格式
        mapper.setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        return mapper;
    }

    /**
     * 性能监控拦截器
     */
    public static class PerformanceInterceptor implements org.springframework.web.servlet.HandlerInterceptor {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PerformanceInterceptor.class);
        private static final String START_TIME = "startTime";

        @Override
        public boolean preHandle(javax.servlet.http.HttpServletRequest request,
                                 javax.servlet.http.HttpServletResponse response,
                                 Object handler) {
            request.setAttribute(START_TIME, System.currentTimeMillis());
            return true;
        }

        @Override
        public void afterCompletion(javax.servlet.http.HttpServletRequest request,
                                    javax.servlet.http.HttpServletResponse response,
                                    Object handler, Exception ex) {
            Long startTime = (Long) request.getAttribute(START_TIME);
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                String uri = request.getRequestURI();
                String method = request.getMethod();

                if (duration > 1000) { // 超过1秒的请求记录警告
                    log.warn("慢请求: {} {} 耗时: {}ms", method, uri, duration);
                } else {
                    log.debug("请求完成: {} {} 耗时: {}ms", method, uri, duration);
                }
            }
        }
    }

    /**
     * 请求日志拦截器
     */
    public static class RequestLoggingInterceptor implements org.springframework.web.servlet.HandlerInterceptor {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RequestLoggingInterceptor.class);

        @Override
        public boolean preHandle(javax.servlet.http.HttpServletRequest request,
                                 javax.servlet.http.HttpServletResponse response,
                                 Object handler) {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String clientIp = getClientIpAddress(request);

            StringBuilder logMessage = new StringBuilder();
            logMessage.append("API请求: ").append(method).append(" ").append(uri);
            if (queryString != null) {
                logMessage.append("?").append(queryString);
            }
            logMessage.append(" from ").append(clientIp);

            log.info(logMessage.toString());
            return true;
        }

        private String getClientIpAddress(javax.servlet.http.HttpServletRequest request) {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip != null ? ip : "unknown";
        }
    }

    /**
     * API版本控制拦截器
     */
    public static class ApiVersionInterceptor implements org.springframework.web.servlet.HandlerInterceptor {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApiVersionInterceptor.class);
        private static final String DEFAULT_VERSION = "1.0";

        @Override
        public boolean preHandle(javax.servlet.http.HttpServletRequest request,
                                 javax.servlet.http.HttpServletResponse response,
                                 Object handler) {
            String version = request.getHeader("API-Version");
            if (version == null || version.isEmpty()) {
                version = DEFAULT_VERSION;
            }

            // 设置API版本到响应头
            response.setHeader("API-Version", version);

            // 可以在这里添加版本兼容性检查逻辑
            if (!isVersionSupported(version)) {
                log.warn("不支持的API版本: {}", version);
                // 可以选择返回false拒绝请求或者继续处理
            }

            return true;
        }

        private boolean isVersionSupported(String version) {
            // 支持的版本列表
            return "1.0".equals(version) || "1.1".equals(version);
        }
    }

    /**
     * 自定义参数解析器
     */
    @Override
    public void addArgumentResolvers(List<org.springframework.web.method.support.HandlerMethodArgumentResolver> resolvers) {
        // 可以添加自定义的参数解析器
        // resolvers.add(new CurrentUserArgumentResolver());
    }

    /**
     * 配置默认Servlet处理
     */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
}