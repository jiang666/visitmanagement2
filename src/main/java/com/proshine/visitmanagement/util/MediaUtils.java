package com.proshine.visitmanagement.util;

import com.proshine.visitmanagement.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 多媒体文件处理工具类
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
public class MediaUtils {
    
    // 支持的图片格式
    private static final Set<String> SUPPORTED_IMAGE_FORMATS = new HashSet<>(Arrays.asList(
        "jpg", "jpeg", "png", "gif", "bmp", "webp"
    ));
    
    // 支持的视频格式
    private static final Set<String> SUPPORTED_VIDEO_FORMATS = new HashSet<>(Arrays.asList(
        "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm"
    ));
    
    // 支持的音频格式
    private static final Set<String> SUPPORTED_AUDIO_FORMATS = new HashSet<>(Arrays.asList(
        "mp3", "wav", "flac", "aac", "ogg", "wma"
    ));
    
    /**
     * 压缩图片
     * 
     * @param file 原始图片文件
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @param quality 压缩质量（0.0-1.0）
     * @return 压缩后的图片字节数组
     */
    public static byte[] compressImage(MultipartFile file, int maxWidth, int maxHeight, float quality) {
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new BusinessException("无法读取图片文件");
            }
            
            // 计算新的尺寸
            Dimension newSize = calculateNewSize(originalImage.getWidth(), originalImage.getHeight(), maxWidth, maxHeight);
            
            // 创建压缩后的图片
            BufferedImage compressedImage = new BufferedImage(
                newSize.width, newSize.height, BufferedImage.TYPE_INT_RGB);
            
            Graphics2D g2d = compressedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.drawImage(originalImage, 0, 0, newSize.width, newSize.height, null);
            g2d.dispose();
            
            // 输出压缩后的图片
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            String formatName = getImageFormat(file.getOriginalFilename());
            ImageIO.write(compressedImage, formatName, outputStream);
            
            byte[] compressedBytes = outputStream.toByteArray();
            
            log.info("图片压缩完成: 原始大小={}KB, 压缩后大小={}KB, 压缩比={:.2f}%",
                file.getSize() / 1024, compressedBytes.length / 1024,
                (1.0 - (double) compressedBytes.length / file.getSize()) * 100);
            
            return compressedBytes;
            
        } catch (IOException e) {
            log.error("图片压缩失败", e);
            throw new BusinessException("图片压缩失败");
        }
    }
    
    /**
     * 生成缩略图
     * 
     * @param file 原始图片文件
     * @param thumbnailWidth 缩略图宽度
     * @param thumbnailHeight 缩略图高度
     * @return 缩略图字节数组
     */
    public static byte[] generateThumbnail(MultipartFile file, int thumbnailWidth, int thumbnailHeight) {
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new BusinessException("无法读取图片文件");
            }
            
            // 创建缩略图
            BufferedImage thumbnail = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = thumbnail.createGraphics();
            
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.drawImage(originalImage, 0, 0, thumbnailWidth, thumbnailHeight, null);
            g2d.dispose();
            
            // 输出缩略图
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            String formatName = getImageFormat(file.getOriginalFilename());
            ImageIO.write(thumbnail, formatName, outputStream);
            
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("生成缩略图失败", e);
            throw new BusinessException("生成缩略图失败");
        }
    }
    
    /**
     * 获取图片信息
     * 
     * @param file 图片文件
     * @return 图片信息
     */
    public static ImageInfo getImageInfo(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new BusinessException("无法读取图片文件");
            }
            
            ImageInfo info = new ImageInfo();
            info.setWidth(image.getWidth());
            info.setHeight(image.getHeight());
            info.setFormat(getImageFormat(file.getOriginalFilename()));
            info.setFileSize(file.getSize());
            info.setAspectRatio((double) image.getWidth() / image.getHeight());
            
            return info;
            
        } catch (IOException e) {
            log.error("获取图片信息失败", e);
            throw new BusinessException("获取图片信息失败");
        }
    }
    
    /**
     * 验证图片文件
     * 
     * @param file 文件
     * @return 是否为有效的图片文件
     */
    public static boolean validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }
        
        String extension = FileUtils.getFileExtension(fileName).toLowerCase().substring(1);
        if (!SUPPORTED_IMAGE_FORMATS.contains(extension)) {
            return false;
        }
        
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            return image != null;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 验证视频文件
     * 
     * @param file 文件
     * @return 是否为有效的视频文件
     */
    public static boolean validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }
        
        String extension = FileUtils.getFileExtension(fileName).toLowerCase().substring(1);
        return SUPPORTED_VIDEO_FORMATS.contains(extension);
    }
    
    /**
     * 验证音频文件
     * 
     * @param file 文件
     * @return 是否为有效的音频文件
     */
    public static boolean validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }
        
        String extension = FileUtils.getFileExtension(fileName).toLowerCase().substring(1);
        return SUPPORTED_AUDIO_FORMATS.contains(extension);
    }
    
    /**
     * 计算新的图片尺寸（保持宽高比）
     * 
     * @param originalWidth 原始宽度
     * @param originalHeight 原始高度
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 新的尺寸
     */
    private static Dimension calculateNewSize(int originalWidth, int originalHeight, int maxWidth, int maxHeight) {
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);
        
        return new Dimension(newWidth, newHeight);
    }
    
    /**
     * 获取图片格式
     * 
     * @param fileName 文件名
     * @return 图片格式
     */
    private static String getImageFormat(String fileName) {
        String extension = FileUtils.getFileExtension(fileName).toLowerCase();
        if (".jpg".equals(extension) || ".jpeg".equals(extension)) {
            return "jpg";
        } else if (".png".equals(extension)) {
            return "png";
        } else if (".gif".equals(extension)) {
            return "gif";
        } else if (".bmp".equals(extension)) {
            return "bmp";
        } else {
            return "jpg"; // 默认格式
        }
    }
    
    /**
     * 图片信息类
     */
    public static class ImageInfo {
        private int width;
        private int height;
        private String format;
        private long fileSize;
        private double aspectRatio;
        
        // Getters and Setters
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        
        public double getAspectRatio() { return aspectRatio; }
        public void setAspectRatio(double aspectRatio) { this.aspectRatio = aspectRatio; }
        
        @Override
        public String toString() {
            return String.format("ImageInfo{width=%d, height=%d, format='%s', fileSize=%d, aspectRatio=%.2f}",
                    width, height, format, fileSize, aspectRatio);
        }
    }
}