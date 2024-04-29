package com.sagar.gcp.gcpcloudstorage.service;

import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.sagar.gcp.gcpcloudstorage.config.SecretConfig;
import com.sagar.gcp.gcpcloudstorage.model.FileMetaData;
import com.sagar.gcp.gcpcloudstorage.repositories.FileMetaDataRepositories;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@Service
public class StorageService {


    private String bucketName;

    @Autowired
    private Storage storage;

    @Autowired
    FileMetaDataRepositories fileMetaDataRepositories;



    @PostConstruct
    public void init(){
        bucketName = SecretConfig.getInstance().get("gcs-resource-test-bucket");
    }
    public FileMetaData upload(MultipartFile file) throws IOException {
        final String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new BadRequestException("Original file name is null");
        }
        final String contentType = Files.probeContentType(new File(fileName).toPath());
        Bucket bucket = storage.get(bucketName);
        byte[] fileData = FileCopyUtils.copyToByteArray(convertFile(file));
        Blob blob = bucket.create(fileName, fileData, contentType);
        if (blob != null) {
            FileMetaData fileMetaData = new FileMetaData();
            fileMetaData.setFileName(blob.getName());
            fileMetaData.setUrl(blob.getMediaLink());
            fileMetaData.setBucket(blob.getBucket());
            return fileMetaDataRepositories.save(fileMetaData);
        }
        return null;
    }

    private File convertFile(MultipartFile file) {

        try {
            if (file.getOriginalFilename() == null) {
                throw new BadRequestException("Original file name is null");
            }
            File convertedFile = new File(file.getOriginalFilename());
            FileOutputStream outputStream = new FileOutputStream(convertedFile);
            outputStream.write(file.getBytes());
            outputStream.close();
            return convertedFile;
        } catch (Exception e) {
            throw new RuntimeException("An error has occurred while converting the file");
        }
    }

    public Blob download(Long fileId) {
        Optional<FileMetaData> fileMetaData = fileMetaDataRepositories.findById(fileId);
        if (fileMetaData.isEmpty()) throw new RuntimeException("FileNotFound");

        Bucket bucket = storage.get(bucketName);
        return bucket.get(fileMetaData.get().getFileName());
    }
}
