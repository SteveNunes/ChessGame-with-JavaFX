package gui;

import java.net.URL;
import java.util.ResourceBundle;

import application.Program;
import gui.util.Controller;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import piece.Piece;
import piece.Position;
import piece.Type;

public class BoardController implements Initializable {
	
	private Image boardImage;
	private Position selectedTile;
	private Bounds bounds;
	private Image[][] pieces;
	
	@FXML
	private GridPane gridPaneBoard;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		Type[] types = {Type.BISHOP, Type.KING, Type.KNIGHT, Type.QUEEN, Type.ROOK, Type.PAWN};
		piece.Color[] colors = {piece.Color.BLACK, piece.Color.WHITE};
		pieces = new Image[2][6];
		for (int c = 0; c < colors.length; c++)
			for (int t = 0; t < types.length; t++) {
				String fileName = "/sprites/pieces/" + colors[c] + "_" + types[t] + ".png";
				pieces[c][t] = Controller.removeBgColor(new Image(fileName), Color.valueOf("#00FF00"), 10);
			}
	}
	
	private void updateBoard() {
		gridPaneBoard.getChildren().clear();
		for (int n = 0; n < 64; n++)
			drawTile(n);
	}
	
	public Image getPieceImage(Piece p) {
		Type[] types = {Type.BISHOP, Type.KING, Type.KNIGHT, Type.QUEEN, Type.ROOK, Type.PAWN};
		piece.Color[] colors = {piece.Color.BLACK, piece.Color.WHITE};
		for (int c = 0; c < colors.length; c++)
			for (int t = 0; t < types.length; t++)
				if (colors[c] == p.getColor() && types[t] == p.getType())
					return pieces[c][t];
		return null;
	}

	public void init() {
		boardImage = new Image("/sprites/board.png");
	  bounds = gridPaneBoard.getCellBounds(0, 0);
	  updateBoard();
	}
	
	private int getBoardWidth()
		{ return (int)bounds.getWidth(); }
	
	private int getBoardHeight()
		{ return (int)bounds.getHeight(); }

	private void drawTile(int index) {
		int width = getBoardWidth();
		int height = getBoardHeight();
		int x = index % 8;
		int y = index / 8;
		
		Piece piece = Program.getBoard().getPieceAtPosition(new Position(y, x));
		Canvas canvas = new Canvas(width, height);
    canvas.getGraphicsContext2D().drawImage(boardImage, x * 150, y * 150, 150, 150, 0, 0, width, height);
		Rectangle rectangle = null;

		if (piece != null) {
			canvas.getGraphicsContext2D().drawImage(getPieceImage(piece), 0, 0, 250, 250, 0, 0, width, height);
			if (selectedTile != null && piece.getPosition().equals(selectedTile)) {
				rectangle = new Rectangle(2, 2, width - 4 , height - 4);
				rectangle.setFill(Color.TRANSPARENT);
				rectangle.setStroke(Color.YELLOW);
				rectangle.setStrokeWidth(4);
			}
    }
		
		Pane pane = new Pane(canvas);
    if (rectangle != null) 
      pane.getChildren().add(rectangle);

    gridPaneBoard.add(pane, x, y);

    pane.setOnMouseClicked(e -> {
    	if (selectedTile != null) {
    		if (selectedTile.equals(new Position(y, x))) {
    			selectedTile = null;
      	  updateBoard();
    			return;
    		}
    	}
    	selectedTile = new Position(y, x);
  	  updateBoard();
    });
	}	

}
	