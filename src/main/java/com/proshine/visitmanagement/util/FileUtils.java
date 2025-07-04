package com.proshine.visitmanagement.util;

import com.proshine.visitmanagement.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文件工具类
 *
 * @author System
 * @since 2024-01-01
 */
@Slf4j
public class FileUtils {

    // 图片文件扩展名
    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg"
    ));

    // 文档文件扩展名
    private static final Set<String> DOCUMENT_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".rtf"
    ));

    /**
     * 上传文件
     *
     * @param file 文件
     * @param uploadDir 上传目录
     * @return 文件相对路径
     */
    public static String uploadFile(MultipartFile file, String uploadDir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String fileName = generateUniqueFileName() + extension;

            // 保存文件
            Path targetPath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath);

            String relativePath = uploadDir + "/" + fileName;
            log.info("文件上传成功: {}", relativePath);

            return relativePath;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }
    }

    /**
     * 批量上传文件
     *
     * @param files 文件数组
     * @param uploadDir 上传目录
     * @return 文件路径列表
     */
    public static List<String> uploadFiles(MultipartFile[] files, String uploadDir) {
        if (files == null || files.length == 0) {
            throw new BusinessException("文件不能为空");
        }

        List<String> filePaths = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    String filePath = uploadFile(file, uploadDir);
                    filePaths.add(filePath);
                } catch (Exception e) {
                    log.warn("文件上传失败: {}, 错误: {}", file.getOriginalFilename(), e.getMessage());
                }
            }
        }

        return filePaths;
    }

    /**
     * 下载文件
     *
     * @param filePath 文件路径
     * @param response HTTP响应
     */
    public static void downloadFile(String filePath, HttpServletResponse response) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new BusinessException("文件不存在");
        }

        try {
            String fileName = path.getFileName().toString();
            downloadFile(filePath, fileName, response);
        } catch (Exception e) {
            log.error("文件下载失败", e);
            throw new BusinessException("文件下载失败");
        }
    }

    /**
     * 下载文件（自定义文件名）
     *
     * @param filePath 文件路径
     * @param fileName 下载文件名
     * @param response HTTP响应
     */
    public static void downloadFile(String filePath, String fileName, HttpServletResponse response) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new BusinessException("文件不存在");
        }

        try {
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setContentLengthLong(Files.size(path));

            // 写入文件内容
            try (InputStream inputStream = Files.newInputStream(path);
                 OutputStream outputStream = response.getOutputStream()) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

            log.info("文件下载成功: {}", filePath);

        } catch (IOException e) {
            log.error("文件下载失败", e);
            throw new BusinessException("文件下载失败");
        }
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }

        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("文件删除成功: {}", filePath);
                return true;
            }
        } catch (IOException e) {
            log.error("文件删除失败: {}", filePath, e);
        }

        return false;
    }

    /**
     * 批量删除文件
     *
     * @param filePaths 文件路径列表
     * @return 删除成功的数量
     */
    public static int deleteFiles(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            return 0;
        }

        int deletedCount = 0;
        for (String filePath : filePaths) {
            if (deleteFile(filePath)) {
                deletedCount++;
            }
        }

        return deletedCount;
    }

    /**
     * 复制文件
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @return 是否复制成功
     */
    public static boolean copyFile(String sourcePath, String targetPath) {
        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);

            if (!Files.exists(source)) {
                throw new BusinessException("源文件不存在");
            }

            // 创建目标目录
            Files.createDirectories(target.getParent());

            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("文件复制成功: {} -> {}", sourcePath, targetPath);
            return true;

        } catch (IOException e) {
            log.error("文件复制失败: {} -> {}", sourcePath, targetPath, e);
            throw new BusinessException("文件复制失败");
        }
    }

    /**
     * 移动文件
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @return 是否移动成功
     */
    public static boolean moveFile(String sourcePath, String targetPath) {
        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);

            if (!Files.exists(source)) {
                throw new BusinessException("源文件不存在");
            }

            // 创建目标目录
            Files.createDirectories(target.getParent());

            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("文件移动成功: {} -> {}", sourcePath, targetPath);
            return true;

        } catch (IOException e) {
            log.error("文件移动失败: {} -> {}", sourcePath, targetPath, e);
            throw new BusinessException("文件移动失败");
        }
    }

    /**
     * 获取文件信息
     *
     * @param filePath 文件路径
     * @return 文件信息
     */
    public static Map<String, Object> getFileInfo(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new BusinessException("文件不存在");
        }

        try {
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileName", path.getFileName().toString());
            fileInfo.put("filePath", filePath);
            fileInfo.put("fileSize", Files.size(path));
            fileInfo.put("fileSizeDescription", getFileSizeDescription(Files.size(path)));
            fileInfo.put("isDirectory", Files.isDirectory(path));
            fileInfo.put("isReadable", Files.isReadable(path));
            fileInfo.put("isWritable", Files.isWritable(path));
            fileInfo.put("lastModified", Files.getLastModifiedTime(path).toInstant());
            fileInfo.put("extension", getFileExtension(path.getFileName().toString()));
            fileInfo.put("isImage", isImageFile(path.getFileName().toString()));
            fileInfo.put("isDocument", isDocumentFile(path.getFileName().toString()));

            return fileInfo;

        } catch (IOException e) {
            log.error("获取文件信息失败", e);
            throw new BusinessException("获取文件信息失败");
        }
    }

    /**
     * 获取目录下的文件列表
     *
     * @param directoryPath 目录路径
     * @return 文件列表
     */
    public static List<Map<String, Object>> listFiles(String directoryPath) {
        Path path = Paths.get(directoryPath);
        if (!Files.exists(path)) {
            throw new BusinessException("目录不存在");
        }

        if (!Files.isDirectory(path)) {
            throw new BusinessException("指定路径不是目录");
        }

        try {
            List<Map<String, Object>> fileList = new ArrayList<>();

            Files.list(path).forEach(filePath -> {
                try {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("fileName", filePath.getFileName().toString());
                    fileInfo.put("filePath", filePath.toString());
                    fileInfo.put("fileSize", Files.isDirectory(filePath) ? 0 : Files.size(filePath));
                    fileInfo.put("isDirectory", Files.isDirectory(filePath));
                    fileInfo.put("lastModified", Files.getLastModifiedTime(filePath).toInstant());
                    fileInfo.put("extension", getFileExtension(filePath.getFileName().toString()));
                    fileInfo.put("isImage", isImageFile(filePath.getFileName().toString()));
                    fileInfo.put("isDocument", isDocumentFile(filePath.getFileName().toString()));

                    fileList.add(fileInfo);
                } catch (IOException e) {
                    log.warn("获取文件信息失败: {}", filePath, e);
                }
            });

            // 按文件类型和名称排序
            fileList.sort((a, b) -> {
                boolean aIsDir = (Boolean) a.get("isDirectory");
                boolean bIsDir = (Boolean) b.get("isDirectory");

                if (aIsDir && !bIsDir) return -1;
                if (!aIsDir && bIsDir) return 1;

                return ((String) a.get("fileName")).compareToIgnoreCase((String) b.get("fileName"));
            });

            return fileList;

        } catch (IOException e) {
            log.error("获取文件列表失败", e);
            throw new BusinessException("获取文件列表失败");
        }
    }

    /**
     * 判断是否为图片文件
     *
     * @param fileName 文件名
     * @return 是否为图片文件
     */
    public static boolean isImageFile(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return false;
        }

        String extension = getFileExtension(fileName).toLowerCase();
        return IMAGE_EXTENSIONS.contains(extension);
    }

    /**
     * 判断是否为文档文件
     *
     * @param fileName 文件名
     * @return 是否为文档文件
     */
    public static boolean isDocumentFile(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return false;
        }

        String extension = getFileExtension(fileName).toLowerCase();
        return DOCUMENT_EXTENSIONS.contains(extension);
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 扩展名（包含点号）
     */
    public static String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex);
        }

        return "";
    }

    /**
     * 生成唯一文件名
     *
     * @return 唯一文件名
     */
    public static String generateUniqueFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return timestamp + "_" + uuid.substring(0, 8);
    }

    /**
     * 获取文件大小描述
     *
     * @param size 文件大小（字节）
     * @return 文件大小描述
     */
    public static String getFileSizeDescription(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 验证文件类型
     *
     * @param fileName 文件名
     * @param allowedExtensions 允许的扩展名
     * @return 是否为允许的类型
     */
    public static boolean isAllowedFileType(String fileName, String[] allowedExtensions) {
        if (!StringUtils.hasText(fileName) || allowedExtensions == null || allowedExtensions.length == 0) {
            return false;
        }

        String extension = getFileExtension(fileName).toLowerCase();
        for (String allowedExt : allowedExtensions) {
            if (extension.equals(allowedExt.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 创建目录
     *
     * @param dirPath 目录路径
     * @return 是否创建成功
     */
    public static boolean createDirectory(String dirPath) {
        try {
            Path path = Paths.get(dirPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("目录创建成功: {}", dirPath);
                return true;
            } else {
                log.info("目录已存在: {}", dirPath);
                return false;
            }
        } catch (IOException e) {
            log.error("目录创建失败: {}", dirPath, e);
            throw new BusinessException("目录创建失败");
        }
    }

    /**
     * 验证文件大小
     *
     * @param file 文件
     * @param maxSizeInMB 最大大小（MB）
     */
    public static void validateFileSize(MultipartFile file, long maxSizeInMB) {
        if (file.getSize() > maxSizeInMB * 1024 * 1024) {
            throw new BusinessException("文件大小不能超过" + maxSizeInMB + "MB");
        }
    }

    /**
     * 验证文件扩展名
     *
     * @param fileName 文件名
     * @param allowedExtensions 允许的扩展名
     */
    public static void validateFileExtension(String fileName, String[] allowedExtensions) {
        if (!isAllowedFileType(fileName, allowedExtensions)) {
            throw new BusinessException("不支持的文件类型，允许的类型：" + String.join("、", allowedExtensions));
        }
    }

    /**
     * 清理文件名（移除特殊字符）
     *
     * @param fileName 原文件名
     * @return 清理后的文件名
     */
    public static String cleanFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "unknown";
        }

        // 移除路径分隔符和其他危险字符
        String cleaned = fileName.replaceAll("[/\\\\:*?\"<>|]", "_");

        // 限制文件名长度
        if (cleaned.length() > 100) {
            String extension = getFileExtension(cleaned);
            String baseName = cleaned.substring(0, cleaned.lastIndexOf('.'));
            if (baseName.length() > 95) {
                baseName = baseName.substring(0, 95);
            }
            cleaned = baseName + extension;
        }

        return cleaned;
    }

    /**
     * 获取MIME类型
     *
     * @param fileName 文件名
     * @return MIME类型
     */
    public static String getMimeType(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "application/octet-stream";
        }

        String extension = getFileExtension(fileName).toLowerCase();

        // 常见MIME类型映射
        Map<String, String> mimeTypes = new HashMap<>();
        mimeTypes.put(".jpg", "image/jpeg");
        mimeTypes.put(".jpeg", "image/jpeg");
        mimeTypes.put(".png", "image/png");
        mimeTypes.put(".gif", "image/gif");
        mimeTypes.put(".pdf", "application/pdf");
        mimeTypes.put(".doc", "application/msword");
        mimeTypes.put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mimeTypes.put(".xls", "application/vnd.ms-excel");
        mimeTypes.put(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        mimeTypes.put(".ppt", "application/vnd.ms-powerpoint");
        mimeTypes.put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        mimeTypes.put(".txt", "text/plain");
        mimeTypes.put(".html", "text/html");
        mimeTypes.put(".css", "text/css");
        mimeTypes.put(".js", "application/javascript");
        mimeTypes.put(".json", "application/json");
        mimeTypes.put(".xml", "application/xml");
        mimeTypes.put(".zip", "application/zip");
        mimeTypes.put(".rar", "application/x-rar-compressed");
        mimeTypes.put(".mp4", "video/mp4");
        mimeTypes.put(".mp3", "audio/mpeg");

        return mimeTypes.getOrDefault(extension, "application/octet-stream");
    }

    /**
     * 压缩文件（如果是图片）
     *
     * @param file 原始文件
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @param quality 压缩质量（0.0-1.0）
     * @return 压缩后的文件数据
     */
    public static byte[] compressImageIfNeeded(MultipartFile file, int maxWidth, int maxHeight, float quality) {
        if (!isImageFile(file.getOriginalFilename())) {
            try {
                return file.getBytes();
            } catch (IOException e) {
                throw new BusinessException("读取文件失败");
            }
        }

        // 这里可以集成图片压缩库，如thumbnailator
        // 暂时返回原始文件数据
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BusinessException("读取文件失败");
        }
    }

    /**
     * 安全的文件路径检查
     *
     * @param filePath 文件路径
     * @return 是否安全
     */
    public static boolean isSecurePath(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }

        // 检查路径遍历攻击
        if (filePath.contains("..") || filePath.contains("./") || filePath.contains(".\\")) {
            return false;
        }

        // 检查绝对路径
        Path path = Paths.get(filePath);
        if (path.isAbsolute()) {
            return false;
        }

        return true;
    }

    /**
     * 获取文件的MD5哈希值
     *
     * @param file 文件
     * @return MD5哈希值
     */
    public static String getFileMD5(MultipartFile file) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(file.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            log.error("计算文件MD5失败", e);
            throw new BusinessException("计算文件哈希值失败");
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 是否存在
     */
    public static boolean exists(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }

        return Files.exists(Paths.get(filePath));
    }

    /**
     * 获取临时目录路径
     *
     * @return 临时目录路径
     */
    public static String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * 创建临时文件
     *
     * @param prefix 文件名前缀
     * @param suffix 文件名后缀
     * @return 临时文件路径
     */
    public static String createTempFile(String prefix, String suffix) {
        try {
            Path tempFile = Files.createTempFile(prefix, suffix);
            return tempFile.toString();
        } catch (IOException e) {
            log.error("创建临时文件失败", e);
            throw new BusinessException("创建临时文件失败");
        }
    }
}