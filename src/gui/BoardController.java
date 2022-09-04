package gui;

import java.net.URL;
import java.security.SecureRandom;
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
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
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
import util.FindFile;
import util.MyFiles;
import util.Sounds;

public class BoardController implements Initializable {

	private Boolean unknownError;
	private Boolean soundEnabled;
	private Cronometro cronometroGame;
	private Cronometro cronometroBlack;
	private Cronometro cronometroWhite;
	private Piece hoveredPiece;
	private PieceColor cronoTurn;
	private PiecePosition mouseHoverPos;
	private ChessPlayMode chessPlayMode;
	private Board board;
	private Image boardImageA;
	private Image boardImageB;
	private Bounds bounds;
	private Image[][] pieces;
	private long cpuPlay;
	private int cpuPlaySpeed;
	private int piecePngType;
	private int boardPngTypeA;
	private int boardPngTypeB;
	private Color pieceTransparentColor;
	
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

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		cronoTurn = null;
		unknownError = false;
		piecePngType = 1;
		boardPngTypeA = 1;
		boardPngTypeB = 1;
		cpuPlaySpeed = 1000;
		soundEnabled = true;
		chessPlayMode = ChessPlayMode.PLAYER_VS_PLAYER;
		FindFile.findDir("./src/sprites/pieces/","*").forEach(file -> {
			final int n = Integer.parseInt(file.getName().replace("Type", ""));
			CheckMenuItem checkMenuItem = new CheckMenuItem("" + n);
			checkMenuItem.setSelected(n == piecePngType);
			checkMenuItem.setOnAction(e -> {
				piecePngType = n;
				reloadPiecesImage();
				updateBoard();
				menuPieceSprite.getItems().forEach(menu -> ((CheckMenuItem)menu).setSelected(false));
				checkMenuItem.setSelected(true);
			});
			menuPieceSprite.getItems().add(checkMenuItem);
		});
		FindFile.findFile("./src/sprites/boards/","*").forEach(file -> {
			for (int x = 0; x < 2; x++) {
				final int n = Integer.parseInt(file.getName().replace("board", "").replace(".png", ""));
				final int xx = x;
				CheckMenuItem checkMenuItem = new CheckMenuItem("" + n);
				checkMenuItem.setSelected(n == (xx == 0 ? boardPngTypeA : boardPngTypeB));
				checkMenuItem.setOnAction(e -> {
					if (xx == 0) {
						boardPngTypeA = n;
						menuBoardOddTilesSprite.getItems().forEach(menu -> ((CheckMenuItem)menu).setSelected(false));
					}
					else {
						boardPngTypeB = n;
						menuBoardEvenTilesSprite.getItems().forEach(menu -> ((CheckMenuItem)menu).setSelected(false));
					}
					checkMenuItem.setSelected(true);
					reloadBoardImages();
					updateBoard();
				});
				if (x == 0)
					menuBoardOddTilesSprite.getItems().add(checkMenuItem);
				else
					menuBoardEvenTilesSprite.getItems().add(checkMenuItem);
			}
		});
		menuItemRandomBoard.setOnAction(e -> {
			int itemSize = menuBoardEvenTilesSprite.getItems().size();
			boardPngTypeA = new SecureRandom().nextInt(itemSize) + 1;
			do
				boardPngTypeB = new SecureRandom().nextInt(itemSize) + 1;
			while (boardPngTypeA == boardPngTypeB);
			for (int n = 0; n < itemSize; n++) {
				((CheckMenuItem)menuBoardOddTilesSprite.getItems().get(n)).setSelected(boardPngTypeA == n + 1);
				((CheckMenuItem)menuBoardEvenTilesSprite.getItems().get(n)).setSelected(boardPngTypeB == n + 1);
			}
			reloadBoardImages();
			updateBoard();
		});
		checkMenuItemRandomPieceSprite.setOnAction(e -> {
			int itemSize = menuPieceSprite.getItems().size();
			piecePngType = new SecureRandom().nextInt(itemSize) + 1;
			for (int n = 1; n < itemSize; n++)
				((CheckMenuItem)menuPieceSprite.getItems().get(n)).setSelected(piecePngType == n);
			reloadPiecesImage();
			updateBoard();
			checkMenuItemRandomPieceSprite.setSelected(false);
		});
		menuCheckItemSound.setSelected(soundEnabled);
		menuCheckItemSound.setOnAction(e -> soundEnabled = !soundEnabled);
		menuItemCloseGame.setOnAction(e -> Program.getMainStage().close());
		reloadPiecesImage();
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
		for (ChessPlayMode gameMode : Arrays.asList(ChessPlayMode.PLAYER_VS_PLAYER, ChessPlayMode.PLAYER_VS_CPU, ChessPlayMode.CPU_VS_CPU)) {
			CheckMenuItem checkMenuItem = new CheckMenuItem(gameMode.getName());
			checkMenuItem.setSelected(chessPlayMode == gameMode);
			checkMenuItem.setOnAction(e -> {
				if (Alerts.confirmation("Confirmation", "Change game mode", "Are you sure you want to change the game mode?\nCurrent game will be lost!")) {
					board.setPlayMode(chessPlayMode = gameMode);
					if (gameMode == ChessPlayMode.PLAYER_VS_CPU) {
						List<String> options = Arrays.asList("White", "Black");
						String choice = Alerts.choiceCombo("Select", "Select CPU color", "Select which color the CPU will play as", options);
						board.setCpuColor(choice.equals(options.get(0)) ? PieceColor.WHITE : PieceColor.BLACK);
					}
					menuGameMode.getItems().forEach(menu -> ((CheckMenuItem)menu).setSelected(false));
					checkMenuItem.setSelected(true);
					resetGame();
				}
			});
			menuGameMode.getItems().add(checkMenuItem);
		}
		for (int n = 0; n <= 5000; n += 500) {
			final int n2 = n;
			CheckMenuItem checkMenuItem = new CheckMenuItem(n + "ms");
			if (cpuPlaySpeed == n)
				checkMenuItem.setSelected(true);
			checkMenuItem.setOnAction(e -> {
				menuCpuSpeed.getItems().forEach(menu -> ((CheckMenuItem)menu).setSelected(false));
				cpuPlaySpeed = n2;
				checkMenuItem.setSelected(true);
			});
			menuCpuSpeed.getItems().add(checkMenuItem);
		}
	}
	
	private void reloadPiecesImage() {
		String fileNameTransp = "./src/sprites/pieces/Type" + piecePngType + "/TRANSPARENT";
		String[] m;
		try
			{ m = MyFiles.readAllLinesFromFile(fileNameTransp).get(0).split(" "); }
		catch (Exception e)
			{ throw new RuntimeException("Erro ao ler arquivo \"" + fileNameTransp + "\""); }
		pieceTransparentColor = Color.valueOf(m[0]);
		List<PieceType> types = PieceType.getListOfAll();
		List<PieceColor> colors = PieceColor.getListOfAll();
		pieces = new Image[2][types.size()];
		for (int c = 0; c < colors.size(); c++)
			for (int t = 0; t < types.size(); t++) {
				String fileName = "/sprites/pieces/Type" + piecePngType + "/" + colors.get(c) + "_" + types.get(t) + ".png";
				try
					{ pieces[c][t] = Controller.removeBgColor(new Image(fileName), pieceTransparentColor, Integer.parseInt(m[1])); }
				catch (Exception e)
					{ throw new RuntimeException("Erro ao ler arquivo \"" + fileName + "\""); }
			}
	}
	
	private void reloadBoardImages() {
		boardImageA = new Image("/sprites/boards/board" + (boardPngTypeA < 10 ? "0" : "") + boardPngTypeA + ".png");
		boardImageB = new Image("/sprites/boards/board" + (boardPngTypeB < 10 ? "0" : "") + boardPngTypeB + ".png");
	}
	
	public void init() {
		reloadBoardImages();
	  bounds = gridPaneBoard.getCellBounds(0, 0);
		board = new Board();
	  initTimer();
		resetGame();
	}
	
	private void resetGame() {
		msg("");
		Program.getMainStage().setTitle("Chess Game (" + chessPlayMode.getName() + ")");
		try {
			board.reset();
			setPiecesOnTheBoard();
			board.validateBoard();
			board.setPlayMode(chessPlayMode);
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
			e.printStackTrace();
		}
	}

	private void cpuPlay() {
		if (board.isCpuTurn() && cpuPlay == 0)
			cpuPlay = System.currentTimeMillis() + (long)cpuPlaySpeed;
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
	  			cpuPlay = System.currentTimeMillis() + (long)cpuPlaySpeed;
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
					cpuPlay = System.currentTimeMillis() + (long)cpuPlaySpeed;
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
		int n = 1;
		if (n == 1)
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
		else
			board.setBoard(new Character[][] {
				{'k',' ',' ',' ',' ',' ',' ',' '},
				{' ',' ',' ','R',' ',' ',' ',' '},
				{' ',' ','R',' ',' ',' ',' ',' '},
				{' ',' ',' ',' ',' ',' ',' ',' '},
				{' ',' ',' ',' ',' ',' ',' ',' '},
				{' ',' ',' ',' ',' ',' ',' ',' '},
				{' ','r',' ',' ',' ',' ','P','P'},
				{' ',' ',' ',' ',' ',' ','P','K'}
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
		if (soundEnabled)
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
    canvas.getGraphicsContext2D().drawImage((x % 2 == 0 && y % 2 == 0) || (x % 2 > 0 && y % 2 > 0) ? boardImageA : boardImageB, x * 150, y * 150, 150, 150, 0, 0, width, height);

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
			Boolean won = chessPlayMode != ChessPlayMode.PLAYER_VS_CPU ||
				board.getWinnerColor() != board.getCpuColor();
			playWav(won ? "checkmate" : "loose");
			cronometroBlack.setPausado(true);
			cronometroWhite.setPausado(true);
			cronometroGame.setPausado(true);
			msg("Checkmate! " + (won ? board.getWinnerColor().name() + " won!" : "You loose"), Color.BLUE);
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
    		if (board.isCpuTurn())
    			cpuPlay = System.currentTimeMillis() + (long)cpuPlaySpeed;
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
	