package com.example.demo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import com.github.sarxos.webcam.WebcamImageTransformer;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ImageProcessor implements  WebcamImageTransformer {

	ZooModel<Image, DetectedObjects> model ;
	Predictor<Image, DetectedObjects> predictor ;
	
	@PostConstruct
	public void loadModel() throws ModelNotFoundException, MalformedModelException, IOException {
		Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .optApplication(Application.CV.OBJECT_DETECTION)
                        .setTypes(Image.class, DetectedObjects.class)
                        .optFilter("backbone", "resnet50")
                        .optEngine(Engine.getDefaultEngineName())
                        .optProgress(new ProgressBar())
                        .build();
		 model = criteria.loadModel();
	     predictor = model.newPredictor();
	}
	
	@PreDestroy
	public void freeResources() {
		if (model!=null) {
			model.close();
		}
	}
	
	@Override
	public BufferedImage transform(BufferedImage image) {
		Image img = ImageFactory.getInstance().fromImage(image);
		try {
			DetectedObjects objects=predictor.predict(img);
			long numberOfPersons=objects.items().stream()
									.filter(o-> "person".equals(o.getClassName()))
									.count();
			writeCountToImage(img,numberOfPersons);
			img.drawBoundingBoxes(objects);
			
		} catch (TranslateException e) {
			e.printStackTrace();
		}
		
		return (BufferedImage) img.getWrappedImage();
	}
	
	private void writeCountToImage(Image img, long count) {
		Font font = new Font("Arial", Font.BOLD, 48);

		Graphics g = ((BufferedImage)img.getWrappedImage()).getGraphics();
		g.setFont(font);
		g.setColor(Color.GREEN);
		g.drawString(String.valueOf(count), 30, 50);
	}
	
}
