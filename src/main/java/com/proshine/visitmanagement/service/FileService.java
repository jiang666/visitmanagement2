package com.proshine.visitmanagement.service;

import com.proshine.visitmanagement.config.FileConfig;
import com.proshine.visitmanagement.exception.BusinessException;
import com.proshine.visitmanagement.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件服务类
 * 
 * @author System
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    
    private final FileConfig fileConfig;
    
    /**
     * 初始化目录结构
     */
    @PostConstruct
    public void initDirectories() {
        FileUtils.createDirectory(fileConfig.getUploadPath());
        FileUtils.createDirectory(fileConfig.getAvatarPath());
        FileUtils.createDirectory(fileConfig.getDocumentPath());
        FileUtils.createDirectory(fileConfig.getImagePath());
        FileUtils.createDirectory(fileConfig.getTempPath());
        log.info("文件目录初始化完成");
    }
    
    /**
     * 上传头像文件
     * 
     * @param file 头像文件
     * @return 文件信息
     */
    public Map<String, Object> uploadAvatar(MultipartFile file) {
        validateImageFile(file);
        
        // 验证文件大小
        FileUtils.validateFileSize(file, fileConfig.getMaxImageSize());
        
        // 验证文件类型
        FileUtils.validateFileExtension(file.getOriginalFilename(), fileConfig.getAllowedImageTypes());
        
        String filePath = FileUtils.uploadFile(file, fileConfig.getAvatarPath());
        
        return createFileInfo(file, filePath, "avatar");
    }
    
    /**
     * 上传文档文件
     * 
     * @param file 文档文件
     * @return 文件信息
     */
    public Map<String, Object> uploadDocument(MultipartFile file) {
        validateDocumentFile(file);
        
        // 验证文件大小
        FileUtils.validateFileSize(file, fileConfig.getMaxDocumentSize());
        
        // 验证文件类型
        FileUtils.validateFileExtension(file.getOriginalFilename(), fileConfig.getAllowedDocumentTypes());
        
        String filePath = FileUtils.uploadFile(file, fileConfig.getDocumentPath());
        
        return createFileInfo(file, filePath, "document");
    }
    
    /**
     * 上传图片文件
     * 
     * @param file 图片文件
     * @return 文件信息
     */
    public Map<String, Object> uploadImage(MultipartFile file) {
        validateImageFile(file);
        
        // 验证文件大小
        FileUtils.validateFileSize(file, fileConfig.getMaxImageSize());
        
        // 验证文件类型
        FileUtils.validateFileExtension(file.getOriginalFilename(), fileConfig.getAllowedImageTypes());
        
        String filePath = FileUtils.uploadFile(file, fileConfig.getImagePath());
        
        return createFileInfo(file, filePath, "image");
    }
    
    /**
     * 通用文件上传
     * 
     * @param file 文件
     * @return 文件信息
     */
    public Map<String, Object> uploadFile(MultipartFile file) {
        validateCommonFile(file);
        
        // 验证文件大小
        FileUtils.validateFileSize(file, fileConfig.getMaxFileSize());
        
        String uploadDir;
        String fileType;
        
        if (FileUtils.isImageFile(file.getOriginalFilename())) {
            uploadDir = fileConfig.getImagePath();
            fileType = "image";
            FileUtils.validateFileExtension(file.getOriginalFilename(), fileConfig.getAllowedImageTypes());
        } else if (FileUtils.isDocumentFile(file.getOriginalFilename())) {
            uploadDir = fileConfig.getDocumentPath();
            fileType = "document";
            FileUtils.validateFileExtension(file.getOriginalFilename(), fileConfig.getAllowedDocumentTypes());
        } else {
            uploadDir = fileConfig.getUploadPath();
            fileType = "other";
        }
        
        String filePath = FileUtils.uploadFile(file, uploadDir);
        
        return createFileInfo(file, filePath, fileType);
    }
    
    /**
     * 批量上传文件
     * 
     * @param files 文件数组
     * @return 上传结果
     */
    public Map<String, Object> uploadFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException("请选择要上传的文件");
        }
        
        if (files.length > 10) {
            throw new BusinessException("批量上传文件数量不能超过10个");
        }
        
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        
        for (MultipartFile file : files) {
            try {
                if (!file.isEmpty()) {
                    uploadFile(file);
                    successCount++;
                }
            } catch (Exception e) {
                failCount++;
                log.warn("文件上传失败: {}, 错误: {}", file.getOriginalFilename(), e.getMessage());
            }
        }
        
        result.put("totalCount", files.length);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        
        return result;
    }
    
    /**
     * 删除文件
     * 
     * @param filePath 文件路径
     * @return 删除结果
     */
    public boolean deleteFile(String filePath) {
        // 安全检查
        if (!FileUtils.isSecurePath(filePath)) {
            throw new BusinessException("文件路径不安全");
        }
        
        return FileUtils.deleteFile(filePath);
    }
    
    /**
     * 获取文件信息
     * 
     * @param filePath 文件路径
     * @return 文件信息
     */
    public Map<String, Object> getFileInfo(String filePath) {
        // 安全检查
        if (!FileUtils.isSecurePath(filePath)) {
            throw new BusinessException("文件路径不安全");
        }
        
        return FileUtils.getFileInfo(filePath);
    }
    
    /**
     * 获取文件列表
     * 
     * @param directory 目录
     * @return 文件列表
     */
    public List<Map<String, Object>> listFiles(String directory) {
        // 安全检查
        if (!FileUtils.isSecurePath(directory)) {
            throw new BusinessException("目录路径不安全");
        }
        
        return FileUtils.listFiles(directory);
    }
    
    /**
     * 验证图片文件
     * 
     * @param file 文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择图片文件");
        }
        
        if (!FileUtils.isImageFile(file.getOriginalFilename())) {
            throw new BusinessException("请上传图片文件");
        }
    }
    
    /**
     * 验证文档文件
     * 
     * @param file 文件
     */
    private void validateDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择文档文件");
        }
        
        if (!FileUtils.isDocumentFile(file.getOriginalFilename())) {
            throw new BusinessException("请上传文档文件");
        }
    }
    
    /**
     * 验证通用文件
     * 
     * @param file 文件
     */
    private void validateCommonFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择文件");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }
        
        // 检查文件名是否安全
        String cleanedName = FileUtils.cleanFileName(fileName);
        if (!fileName.equals(cleanedName)) {
            log.warn("文件名包含不安全字符，已清理: {} -> {}", fileName, cleanedName);
        }
    }
    
    /**
     * 创建文件信息
     * 
     * @param file 文件
     * @param filePath 文件路径
     * @param fileType 文件类型
     * @return 文件信息
     */
    private Map<String, Object> createFileInfo(MultipartFile file, String filePath, String fileType) {
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("originalName", file.getOriginalFilename());
        fileInfo.put("fileName", FileUtils.cleanFileName(file.getOriginalFilename()));
        fileInfo.put("filePath", filePath);
        fileInfo.put("fileSize", file.getSize());
        fileInfo.put("fileSizeDescription", FileUtils.getFileSizeDescription(file.getSize()));
        fileInfo.put("contentType", file.getContentType());
        fileInfo.put("fileType", fileType);
        fileInfo.put("extension", FileUtils.getFileExtension(file.getOriginalFilename()));
        fileInfo.put("isImage", FileUtils.isImageFile(file.getOriginalFilename()));
        fileInfo.put("isDocument", FileUtils.isDocumentFile(file.getOriginalFilename()));
        fileInfo.put("md5", FileUtils.getFileMD5(file));
        fileInfo.put("accessUrl", fileConfig.getUrlPrefix() + "/" + filePath.replace("\\", "/"));
        
        return fileInfo;
    }
}