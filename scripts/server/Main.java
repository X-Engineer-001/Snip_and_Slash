package server;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Main extends Application implements Initializable{
	private static final int RPS=20,FPS=20,fieldWidth=1000,fieldHeight=1000,windowWidth=500,windowHeight=500,keenDelay=3*RPS,loadDelay=1*RPS;
	public static Robot robot;
	public static Stage stage;
//	public static double screenWidth,screenHeight,mouseSceneX,mouseSceneY,mouseScreenX,mouseScreenY,sceneX,sceneY;
	public static SnapshotParameters s=new SnapshotParameters();
//	public static WritableImage serverScreen=new WritableImage(fieldWidth+windowWidth,fieldHeight+windowHeight);
	public static LinkedList<player> players=new LinkedList<player>();
	public static Queue<player> newPlayers=new LinkedList<player>();
	@FXML
	public Pane scene;
	@FXML
	public Pane field;
	@FXML
	public Circle playerT;
	@FXML
	public Ellipse bladeT;
	@FXML
	public Arc slashCritical;
	@FXML
	public Arc slashAim;
	@FXML
	public Arc slashBig;
	@FXML
	public Arc slashWindup;
	@FXML
	public Arc slashNormal;
	@FXML
	public Arc stabT;
	@FXML
	public Rectangle gunT;
	@FXML
	public Rectangle fireAim;
	@FXML
	public Rectangle fireSnip;
	public static Random random=new Random(System.currentTimeMillis());
	enum States {NORMAL,WINDUP,AIMING,LOCK,DEAD;}
	enum Acts {WALK,SHEATH,RELOAD;}
	public class Blade extends Ellipse{
		public player owner;
		public int side=1;
		public Blade(player o) {
			this.owner=o;
			this.setLayoutX(owner.getLayoutX());
			this.setLayoutY(owner.getLayoutY());
			this.setCenterX(bladeT.getCenterX());
			this.setCenterY(bladeT.getCenterY());
			this.setRadiusX(bladeT.getRadiusX());
			this.setRadiusY(bladeT.getRadiusY());
			this.setStroke(bladeT.getStroke());
			this.setFill(bladeT.getFill());
			this.getTransforms().add(new Rotate(owner.dir+this.side*135,0,0));
			this.getTransforms().add(new Scale(1,1,this.getCenterX()-this.getRadiusX(),0));
		}
	}
	public class Gun extends Rectangle{
		public player owner;
		public Gun(player o) {
			this.owner=o;
			this.setLayoutX(owner.getLayoutX());
			this.setLayoutY(owner.getLayoutY());
			this.setX(gunT.getX());
			this.setY(gunT.getY());
			this.setWidth(gunT.getWidth());
			this.setHeight(gunT.getHeight());
			this.setStroke(gunT.getStroke());
			this.setFill(gunT.getFill());
			this.getTransforms().add(new Rotate(owner.dir,0,0));
			this.getTransforms().add(new Scale(1,1,this.getX()+this.getWidth(),0));
		}
	}
	public class Slash extends Arc{
		public player owner;
		public Slash(player o) {
			this.setVisible(false);
			this.owner=o;
			this.setLayoutX(owner.getLayoutX());
			this.setLayoutY(owner.getLayoutY());
			this.setStroke(Color.TRANSPARENT);
			this.getTransforms().add(new Rotate(owner.dir,0,0));
		}
	}
	public class Fire extends Rectangle{
		public player owner;
		public Fire(player o) {
			this.setVisible(false);
			this.owner=o;
			this.setLayoutX(owner.getLayoutX());
			this.setLayoutY(owner.getLayoutY());
			this.setX(fireAim.getX());
			this.setY(fireAim.getY());
			this.setWidth(Math.sqrt(fieldWidth*fieldWidth+fieldHeight*fieldHeight));
			this.setHeight(fireAim.getHeight());
			this.setStroke(Color.TRANSPARENT);
			this.getTransforms().add(new Rotate(owner.dir,0,0));
		}
	}
	public class player extends Circle implements Runnable{
		public WritableImage screen=new WritableImage(windowWidth,windowHeight);
		public States state=States.NORMAL;
		public Acts act=Acts.WALK;
		private Socket socket;
		public int dir=0,keen=keenDelay,load=loadDelay;
		public Blade blade;
		public Gun gun;
		public Slash slash;
		public Fire fire;
		public player(Socket s){
			this.socket=s;
			this.setLayoutX(random.nextDouble()*fieldWidth);
			this.setLayoutY(random.nextDouble()*fieldHeight);
			this.setCenterX(playerT.getCenterX());
			this.setCenterY(playerT.getCenterY());
			this.setRadius(playerT.getRadius());
			this.setStroke(playerT.getStroke());
			this.setFill(playerT.getFill());
			this.blade=new Blade(this);
			this.gun=new Gun(this);
			this.slash=new Slash(this);
			this.fire=new Fire(this);
		}
		public void turn() {
			this.setRotate(this.dir);
			this.blade.getTransforms().set(0,new Rotate(this.dir+this.blade.side*135,0,0));
			this.gun.getTransforms().set(0,new Rotate(this.dir,0,0));
		}
		public void move(double distance) {
			this.setLayoutX(this.getLayoutX()+distance*Math.cos(Math.toRadians(this.dir)));
			this.setLayoutY(this.getLayoutY()+distance*Math.sin(Math.toRadians(this.dir)));
			if(this.getLayoutX()<0) {
				this.setLayoutX(0);
			}else if(this.getLayoutX()>fieldWidth) {
				this.setLayoutX(fieldWidth);
			}
			if(this.getLayoutY()<0) {
				this.setLayoutY(0);
			}else if(this.getLayoutY()>fieldHeight) {
				this.setLayoutY(fieldHeight);
			}
			double targetX=this.getLayoutX(),targetY=this.getLayoutY();
			this.blade.setLayoutX(targetX);
			this.blade.setLayoutY(targetY);
			this.gun.setLayoutX(targetX);
			this.gun.setLayoutY(targetY);
		}
		public void performBig() {
			Timeline Anime=new Timeline(new KeyFrame(Duration.millis(0),(e)->{
				this.turn();
				if(this.load>0) {
					this.load=loadDelay;
					this.gun.getTransforms().set(1,new Scale(1,1,this.gun.getX()+this.gun.getWidth(),0));
				}
				this.slash.setLayoutX(this.getLayoutX());
				this.slash.setLayoutY(this.getLayoutY());
				this.slash.getTransforms().set(0,new Rotate(this.dir,0,0));
				this.slash.setRadiusX(slashAim.getRadiusX());
				this.slash.setRadiusY(slashAim.getRadiusY());
				this.slash.setStartAngle(slashAim.getStartAngle());
				this.slash.setLength(slashAim.getLength());
				this.slash.setType(slashAim.getType());
				this.slash.setFill(slashAim.getFill());
				this.slash.setVisible(true);
			}),new KeyFrame(Duration.millis(500),(e)->{
				if(this.state==States.LOCK) {
					this.slash.setRadiusX(slashBig.getRadiusX());
					this.slash.setRadiusY(slashBig.getRadiusY());
					this.slash.setStartAngle(slashBig.getStartAngle());
					this.slash.setLength(slashBig.getLength());
					this.slash.setType(slashBig.getType());
					this.slash.setFill(slashBig.getFill());
					for(var p:players) {
						if(p!=this&&Shape.intersect(this.slash,p).getLayoutBounds().getWidth()>0) {
							p.state=States.DEAD;
						}
					}
				}
			}),new KeyFrame(Duration.millis(750),(e)->{
				this.blade.side*=-1;
				this.turn();
				this.blade.getTransforms().set(1,new Scale(1,1,this.blade.getCenterX()-this.blade.getRadiusX(),0));
				this.keen=keenDelay;
				this.slash.setVisible(false);
				if(this.state!=States.DEAD) {
					this.state=States.NORMAL;
				}
			}));
			Anime.setCycleCount(1);
			Anime.play();
		}
		public void performCritical(Point2D target) {
			Timeline Anime=new Timeline(new KeyFrame(Duration.millis(0),(e)->{
				int ang=(int)new Point2D(target.getX()-this.getLayoutX(),target.getY()-this.getLayoutY()).angle(1,0);
				if(target.getY()<this.getLayoutY()) {
					ang*=-1;
				}
				if(this.load>0) {
					this.load=loadDelay;
					this.gun.getTransforms().set(1,new Scale(1,1,this.gun.getX()+this.gun.getWidth(),0));
				}
				this.slash.setLayoutX(this.getLayoutX());
				this.slash.setLayoutY(this.getLayoutY());
				this.slash.getTransforms().set(0,new Rotate(ang,0,0));
				this.slash.setRadiusX(slashCritical.getRadiusX());
				this.slash.setRadiusY(slashCritical.getRadiusY());
				this.slash.setStartAngle(slashCritical.getStartAngle());
				this.slash.setLength(slashCritical.getLength());
				this.slash.setType(slashCritical.getType());
				this.slash.setFill(slashCritical.getFill());
				this.slash.setVisible(true);
			}),new KeyFrame(Duration.millis(250),(e)->{
				this.blade.side*=-1;
				this.turn();
				this.blade.getTransforms().set(1,new Scale(1,1,this.blade.getCenterX()-this.blade.getRadiusX(),0));
				this.keen=keenDelay;
				this.slash.setVisible(false);
				if(this.state!=States.DEAD) {
					this.state=States.NORMAL;
				}
			}));
			Anime.setCycleCount(1);
			Anime.play();
		}
		public void performSlash() {
			Timeline Anime=new Timeline(new KeyFrame(Duration.millis(0),(e)->{
				this.turn();
				if(this.load>0) {
					this.load=loadDelay;
					this.gun.getTransforms().set(1,new Scale(1,1,this.gun.getX()+this.gun.getWidth(),0));
				}
				this.keen=keenDelay;
				this.blade.getTransforms().set(1,new Scale(1,1,this.blade.getCenterX()-this.blade.getRadiusX(),0));
				this.slash.setLayoutX(this.getLayoutX());
				this.slash.setLayoutY(this.getLayoutY());
				this.slash.getTransforms().set(0,new Rotate(this.dir,0,0));
				this.slash.setRadiusX(slashWindup.getRadiusX());
				this.slash.setRadiusY(slashWindup.getRadiusY());
				this.slash.setStartAngle(slashWindup.getStartAngle());
				this.slash.setLength(slashWindup.getLength());
				this.slash.setType(slashWindup.getType());
				this.slash.setFill(slashWindup.getFill());
				this.slash.setVisible(true);
			}),new KeyFrame(Duration.millis(250),(e)->{
				if(this.state==States.LOCK) {
					this.blade.setVisible(false);
					this.slash.setRadiusX(slashNormal.getRadiusX());
					this.slash.setRadiusY(slashNormal.getRadiusY());
					this.slash.setStartAngle(slashNormal.getStartAngle());
					this.slash.setLength(slashNormal.getLength());
					this.slash.setType(slashNormal.getType());
					this.slash.setFill(slashNormal.getFill());
					for(var p:players) {
						if(p!=this&&Shape.intersect(this.slash,p).getLayoutBounds().getWidth()>0) {
							p.state=States.DEAD;
						}
					}
				}
			}),new KeyFrame(Duration.millis(500),(e)->{
				this.blade.side*=-1;
				this.turn();
				this.blade.setVisible(true);
				this.slash.setVisible(false);
				if(this.state!=States.DEAD) {
					this.state=States.NORMAL;
				}
			}));
			Anime.setCycleCount(1);
			Anime.play();
		}
		public void performSnip() {
			Timeline Anime=new Timeline(new KeyFrame(Duration.millis(0),(e)->{
				this.turn();
				if(this.keen>0) {
					this.keen=keenDelay;
					this.blade.getTransforms().set(1,new Scale(1,1,this.blade.getCenterX()-this.blade.getRadiusX(),0));
				}
				this.fire.setLayoutX(this.getLayoutX());
				this.fire.setLayoutY(this.getLayoutY());
				this.fire.setWidth(Math.sqrt(fieldWidth*fieldWidth+fieldHeight*fieldHeight));
				this.fire.getTransforms().set(0,new Rotate(this.dir,0,0));
				this.fire.setFill(fireAim.getFill());
				this.fire.setVisible(true);
			}),new KeyFrame(Duration.millis(500),(e)->{
				if(this.state==States.LOCK) {
					player nearP=null;
					double nearD=Double.POSITIVE_INFINITY;
					Point2D thisLocation=new Point2D(this.getLayoutX(),this.getLayoutY());
					for(var p:players) {
						if(p!=this&&Shape.intersect(this.fire,p).getLayoutBounds().getWidth()>0) {
							double D=thisLocation.distance(p.getLayoutX(),p.getLayoutY());
							if(D<nearD) {
								nearD=D;
								nearP=p;
							}
						}
					}
					if(nearD!=Double.POSITIVE_INFINITY) {
						if(nearP.keen==0&&nearP.state==States.NORMAL) {
							nearP.state=States.LOCK;
							nearP.performCritical(thisLocation);
						}else {							
							nearP.state=States.DEAD;
						}
						this.fire.setWidth(nearD-this.fire.getX());
					}
					this.load=loadDelay;
					this.gun.getTransforms().set(1,new Scale(1,1,this.gun.getX()+this.gun.getWidth(),0));
					this.fire.setFill(fireSnip.getFill());
				}
			}),new KeyFrame(Duration.millis(750),(e)->{
				this.fire.setVisible(false);
				if(this.state!=States.DEAD) {
					this.state=States.NORMAL;
				}
			}));
			Anime.setCycleCount(1);
			Anime.play();
		}
		public void performStab() {
			Timeline Anime=new Timeline(new KeyFrame(Duration.millis(0),(e)->{
				this.turn();
				if(this.load>0) {
					this.load=loadDelay;
					this.gun.getTransforms().set(1,new Scale(1,1,this.gun.getX()+this.gun.getWidth(),0));
				}
				this.slash.setLayoutX(this.getLayoutX());
				this.slash.setLayoutY(this.getLayoutY());
				this.slash.getTransforms().set(0,new Rotate(this.dir,0,0));
				this.slash.setRadiusX(stabT.getRadiusX());
				this.slash.setRadiusY(stabT.getRadiusY());
				this.slash.setStartAngle(stabT.getStartAngle());
				this.slash.setLength(stabT.getLength());
				this.slash.setType(stabT.getType());
				this.slash.setFill(stabT.getFill());
				this.slash.setVisible(true);
				player nearP=null;
				double nearD=Double.POSITIVE_INFINITY;
				Point2D thisLocation=new Point2D(this.getLayoutX(),this.getLayoutY());
				for(var p:players) {
					if(p!=this&&Shape.intersect(this.slash,p).getLayoutBounds().getWidth()>0) {
						double D=thisLocation.distance(p.getLayoutX(),p.getLayoutY());
						if(D<nearD) {
							nearD=D;
							nearP=p;
						}
					}
				}
				if(nearD!=Double.POSITIVE_INFINITY) {				
					nearP.state=States.DEAD;
				}
			}),new KeyFrame(Duration.millis(250),(e)->{
				this.slash.setVisible(false);
				if(this.state!=States.DEAD) {
					this.state=States.NORMAL;
				}
			}));
			Anime.setCycleCount(1);
			Anime.play();
		}
		@Override
		public void run() {
			byte[] outbuf=new byte[windowWidth*windowHeight*4];
			byte[] inbuf=new byte[5];
			try {
				InputStream input=socket.getInputStream();
				OutputStream output=socket.getOutputStream();
				while(this.state!=States.DEAD) {
					output.write(2);
					this.screen.getPixelReader().getPixels(0,0, windowWidth, windowHeight, PixelFormat.getByteBgraInstance(), outbuf, 0, windowWidth*4);
					for(int j=0;j<windowWidth*windowHeight*4;j+=50000){
						output.write(outbuf,j,50000);
					}
					this.act=Acts.values()[input.read()];
					int click=input.read();
					if(this.state==States.NORMAL) {
						this.state=States.values()[click];
					}
					int len=input.read(inbuf);
					this.dir=Integer.parseInt(new String(Arrays.copyOfRange(inbuf,0,len)));
					
					Thread.sleep(1000/FPS);
				}
				for(int i=0;i<FPS*3;i++) {
					output.write(1);
					this.screen.getPixelReader().getPixels(0,0, windowWidth, windowHeight, PixelFormat.getByteBgraInstance(), outbuf, 0, windowWidth*4);
					for(int j=0;j<windowWidth*windowHeight*4;j+=50000){
						output.write(outbuf,j,50000);
					}
					Thread.sleep(1000/FPS);
				}
				output.write(0);
			} catch (IOException e) {
//				e.printStackTrace();
			} catch (InterruptedException e) {
//				e.printStackTrace();
			} finally {
				this.state=States.DEAD;
				try {
					this.socket.close();
				}catch(IOException e) {}
			}
		}
	}
	
	public class listen implements Runnable{
		@Override
		public void run() {
			String serverName="127.0.0.1";
			int serverPort=12000;
			InetSocketAddress socketAddress=new InetSocketAddress(serverName, serverPort);
			ServerSocket serverSocket=null;
			Socket socket=null;
			try {
				serverSocket=new ServerSocket();
				serverSocket.bind(socketAddress);
				System.out.println("Server On");
				while(true) {
					socket=serverSocket.accept();
					player p=new player(socket);
					newPlayers.offer(p);
				}
			}catch(IOException e) {
				e.printStackTrace();
			}finally {
				try {
					serverSocket.close();
				}catch(IOException e) {}
			}
		}
	}
	
	public void initialize(URL arg0, ResourceBundle arg1) {
		Thread listenT=new Thread(new listen());
		listenT.start();
		Timeline refresh=new Timeline(new KeyFrame(Duration.millis(1000/RPS),(e)->{
			while(!newPlayers.isEmpty()) {
				player p=newPlayers.poll();
				players.add(p);
				field.getChildren().add(p);
				field.getChildren().add(p.blade);
				field.getChildren().add(p.gun);
				field.getChildren().add(p.slash);
				field.getChildren().add(p.fire);
				Thread playerT=new Thread(p);
				playerT.start();
			}
			for(player p:players) {
				s.setViewport(new Rectangle2D((int)(Math.min(Math.max(0,p.getLayoutX()),fieldWidth)), (int)(Math.min(Math.max(0,p.getLayoutY()),fieldHeight)), windowWidth, windowHeight));
				scene.snapshot(s,p.screen);

				if(p.state==States.DEAD) {
					field.getChildren().remove(p.blade);
					field.getChildren().remove(p.gun);
					field.getChildren().remove(p.slash);
					field.getChildren().remove(p.fire);
					field.getChildren().remove(p);
				}else {
					if(p.state==States.WINDUP) {
						p.state=States.LOCK;
						if(p.keen>0) {
							p.performSlash();
						}else {
							p.performBig();
						}
					}else if(p.state==States.AIMING) {
						p.state=States.LOCK;
						if(p.load>0) {
							p.performStab();
						}else {
							p.performSnip();
						}
					}
					if(p.state==States.NORMAL) {
						if(p.keen==0) {
							Point2D thisLocation=new Point2D(p.getLayoutX(),p.getLayoutY());
							for(var p1:players) {
								if(p1!=p&&thisLocation.distance(p1.getLayoutX(),p1.getLayoutY())<slashCritical.getRadiusX()) {
									p.state=States.LOCK;
									p.performCritical(new Point2D(p1.getLayoutX(),p1.getLayoutY()));
									if(p1.keen==0&&p1.state==States.NORMAL) {
										p1.state=States.LOCK;
										p1.performCritical(thisLocation);
									}else{
										p1.state=States.DEAD;
									}
								}
							}
						}
						if(p.act==Acts.WALK) {
							if(p.load>0) {
								p.load=loadDelay;
							}
							if(p.keen>0) {
								p.keen=keenDelay;
							}
							p.turn();
							p.move(100/RPS);
						}else if(p.act==Acts.SHEATH) {
							if(p.keen>0) {
								if(p.load>0) {
									p.load=loadDelay;
								}
								p.keen--;
							}else if(p.load>0) {
								p.load--;
							}
						}else if(p.act==Acts.RELOAD) {
							if(p.load>0) {
								if(p.keen>0) {
									p.keen=keenDelay;
								}
								p.load--;
							}else if(p.keen>0) {
								p.keen--;
							}
						}
					}
					p.blade.getTransforms().set(1,new Scale((double)p.keen/keenDelay,(double)p.keen/keenDelay,p.blade.getCenterX()-p.blade.getRadiusX(),0));
					p.gun.getTransforms().set(1,new Scale((double)p.load/loadDelay,1,p.gun.getX()+p.gun.getWidth(),0));
				}
				if(p.socket.isClosed()) {
					players.remove(p);
				}
			}
		}));
		refresh.setCycleCount(Timeline.INDEFINITE);
		refresh.play();
	}
	public void onEnd(MouseEvent e) throws IOException {
		Main.stage.close();
	}
	public static void main(String[] args){

//		screenWidth=java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();
//		screenHeight=java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		launch(args);
		
	}
	public void start(Stage mainStage) throws IOException{
		robot=new Robot();
	    mainStage.setTitle("左居合右狙擊_伺服器畫面");
	    mainStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("0.fxml"))));
		stage=mainStage;
//	    mainStage.show();
	}
}
