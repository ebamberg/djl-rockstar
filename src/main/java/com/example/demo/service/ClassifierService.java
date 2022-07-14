package com.example.demo.service;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.translate.Translator;

@Service
public class ClassifierService {

	@Value ("${datasetRoot}")
	private Path datasetRoot;
	@Value ("${modelDir}")
	private Path modelDir;
	@Value ("${modelName}")
    public String modelName;
	@Value ("${imageWidth}")
	private int imageWidth;
	@Value ("${imageHeight}")
    private int imageHeigh;

    @Autowired 
    private TrainModelService trainModelService;
	
  //  @PostConstruct
	public Classifications findClassForImage(Image img) throws Exception {
        // the path of image to classify
    //    String    imageFilePath = "Sandals/Heel/Annie/7350693.3.jpg";

        // Load the image file from the path
    //    Image img = ImageFactory.getInstance().fromFile(datasetRoot.resolve(imageFilePath));

        try (Model model = trainModelService.buildModel()) { // empty model instance
            // load the model
            model.load(modelDir, modelName);

            // define a translator for pre and post processing
            // out of the box this translator converts images to ResNet friendly ResNet 18 shape
            Translator<Image, Classifications> translator =
                    ImageClassificationTranslator.builder()
                            .addTransform(new Resize(imageHeigh, imageWidth))
                            .addTransform(new ToTensor())
                            .optApplySoftmax(true)
                            .build();

            // run the inference using a Predictor
            try (Predictor<Image, Classifications> predictor = model.newPredictor(translator)) {
                // holds the probability score per label
                Classifications predictResult = predictor.predict(img);
                System.out.println(predictResult);
                return predictResult;
            }
        }
       

	}
	

	
}
