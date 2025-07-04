package com.proshine.visitmanagement.controller;

import com.proshine.visitmanagement.dto.response.ApiResponse;
import com.proshine.visitmanagement.exception.BusinessException;
import com.proshine.visitmanagement.service.FileService;
import com.proshine.visitmanagement.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件控制器
 * 处理文件上传、下载、删除等操作
 *
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 单文件上传
     *
     * @param file 上传的文件
     * @return 上传结果
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public ApiResponse<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("开始上传文件: fileName={}, size={}", file.getOriginalFilename(), file.getSize());

        try {
            Map<String, Object> result = fileService.uploadFile(file);
            return ApiResponse.success(result, "文件上传成功");

        } catch (BusinessException e) {
            log.error("文件上传失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("文件上传异常", e);
            return ApiResponse.error("文件上传失败");
        }
    }

    /**
     * 上传头像
     *
     * @param file 头像文件
     * @return 上传结果
     */
    @PostMapping(value = "/upload/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public ApiResponse<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("开始上传头像: fileName={}, size={}", file.getOriginalFilename(), file.getSize());

        try {
            Map<String, Object> result = fileService.uploadAvatar(file);
            return ApiResponse.success(result, "头像上传成功");

        } catch (BusinessException e) {
            log.error("头像上传失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("头像上传异常", e);
            return ApiResponse.error("头像上传失败");
        }
    }

    /**
     * 上传文档
     *
     * @param file 文档文件
     * @return 上传结果
     */
    @PostMapping(value = "/upload/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public ApiResponse<Map<String, Object>> uploadDocument(@RequestParam("file") MultipartFile file) {
        log.info("开始上传文档: fileName={}, size={}", file.getOriginalFilename(), file.getSize());

        try {
            Map<String, Object> result = fileService.uploadDocument(file);
            return ApiResponse.success(result, "文档上传成功");

        } catch (BusinessException e) {
            log.error("文档上传失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("文档上传异常", e);
            return ApiResponse.error("文档上传失败");
        }
    }

    /**
     * 上传图片
     *
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public ApiResponse<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("开始上传图片: fileName={}, size={}", file.getOriginalFilename(), file.getSize());

        try {
            Map<String, Object> result = fileService.uploadImage(file);
            return ApiResponse.success(result, "图片上传成功");

        } catch (BusinessException e) {
            log.error("图片上传失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("图片上传异常", e);
            return ApiResponse.error("图片上传失败");
        }
    }

    /**
     * 多文件上传
     *
     * @param files 上传的文件数组
     * @return 上传结果
     */
    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public ApiResponse<Map<String, Object>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        log.info("开始批量上传文件: fileCount={}", files.length);

        try {
            Map<String, Object> result = fileService.uploadFiles(files);
            return ApiResponse.success(result, "批量文件上传完成");

        } catch (BusinessException e) {
            log.error("批量文件上传失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("批量文件上传异常", e);
            return ApiResponse.error("批量文件上传失败");
        }
    }

    /**
     * 文件下载
     *
     * @param filePath 文件路径
     * @param response HTTP响应对象
     */
    @GetMapping("/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public void downloadFile(@RequestParam("path") String filePath, HttpServletResponse response) {
        log.info("开始下载文件: filePath={}", filePath);

        try {
            FileUtils.downloadFile(filePath, response);

        } catch (BusinessException e) {
            log.error("文件下载失败: {}", e.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("文件下载异常", e);
            writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件下载失败");
        }
    }

    /**
     * 带自定义文件名的文件下载
     *
     * @param filePath 文件路径
     * @param fileName 下载时的文件名
     * @param response HTTP响应对象
     */
    @GetMapping("/download/custom")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public void downloadFileWithCustomName(@RequestParam("path") String filePath,
                                           @RequestParam("name") String fileName,
                                           HttpServletResponse response) {
        log.info("开始下载文件（自定义名称）: filePath={}, fileName={}", filePath, fileName);

        try {
            FileUtils.downloadFile(filePath, fileName, response);

        } catch (BusinessException e) {
            log.error("文件下载失败: {}", e.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("文件下载异常", e);
            writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件下载失败");
        }
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<Boolean> deleteFile(@RequestParam("path") String filePath) {
        log.info("开始删除文件: filePath={}", filePath);

        try {
            boolean deleted = fileService.deleteFile(filePath);

            if (deleted) {
                return ApiResponse.success(true, "文件删除成功");
            } else {
                return ApiResponse.success(false, "文件不存在或已被删除");
            }

        } catch (BusinessException e) {
            log.error("文件删除失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("文件删除异常", e);
            return ApiResponse.error("文件删除失败");
        }
    }

    /**
     * 批量删除文件
     *
     * @param filePaths 文件路径列表
     * @return 删除结果
     */
    @DeleteMapping("/delete/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<Map<String, Object>> deleteFiles(@RequestBody List<String> filePaths) {
        log.info("开始批量删除文件: fileCount={}", filePaths.size());

        try {
            int deletedCount = 0;
            int failedCount = 0;

            for (String filePath : filePaths) {
                try {
                    if (fileService.deleteFile(filePath)) {
                        deletedCount++;
                    } else {
                        failedCount++;
                    }
                } catch (Exception e) {
                    failedCount++;
                    log.warn("删除文件失败: {}, 错误: {}", filePath, e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", filePaths.size());
            result.put("deletedCount", deletedCount);
            result.put("failedCount", failedCount);

            return ApiResponse.success(result, "批量删除文件完成");

        } catch (Exception e) {
            log.error("批量删除文件异常", e);
            return ApiResponse.error("批量删除文件失败");
        }
    }

    /**
     * 获取文件信息
     *
     * @param filePath 文件路径
     * @return 文件信息
     */
    @GetMapping("/info")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public ApiResponse<Map<String, Object>> getFileInfo(@RequestParam("path") String filePath) {
        log.info("获取文件信息: filePath={}", filePath);

        try {
            Map<String, Object> fileInfo = fileService.getFileInfo(filePath);
            return ApiResponse.success(fileInfo, "获取文件信息成功");

        } catch (BusinessException e) {
            log.error("获取文件信息失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取文件信息异常", e);
            return ApiResponse.error("获取文件信息失败");
        }
    }

    /**
     * 复制文件
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @return 复制结果
     */
    @PostMapping("/copy")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<Boolean> copyFile(@RequestParam("source") String sourcePath,
                                         @RequestParam("target") String targetPath) {
        log.info("开始复制文件: sourcePath={}, targetPath={}", sourcePath, targetPath);

        try {
            boolean copied = FileUtils.copyFile(sourcePath, targetPath);

            if (copied) {
                return ApiResponse.success(true, "文件复制成功");
            } else {
                return ApiResponse.error("文件复制失败");
            }

        } catch (BusinessException e) {
            log.error("文件复制失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("文件复制异常", e);
            return ApiResponse.error("文件复制失败");
        }
    }

    /**
     * 移动文件
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @return 移动结果
     */
    @PostMapping("/move")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<Boolean> moveFile(@RequestParam("source") String sourcePath,
                                         @RequestParam("target") String targetPath) {
        log.info("开始移动文件: sourcePath={}, targetPath={}", sourcePath, targetPath);

        try {
            boolean moved = FileUtils.moveFile(sourcePath, targetPath);

            if (moved) {
                return ApiResponse.success(true, "文件移动成功");
            } else {
                return ApiResponse.error("文件移动失败");
            }

        } catch (BusinessException e) {
            log.error("文件移动失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("文件移动异常", e);
            return ApiResponse.error("文件移动失败");
        }
    }

    /**
     * 获取文件列表
     *
     * @param directory 目录路径
     * @return 文件列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<List<Map<String, Object>>> listFiles(@RequestParam("directory") String directory) {
        log.info("获取文件列表: directory={}", directory);

        try {
            List<Map<String, Object>> fileList = fileService.listFiles(directory);
            return ApiResponse.success(fileList, "获取文件列表成功");

        } catch (BusinessException e) {
            log.error("获取文件列表失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取文件列表异常", e);
            return ApiResponse.error("获取文件列表失败");
        }
    }

    /**
     * 创建目录
     *
     * @param directoryPath 目录路径
     * @return 创建结果
     */
    @PostMapping("/mkdir")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<Boolean> createDirectory(@RequestParam("path") String directoryPath) {
        log.info("创建目录: directoryPath={}", directoryPath);

        try {
            boolean created = FileUtils.createDirectory(directoryPath);

            if (created) {
                return ApiResponse.success(true, "目录创建成功");
            } else {
                return ApiResponse.success(false, "目录已存在");
            }

        } catch (BusinessException e) {
            log.error("创建目录失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建目录异常", e);
            return ApiResponse.error("创建目录失败");
        }
    }

    /**
     * 获取文件预览信息
     *
     * @param filePath 文件路径
     * @return 预览信息
     */
    @GetMapping("/preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public ApiResponse<Map<String, Object>> getFilePreview(@RequestParam("path") String filePath) {
        log.info("获取文件预览: filePath={}", filePath);

        try {
            Map<String, Object> fileInfo = fileService.getFileInfo(filePath);

            // 添加预览相关信息
            String fileName = (String) fileInfo.get("fileName");
            boolean isImage = FileUtils.isImageFile(fileName);
            boolean isDocument = FileUtils.isDocumentFile(fileName);

            Map<String, Object> previewInfo = new HashMap<>();
            previewInfo.putAll(fileInfo);
            previewInfo.put("canPreview", isImage || isDocument);
            previewInfo.put("previewType", isImage ? "image" : (isDocument ? "document" : "none"));

            return ApiResponse.success(previewInfo, "获取文件预览信息成功");

        } catch (BusinessException e) {
            log.error("获取文件预览失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取文件预览异常", e);
            return ApiResponse.error("获取文件预览失败");
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 是否存在
     */
    @GetMapping("/exists")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public ApiResponse<Boolean> fileExists(@RequestParam("path") String filePath) {
        log.debug("检查文件是否存在: filePath={}", filePath);

        try {
            boolean exists = FileUtils.exists(filePath);
            return ApiResponse.success(exists, exists ? "文件存在" : "文件不存在");

        } catch (Exception e) {
            log.error("检查文件存在性异常", e);
            return ApiResponse.error("检查文件失败");
        }
    }

    /**
     * 获取磁盘使用情况
     *
     * @return 磁盘使用信息
     */
    @GetMapping("/disk-usage")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<Map<String, Object>> getDiskUsage() {
        log.info("获取磁盘使用情况");

        try {
            Map<String, Object> diskInfo = new HashMap<>();

            // 获取当前目录的磁盘使用情况
            java.io.File currentDir = new java.io.File(".");
            long totalSpace = currentDir.getTotalSpace();
            long freeSpace = currentDir.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;

            diskInfo.put("totalSpace", totalSpace);
            diskInfo.put("freeSpace", freeSpace);
            diskInfo.put("usedSpace", usedSpace);
            diskInfo.put("totalSpaceDescription", FileUtils.getFileSizeDescription(totalSpace));
            diskInfo.put("freeSpaceDescription", FileUtils.getFileSizeDescription(freeSpace));
            diskInfo.put("usedSpaceDescription", FileUtils.getFileSizeDescription(usedSpace));
            diskInfo.put("usagePercentage", String.format("%.2f%%", (double) usedSpace / totalSpace * 100));

            return ApiResponse.success(diskInfo, "获取磁盘使用情况成功");

        } catch (Exception e) {
            log.error("获取磁盘使用情况异常", e);
            return ApiResponse.error("获取磁盘使用情况失败");
        }
    }

    /**
     * 写入错误响应
     *
     * @param response HTTP响应
     * @param status 状态码
     * @param message 错误消息
     */
    private void writeErrorResponse(HttpServletResponse response, int status, String message) {
        try {
            response.setStatus(status);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
        } catch (IOException e) {
            log.error("写入错误响应失败", e);
        }
    }
}