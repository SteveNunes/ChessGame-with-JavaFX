package gui;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import board.Board;
import enums.Icons;
import enums.PieceColor;
import enums.PieceType;
import gui.util.Controller;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import piece.Piece;
import piece.Position;
import pieces.Bishop;
import pieces.King;
import pieces.Knight;
import pieces.Pawn;
import pieces.Queen;
import pieces.Rook;
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
	@FXML
	private Button buttonUndo;
	@FXML
	private Button buttonRedo;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		List<PieceType> types = PieceType.getListOfAll();
		List<PieceColor> colors = PieceColor.getListOfAll();
		pieces = new Image[2][types.size()];
		for (int c = 0; c < colors.size(); c++)
			for (int t = 0; t < types.size(); t++) {
				String fileName = "/sprites/pieces/" + colors.get(c) + "_" + types.get(t) + ".png";
				pieces[c][t] = Controller.removeBgColor(new Image(fileName), Color.valueOf("#00FF00"), 10);
			}
		buttonUndo.setOnAction(e -> {
			board.undoMove();
			updateBoard();
		});
		buttonRedo.setOnAction(e -> {
			board.redoMove();
			updateBoard();
		});
		Controller.addIconToButton(buttonUndo, Icons.ICON_MOVEMAXLEFT.getValue(), 18, 18, 20);
		Controller.addIconToButton(buttonRedo, Icons.ICON_MOVEMAXRIGHT.getValue(), 18, 18, 20);
	}
	
	private void checkUndoButtons() {
		buttonRedo.setDisable(!board.canRedoMove());
		buttonUndo.setDisable(!board.canUndoMove());
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
		if (board.checkMate()) {
			playWav("checkmate");
			msg(board.getCurrentColorTurn().name() + " won!");
		}
		else
			imageViewTurn.setImage(getPieceImage(new Pawn(null, null, board.getCurrentColorTurn())));
		checkUndoButtons();
	}
	
	public Image getPieceImage(Piece p) {
		List<PieceType> types = PieceType.getListOfAll();
		List<PieceColor> colors = PieceColor.getListOfAll();
		for (int c = 0; c < colors.size(); c++)
			for (int t = 0; t < types.size(); t++)
				if (colors.get(c) == p.getColor() && types.get(t) == p.getType())
					return pieces[c][t];
		return null;
	}
	
	public Image getPieceImage(PieceType type, PieceColor color) {
		if (type == PieceType.BISHOP)
			return getPieceImage(new Bishop(null, null, color));
		if (type == PieceType.KING)
			return getPieceImage(new King(null, null, color));
		if (type == PieceType.KNIGHT)
			return getPieceImage(new Knight(null, null, color));
		if (type == PieceType.QUEEN)
			return getPieceImage(new Queen(null, null, color));
		if (type == PieceType.ROOK)
			return getPieceImage(new Rook(null, null, color));
		return getPieceImage(new Pawn(null, null, color));
	}

	public void init() {
		boardImage = new Image("/sprites/board.png");
	  bounds = gridPaneBoard.getCellBounds(0, 0);
		board = new Board(PieceColor.BLACK);
		board.reset();
		setPiecesOnTheBoardForTestCheckMate();
	  updateBoard();
		buttonUndo.setDisable(true);
		buttonRedo.setDisable(true);
	}
	
	private void setPiecesOnTheBoard() {
		board.addNewPiece("a8", PieceType.ROOK, PieceColor.WHITE);
		board.addNewPiece("b8", PieceType.KNIGHT, PieceColor.WHITE);
		board.addNewPiece("c8", PieceType.BISHOP, PieceColor.WHITE);
		board.addNewPiece("d8", PieceType.QUEEN, PieceColor.WHITE);
		board.addNewPiece("e8", PieceType.KING, PieceColor.WHITE);
		board.addNewPiece("f8", PieceType.BISHOP, PieceColor.WHITE);
		board.addNewPiece("g8", PieceType.KNIGHT, PieceColor.WHITE);
		board.addNewPiece("h8", PieceType.ROOK, PieceColor.WHITE);
		board.addNewPiece("a1", PieceType.ROOK, PieceColor.BLACK);
		board.addNewPiece("b1", PieceType.KNIGHT, PieceColor.BLACK);
		board.addNewPiece("c1", PieceType.BISHOP, PieceColor.BLACK);
		board.addNewPiece("d1", PieceType.QUEEN, PieceColor.BLACK);
		board.addNewPiece("e1", PieceType.KING, PieceColor.BLACK);
		board.addNewPiece("f1", PieceType.BISHOP, PieceColor.BLACK);
		board.addNewPiece("g1", PieceType.KNIGHT, PieceColor.BLACK);
		board.addNewPiece("h1", PieceType.ROOK, PieceColor.BLACK);
	}

	private void setPiecesOnTheBoardForTestCheckMate() {
		board.addNewPiece("h8", PieceType.KING, PieceColor.WHITE);
		board.addNewPiece("f6", PieceType.QUEEN, PieceColor.BLACK);
		board.addNewPiece("a8", PieceType.ROOK, PieceColor.WHITE);
		board.addNewPiece("e1", PieceType.KING, PieceColor.BLACK);
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
	
	private void playWav(String wav) {
		try
			{ Sounds.playWav("./src/sounds/" + wav + ".wav"); }
		catch (Exception e) {}
	}
	
	private void drawTile(int index) {
		int width = getBoardWidth();
		int height = getBoardHeight();
		int x = index % 8;
		int y = index / 8;
		Position pos = new Position(y, x);

		Rectangle rectangle = null;
		Piece piece = board.getPieceAt(pos);
		Canvas canvas = new Canvas(width, height);
    canvas.getGraphicsContext2D().drawImage(boardImage, x * 150, y * 150, 150, 150, 0, 0, width, height);

		if (piece != null) {
			canvas.getGraphicsContext2D().drawImage(getPieceImage(piece), 0, 0, 250, 250, 0, 0, width, height);
			if (board.checkEnPassant() && board.getEnPassantPiece() == piece)
				rectangle = newRectangle(Color.ORANGE);

			if (board.currentColorIsChecked() &&
					piece.getColor() == board.getCurrentColorTurn() &&
					piece.getType() == PieceType.KING)
						rectangle = newRectangle(Color.PINK); // Marca o rei com retângulo rosa, se ele estiver em check
		}
		
		if (board.pieceIsSelected()) { 
			if (board.getSelectedPiece().equals(piece)) // Marca com retângulo amarelo a pedra selecionada atualmente
				rectangle = newRectangle(Color.YELLOW);
			else if (board.getSelectedPiece().canMoveToPosition(pos)) // Marca com retângulo verde a casa onde a pedra selecionada pode ir (Se for casa onde houver uma pedra adversária, marca em vermelho)
				rectangle = newRectangle(board.getPieceAt(pos) != null ? Color.RED : Color.LIGHTGREEN);
		}
		
		Pane pane = new Pane(canvas);
		if (rectangle != null)
      pane.getChildren().add(rectangle);
    gridPaneBoard.add(pane, x, y);

    pane.setOnMouseClicked(e -> {
    	msg("");
    	if (checkIfPieceIsPromoted(true))
    		return;
    		
    	try {
	    	if (board.pieceIsSelected()) {
	    		playWav(board.getSelectedPiece().getPosition().equals(pos) ? "unselect" :
	    			board.getPieceAt(pos) != null ? "capture" : "move");
	    		board.movePieceTo(pos);
	    	}
	    	else {
	    		board.selectPiece(pos);
	    		playWav("select");
	    	}
    	}
  		catch (Exception ex) {
  			if (board.pieceIsSelected() && piece != null &&
  					!board.isOpponentPieces(board.getSelectedPiece(), piece)) {
		  				// Se clicar em cima de uma pedra da mesma cor, já tendo uma pedra previamente selecionada, muda a seleção para a pedra atual (se for uma pedra diferete da selecionada) ou cancela a seleção atual
  						board.cancelSelection();
			    		playWav(board.getSelectedPiece() != piece ? "select" : "unselect");
		  				if (board.getSelectedPiece() != piece) {
		  					try
		  						{ board.selectPiece(pos); }
		  		  		catch (Exception ex2) {
		  		    		playWav("error");
		  			  		errorMsg(ex2.getMessage());
		  		  			ex2.printStackTrace();
		  		  		}
		  				}
  			}
  			else {
	    		playWav("error");
	  			errorMsg(ex.getMessage());
	  			ex.printStackTrace();
  			}
  		}
  	  updateBoard();
  	  checkIfPieceIsPromoted();
    });
	}

	private Boolean checkIfPieceIsPromoted(Boolean playSoundIfOpenWindow) {
  	if (board.pieceWasPromoted()) {
  		if (playSoundIfOpenWindow)
  			playWav("select");
	  	openPromoteWindow();
	  	return true;
  	}
  	return false;
	}

	private Boolean checkIfPieceIsPromoted()
		{ return checkIfPieceIsPromoted(false); }

	private void openPromoteWindow() {
		int width = 300;
		int height = 116;
		Group group = new Group(); 
		Scene scene = new Scene(group, width, height, Color.valueOf("#EEEEEE"));
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setWidth(width);
		stage.setHeight(height);
		stage.setResizable(false);
		VBox vBox = new VBox();
		vBox.setSpacing(5);
		vBox.setPrefSize(width, height + 20);
		vBox.setPadding(new Insets(5, 10, 5, 5));
		group.getChildren().add(vBox);
		Text text = new Text("Select a new piece for promotion");
		text.setStyle("-fx-font-size: 14px; -fx-font-family: \"Lucida Console\";");
		vBox.getChildren().add(text);
		HBox hBox = new HBox();
		vBox.getChildren().add(hBox);
		hBox.setPrefSize(width, height);
		hBox.setSpacing(5);
		hBox.setAlignment(Pos.TOP_CENTER);
		for (PieceType type : Arrays.asList(PieceType.BISHOP, PieceType.KNIGHT, PieceType.QUEEN, PieceType.ROOK)) {
			ImageView imageView = new ImageView(getPieceImage(type, board.getCurrentColorTurn()));
			imageView.setFitWidth(42);
			imageView.setFitHeight(42);
			Button button = new Button();
			button.setGraphic(imageView);
			button.setOnAction(e -> {
				board.promotePieceTo(type);
				stage.close();
    		playWav("promotion");
				updateBoard();
			});
			hBox.getChildren().add(button);
		}
		stage.initOwner(gridPaneBoard.getScene().getWindow());
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setTitle("Promotion");
		stage.showAndWait();
	}	

}
	