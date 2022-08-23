package application;

import java.util.ArrayList;
import java.util.List;

import gui.BoardController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import util.Alerts;

public class Program extends Application {
	
	private static Class<? extends Program> mainClass;
	private static Scene mainScene;
	private static Stage mainStage;
	private static List<Stage> stageList = new ArrayList<>();
	
	public static Class<? extends Program> getMainClass()
		{ return mainClass; }
	
	public static Stage getMainStage()
		{ return mainStage; }

	public static Scene getMainScene()
		{ return mainScene; }

	public static Boolean isActive() {
		for (Stage stage : stageList)
			if (stage.isFocused())
				return true;
		return false;
	}
	
	public static void addStageToStageList(Stage stage)
		{ stageList.add(stage); }
	
	public static void addStageToStageList(Window window)
		{ addStageToStageList((Stage) window); }

	public static void removeStageFromStageList(Stage stage)
		{ stageList.remove(stage); }

	@Override
	public void start(Stage stage) {
		mainClass = getClass();
		stageList.add(mainStage = stage);
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/BoardView.fxml"));
			VBox vBox = loader.load();
			mainScene = new Scene(vBox);
			stage.setScene(mainScene);
			stage.setResizable(false);
			stage.setTitle("Chess Game");
			BoardController controller = loader.getController();
			stage.show();
			controller.init();
		}
		catch (Exception e) {
			e.printStackTrace();
			Alerts.exception("Erro", "Erro ao iniciar o programa", e.getMessage(), e);
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	public static void showAndWait(Stage stage) {
		addStageToStageList(stage);
		stage.showAndWait();
		removeStageFromStageList(stage);
	}

}
