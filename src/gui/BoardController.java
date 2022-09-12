package gui;

import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import application.Program;
import board.Board;
import entities.PieceImage;
import enums.ChessPlayMode;
import enums.Icons;
import enums.PieceColor;
import enums.PieceType;
import gameutil.FPSHandler;
import gameutil.GameTools;
import gameutil.Position;
import gui.util.Controller;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import piece.Piece;
import util.Alerts;
import util.ChessSprites;
import util.Cronometro;
import util.IniFile;
import util.Sounds;

public class BoardController implements Initializable {

	private Boolean unknownError;
	private Boolean soundEnabled;
	private Boolean blinkPiece;
	private Boolean tryCatchOnConsole = false;
	private Boolean gameOver;
	private Boolean boardTimerIsRunning;
	private Cronometro cronometroGame;
	private Cronometro cronometroBlack;
	private Cronometro cronometroWhite;
	private Piece hoveredPiece;
	private Piece travelingPiece;
	private PieceTravel pieceTravel;
	private PieceColor cronoTurn;
	private PieceColor cpuColor;
	private Position mouseHoverPos;
	private ChessPlayMode chessPlayMode;
	private FPSHandler fpsHandler;
	private Board board;
	private List<KeyCode> pressedKeys;

	private long cpuPlay;
	private long clearMsg;
	private int cpuPlaySpeed;
	private int piecePngType;
	private int boardPngTypeA;
	private int boardPngTypeB;
	private int maxBoardsSprites;
	private int maxPieceSprites;
	private int movePieceDelay;
	
	@FXML
	private VBox vBoxMainWindow;
  @FXML
  private Menu menuGameMode;
  @FXML
  private Menu menuCpuSpeed;
  @FXML
  private MenuItem menuItemRandomBoard;
  @FXML
  private CheckMenuItem checkMenuItemRandomPieceSprite;
  @FXML
  private Menu menuBoardEvenTilesSprite;
  @FXML
  private Menu menuBoardOddTilesSprite;
  @FXML
  private Menu menuPieceSprite;
  @FXML
  private CheckMenuItem menuCheckItemSound;
  @FXML
  private MenuItem menuItemCloseGame;
  @FXML
	private Canvas canvasBoard;
  @FXML
	private Canvas canvasMovePiece;
  @FXML
  private ImageView imageViewBoardFrame;
	@FXML
	private Canvas canvasTurn;
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
  private FlowPane flowPaneBlackCapturedPieces;
  @FXML
  private FlowPane flowPaneWhiteCapturedPieces;
  @FXML
  private CheckMenuItem menuCheckItemEditMode;
  @FXML
  private MenuItem menuItemCreateNewBoard;
  @FXML
  private Menu menuLoadBoard;
  @FXML
  private MenuItem menuItemSaveBoard;
  @FXML
  private CheckMenuItem menuCheckItemHoverBlink;
  @FXML
  private CheckMenuItem menuCheckItemHoverLift;
  @FXML
  private Menu menuMovingPieceDelay;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		cronoTurn = null;
		unknownError = false;
		blinkPiece = false;
		gameOver = false;
		boardTimerIsRunning = false;
		pressedKeys = new ArrayList<>();
		mouseHoverPos = new Position(0, 0);
		pieceTravel = new PieceTravel();
		travelingPiece = null;
		clearMsg = 0;
		fpsHandler = new FPSHandler(30, 0);
		loadConfigsFromDisk();
		
		ChessSprites.initialize();
		addMenus();
		addListeners();
		Controller.addIconToButton(buttonUndo, Icons.ICON_MOVEMAXLEFT.getValue(), 18, 18, 20);
		Controller.addIconToButton(buttonRedo, Icons.ICON_MOVEMAXRIGHT.getValue(), 18, 18, 20);
		imageViewBoardFrame.setImage(new Image("./sprites/boards/board frame.jpg"));
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

