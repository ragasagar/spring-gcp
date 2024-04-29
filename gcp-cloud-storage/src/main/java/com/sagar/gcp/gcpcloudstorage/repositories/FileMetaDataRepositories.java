package com.sagar.gcp.gcpcloudstorage.repositories;

import com.sagar.gcp.gcpcloudstorage.model.FileMetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetaDataRepositories extends JpaRepository<FileMetaData,Long> {
}
