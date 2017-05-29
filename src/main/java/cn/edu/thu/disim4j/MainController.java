package cn.edu.thu.disim4j;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import cn.edu.thu.disim4j.elements.ElementController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class MainController implements Initializable, GlobalController{

	DrawType drawType=DrawType.None;
	Group none=new Group();//无意义的
	int counter=0;//每个元素都给一个唯一编号
	Map<Integer, Group> elements=new HashMap<>();//存储画板上所有元素
	Map<Group, Integer> reverseElements=new HashMap<>();
	@FXML 
	Pane paintPane;
	@FXML
	ScrollPane scrolPane;
	@FXML
	Circle leftPlace;
	@FXML
	Rectangle leftRect;
	@FXML
	Line leftLine;
	@FXML
	Button compileButton;
	@FXML
	Button startButton;
	@FXML
	Button resetButton;
	@FXML
	Button addHButton;
	@FXML
	Button addVButton;

	Group placeOnCursor;
	Group transitionOnCursor;
	//TODO 新增元素时在这里加新的变量
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initNodesOnCursor();
		addHButton.setOnAction( (EventHandler<ActionEvent>) e->{
			paintPane.setMinWidth(paintPane.getWidth()+500);
		});
		addVButton.setOnAction( (EventHandler<ActionEvent>) e->{
			paintPane.setMinHeight(paintPane.getHeight()+500);
		});
		
		paintPane.setOnMouseEntered(mouseEnterOrLeaveGlobal);
		paintPane.setOnMouseExited(mouseEnterOrLeaveGlobal);
		paintPane.setOnMouseClicked(paintPaneOnMouseClickedEventHandler);
		
	}
	
	@Override
	public void remove(Node node) {
		paintPane.getChildren().remove(node);
		Integer integer=reverseElements.get(node);
		if(integer!=null){
			elements.remove(integer);
			reverseElements.remove(node);
		}
		
	}


	//用户点击了左边的库所元素时
	private EventHandler<MouseEvent> leftNodeOnMouseClickedEventHandler = 
			new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent t) {
			//TODO 新增元素时扩展这里
			if(paintPane.getOnMouseMoved()==null){//确保重复点击无效
				if(t.getSource().equals(leftPlace)){//如果点击的是库所
					drawType=DrawType.Place;
				}else if(t.getSource().equals(leftRect)){
					drawType=DrawType.Transition;
				}
				paintPane.setOnMouseMoved(mouseMoveGlobal);
				paintPane.getChildren().add(getNodeOnCursor());
			}
		}
	};
	//用户在主画板上点击了鼠标
	private EventHandler<MouseEvent> paintPaneOnMouseClickedEventHandler = 
			new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent t) {
			if(paintPane.getOnMouseMoved()!=null){//==null时相当于用户并没有点选左边的基础元素（此时drawType==None）
				addNewNodeOnPaintPane(t);
			}
		}

		
	};
	
	//在paintPane中移动鼠标事件
	private EventHandler<MouseEvent> mouseMoveGlobal = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent t) {
			getNodeOnCursor().relocate(t.getX()-25, t.getY()-30);
		}
	};
	
	//鼠标进入或者离开paintPane的时候的事件
	private EventHandler<MouseEvent> mouseEnterOrLeaveGlobal = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent t) {
			if(t.getEventType().equals(MouseEvent.MOUSE_ENTERED)){
				getNodeOnCursor().setVisible(true);
			}else{
				getNodeOnCursor().setVisible(false);
			}
		}
	};
	
	//发现按下了esc 就将鼠标上的图形去掉
	EventHandler<KeyEvent> keyTypedHandler = new EventHandler<KeyEvent>() {
		@Override
		public void handle(KeyEvent event) {
            if (event.getCode() == KeyCode.ESCAPE){
            	getNodeOnCursor().setVisible(false);
                paintPane.getChildren().remove(getNodeOnCursor());
                paintPane.setOnMouseMoved(null);
            	drawType=DrawType.None;
            }
		}
	};

	private void addOneElement(double x, double y, String fxml){
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
		try {
			counter++;
			Group group = (Group)fxmlLoader.load();
			ElementController controller=(ElementController)fxmlLoader.getController();
			controller.addListener(this);
			paintPane.getChildren().add(group);
			elements.put(counter, group);
			reverseElements.put(group, counter);
			group.relocate(x, y);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	

	
	static enum DrawType{//如果新增其他基础元素，请扩展这里 TODO
		None, Place, Transition
	}
	
	private Node getNodeOnCursor(){//如果新增其他基础元素，请扩展这里 TODO
		switch (drawType) {
		case None:
			return none;
		case Place:
			return placeOnCursor;
		case Transition:
			return transitionOnCursor;
		default:
			return none;
		}
	}
	private void initNodesOnCursor(){//如果新增其他基础元素，请扩展这里 TODO
		try {
			placeOnCursor = (Group)new FXMLLoader(getClass().getResource("elements/place.fxml")).load();
			placeOnCursor.setVisible(false);
			placeOnCursor.getChildren().get(3).setVisible(false);;//FIXME 如果修改了place.fxml 请注意修改这个index的值
			leftPlace.setOnMouseClicked(leftNodeOnMouseClickedEventHandler);
			//如果新增其他基础元素，请扩展这里 TODO
			transitionOnCursor= (Group)new FXMLLoader(getClass().getResource("elements/transition.fxml")).load();
			transitionOnCursor.setVisible(false);
			transitionOnCursor.getChildren().get(2).setVisible(false);;//FIXME 如果修改了transition.fxml 请注意修改这个index的值
			leftRect.setOnMouseClicked(leftNodeOnMouseClickedEventHandler);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		
	}
	private void addNewNodeOnPaintPane(MouseEvent t) {
		switch (drawType) {//如果新增其他基础元素，请扩展这里 TODO
		case Place:
			addOneElement(t.getX()-25, t.getY()-30, "elements/place.fxml");
			break;
		case Transition:
			addOneElement(t.getX()-45, t.getY()-25, "elements/transition.fxml");
			break;
		default:
			break;
		}
	}
}