	private void addListeners() {
		buttonUndo.setOnAction(e -> {
			board.undoMove();
			updateBoard();
		});
		buttonRedo.setOnAction(e -> {
			board.redoMove();
			updateBoard();
		});
		buttonResetGame.setOnAction(e -> {
			if (Alerts.confirmation("Restart game", "Are you sure?"))
				resetGame();
		});
		vBoxMainWindow.setOnKeyPressed(e -> keyHandler(e));
		vBoxMainWindow.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));
		canvasMovePiece.setOnMouseMoved(e ->
			canvasBoardMoved((int)(e.getY() / 64), (int)(e.getX() / 64)));
		canvasMovePiece.setOnMouseClicked(e ->
	    canvasBoardClicked((int)(e.getY() / 64), (int)(e.getX() / 64)));
	}
	
	private void addMenus() {
		maxPieceSprites = 0;
		menuCheckItemHoverBlink.setOnAction(e ->
			menuCheckItemHoverLift.setSelected(menuCheckItemHoverBlink.isSelected()));
		menuCheckItemHoverLift.setOnAction(e ->
		menuCheckItemHoverBlink.setSelected(menuCheckItemHoverLift.isSelected()));
		for (int n = 10; n <= 60; n += 10) {
			CheckMenuItem checkMenuItem = new CheckMenuItem("" + n + " frames");
			final int x = n;
			checkMenuItem.setOnAction(e -> {
				movePieceDelay = x;
				for (int n2 = 0; n2 < menuMovingPieceDelay.getItems().size(); n2++)
					((CheckMenuItem) menuMovingPieceDelay.getItems().get(n2)).setSelected(movePieceDelay == n2);
			});
			checkMenuItem.setSelected(movePieceDelay == n);
			menuMovingPieceDelay.getItems().add(checkMenuItem);
		}
		ChessSprites.pieceImages.forEach(i -> {
			CheckMenuItem checkMenuItem = new CheckMenuItem();
			checkMenuItem.setSelected(maxPieceSprites == piecePngType);
			PieceImage pi = ChessSprites.pieceImages.get(maxPieceSprites);
			checkMenuItem.setGraphic(ChessSprites.getPieceImage(PieceType.PAWN, PieceColor.WHITE, maxPieceSprites, pi.getTargetW() / 2, pi.getTargetH() / 2));
			final int n = maxPieceSprites;
			checkMenuItem.setOnAction(e -> setPieceSprite(n));
			menuPieceSprite.getItems().add(checkMenuItem);
			maxPieceSprites++;
		});
		maxBoardsSprites = 0;
		ChessSprites.boardTilesImages.forEach(i -> {
			for (int x = 0; x < 2; x++) {
				final int xx = x;
				final int s = maxBoardsSprites;
				CheckMenuItem checkMenuItem = new CheckMenuItem();
				checkMenuItem.setSelected(maxBoardsSprites == (xx == 0 ? boardPngTypeA : boardPngTypeB));
				checkMenuItem.setOnAction(e -> {
					if (xx == 0) {
						boardPngTypeA = s;
						menuBoardOddTilesSprite.getItems().forEach(menu -> ((CheckMenuItem)menu).setSelected(false));
					}
					else {
						boardPngTypeB = s;
						menuBoardEvenTilesSprite.getItems().forEach(menu -> ((CheckMenuItem)menu).setSelected(false));
					}
					checkMenuItem.setSelected(true);
					updateBoard();
				});
				checkMenuItem.setGraphic(ChessSprites.getBoardTileImageView(maxBoardsSprites, xx, 0, 64, 64));
				if (x == 0)
					menuBoardOddTilesSprite.getItems().add(checkMenuItem);
				else
					menuBoardEvenTilesSprite.getItems().add(checkMenuItem);
			}
			maxBoardsSprites++;
		});
		menuItemRandomBoard.setOnAction(e -> setRandomBoardTilesSprites());
		checkMenuItemRandomPieceSprite.setOnAction(e -> setRandomPiecesSprites());
		menuCheckItemSound.setSelected(soundEnabled);
		menuCheckItemSound.setOnAction(e -> soundEnabled = !soundEnabled);
		menuItemCloseGame.setOnAction(e -> Program.getMainStage().close());
		for (ChessPlayMode gameMode : Arrays.asList(ChessPlayMode.PLAYER_VS_PLAYER, ChessPlayMode.PLAYER_VS_CPU, ChessPlayMode.CPU_VS_CPU)) {
			CheckMenuItem checkMenuItem = new CheckMenuItem(gameMode.getName());
			checkMenuItem.setSelected(chessPlayMode == gameMode);
			checkMenuItem.setOnAction(e -> {
				if (Alerts.confirmation("Confirmation", "Change game mode", "Are you sure you want to change the game mode?\nCurrent game will be lost!")) {
					chessPlayMode = gameMode;
					menuGameMode.getItems().forEach(menu -> ((CheckMenuItem)menu).setSelected(false));
					checkMenuItem.setSelected(true);
					resetGame();
					if (gameMode == ChessPlayMode.PLAYER_VS_CPU) {
						List<String> options = Arrays.asList("White", "Black");
						String choice = Alerts.choiceCombo("Select", "Select CPU color", "Select which color the CPU will play as", options);
						board.setCpuColor(cpuColor = choice.equals(options.get(0)) ? PieceColor.WHITE : PieceColor.BLACK);
					}
				}
				menuCpuSpeed.setDisable(gameMode == ChessPlayMode.PLAYER_VS_PLAYER);
			});
			menuGameMode.getItems().add(checkMenuItem);
		}
		updateCpuSpeedMenu();
	}
	
	private void setPieceSprite(int n) {
		canvasMovePiece.getGraphicsContext2D().clearRect(0, 0, 512, 576);
		piecePngType = n;
		updateBoard();
  	setTitle();
		for (int x = 0; x < maxPieceSprites; x++)
			((CheckMenuItem)menuPieceSprite.getItems().get(x + 1)).setSelected(x == piecePngType);
		checkMenuItemRandomPieceSprite.setSelected(false);
	}

	private void setRandomPiecesSprites()
		{ setPieceSprite(new SecureRandom().nextInt(maxPieceSprites + 1)); }

	private void setRandomBoardTilesSprites() {
		boardPngTypeA = new SecureRandom().nextInt(maxBoardsSprites + 1);
		do
			boardPngTypeB = new SecureRandom().nextInt(maxBoardsSprites + 1);
		while (boardPngTypeA == boardPngTypeB);
		for (int n = 0; n < maxBoardsSprites; n++) {
			((CheckMenuItem)menuBoardOddTilesSprite.getItems().get(n)).setSelected(boardPngTypeA == n);
			((CheckMenuItem)menuBoardEvenTilesSprite.getItems().get(n)).setSelected(boardPngTypeB == n);
		}
		updateBoard();
  	setTitle();
	}

	private void updateCpuSpeedMenu() {
		if (menuCpuSpeed.getItems().size() > 1)
			menuCpuSpeed.getItems().remove(1, menuCpuSpeed.getItems().size());
		for (int n = 0; n <= 5000; n += 500) {
			final int n2 = n;
			CheckMenuItem checkMenuItem = new CheckMenuItem(n + "ms");
			if (cpuPlaySpeed == n)
				checkMenuItem.setSelected(true);
			checkMenuItem.setOnAction(e -> {
				menuCpuSpeed.getItems().forEach(menu -> ((CheckMenuItem)menu).setSelected(false));
				cpuPlaySpeed = n2;
				checkMenuItem.setSelected(true);
				setTitle();
			});
			menuCpuSpeed.getItems().add(checkMenuItem);
		}
		menuCpuSpeed.setDisable(chessPlayMode == ChessPlayMode.PLAYER_VS_PLAYER);
	}

	public void keyHandler(KeyEvent event) {
  	pressedKeys.add(event.getCode());
	  if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
	  	int inc = event.getCode() == KeyCode.UP ? 1 : -1;
	  	if (event.getCode() == KeyCode.MULTIPLY) {
	  		if (pressedKeys.contains(KeyCode.CONTROL))
	  			setRandomBoardTilesSprites();
	  		else
	  			setRandomPiecesSprites();
	  	}
	  	else if (pressedKeys.contains(KeyCode.SHIFT) || pressedKeys.contains(KeyCode.CONTROL)) {
				int n = 0;
		  	if (pressedKeys.contains(KeyCode.SHIFT) && pressedKeys.contains(KeyCode.CONTROL)) {
		  		if ((piecePngType += inc) == maxPieceSprites)
		  			piecePngType = 0;
					else if (piecePngType == -1)
  					piecePngType = maxPieceSprites - 1;
		  		setPieceSprite(piecePngType);
		  	}
		  	else {
		  		if (pressedKeys.contains(KeyCode.CONTROL)) { 
			  		if ((boardPngTypeA += inc) == maxBoardsSprites)
							boardPngTypeA = 0;
						else if (boardPngTypeA == -1)
							boardPngTypeA = maxBoardsSprites - 1;
			  	}
			  	else if (pressedKeys.contains(KeyCode.SHIFT)) { 
			  		if ((boardPngTypeB += inc) == maxBoardsSprites)
							boardPngTypeB = 0;
						else if (boardPngTypeB == -1)
							boardPngTypeB = maxBoardsSprites - 1;
			  	}
					for (MenuItem checkMenuItem : menuBoardEvenTilesSprite.getItems())
						((CheckMenuItem)checkMenuItem).setSelected(n++ == boardPngTypeA);
		  	}
				updateBoard();
		  }
	  	else {
	  		if ((cpuPlaySpeed += inc * 500) > 5000)
	  			cpuPlaySpeed = 0;
	  		else if (cpuPlaySpeed < 0)
	  			cpuPlaySpeed = 5000;
	  		updateCpuSpeedMenu();
	  	}
	  	setTitle();
		};
	}

	public void init() {
		board = new Board();
		resumirCronometro(null);
	  boardTimer();
		resetGame();
	}
	
	private void setTitle() {
		String title = "Chess Game (" + chessPlayMode.getName() + ")";
		if (chessPlayMode == ChessPlayMode.PLAYER_VS_CPU)
			title += " - Cpu play speed: " + cpuPlaySpeed + "ms";
		title += " - Piece Sprite: [" + (piecePngType + 1) + "] - Board Sprite: [" + (boardPngTypeA + 1) + " / " + (boardPngTypeB + 1) + "]";
		Program.getMainStage().setTitle(title);
	}
	
	private void resetGame() {
		while (boardTimerIsRunning) {
			try
				{ Thread.sleep(100); }
			catch (Exception e) {}
		}
		msg("");
		setTitle();
		try {
			board.reset();
			setPiecesOnTheBoard();
			board.validateBoard();
			board.setPlayMode(chessPlayMode);
			if (chessPlayMode == ChessPlayMode.PLAYER_VS_CPU)
				board.setCpuColor(cpuColor);
		  updateBoard();
			buttonUndo.setDisable(true);
			buttonRedo.setDisable(true);
			resumirCronometro(null);
			hoveredPiece = null;
			cpuPlay = 0;
			cpuPlay();
			boardTimer();
		}
		catch (Exception e) {
			Program.getMainStage().close();
			Alerts.error("Erro", e.getMessage());
			if (tryCatchOnConsole) {
				System.err.println("Error on catch 001");
				e.printStackTrace();
			}
		}
	}

	private void cpuPlay() {
		if (board.isCpuTurn() && cpuPlay == 0)
			cpuPlay = System.currentTimeMillis() + (long)cpuPlaySpeed;
	  updateBoard();
	}

	private void boardTimer() {
		if (boardTimerIsRunning)
			return;
		boardTimerIsRunning = true;
		if (clearMsg != 0 && System.currentTimeMillis() >= clearMsg) {
			clearMsg = 0;
			msg("");
		}
		if (pieceTravel.isActive()) {
			if (!pieceTravel.incPos()) {
				canvasMovePiece.getGraphicsContext2D().clearRect(0, 0, 512, 576);
				movePieceTo(pieceTravel.getTargetPosition());
				fpsHandler.setCPS(30);
			}
			else {
				PieceImage pieceImage = ChessSprites.pieceImages.get(piecePngType);
				int[] p = ChessSprites.getXYFromPieceInfo(travelingPiece, piecePngType);
				canvasMovePiece.getGraphicsContext2D().clearRect(0, 0, 512, 576);
				canvasMovePiece.getGraphicsContext2D().drawImage(ChessSprites.getPieceImageSet(piecePngType), p[0], p[1], pieceImage.getSourceW(), pieceImage.getSourceH(), pieceTravel.getSourceX() + 32 - pieceImage.getTargetW() / 2, pieceTravel.getSourceY() + 128 - pieceImage.getTargetH(), pieceImage.getTargetW(), pieceImage.getTargetH());
			}
		}
		else
			updateBoard();
		textCronometroGame.setText(cronometroGame.getDuracaoStr().substring(0, 10));
		textCronometroBlack.setText(cronometroBlack.getDuracaoStr().substring(0, 10));
		textCronometroWhite.setText(cronometroWhite.getDuracaoStr().substring(0, 10));
		if (!unknownError && !board.isGameOver() && board.isCpuTurn() && System.currentTimeMillis() >= cpuPlay) {
			if (board.getChessAI().cpuSelectedAPiece()) {
  			cpuPlay += 100000;
  			startPieceTravel(board.getChessAI().cpuSelectedTargetPosition());
			}
			else {
    		if (cronoTurn == null)
    			resumirCronometro(cronoTurn = board.getCurrentColorTurn());
      	msg("");
				playWav("select");
				try {
					board.getChessAI().doCpuSelectAPiece();
					updateBoard();
					cpuPlay = System.currentTimeMillis() + (long)cpuPlaySpeed;
				}
				catch (Exception e) {
	    		playWav("error");
					msg(e.getMessage(), Color.RED);
					if (tryCatchOnConsole) {
						System.err.println("Error on catch 002");
						e.printStackTrace();
					}
					cronometroBlack.setPausado(true);
					cronometroWhite.setPausado(true);
					cronometroGame.setPausado(true);
					unknownError = true;
				}
			}
		}
		fpsHandler.fpsCounter();
		if (Program.windowIsOpen() && !gameOver)
			GameTools.callMethodAgain(e -> boardTimer());
		boardTimerIsRunning = false;
	}

	private void checkUndoButtons() {
		buttonRedo.setDisable(!board.canRedoMove());
		buttonUndo.setDisable(!board.canUndoMove());
	}

	private void msg(String text, Color color) {
		Text txt = new Text(text);
		txt.setFont(Font.font("Lucida Console", 20));
		GraphicsContext gc = canvasMovePiece.getGraphicsContext2D();
		gc.clearRect(0, 0, 512, 576);
		if (!text.isEmpty()) {
			gc.setFill(Color.BLUE);
			gc.fillRect(canvasMovePiece.getWidth() / 2 - txt.getLayoutBounds().getWidth() / 2 - 13, 10, txt.getLayoutBounds().getWidth() + 26, 38);
			gc.setFill(Color.YELLOW);
			gc.fillRect(canvasMovePiece.getWidth() / 2 - txt.getLayoutBounds().getWidth() / 2 - 10, 13, txt.getLayoutBounds().getWidth() + 20, 32);
		}
    gc.setFill(color);
    gc.setTextAlign(TextAlignment.CENTER);
    gc.setFont(Font.font("Lucida Console", 20));
    gc.fillText(text, canvasMovePiece.getWidth() / 2, 35);
    clearMsg = System.currentTimeMillis() + 3000;
	}
	
	private void msg(String text)
		{ msg(text, Color.BLACK); }
	
	private void updateBoard() {
		canvasBoard.getGraphicsContext2D().clearRect(0, 0, 512, 576);
		if (board.pieceIsSelected())
			hoveredPiece = null;
		for (int y = 0; y < 8; y++)
			for (int x = 0; x < 8; x++)
				canvasBoard.getGraphicsContext2D().drawImage(ChessSprites.boardTilesImages.get((x + y) % 2 == 0 ? boardPngTypeA : boardPngTypeB), x * 150, y * 150, 150, 150, x * 64, y * 64 + 64, 64, 64);
		for (int y = 0; y < 8; y++)
			for (int x = 0; x < 8; x++)
				drawTile(x, y);

		PieceImage pieceImage = ChessSprites.pieceImages.get(piecePngType);
		int[] p = ChessSprites.getXYFromPieceInfo(PieceType.PAWN, board.getCurrentColorTurn(), piecePngType);
		canvasTurn.getGraphicsContext2D().clearRect(0, 0, 48, 80);
		canvasTurn.getGraphicsContext2D().strokeRect(0, 0, 48, 80);
		canvasTurn.getGraphicsContext2D().drawImage(ChessSprites.getPieceImageSet(piecePngType), p[0], p[1], pieceImage.getSourceW(), pieceImage.getSourceH(), 0, 80 - pieceImage.getTargetH() * 0.7, pieceImage.getTargetW() * 0.7, pieceImage.getTargetH() * 0.7);

		checkUndoButtons();
		updateCapturedPieces();
		textTurn.setText("" + board.getTurns());
		gameOver();
	}
	
	private void updateCapturedPieces() {
		flowPaneWhiteCapturedPieces.getChildren().clear();
		int w = ChessSprites.pieceImages.get(piecePngType).getTargetW();
		int h = ChessSprites.pieceImages.get(piecePngType).getTargetH();
		for (Piece piece : board.sortPieceListByPieceValue(board.getCapturedBlackPieces()))
			flowPaneWhiteCapturedPieces.getChildren().add(ChessSprites.getPieceImage(piece, piecePngType, w / 2, h / 2));
		flowPaneBlackCapturedPieces.getChildren().clear();
		for (Piece piece : board.sortPieceListByPieceValue(board.getCapturedWhitePieces()))
			flowPaneBlackCapturedPieces.getChildren().add(ChessSprites.getPieceImage(piece, piecePngType, w / 2, h / 2));
	}
	
	private void playWav(String wav) {
		if (soundEnabled)
			try
				{ Sounds.playWav("./src/sounds/" + wav + ".wav"); }
			catch (Exception e) {
				if (tryCatchOnConsole) {
					System.err.println("Error on catch 003");
					e.printStackTrace();
				}
			}
	}
	
	private void drawTile(int x, int y) {
		Position pos = new Position(x, y);

		Color rectangleColor = null;
		Piece piece = board.getPieceAt(pos);
		Boolean justHovered = hoveredPiece != null;
		Piece selectedPiece = justHovered ? hoveredPiece : board.getSelectedPiece();

		if (!pieceTravel.isActive())
			if (piece != null && (!pieceTravel.isActive() || piece != travelingPiece)) {
				if (board.pieceCanDoEnPassant(selectedPiece) &&
						board.getEnPassantPawn() == piece)
							rectangleColor = Color.ORANGERED;
				if (board.isChecked() &&
						piece.getColor() == board.getCurrentColorTurn() &&
						piece.getType() == PieceType.KING)
							rectangleColor = Color.ORANGE; // Marca o rei com retângulo rosa, se ele estiver em check
			}
			if (board.pieceIsSelected()) { 
				if (selectedPiece.equals(piece)) // Marca com retângulo amarelo a pedra selecionada atualmente
					rectangleColor = Color.YELLOW;
				else if ((!board.isCpuTurn() && pos.equals(mouseHoverPos)) ||
					(board.isCpuTurn() && board.pieceIsSelected() &&
					pos.equals(board.getChessAI().cpuSelectedTargetPosition())))
						rectangleColor = Color.RED;
				else if (selectedPiece.canMoveToPosition(pos)) // Marca com retângulo verde a casa onde a pedra selecionada pode ir (Se for casa onde houver uma pedra adversária, marca em vermelho)
					rectangleColor = !board.isCpuTurn() && board.getPieceAt(pos) != null ? Color.RED : Color.LIGHTGREEN;
			}
		
		if (rectangleColor != null) {
			GraphicsContext gc = canvasBoard.getGraphicsContext2D();
			gc.setStroke(rectangleColor);
			for (int n = 0; n < 5; n++)
				gc.strokeRect(x * 64 + n, y * 64 + 64 + n, 64 - n * 2, 64 - n * 2);
		}

		if (menuCheckItemHoverBlink.isSelected() && piece != null && selectedPiece == piece)
			blinkPiece = (fpsHandler.getElapsedFrames() / 2) % 2 == 0;
		if (piece != null && (!pieceTravel.isActive() || piece != travelingPiece) &&
				(selectedPiece != piece || menuCheckItemHoverLift.isSelected() || piece.getColor() != board.getCurrentColorTurn() || !blinkPiece)) {
					int[] p = ChessSprites.getXYFromPieceInfo(piece, piecePngType);
					PieceImage pieceImage = ChessSprites.pieceImages.get(piecePngType);
					int lift = menuCheckItemHoverLift.isSelected() && piece == selectedPiece && piece.getColor() == board.getCurrentColorTurn() ? 8 : 0;
					canvasBoard.getGraphicsContext2D().drawImage(ChessSprites.getPieceImageSet(piecePngType), p[0], p[1], pieceImage.getSourceW(), pieceImage.getSourceH(), x * 64 + 32 - pieceImage.getTargetW() / 2, y * 64 + 128 - pieceImage.getTargetH() - lift, pieceImage.getTargetW(), pieceImage.getTargetH());
		}
	}
	
	private void canvasBoardMoved(int row, int column) {
    if (!pieceTravel.isActive() && !unknownError && !board.isGameOver() && !board.isCpuTurn()) {
	  	Position position = new Position(column, row - 1);
			if (!mouseHoverPos.equals(position)) {
				mouseHoverPos.setPosition(position);
				boardMouseHover(board.getPieceAt(position), position);
			}
    }
	}

	private void canvasBoardClicked(int row, int column) {
    if (!pieceTravel.isActive() && !unknownError && !board.isGameOver() && !board.isCpuTurn()) {
    	Position position = new Position(column, row - 1);
    	boardClick(board.getPieceAt(position), position);
    }		
	}

	private void boardMouseHover(Piece piece, Position pos) {
    if (!board.pieceIsSelected() && (hoveredPiece == null || piece != hoveredPiece)) {
			if (piece != null)
  			hoveredPiece = piece;
			else
				hoveredPiece = null;
			updateBoard();
    }
    if (mouseHoverPos == null || !mouseHoverPos.equals(pos)) {
			mouseHoverPos = new Position(pos);
			updateBoard();
    }
	}

	private void boardClick(Piece piece, Position pos) {
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
    		else if (!board.checkIfCanMovePieceTo(pos))
    			board.movePieceTo(pos);
    		else 
    			startPieceTravel(pos);
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
			if (tryCatchOnConsole) {
				System.err.println("Error on catch 004");
				ex.printStackTrace();
			}
		}
	  updateBoard();
	  checkIfPieceIsPromoted();
	}

	private void startPieceTravel(Position targetPosition) {
		travelingPiece = board.getSelectedPiece();
		pieceTravel.setTravel(travelingPiece.getPosition(), targetPosition, movePieceDelay);
		playWav("clicked");
		fpsHandler.setCPS(120);
		updateBoard();
	}

	private void movePieceTo(Position pos) {
		if (pieceTravel.isActive())
			return;
		Boolean wasCheckedBefore = board.isChecked();
		try {
			if (!board.isCpuTurn()) {
				board.movePieceTo(pieceTravel.getTargetPosition());
				updateBoard();
			}
			else {
				board.getChessAI().doCpuMoveSelectedPiece();
				if (board.pawnWasPromoted()) {
					try
						{ board.promotePawnTo(PieceType.QUEEN); }
					catch (Exception e) {
						if (tryCatchOnConsole) {
							System.err.println("Error on catch 005");
							e.printStackTrace();
						}
					}
	    		playWav("promotion");
	  			cpuPlay = System.currentTimeMillis() + (long)cpuPlaySpeed;
					updateBoard();
				}
			}
		}
		catch (Exception e) {
  		playWav("error");
			msg(e.getMessage(), Color.RED);
			if (tryCatchOnConsole) {
				System.err.println("Error on catch 006");
				e.printStackTrace();
			}
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
		if (cronometroGame.getDuracao() == 0)
			return false;
		if (!board.isGameOver())
			return false;
		if (board.checkMate()) {
			Boolean won = chessPlayMode != ChessPlayMode.PLAYER_VS_CPU ||
				board.getWinnerColor() != board.getCpuColor();
			playWav(won ? "checkmate" : "loose");
			cronometroBlack.setPausado(true);
			cronometroWhite.setPausado(true);
			cronometroGame.setPausado(true);
			msg("Checkmate! " + (won ? board.getWinnerColor().name() + " won!" : "You loose"), Color.BLUE);
			gameOver = true;
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
			gameOver = true;
		}
		return true;
	}

	private void pieceWasUnselected(Position position) {
		try {
			board.cancelSelection();
			if (position != null)
				board.selectPiece(position);
			playWav(position != null ? "select" : "unselect");
			boardMouseHover(board.getPieceAt(mouseHoverPos), mouseHoverPos);
		}
		catch (Exception e) {
  		playWav("error");
			msg(e.getMessage(), Color.RED);
			if (tryCatchOnConsole) {
				System.err.println("Error on catch 007");
				e.printStackTrace();
			}
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
			Button button = new Button();
			button.setGraphic(ChessSprites.getPieceImage(type, board.getCurrentColorTurn(), piecePngType, 42, 42));
			button.setOnAction(e -> {
				try
					{ board.promotePawnTo(type); }
				catch (Exception ex) {
					if (tryCatchOnConsole) {
						System.err.println("Error on catch 008");
						ex.printStackTrace();
					}
				}
				stage.close();
    		playWav("promotion");
    		if (board.isCpuTurn())
    			cpuPlay = System.currentTimeMillis() + (long)cpuPlaySpeed;
				updateBoard();
			});
			hBox.getChildren().add(button);
		}
		stage.initOwner(canvasBoard.getScene().getWindow());
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setTitle("Promotion");
		stage.showAndWait();
	}
	
	public void loadConfigsFromDisk() {
		try {
			IniFile ini = IniFile.getNewIniFileInstance("./config.ini");
			movePieceDelay = Integer.parseInt(ini.read("CONFIG", "movePieceDelay"));
			boardPngTypeA = Integer.parseInt(ini.read("CONFIG", "boardPngTypeA"));
			boardPngTypeB = Integer.parseInt(ini.read("CONFIG", "boardPngTypeB"));
			cpuPlaySpeed = Integer.parseInt(ini.read("CONFIG", "cpuPlaySpeed"));
			soundEnabled = Boolean.parseBoolean(ini.read("CONFIG", "soundEnabled"));
			chessPlayMode = ChessPlayMode.valueOf(ini.read("CONFIG", "chessPlayMode"));
			cpuColor = PieceColor.valueOf(ini.read("CONFIG", "cpuColor"));
			menuCheckItemHoverBlink.setSelected(ini.read("CONFIG", "hoverPieceMode").equals("1"));
			menuCheckItemHoverLift.setSelected(ini.read("CONFIG", "hoverPieceMode").equals("2"));
		}
		catch (Exception e) {
			movePieceDelay = 20;
			piecePngType = 0;
			boardPngTypeA = 0;
			boardPngTypeB = 0;
			cpuPlaySpeed = 1000;
			soundEnabled = true;
			chessPlayMode = ChessPlayMode.PLAYER_VS_PLAYER;
			cpuColor = PieceColor.BLACK;
			menuCheckItemHoverBlink.setSelected(true);
			if (tryCatchOnConsole) {
				System.err.println("Error on catch 009");
				e.printStackTrace();
			}
		}
	}

	public void saveConfigsToDisk() {
		try {
			IniFile ini = IniFile.getNewIniFileInstance("./config.ini");
			ini.write("CONFIG", "piecePngType", "" + piecePngType);
			ini.write("CONFIG", "boardPngTypeA", "" + boardPngTypeA);
			ini.write("CONFIG", "boardPngTypeB", "" + boardPngTypeB);
			ini.write("CONFIG", "cpuPlaySpeed", "" + cpuPlaySpeed);
			ini.write("CONFIG", "soundEnabled", "" + soundEnabled);
			ini.write("CONFIG", "chessPlayMode", chessPlayMode.name());
			ini.write("CONFIG", "cpuColor", cpuColor.name());
			ini.write("CONFIG", "movePieceDelay", "" + movePieceDelay);
			ini.write("CONFIG", "hoverPieceMode", menuCheckItemHoverBlink.isSelected() ? "1" : "2");
			ini.saveToDisk();
		}
		catch (Exception e) {
			if (tryCatchOnConsole) {
				System.err.println("Error on catch 010");
				e.printStackTrace();
			}
		}
	}	

}
	
