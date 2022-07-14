package com.example.demo.service;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.basicmodelzoo.cv.classification.ResNetV1;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.TrainingConfig;
import ai.djl.training.TrainingResult;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.SaveModelTrainingListener;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.translate.TranslateException;

@Service
public class TrainModelService {

	@Value ("${datasetRoot}")
	private Path datasetRoot;
	@Value ("${modelDir}")
	private Path modelDir;
	
    // the height and width for pre-processing of the image
	private static final int IMAGE_HEIGHT = 100;
    private static final int IMAGE_WIDTH = 100;
    private static final int BATCH_SIZE=20;
    public static final String MODEL_NAME = "shoeclassifier";

	private static final long NUM_OF_OUTPUT = 4; // number of classes, like boots, sandals ects

	private static final int EPOCHS = 10;
	
//	@PostConstruct
	public void startTraining() throws Exception {
		
		// ------------------ Step 1: we need a dataset ------------------
		
		// first we need a dataset, we have many datasets prepared ready to use like CSV Dataset
		// in our case we just can use the imageFolder DataSet
		
		var dataset=ImageFolder.builder()
                // retrieve the data
                .setRepositoryPath(datasetRoot)
                .optMaxDepth(10)
                .addTransform(new Resize(IMAGE_WIDTH, IMAGE_HEIGHT))
                .addTransform(new ToTensor())
                // random sampling; don't process the data in order
                .setSampling(BATCH_SIZE, true)
             //   .optLimit(100)
                .build();
		dataset.prepare();
		
        // Split the dataset set into training dataset and validate dataset
        RandomAccessDataset[] datasets = dataset.randomSplit(8, 2);
        
        
     // ------------------ Step 2: we need a model ------------------
        
        // lets explain why we need a try block around here. Memory management. Imaging you have a GPU and exception occurs.....
		try (var model=buildModel()) {
			
			// ------------------ Step 3: we need a Trainer ------------------
			
			
			try (var trainer = model.newTrainer(buildTrainingConfig())) {
	            Shape inputShape = new Shape(1, 3, IMAGE_HEIGHT, IMAGE_HEIGHT);
	            // trainer has to know how our input looks like , initialize trainer with proper input shape
	            trainer.initialize(inputShape);
	            // find the patterns in data
	            
	         // ------------------ Step 4: run the training process ------------------
	            
	            EasyTrain.fit(trainer, EPOCHS, datasets[0], datasets[1]);
	            
	            
	         
				// ------------------ Step 5: save our trained model ------------------ 
	            // save the model after done training for inference later
	            // model saved as shoeclassifier-0000.params
	            model.save(modelDir, MODEL_NAME);
	            // save labels into model directory
	            saveSynset(modelDir, dataset.getSynset());
	            
			}
		}

	}
	

	public Model buildModel() {
        // create new instance of an empty model
        Model model = Model.newInstance(MODEL_NAME);

        // Block is a composable unit that forms a neural network; combine them like Lego blocks
        // to form a complex network
        Block resNet50 =
                ResNetV1.builder() // construct the network
                        .setImageShape(new Shape(3, IMAGE_HEIGHT, IMAGE_WIDTH))
                        .setNumLayers(50)
                        .setOutSize(NUM_OF_OUTPUT)
                        .build();

        // set the neural network to the model
        model.setBlock(resNet50);
        return model;
	}
	
	   private TrainingConfig buildTrainingConfig() {
		   SaveModelTrainingListener listener = new SaveModelTrainingListener("./tmpModel/",null,2);
	        listener.setSaveModelCallback(
	                trainer -> {
	                    TrainingResult result = trainer.getTrainingResult();
	                    Model model = trainer.getModel();
	                    float accuracy = result.getValidateEvaluation("Accuracy");
	                    model.setProperty("Accuracy", String.format("%.5f", accuracy));
	                    model.setProperty("Loss", String.format("%.5f", result.getValidateLoss()));
	                });
	        return new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
	                .addEvaluator(new Accuracy())
	                .addTrainingListeners(TrainingListener.Defaults.logging())
	                .addTrainingListeners(listener);
	    }

	    public void saveSynset(Path modelDir, List<String> synset) throws IOException {
	        Path synsetFile = modelDir.resolve("synset.txt");
	        try (Writer writer = Files.newBufferedWriter(synsetFile)) {
	            writer.write(String.join("\n", synset));
	        }
	    }
	   
	
}
