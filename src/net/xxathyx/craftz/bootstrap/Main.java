package net.xxathyx.craftz.bootstrap;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import net.xxathyx.craftz.bootstrap.logging.Logger;
import net.xxathyx.craftz.bootstrap.updater.Updater;

public class Main extends Application {
	
	private static final Logger logger = new Logger(new File(Updater.getLogsFolder(), (Updater.getLogsFolder().listFiles().length)+".log"));
    
	private final String os = System.getProperty("os.name").toLowerCase();
	
    public double width = 748;
    public double height = 356;
	
    public ProgressBar progressBar;
    
	private double xOffset = 0; 
	private double yOffset = 0;
		
	@Override
	public void start(Stage stage) {
		try {
			
			File pidFile = new Updater().getPidFile();
			
			if(pidFile.exists()) {
				Scanner in = new Scanner(new FileReader(pidFile));
				
				StringBuilder stringBuilder = new StringBuilder();
				String out;
				
				while(in.hasNext()) stringBuilder.append(in.next());
				in.close();
				out = stringBuilder.toString();
						        
	            ProcessBuilder processBuilder;
	            
	            if(os.contains("win")) {
	                processBuilder = new ProcessBuilder("taskkill", "/PID", out, "/F");
	            }else processBuilder = new ProcessBuilder("kill", "-9", out);

		        processBuilder.start();
			}
			
			if(!pidFile.exists()) pidFile.createNewFile();
			
		    FileWriter writer = new FileWriter(pidFile);
		    writer.write(String.valueOf(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]));
		    writer.close();
			
			StackPane root = new StackPane();
			
			Scene scene = new Scene(root, width, height);
			scene.getStylesheets().add(getClass().getResource("resources/style.css").toExternalForm());
	        scene.setRoot(root);
	        
	        root.getStyleClass().add("parent-container");
	        
	        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
	        stage.setX((screenBounds.getWidth()-width)/2); 
	        stage.setY((screenBounds.getHeight()-height)/2); 
	        
	        stage.setAlwaysOnTop(true);
	        stage.setAlwaysOnTop(false);
	        stage.toFront();

			stage.getIcons().add(new Image(this.getClass().getResourceAsStream("resources/icon.png")));			
			
			Image background = new Image(this.getClass().getResourceAsStream("resources/background.png"));
			
			progressBar = new ProgressBar(0);
			progressBar.getStyleClass().add("progress-bar");
			//progressBar.setPrefSize(390, 10);
			progressBar.setTranslateX(145);
			progressBar.setTranslateY(85);
			
			ImageView selectedImage = new ImageView();   
			selectedImage.setImage(background);
			
			root.getChildren().addAll(selectedImage, progressBar);

	        stage.initStyle(StageStyle.TRANSPARENT);
	        scene.setFill(Color.TRANSPARENT);
	        
	        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), root);
	        fadeTransition.setFromValue(0);
	        fadeTransition.setToValue(1);
	        fadeTransition.play();
	        			
	        root.setOnMousePressed(new EventHandler<MouseEvent>() {
	            @Override
	            public void handle(MouseEvent event) {
	                xOffset = event.getSceneX();
	                yOffset = event.getSceneY();
	            }
	        });
	        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
	            @Override
	            public void handle(MouseEvent event) {
	                stage.setX(event.getScreenX() - xOffset);
	                stage.setY(event.getScreenY() - yOffset);
	            }
	        });
			
			stage.setScene(scene);
			stage.show();
			
			Main main = this;
			Timer timer = new Timer();
		    timer.schedule(new TimerTask() {
		    	
				@Override
				public void run() {
					Platform.runLater(new Runnable() {
		                @Override
		                public void run() {
							
							Updater updater;
							try {
								updater = new Updater(main);
								if(!updater.isUpdated()) {
									updater.download();
								}else {
									progressBar.setProgress(1);
									updater.launch();
								}
							}catch (IOException | InterruptedException e) {
								e.printStackTrace();
								logger.log(e.toString());
							}
					        timer.cancel();
		                }
		            });
				}		    	
		    }, 500);
		}catch(Exception e) {
			e.printStackTrace();
			logger.log(e.toString());
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}