class PieceTravel {

	private Boolean isActive;
	private double initialX;
	private double initialY;
	private double sourceX;
	private double sourceY;
	private double targetX;
	private double targetY;
	private double incX;
	private double incY;
	private int frames;
	
	public PieceTravel() {
		setTravel(0, 0, 0, 0, 0);
		isActive = false;
	}
	
	public void setTravel(double sourceX, double sourceY, double targetX, double targetY, int frames) {
		this.sourceX = initialX = sourceX;
		this.sourceY = initialY = sourceY;
		this.targetX = targetX;
		this.targetY = targetY;
		this.frames = frames;
		incX = (targetX - sourceX) / frames;
		incY = (targetY - sourceY) / frames;
		isActive = true;
	}
	
	public void setTravel(Position sourcePosition, Position targetPosition, int frames)
		{ setTravel(sourcePosition.getX() * 64, sourcePosition.getY() * 64, targetPosition.getX() * 64, targetPosition.getY() * 64, frames); }
	
	public Boolean incPos() {
		sourceX += incX;
		sourceY += incY;
		if (--frames == 0)
			isActive = false;
		return isActive;
	}

	public double getInitialX()
		{ return initialX; }
	
	public double getInitialY()
		{ return initialY; }
	
	public double getSourceX()
		{ return sourceX; }

	public double getSourceY()
		{ return sourceY; }

	public double getTargetX()
		{ return targetX; }	
	
	public double getTargetY()
		{ return targetY; }	

	public Position getSourcePosition()
		{ return new Position((int)initialX / 64, (int)initialY / 64); }
	
	public Position getCurrentPosition()
		{ return new Position((int)sourceX / 64, (int)sourceY / 64); }
	
	public Position getTargetPosition()
		{ return new Position((int)targetX / 64, (int)targetY / 64); }
	
	public Boolean isActive()
		{ return isActive; }

}