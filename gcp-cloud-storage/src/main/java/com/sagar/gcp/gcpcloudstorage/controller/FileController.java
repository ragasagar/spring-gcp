package com.sagar.gcp.gcpcloudstorage.controller;

import com.google.cloud.storage.Blob;
import com.sagar.gcp.gcpcloudstorage.model.FileMetaData;
import com.sagar.gcp.gcpcloudstorage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileController {


    @Autowired
    StorageService storageService;


    @PostMapping
    public FileMetaData upload(@RequestParam("file") MultipartFile file) throws IOException {
        return storageService.upload(file);

    }

    @GetMapping("/{file_id}")
    @ResponseBody
    public ResponseEntity<?> serveFile(@PathVariable Long file_id) {
        Blob blob = storageService.download(file_id);
        ByteArrayResource resource = null;
        try {
            resource = new ByteArrayResource(blob.getContent());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(blob.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + blob.getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
