package gui;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import application.Program;
import board.Board;
import enums.ChessPlayMode;
import enums.Icons;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollBar;
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

	private Boolean unknownError;
	private Cronometro cronometroGame;
	private Cronometro cronometroBlack;
	private Cronometro cronometroWhite;
	private Piece hoveredPiece;
	private PieceColor cronoTurn;
	private PiecePosition mouseHoverPos;
	private Board board;
	private Image boardImage;
	private Bounds bounds;
	private Image[][] pieces;
	private long cpuPlay;
	
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
  private Text textTurn;
  @FXML
  private Text textCpuSpeed;
  @FXML
  private FlowPane flowPaneBlackCapturedPieces;
  @FXML
  private FlowPane flowPaneWhiteCapturedPieces;
  @FXML
  private VBox vBoxCpuSpeed;
  @FXML
  private ScrollBar scrollBarCpuSpeed;
  @FXML
  private CheckBox checkBoxSound;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		List<PieceType> types = PieceType.getListOfAll();
		List<PieceColor> colors = PieceColor.getListOfAll();
		pieces = new Image[2][types.size()];
		cronoTurn = null;
		unknownError = false;
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
			if (Alerts.confirmation("Restart game", "Are you sure?"))
				resetGame();
		});
		checkBoxSound.setSelected(true);
	}
	
	public void init() {
		boardImage = new Image("/sprites/board.png");
	  bounds = gridPaneBoard.getCellBounds(0, 0);
		board = new Board(PieceColor.BLACK);
	  initTimer();
		resetGame();
	}
	
	private void resetGame() {
		msg("");
		List<String> opcoes = Arrays.asList("Player Vs. Player", "Player Vs. CPU", "CPU Vs. CPU");
		String opcao = Alerts.choiceCombo("Chess Game with JavaFX", "Game mode", "Select the game mode", opcoes);
		board = new Board();
		if (opcao == null || opcao.equals(opcoes.get(0)))
			board.setPlayMode(ChessPlayMode.PLAYER_VS_PLAYER);
		else if (opcao.equals(opcoes.get(1)))
			board.setPlayMode(ChessPlayMode.PLAYER_VS_CPU);
		else
			board.setPlayMode(ChessPlayMode.CPU_VS_CPU);
		vBoxCpuSpeed.setVisible(board.getPlayMode() != ChessPlayMode.PLAYER_VS_PLAYER);
		vBoxCpuSpeed.setDisable(board.getPlayMode() == ChessPlayMode.PLAYER_VS_PLAYER);
		Program.getMainStage().setTitle("Chess Game (" + opcao + ")");
		try {
			setPiecesOnTheBoard();
			board.validateBoard();
		  updateBoard();
			buttonUndo.setDisable(true);
			buttonRedo.setDisable(true);
			resumirCronometro(null);
			hoveredPiece = null;
			cpuPlay = 0;
			cpuPlay();
		}
		catch (Exception e) {
			Program.getMainStage().close();
			Alerts.error("Erro", e.getMessage());
		}
	}

	private void cpuPlay() {
		if (board.isCpuTurn() && cpuPlay == 0)
			cpuPlay = System.currentTimeMillis() + (long)scrollBarCpuSpeed.getValue();
	  updateBoard();
	}

	private void initTimer() {
		resumirCronometro(null);
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(50), e -> boardTimer()));
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
		boardTimer();
	}

	private void boardTimer() {
		textCpuSpeed.setText("" + (int)scrollBarCpuSpeed.getValue());
		textCronometroGame.setText(cronometroGame.getDuracaoStr());
		textCronometroBlack.setText(cronometroBlack.getDuracaoStr());
		textCronometroWhite.setText(cronometroWhite.getDuracaoStr());
		if (!unknownError && !board.isGameOver() && board.isCpuTurn() && cpuPlay != 0 && System.currentTimeMillis() >= cpuPlay) {
			if (board.getChessAI().cpuSelectedAPiece()) {
				movedPieceTo();
				if (board.pawnWasPromoted()) {
					try
						{ board.promotePawnTo(PieceType.QUEEN); }
					catch (Exception e) {}
	    		playWav("promotion");
					cpuPlay += scrollBarCpuSpeed.getValue();
					updateBoard();
				}
			}
			else {
    		if (cronoTurn == null)
    			resumirCronometro(cronoTurn = board.getCurrentColorTurn());
      	msg("");
				playWav("select");
				try {
					board.getChessAI().doCpuSelectAPiece();
					updateBoard();
					cpuPlay += scrollBarCpuSpeed.getValue();
				}
				catch (Exception e) {
	    		playWav("error");
					msg(e.getMessage(), Color.RED);
					e.printStackTrace();
					cronometroBlack.setPausado(true);
					cronometroWhite.setPausado(true);
					cronometroGame.setPausado(true);
					unknownError = true;
				}
			}
		}
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
		if (board.pieceIsSelected())
			hoveredPiece = null;
		gridPaneBoard.getChildren().clear();
		for (int n = 0; n < 64; n++)
			drawTile(n);
		imageViewTurn.setImage(getPieceImage(new Pawn(null, null, board.getCurrentColorTurn())));
		checkUndoButtons();
		flowPaneWhiteCapturedPieces.getChildren().clear();
		for (Piece piece : board.sortPieceListByPieceValue(board.getCapturedBlackPieces())) {
			ImageView imageView = new ImageView(getPieceImage(piece));
			imageView.setFitWidth(32);
			imageView.setFitHeight(32);
			flowPaneWhiteCapturedPieces.getChildren().add(imageView);
		}
		flowPaneBlackCapturedPieces.getChildren().clear();
		for (Piece piece : board.sortPieceListByPieceValue(board.getCapturedWhitePieces())) {
			ImageView imageView = new ImageView(getPieceImage(piece));
			imageView.setFitWidth(32);
			imageView.setFitHeight(32);
			flowPaneBlackCapturedPieces.getChildren().add(imageView);
		}
		textTurn.setText("" + board.getTurns());
		gameOver();
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

	private void setPiecesOnTheBoard() throws Exception {
		board.setBoard(new Character[][] {
			{'r','n','b','q','k','b','n','r'},
			{'p','p','p','p','p','p','p','p'},
			{' ',' ',' ',' ',' ',' ',' ',' '},
			{' ',' ',' ',' ',' ',' ',' ',' '},
			{' ',' ',' ',' ',' ',' ',' ',' '},
			{' ',' ',' ',' ',' ',' ',' ',' '},
			{'P','P','P','P','P','P','P','P'},
			{'R','N','B','Q','K','B','N','R'}
		});
	}
	
	private int getBoardWidth()
		{ return (int)bounds.getWidth(); }
	
	private int getBoardHeight()
		{ return (int)bounds.getHeight(); }
	
	private Rectangle newRectangle(Color color) {
		int width = getBoardWidth();
		int height = getBoardHeight();
		Rectangle rectangle = new Rectangle(3, 3, width - 6 , height - 6);
		rectangle.setFill(Color.TRANSPARENT);
		rectangle.setStroke(color);
		rectangle.setStrokeWidth(6);
		return rectangle;
	}
	
	private void playWav(String wav) {
		if (checkBoxSound.isSelected())
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
		Boolean justHovered = hoveredPiece != null;
		Piece selectedPiece = justHovered ? hoveredPiece : board.getSelectedPiece();
		Canvas canvas = new Canvas(width, height);
    canvas.getGraphicsContext2D().drawImage(boardImage, x * 150, y * 150, 150, 150, 0, 0, width, height);

		if (piece != null) {
			canvas.getGraphicsContext2D().drawImage(getPieceImage(piece), 0, 0, 250, 250, 0, 0, width, height);
			if (board.pieceCanDoEnPassant(selectedPiece) &&
					board.getEnPassantPawn() == piece)
						rectangle = newRectangle(justHovered ? Color.ORANGE : Color.ORANGERED);

			if (board.isChecked() &&
					piece.getColor() == board.getCurrentColorTurn() &&
					piece.getType() == PieceType.KING)
						rectangle = newRectangle(Color.PINK); // Marca o rei com retângulo rosa, se ele estiver em check
		}
		
		if ((justHovered && hoveredPiece.isSameColorOf(board.getCurrentColorTurn())) || board.pieceIsSelected()) { 
			if (selectedPiece.equals(piece)) // Marca com retângulo amarelo a pedra selecionada atualmente
				rectangle = newRectangle(justHovered ? Color.ANTIQUEWHITE : Color.YELLOW);
			else if ((!board.isCpuTurn() && pos.equals(mouseHoverPos)) ||
				(board.isCpuTurn() && board.pieceIsSelected() &&
				pos.equals(board.getChessAI().cpuSelectedTargetPosition())))
					rectangle = newRectangle(Color.RED);
			else if (selectedPiece.canMoveToPosition(pos)) // Marca com retângulo verde a casa onde a pedra selecionada pode ir (Se for casa onde houver uma pedra adversária, marca em vermelho)
				rectangle = newRectangle(!board.isCpuTurn() && board.getPieceAt(pos) != null ? 
						(justHovered ? Color.INDIANRED : Color.RED) :
						(justHovered ? Color.LIGHTBLUE : Color.LIGHTGREEN));
		}
		
		Pane pane = new Pane(canvas);
		if (rectangle != null)
      pane.getChildren().add(rectangle);
    gridPaneBoard.add(pane, x, y);

    if (!unknownError && !board.isGameOver() && !board.isCpuTurn()) {
	    pane.setOnMouseClicked(e -> boardClick(piece, pos));
	  	pane.hoverProperty().addListener((obs, wasHover, isHover) -> boardMouseHover(piece, pos, wasHover, isHover));
    }
	}
	
	private void boardMouseHover(Piece piece, PiecePosition pos, Boolean wasHover, Boolean isHover) {
		if (isHover) {
	    if (!board.pieceIsSelected() && (hoveredPiece == null || piece != hoveredPiece)) {
  			if (piece != null)
	  			hoveredPiece = piece;
  			else
  				hoveredPiece = null;
  			updateBoard();
	    }
	    if (mouseHoverPos == null || !mouseHoverPos.equals(pos)) {
  			mouseHoverPos = new PiecePosition(pos);
  			updateBoard();
	    }
		}
	}

	private void boardClick(Piece piece, PiecePosition pos) {
  	if (board.isGameOver())
  		return;
  	msg("");
  	if (checkIfPieceIsPromoted(true))
  		return;
  		
  	try {
    	if (board.pieceIsSelected()) {
    		if (board.getSelectedPiece().getPosition().equals(pos))
    			pieceWasUnselected();
    		else if (board.getPieceAt(pos) != null && board.getSelectedPiece().isSameColorOf(board.getPieceAt(pos)))
    			pieceWasUnselected(pos);
    		else
    			movedPieceTo(pos);
    	}
    	else {
     		board.selectPiece(pos);
    		playWav("select");
    		if (cronoTurn == null)
    			resumirCronometro(cronoTurn = board.getCurrentColorTurn());
    	}
  	}
		catch (Exception ex) {
  		playWav("error");
  		msg(ex.getMessage(), Color.RED);
		}
	  updateBoard();
	  checkIfPieceIsPromoted();
	}

	private void movedPieceTo(PiecePosition pos) {
		Boolean wasCheckedBefore = board.isChecked();
		try {
			if (!board.isCpuTurn())
				board.movePieceTo(pos);
			else {
				board.getChessAI().doCpuMoveSelectedPiece();
				updateBoard();
			}
		}
		catch (Exception e) {
  		playWav("error");
			msg(e.getMessage(), Color.RED);
			return;
		}
		cpuPlay = 0;
		cpuPlay();
		playWav(board.pieceWasCaptured() ? "capture" : "move");
		if (!gameOver()) {
			if (!wasCheckedBefore && board.isChecked()) {
				msg(board.getCurrentColorTurn().name() + " is checked!", Color.RED);
				playWav("check");
			}
			else if (board.lastMoveWasCastling()) {
				playWav("castling");
				msg(board.getOpponentColor().name() + " performed a \"castling\"", Color.GREEN);
			}
			else if (board.lastMoveWasEnPassant())
				msg(board.getOpponentColor().name() + " performed an \"En Passant\"", Color.GREEN);
			if (cronoTurn != null && cronoTurn != board.getCurrentColorTurn()) {
				resumirCronometro(board.getCurrentColorTurn());
				cronoTurn = null;
			}
			cpuPlay();
		}
	}
	
	private Boolean gameOver() {
		if (!board.isGameOver())
			return false;
		if (board.checkMate()) {
			playWav("checkmate");
			cronometroBlack.setPausado(true);
			cronometroWhite.setPausado(true);
			cronometroGame.setPausado(true);
			msg("Checkmate! " + board.getWinnerColor().name() + " won!", Color.BLUE);
		}
		else {
			playWav("loose");
			cronometroBlack.setPausado(true);
			cronometroWhite.setPausado(true);
			cronometroGame.setPausado(true);
			if (board.isDrawByStalemate())
				msg("Draw game (Stalemate)", Color.RED);
			else if (board.isDrawByBareKings())
				msg("Draw game (Bare Kings)", Color.RED);
			else if (board.isDrawByThreefoldRepetition())
				msg("Draw game (Threefold-repetition)", Color.RED);
			else
				msg("Draw game", Color.RED);
		}
		return true;
	}

	private void movedPieceTo()
		{ movedPieceTo(null); }

	private void pieceWasUnselected(PiecePosition position) {
		try {
			board.cancelSelection();
			if (position != null)
				board.selectPiece(position);
			playWav(position != null ? "select" : "unselect");
		}
		catch (Exception e) {
  		playWav("error");
			msg(e.getMessage(), Color.RED);
		}
	}
	
	private void pieceWasUnselected()
		{ pieceWasUnselected(null); }

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
  	if (board.pawnWasPromoted()) {
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
				try
					{ board.promotePawnTo(type); }
				catch (Exception ex) {}
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
	