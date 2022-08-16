package gui;

import java.net.URL;
import java.util.ResourceBundle;

import application.Program;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import piece.Piece;
import piece.Position;

public class BoardController implements Initializable {
	
	@FXML
	private GridPane gridPaneBoard;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		for (int y = 0; y < 8; y++)
			for (int x = 0; x < 8; x++)
				drawTile(y, x);
	
	}

	private void drawTile(int x, int y) {
		Piece piece = Program.getBoard().getPieceAtPosition(new Position(y, x));
		String fileName = null;
		if (piece != null)
			fileName = "/sprites/pieces/" + piece.getColor().name() + "_" + piece.getType().name() + ".png";
		if (piece != null) {
			Image image = new Image(fileName);
			ImageView imageView = new ImageView(image);
			imageView.setFitWidth(64);
			imageView.setFitHeight(64);
			gridPaneBoard.add(imageView, x, y);
		}
	}

}
	