package com.example.demo;

import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

import ai.djl.Application;
import ai.djl.repository.Artifact;
import ai.djl.repository.zoo.ModelZoo;

@Component
public class MainWindow implements ApplicationRunner{

	@Autowired
	public ImageProcessor imageProcessor;
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		
		Map<Application, List<Artifact>> models = ModelZoo.listModels();
        models.forEach(
                (app, list) -> {
                    String appName = app.toString();
                    list.forEach(artifact -> System.out.println(appName+" "+artifact));
                });
		
		
		Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.VGA.getSize());
		webcam.setImageTransformer(imageProcessor);

		WebcamPanel panel = new WebcamPanel(webcam);
		panel.setImageSizeDisplayed(true);

		JFrame window = new JFrame("Webcam");
		window.add(panel);
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
		
	}

	
	
}
