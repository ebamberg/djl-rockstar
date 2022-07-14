package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.ClassifierService;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.ImageFactory;

@RestController
public class PredictionController {
	
	
	@Autowired
	private ClassifierService service;
	
	
	/**
	 * curl -X POST http://127.0.0.1:8080/upload -F "image=@./Sandals/Heel/Annie/7350693.3.jpg" 
 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	@PostMapping (value="upload")
	public String upload(@RequestParam("image") MultipartFile file) throws Exception {
		System.out.println("Hello Detection");
		
		var image=ImageFactory.getInstance().fromInputStream(file.getInputStream());
		return service.findClassForImage(image).toJson();
	}

}
