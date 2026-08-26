package com.keshav.service;

import org.springframework.web.multipart.MultipartFile;

public interface IImageService {

    String uploadProductImage(MultipartFile file);
}