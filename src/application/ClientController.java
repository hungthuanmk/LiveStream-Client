package application;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ClientController {
	@FXML
	private ImageView imgViewer;

	static boolean streaming = false;
	
	public static Image mat2Image(Mat img) {
		MatOfByte byteMat = new MatOfByte();
		Imgcodecs.imencode(".bmp", img, byteMat);
		return new Image(new ByteArrayInputStream(byteMat.toArray()));
	}
	
	public static BufferedImage Mat2bufferedImage(Mat image) throws IOException {
        MatOfByte bytemat = new MatOfByte();
        Imgcodecs.imencode(".jpg", image, bytemat);
        byte[] bytes = bytemat.toArray();
        InputStream in = new ByteArrayInputStream(bytes);
        BufferedImage img = null;
        img = ImageIO.read(in);
        return img;
    }
	
	OutputStream os = null;
	
	@FXML
	public void onStart(ActionEvent event) {
		
		Socket socket = null;
		
	    BufferedReader is = null;
			
		 try {
			 socket = new Socket("10.12.0.50", 1998);
			 
	         os = socket.getOutputStream();
//	         is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	 
	       } catch (UnknownHostException e) {
	           System.err.println("Don't know about host " + "localhost");
	           return;
	       } catch (IOException e) {
	           System.err.println("Couldn't get I/O for the connection to " + "localhost");
	           return;
	       }
		

		init();
		VideoCapture cap = new VideoCapture(0);
		streaming = true;
		
		Mat frame = new Mat();	
			
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (streaming) {
					cap.read(frame);	
					
					try {
						BufferedImage img = Mat2bufferedImage(frame);
			            ByteArrayOutputStream baos = new ByteArrayOutputStream();
			            ImageIO.write(img, "jpg", baos);
			            byte[] imageBytes = baos.toByteArray();
						os.write(imageBytes);
						os.flush();
						System.out.println(imageBytes.length);
					} catch (Exception e1) { 
						e1.printStackTrace();
					}
					
					Platform.runLater(() -> {
						imgViewer.setImage(mat2Image(frame));
					});
					try {
						TimeUnit.MILLISECONDS.sleep(16);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//System.out.println(frame.dump());
				}
			}	
		});
			
		thread.setDaemon(true);
		thread.start();
		
		
	}
	// Event Listener on Button.onAction
	@FXML
	public void onStop(ActionEvent event) {
		Platform.runLater(() -> {
			streaming = false;
		});
		
	}
	
	public boolean init() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		return true;
	}
}
