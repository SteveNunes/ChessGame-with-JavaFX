package gui;

import java.net.URL;
import java.util.ResourceBundle;

import board.Board;
import gui.util.Controller;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import piece.Piece;
import piece.Position;
import piece.Type;
import pieces.Pawn;
import util.Sounds;

public class BoardController implements Initializable {
	
	private Board board;
	private Image boardImage;
	private Bounds bounds;
	private Image[][] pieces;
	
	@FXML
	private GridPane gridPaneBoard;
	@FXML
	private Text textErrorMessage;
	@FXML
	private ImageView imageViewTurn;

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
	
	private void errorMsg(String text) {
		textErrorMessage.setText(text);
		textErrorMessage.setFill(Color.RED);
	}
	
	private void alertMsg(String text) {
		textErrorMessage.setText(text);
		textErrorMessage.setFill(Color.YELLOW);
	}

	private void msg(String text) {
		textErrorMessage.setText(text);
		textErrorMessage.setFill(Color.DARKBLUE);
	}

	private void updateBoard() {
		gridPaneBoard.getChildren().clear();
		for (int n = 0; n < 64; n++)
			drawTile(n);
		imageViewTurn.setImage(getPieceImage(new Pawn(null, null, board.getCurrentColorTurn())));
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
		board = new Board();
		board.reset();
	  updateBoard();
	}
	
	private int getBoardWidth()
		{ return (int)bounds.getWidth(); }
	
	private int getBoardHeight()
		{ return (int)bounds.getHeight(); }
	
	private Rectangle newRectangle(Color color) {
		int width = getBoardWidth();
		int height = getBoardHeight();
		Rectangle rectangle = new Rectangle(2, 2, width - 4 , height - 4);
		rectangle.setFill(Color.TRANSPARENT);
		rectangle.setStroke(color);
		rectangle.setStrokeWidth(4);
		return rectangle;
	}
	
	private void playWav(String wav)
		{ Sounds.playWav("./src/sounds/" + wav + ".wav"); }
	
	private void drawTile(int index) {
		int width = getBoardWidth();
		int height = getBoardHeight();
		int x = index % 8;
		int y = index / 8;
		Position pos = new Position(y, x);

		Rectangle rectangle = null;
		Piece piece = board.getPieceAtPosition(pos);
		Canvas canvas = new Canvas(width, height);
    canvas.getGraphicsContext2D().drawImage(boardImage, x * 150, y * 150, 150, 150, 0, 0, width, height);

		if (piece != null)
			canvas.getGraphicsContext2D().drawImage(getPieceImage(piece), 0, 0, 250, 250, 0, 0, width, height);
		
		if (board.pieceIsSelected()) { 
			if (board.getSelectedPiece().equals(piece))
				rectangle = newRectangle(Color.YELLOW);
			else if (board.getSelectedPiece().canMoveToPosition(pos))
				rectangle = newRectangle(board.getPieceAtPosition(pos) != null ? Color.RED : Color.LIGHTGREEN);
		}

		Pane pane = new Pane(canvas);
		if (rectangle != null)
      pane.getChildren().add(rectangle);
    gridPaneBoard.add(pane, x, y);

    pane.setOnMouseClicked(e -> {
    	msg("");
    	try {
	    	if (board.pieceIsSelected()) {
	    		playWav(board.getSelectedPiece().getPosition().equals(pos) ? "unselect" :
	    			board.getPieceAtPosition(pos) != null ? "capture" : "move");
	    		board.movePieceTo(pos);
	    	}
	    	else {
	    		board.selectPiece(pos);
	    		playWav("select");
	    	}
    	}
  		catch (Exception ex) {
    		playWav("error");
  			errorMsg(ex.getMessage());
  		}
  	  updateBoard();
    });
	}	

}
	