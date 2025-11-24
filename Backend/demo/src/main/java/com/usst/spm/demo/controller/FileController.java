package com.usst.spm.demo.controller;

import com.usst.spm.demo.model.File;
import com.usst.spm.demo.repository.FileRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    private static final String UPLOAD_DIR = "uploads/";
    private final FileRepository fileRepository;

    public FileController(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        // 确保上传目录存在
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            System.err.println("无法创建上传目录: " + e.getMessage());
        }
    }

    /**
     * 上传文件
     * POST /api/files
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId) {
        
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件不能为空");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件名不能为空");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!isAllowedFileType(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不支持的文件类型，仅支持 PDF、DOC、DOCX、TXT 等格式");
        }

        try {
            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + "." + extension;
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            
            // 保存文件
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 保存文件记录到数据库
            File fileEntity = new File();
            fileEntity.setFileName(fileName);
            fileEntity.setOriginalName(originalFilename);
            fileEntity.setStoragePath(filePath.toString());
            fileEntity.setMimeType(file.getContentType());
            fileEntity.setFileSize(file.getSize());
            fileEntity.setUploaderId(uploaderId);
            fileEntity = fileRepository.save(fileEntity);

            Map<String, Object> response = new HashMap<>();
            response.put("id", fileEntity.getId());
            response.put("fileName", fileEntity.getOriginalName());
            response.put("fileSize", fileEntity.getFileSize());
            response.put("mimeType", fileEntity.getMimeType());
            response.put("url", "/api/files/" + fileEntity.getId());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     * GET /api/files/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        File fileEntity = fileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "文件不存在"));

        if (fileEntity.getDeleted() != null && fileEntity.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文件不存在");
        }

        try {
            Path filePath = Paths.get(fileEntity.getStoragePath());
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文件不存在");
            }

            String contentType = fileEntity.getMimeType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + fileEntity.getOriginalName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件类型是否允许
     */
    private boolean isAllowedFileType(String extension) {
        String[] allowedTypes = {"pdf", "doc", "docx", "txt", "xls", "xlsx", "ppt", "pptx", "zip", "rar"};
        for (String type : allowedTypes) {
            if (type.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}

