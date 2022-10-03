package client;

import java.io.*;
import java.net.*;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import server.Main.listen;
import server.Main.player;

public class Main extends Application implements Initializable {
	private static final int windowWidth=500,windowHeight=500;
	public static Robot robot;
	public static Stage stage;
	public static int ang;
	@FXML
	public Pane field;
	@FXML
	public ImageView simg;
	public static WritableImage screen=new WritableImage(500,500);
	enum Clicks {NONE,LEFT,RIGHT;}
	enum Acts {WALK,SHEATH,RELOAD;}
	private static Acts act=Acts.WALK;
	private static Clicks click=Clicks.NONE;
	private static String serverName="127.0.0.1";
	private static int serverPort=12000;
	private static SocketAddress socketAddress=new InetSocketAddress(serverName, serverPort);
	private static Socket socket=null;
	private static byte[] inbuf=new byte[windowWidth*windowHeight*4];
	private static byte[] outbuf;
	private static int connectState=2;
	public class connect implements Runnable{

		@Override
		public void run() {
			try {
				socket=new Socket();
				socket.connect(socketAddress);
				InputStream input=socket.getInputStream();
				OutputStream output=socket.getOutputStream();
				while(true) {
					connectState=input.read();
					if(connectState>0) {
						for(int j=0;j<windowWidth*windowHeight*4;j+=50000){
							input.read(inbuf,j,50000);
						}
						screen.getPixelWriter().setPixels(0, 0, windowWidth, windowHeight, PixelFormat.getByteBgraInstance(), inbuf, 0, windowWidth*4);
						simg.setImage(screen);
						if(connectState>1) {
							output.write(act.ordinal());
							output.write(click.ordinal());
							click=Clicks.NONE;
							outbuf=String.valueOf(ang).getBytes();
							output.write(outbuf);
						}
						Thread.sleep(10);
					}else {
						break;
					}
				}
			}catch(IOException e) {
//				e.printStackTrace();
			}catch (InterruptedException e) {
//				e.printStackTrace();
			}finally {
				try {
					socket.close();
				}catch(IOException e) {}
			}
		}
	}
	@Override
	public void initialize(URL arg0,ResourceBundle arg1) {
		Thread connectT=new Thread(new connect());
		connectT.start();
	}
	public void onMove(MouseEvent e) throws IOException {
		if(new Point2D(e.getSceneX(),e.getSceneY()).distance(windowWidth*0.5,windowHeight*0.5)>50){
			act=Acts.WALK;
			ang=(int)new Point2D(e.getSceneX()-windowWidth*0.5,e.getSceneY()-windowHeight*0.5).angle(1,0);
			if(e.getSceneY()<windowHeight*0.5) {
				ang*=-1;
			}
		}else{
			if(((e.getSceneY()-windowHeight*0.5)/Math.tan(Math.toRadians(ang))-(e.getSceneX()-windowWidth*0.5))*ang>0) {
				act=Acts.RELOAD;
			}else {
				act=Acts.SHEATH;
			}
		}
	}
	public void onClick(MouseEvent e) throws IOException {
//		System.out.println(socket.isClosed());
		if(socket.isClosed()) {
			Thread connectT=new Thread(new connect());
			connectT.start();
		}else if(new Point2D(e.getSceneX(),e.getSceneY()).distance(windowWidth*0.5,windowHeight*0.5)>50) {
			if(e.getButton()==MouseButton.PRIMARY) {
				click=Clicks.LEFT;
			}else if(e.getButton()==MouseButton.SECONDARY){
				click=Clicks.RIGHT;
			}
		}
	}
	public static void main(String[] args){
		launch(args);
	}
	public void start(Stage mainStage) throws IOException{
		
		robot=new Robot();
	    mainStage.setTitle("¥ª©~¦X¥kª®À»");
	    mainStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("0.fxml"))));
	    mainStage.setOnCloseRequest( ev -> {
	        try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    });
		stage=mainStage;
	    mainStage.show();

	}
}
