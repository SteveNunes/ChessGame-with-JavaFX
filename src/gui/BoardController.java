package gui;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import board.Board;
import enums.Icons;
import enums.PeriodToMillis;
import enums.PieceColor;
import enums.PieceType;
import gui.util.Controller;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import piece.Piece;
import piece.PiecePosition;
import pieces.Bishop;
import pieces.King;
import pieces.Knight;
import pieces.Pawn;
import pieces.Queen;
import pieces.Rook;
import util.Alerts;
import util.Cronometro;
import util.Sounds;

public class BoardController implements Initializable {
	
	private Cronometro cronometroGame;
	private Cronometro cronometroBlack;
	private Cronometro cronometroWhite;
	private PieceColor cronoTurn;
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
  @FXML
  private Button buttonResetGame;
  @FXML
  private Text textCronometroBlack;
  @FXML
  private Text textCronometroGame;
  @FXML
  private Text textCronometroWhite;
  @FXML
  private FlowPane flowPaneBlackCapturedPieces;
  @FXML
  private FlowPane flowPaneWhiteCapturedPieces;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		List<PieceType> types = PieceType.getListOfAll();
		List<PieceColor> colors = PieceColor.getListOfAll();
		pieces = new Image[2][types.size()];
		cronoTurn = null;
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
		buttonResetGame.setOnAction(e -> {
			if (Alerts.confirmation("Reiniciar jogo", "Deseja mesmo reiniciar o jogo?"))
				resetGame();
		});
	}
	
	private void checkUndoButtons() {
		buttonRedo.setDisable(!board.canRedoMove());
		buttonUndo.setDisable(!board.canUndoMove());
	}

	private void msg(String text, Color color) {
		textErrorMessage.setText(text);
		textErrorMessage.setFill(color);
	}
	
	private void msg(String text)
		{ msg(text, Color.BLACK); }

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
		flowPaneWhiteCapturedPieces.getChildren().clear();
		for (Piece piece : board.getCapturedPieces(PieceColor.BLACK)) {
			ImageView imageView = new ImageView(getPieceImage(piece));
			imageView.setFitWidth(32);
			imageView.setFitHeight(32);
			flowPaneWhiteCapturedPieces.getChildren().add(imageView);
		}
		flowPaneBlackCapturedPieces.getChildren().clear();
		for (Piece piece : board.getCapturedPieces(PieceColor.WHITE)) {
			ImageView imageView = new ImageView(getPieceImage(piece));
			imageView.setFitWidth(32);
			imageView.setFitHeight(32);
			flowPaneBlackCapturedPieces.getChildren().add(imageView);
		}
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
	  initTimer();
		resetGame();
	}
	
	private void resetGame() {
		board.reset();
		setPiecesOnTheBoard();
	  updateBoard();
	  Board.resetUndoMoves();
		buttonUndo.setDisable(true);
		buttonRedo.setDisable(true);
		resumirCronometro(null);
	}

	private void initTimer() {
		resumirCronometro(null);
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(50), e -> boardTimer()));
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
		boardTimer();
	}

	private void boardTimer() {
		textCronometroGame.setText(new SimpleDateFormat("HH:mm:ss.SSS").format(cronometroGame.getDuracao() + PeriodToMillis.HOUR.getValue() * 3));
		textCronometroBlack.setText(new SimpleDateFormat("HH:mm:ss.SSS").format(cronometroBlack.getDuracao() + PeriodToMillis.HOUR.getValue() * 3));
		textCronometroWhite.setText(new SimpleDateFormat("HH:mm:ss.SSS").format(cronometroWhite.getDuracao() + PeriodToMillis.HOUR.getValue() * 3));
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
		board.addNewPiece("a7", PieceType.PAWN, PieceColor.WHITE);
		board.addNewPiece("b7", PieceType.PAWN, PieceColor.WHITE);
		board.addNewPiece("c7", PieceType.PAWN, PieceColor.WHITE);
		board.addNewPiece("d7", PieceType.PAWN, PieceColor.WHITE);
		board.addNewPiece("e7", PieceType.PAWN, PieceColor.WHITE);
		board.addNewPiece("f7", PieceType.PAWN, PieceColor.WHITE);
		board.addNewPiece("g7", PieceType.PAWN, PieceColor.WHITE);
		board.addNewPiece("h7", PieceType.PAWN, PieceColor.WHITE);
		
		board.addNewPiece("a2", PieceType.PAWN, PieceColor.BLACK);
		board.addNewPiece("b2", PieceType.PAWN, PieceColor.BLACK);
		board.addNewPiece("c2", PieceType.PAWN, PieceColor.BLACK);
		board.addNewPiece("d2", PieceType.PAWN, PieceColor.BLACK);
		board.addNewPiece("e2", PieceType.PAWN, PieceColor.BLACK);
		board.addNewPiece("f2", PieceType.PAWN, PieceColor.BLACK);
		board.addNewPiece("g2", PieceType.PAWN, PieceColor.BLACK);
		board.addNewPiece("h2", PieceType.PAWN, PieceColor.BLACK);
		board.addNewPiece("a1", PieceType.ROOK, PieceColor.BLACK);
		board.addNewPiece("b1", PieceType.KNIGHT, PieceColor.BLACK);
		board.addNewPiece("c1", PieceType.BISHOP, PieceColor.BLACK);
		board.addNewPiece("d1", PieceType.QUEEN, PieceColor.BLACK);
		board.addNewPiece("e1", PieceType.KING, PieceColor.BLACK);
		board.addNewPiece("f1", PieceType.BISHOP, PieceColor.BLACK);
		board.addNewPiece("g1", PieceType.KNIGHT, PieceColor.BLACK);
		board.addNewPiece("h1", PieceType.ROOK, PieceColor.BLACK);
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
		PiecePosition pos = new PiecePosition(y, x);

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

    pane.setOnMouseClicked(e -> boardClick(piece, pos));
	}
	
	private void boardClick(Piece piece, PiecePosition pos) {
  	if (board.checkMate()) {
  		board.reset();
  		Board.resetUndoMoves();
  	  updateBoard();
    	msg("Tabuleiro reiniciado.");
    	setPiecesOnTheBoard();
  		return;
  	}
  	msg("");
  	if (checkIfPieceIsPromoted(true))
  		return;
  		
  	try {
    	if (board.pieceIsSelected()) {
    		playWav(board.getSelectedPiece().getPosition().equals(pos) ? "unselect" :
    			board.getPieceAt(pos) != null ? "capture" : "move");
    		board.movePieceTo(pos);
    		if (cronoTurn != null && cronoTurn != board.getCurrentColorTurn()) {
    			resumirCronometro(board.getCurrentColorTurn());
    			cronoTurn = null;
    		}
    	}
    	else {
    		board.selectPiece(pos);
    		playWav("select");
    		if (cronoTurn == null)
    			resumirCronometro(cronoTurn = board.getCurrentColorTurn());
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
	  			  		msg(ex2.getMessage(), Color.RED);
	  		  		}
	  				}
			}
			else {
    		playWav("error");
	  		msg(ex.getMessage(), Color.RED);
			}
		}
	  updateBoard();
	  checkIfPieceIsPromoted();
	}

	private void resumirCronometro(PieceColor color) {
		cronoTurn = color;
		if (cronometroGame == null)
			cronometroGame = new Cronometro();
		else if (color != null && cronometroGame.isPausado())
			cronometroGame.setPausado(false);
		if (cronometroBlack == null)
			cronometroBlack = new Cronometro();
		if (cronometroWhite == null)
			cronometroWhite = new Cronometro();
		cronometroBlack.setPausado(color == null || color != PieceColor.BLACK);
		cronometroWhite.setPausado(color == null || color != PieceColor.WHITE);
		if (color == null) {
			cronometroGame.setPausado(true);
			cronometroGame.reset();
			cronometroWhite.reset();
			cronometroBlack.reset();
		}
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
	