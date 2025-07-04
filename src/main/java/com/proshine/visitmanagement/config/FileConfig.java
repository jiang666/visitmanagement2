package com.proshine.visitmanagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件配置类
 * 
 * @author System
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileConfig {
    
    /**
     * 文件上传根目录
     */
    private String uploadPath = "./uploads";
    
    /**
     * 头像上传目录
     */
    private String avatarPath = "./uploads/avatars";
    
    /**
     * 文档上传目录
     */
    private String documentPath = "./uploads/documents";
    
    /**
     * 图片上传目录
     */
    private String imagePath = "./uploads/images";
    
    /**
     * 临时文件目录
     */
    private String tempPath = "./uploads/temp";
    
    /**
     * 最大文件大小（MB）
     */
    private long maxFileSize = 50;
    
    /**
     * 最大图片大小（MB）
     */
    private long maxImageSize = 10;
    
    /**
     * 最大文档大小（MB）
     */
    private long maxDocumentSize = 100;
    
    /**
     * 允许的图片格式
     */
    private String[] allowedImageTypes = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
    
    /**
     * 允许的文档格式
     */
    private String[] allowedDocumentTypes = {".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt"};
    
    /**
     * 是否启用文件压缩
     */
    private boolean enableCompression = true;
    
    /**
     * 图片压缩质量（0.0-1.0）
     */
    private float compressionQuality = 0.8f;
    
    /**
     * 图片最大宽度
     */
    private int maxImageWidth = 1920;
    
    /**
     * 图片最大高度
     */
    private int maxImageHeight = 1080;
    
    /**
     * 文件访问URL前缀
     */
    private String urlPrefix = "/files";
    
    /**
     * 是否启用文件去重
     */
    private boolean enableDeduplication = true;
    
    /**
     * 文件清理周期（天）
     */
    private int cleanupDays = 30;
}