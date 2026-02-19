package com.groupy.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map uploadImage(MultipartFile file) {
        try {
            return cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "folder", "posts",
                    "resource_type", "image"
                )
            );
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed", e);
        }
    }

    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.emptyMap()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image", e);
        }
    }
    
    public Map uploadFile(MultipartFile file) throws IOException {

        Map<String, Object> options = new HashMap<>();

        // Auto detect resource type (important for video/audio)
        options.put("resource_type", "auto");

        return cloudinary.uploader().upload(file.getBytes(), options);
    }
}