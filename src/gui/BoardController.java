package gui;

import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import application.Program;
import board.Board;
import entities.PieceImage;
import entities.TravelingPiece;
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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
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

	private static Boolean tryCatchOnConsole = true;
	private final static String CHESS_DEFAULT = "Chess_default";
	private final static String DEFAULT_STRING_BOARD = "RNBQKBNR,PPPPPPPP,........,........,........,........,pppppppp,rnbqkbnr";
	private final static String DEFAULT_STRING_CLEAR_BOARD = "...QK...,........,........,........,........,........,........,...qk...";

	private Boolean unknownError;
	private Boolean soundEnabled;
	private Boolean gameOver;
	private Boolean disabledControlsWhileIsCpuTurn;
	private Boolean justStarted;
	private long cpuPlay;
	private long clearMsg;
	private int cpuPlaySpeed;
	private int piecePngType;
	private int boardPngTypeA;
	private int boardPngTypeB;
	private int maxBoardsSprites;
	private int maxPieceSprites;
	private int movePieceDelay;
	private int linearFiltering;

	private Cronometro cronometroGame;
	private Cronometro cronometroBlack;
	private Cronometro cronometroWhite;
	private Piece hoveredPiece;
	private PieceColor cronoTurn;
	private PieceColor cpuColor;
	private Position mouseHoverPos;
	private ChessPlayMode chessPlayMode;
	private FPSHandler fpsHandler;
	private Board board;
	private List<KeyCode> pressedKeys;
	private IniFile configIniFile;
	private String loadedBoardName;
	private Character[][] loadedBoard;
	private PieceType editBoardPieceType;

	@FXML
	private VBox vBoxMainWindow;
	@FXML
	private VBox vBoxEditHelper;
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
  private Menu menuRenameBoard;
  @FXML
  private Menu menuRemoveBoard;
  @FXML
  private CheckMenuItem menuCheckItemHoverBlink;
  @FXML
  private CheckMenuItem menuCheckItemHoverLift;
  @FXML
  private CheckMenuItem menuCheckItemTransparent;
  @FXML
  private CheckMenuItem menuCheckItemLinearFilteringOff;
  @FXML
  private CheckMenuItem menuCheckItemLinearFilteringX1;
  @FXML
  private CheckMenuItem menuCheckItemLinearFilteringX2;
  @FXML
  private CheckMenuItem menuCheckItemLinearFilteringX3;
  @FXML
  private CheckMenuItem menuCheckItemSwapBoard;
  @FXML
  private Menu menuMovingPieceDelay;
  @FXML
  private MenuBar menuBar;
  @FXML
  private Menu menuLoadSpriteSetup;
  @FXML
  private MenuItem menuItemSaveSpriteSetup;
  @FXML
  private HBox hBoxUndoControls;
  @FXML
  private Text textCapturedPieces1;
  @FXML
  private Text textCapturedPieces2;
  @FXML
  private VBox vBoxChronometer1;
  @FXML
  private VBox vBoxChronometer2;
  @FXML
  private VBox vBoxChronometer3;
  @FXML
  private VBox vBoxTurns;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		editBoardPieceType = PieceType.PAWN;
		cronoTurn = null;
		unknownError = false;
		gameOver = true;
		justStarted = true;
		pressedKeys = new ArrayList<>();
		mouseHoverPos = new Position(0, 0);
		clearMsg = 0;
		fpsHandler = new FPSHandler(30, 0);
		configIniFile = IniFile.getNewIniFileInstance("./config.ini");
		vBoxEditHelper.setVisible(false);
		loadConfigsFromDisk();
		setLinearFiltering();
		ChessSprites.initialize();
		addMenus();
		addListeners();
		Controller.addIconToButton(buttonUndo, Icons.ICON_MOVEMAXLEFT.getValue(), 18, 18, 20);
		Controller.addIconToButton(buttonRedo, Icons.ICON_MOVEMAXRIGHT.getValue(), 18, 18, 20);
		imageViewBoardFrame.setImage(new Image("/sprites/boards/board frame.jpg"));
	}
	
	private void setLinearFiltering() {
		List<CheckMenuItem> list = Arrays.asList(menuCheckItemLinearFilteringOff,
			menuCheckItemLinearFilteringX1, menuCheckItemLinearFilteringX2, menuCheckItemLinearFilteringX3);
		for (int n = 0; n < list.size(); n++)
			if (list.get(n).isSelected())
				canvasBoard.getGraphicsContext2D().setEffect(new BoxBlur(1, 1, linearFiltering = n));
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
			if (menuCheckItemEditMode.isSelected()) {
				try {
					board.validateBoard();
					String name = loadedBoardName;
					Boolean ask = false;
					while (name.equals(CHESS_DEFAULT) && !ask) {
						name = Alerts.textPrompt("Text prompt", "Save board", null, "Enter the name for the new board");
						if (name == null) {
							name = CHESS_DEFAULT;
							ask = true;
						}
						else if (name.equals(CHESS_DEFAULT))
							error("You can't use this name");
						else if (configIniFile.read("SAVED_BOARDS", name) != null)
							error("This name is already in use for another board");
					}
					if (configIniFile.read("SAVED_BOARDS", name) ==  null || !ask ||
							Alerts.confirmation("Overwrite", "There already have a board with this name. Overwrite it?")) {
						configIniFile.write("SAVED_BOARDS", loadedBoardName = name, convertCurrentBoardToString());
						configIniFile.saveToDisk();
						msg("Board saved", Color.GREEN);
					}
				}
				catch (Exception ex) {
	    		playWav("error");
	    		Alerts.error("Error", "Error on board layout:\n", ex.getMessage());
				}
			}
			else {
				Boolean wasGameOver = gameOver;
				gameOver = true;
				if (Alerts.confirmation("Restart game", "Are you sure?"))
					resetGame();
				else if (!wasGameOver) {
					gameOver = false;
					boardTimer();
				}
			}
		});
		vBoxMainWindow.setOnKeyPressed(e -> keyHandler(e));
		vBoxMainWindow.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));
		canvasMovePiece.setOnMouseMoved(e ->
			canvasBoardMoved((int)(e.getY() / 64), (int)(e.getX() / 64)));
		canvasMovePiece.setOnMouseClicked(e -> {
			int button = e.getButton() == MouseButton.PRIMARY ? 0 :
				e.getButton() == MouseButton.SECONDARY ? 1 : 2;
	    canvasBoardClicked((int)(e.getY() / 64), (int)(e.getX() / 64), button);
	  });
	}
	
	private String convertCurrentBoardToString() {
		StringBuilder sb = new StringBuilder();
		for (Character[] chs : loadedBoard) {
			if (!sb.isEmpty())
				sb.append(",");
			for (Character c : chs)
				sb.append(c == null || c == ' ' ? '.' : c);
		}
		return sb.toString();
	}

	private void loadBoardFromString(String board) {
		loadedBoard = new Character[8][8];
		String[] split = board.split(",");
		for (int y = 0; y < 8; y++)
			for (int x = 0; x < 8; x++)
				loadedBoard[y][x] = split[y].charAt(x) == '.' ? null : split[y].charAt(x);
	}
	
	private void updateCheckMenuItensBasedOnLastSelected(List<CheckMenuItem> checkMenuItemList, Consumer<?> consumerWhenSelect) {
		for (int n = 0; n < checkMenuItemList.size(); n++) {
			final int x = n;
			final List<CheckMenuItem> list2 = new ArrayList<>(checkMenuItemList);
			checkMenuItemList.get(n).setOnAction(e -> {
				for (int n2 = 0; n2 < list2.size(); n2++)
					list2.get(n2).setSelected(n2 == x);
				if (consumerWhenSelect != null)
					consumerWhenSelect.accept(null);
			});
		}
	}

	private void updateCheckMenuItensBasedOnLastSelected(List<CheckMenuItem> checkMenuItemList)
		{ updateCheckMenuItensBasedOnLastSelected(checkMenuItemList, null); }

	private void addMenus() {
		updateCheckMenuItensBasedOnLastSelected(Arrays.asList(menuCheckItemLinearFilteringOff,
				menuCheckItemLinearFilteringX1, menuCheckItemLinearFilteringX2,
				menuCheckItemLinearFilteringX3), e -> setLinearFiltering());
		updateCheckMenuItensBasedOnLastSelected(Arrays.asList(menuCheckItemHoverBlink, menuCheckItemHoverLift, menuCheckItemTransparent));
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
		menuCheckItemSwapBoard.setOnAction(e -> board.swapSides());
		updateMenuSpriteSetup();
		setEditBoardMenu();
		menuItemSaveSpriteSetup.setOnAction(e -> {
			String name = Alerts.textPrompt("Save sprite setup", "Save sprite setup", null, "Type the name for save the current sprite\nsetup, which includes the both odd and\neven board sprites, and piece sprites");
			if (name != null) {
				Boolean newItem = configIniFile.read("BOARD_SPRITES_SETUP", name) == null;
				if (newItem || Alerts.confirmation("Confirmation", "There already exists a setup with this name. Overwrite it?")) {
					configIniFile.write("BOARD_SPRITES_SETUP", name, piecePngType + " " + boardPngTypeA + " " + boardPngTypeB);
					Alerts.information("Information", "Sprite setup successfully saved");
				}
				if (newItem)
					updateMenuSpriteSetup();
			}
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
					gameOver = true;
					menuGameMode.getItems().forEach(menu -> ((CheckMenuItem)menu).setSelected(false));
					checkMenuItem.setSelected(true);
					resetGame();
				}
				menuCpuSpeed.setDisable(gameMode == ChessPlayMode.PLAYER_VS_PLAYER);
			});
			menuGameMode.getItems().add(checkMenuItem);
		}
		updateCpuSpeedMenu();
	}
	
	private void setEditBoardMenu() {
		menuLoadBoard.getItems().clear();
		menuRenameBoard.getItems().clear();
		menuRemoveBoard.getItems().clear();
		menuItemCreateNewBoard.setOnAction(e -> {
			String name = null;
			Boolean ok = false;
			do {
				name = Alerts.textPrompt("Text prompt", "Create board", null, "Enter the name for the new board");
				if (name == null)
					return;
				name.replace(' ', '_');
				if (name.equals(CHESS_DEFAULT))
					error("You can't use this name");
				else if (configIniFile.read("SAVED_BOARDS", name) != null)
					error("This name is already in use for another board");
				else
					ok = true;
			}
			while (name != null && !ok);
			if (ok) {
				configIniFile.write("SAVED_BOARDS", name, DEFAULT_STRING_CLEAR_BOARD );
				configIniFile.saveToDisk();
				loadedBoardName = name;
				reloadBoard(loadedBoardName = name);
				setEditBoardMenu();
				updateBoard();
				Alerts.information("Information", "Board successfully created");
			}
		});
		menuCheckItemEditMode.setOnAction(e -> {
			for (Node node : Arrays.asList(textCapturedPieces1, textCapturedPieces2, vBoxChronometer1,
						vBoxChronometer2, vBoxChronometer3, vBoxTurns))
							node.setVisible(!menuCheckItemEditMode.isSelected());
			vBoxEditHelper.setVisible(menuCheckItemEditMode.isSelected());
			buttonResetGame.setText(menuCheckItemEditMode.isSelected() ? "Save board" : "Reset game");
			if (menuCheckItemEditMode.isSelected())
				boardTimer();
		});
		for (int n = -1; n < configIniFile.getItemList("SAVED_BOARDS").size(); n++) {
			String item = n == -1 ? CHESS_DEFAULT : configIniFile.getItemList("SAVED_BOARDS").get(n);
			CheckMenuItem checkMenuLoadBoard = new CheckMenuItem(item.replace('_', ' '));
			MenuItem checkMenuRenameBoard = new CheckMenuItem(item.replace('_', ' '));
			MenuItem checkMenuRemoveBoard = new CheckMenuItem(item.replace('_', ' '));
			final int x = n;
			checkMenuLoadBoard.setSelected(item.equals(loadedBoardName));
			checkMenuLoadBoard.setOnAction(ex -> {
				loadBoardFromString(x == -1 ? DEFAULT_STRING_BOARD : configIniFile.read("SAVED_BOARDS", item));
				reloadBoard(loadedBoardName = item);
				for (MenuItem menu : menuLoadBoard.getItems())
					((CheckMenuItem)menu).setSelected(menu.getText().equals(item.replace('_', ' ')));
				updateBoard();
			});
			menuLoadBoard.getItems().add(checkMenuLoadBoard);
			if (n > -1) {
				checkMenuRenameBoard.setOnAction(e -> {
					String newName = null;
					Boolean ok = false;
					do {
						newName = Alerts.textPrompt("Text prompt", "Rename board", item, "Enter the new name for the board");
						if (newName == null)
							return;
						newName.replace(' ', '_');
						if (newName.equals(CHESS_DEFAULT))
							error("You can't use this name");
						else if (configIniFile.read("SAVED_BOARDS", newName) != null)
							error("This name is already in use for another board");
						else
							ok = true;
					}
					while (newName != null && !ok);
					if (ok) {
						String m = configIniFile.read("SAVED_BOARDS", item);
						configIniFile.remove("SAVED_BOARDS", item);
						configIniFile.write("SAVED_BOARDS", newName, m);
						configIniFile.saveToDisk();
						Alerts.information("Information", "Board successfully renamed");
						setEditBoardMenu();
					}
				});
				checkMenuRemoveBoard.setOnAction(e -> {
					if (Alerts.confirmation("Confirmation", "Remove board", "Are you sure you want to remove\nthe board \"" + item + "\"?\nIt can't be undone")) {
						configIniFile.remove("SAVED_BOARDS", checkMenuRenameBoard.getText());
						Alerts.information("Information", "Board successfully removed");
						setEditBoardMenu();
					}
				});
				menuRenameBoard.getItems().add(checkMenuRenameBoard);
				menuRemoveBoard.getItems().add(checkMenuRemoveBoard);
				menuRenameBoard.setDisable(menuRenameBoard.getItems().isEmpty());
				menuRemoveBoard.setDisable(menuRemoveBoard.getItems().isEmpty());
			}
		}
		menuLoadBoard.getItems().sort((a, b) -> a.getText().replace(' ', '_').equals(CHESS_DEFAULT) ? -Integer.MAX_VALUE : a.getText().compareTo(b.getText()));
		menuRenameBoard.getItems().sort((a, b) -> a.getText().compareTo(b.getText()));
		menuRemoveBoard.getItems().sort((a, b) -> a.getText().compareTo(b.getText()));
	}

	private void updateMenuSpriteSetup() {
		menuLoadSpriteSetup.getItems().clear();
		for (String s : configIniFile.getItemList("BOARD_SPRITES_SETUP")) {
			MenuItem menuItem = new MenuItem(s);
			menuItem.setOnAction(e -> {
				String[] split = configIniFile.read("BOARD_SPRITES_SETUP", s).split(" ");
				setPieceSprite(Integer.parseInt(split[0]));
				boardPngTypeA = Integer.parseInt(split[1]);
				boardPngTypeB = Integer.parseInt(split[2]);
				updateBoard();
			});
			menuLoadSpriteSetup.getItems().add(menuItem);
		}
		menuLoadSpriteSetup.setDisable(menuLoadSpriteSetup.getItems().isEmpty());
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
		{ setPieceSprite(new SecureRandom().nextInt(maxPieceSprites)); }

	private void setRandomBoardTilesSprites() {
		boardPngTypeA = new SecureRandom().nextInt(maxBoardsSprites);
		do
			boardPngTypeB = new SecureRandom().nextInt(maxBoardsSprites);
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
		KeyCode keyCode = event.getCode();
  	pressedKeys.add(keyCode);
  	if (menuCheckItemEditMode.isSelected()) {
  		if (keyCode == KeyCode.Q)
  			editBoardPieceType = PieceType.QUEEN;
  		else if (keyCode == KeyCode.K)
  			editBoardPieceType = PieceType.KING;
  		else if (keyCode == KeyCode.R)
  			editBoardPieceType = PieceType.ROOK;
  		else if (keyCode == KeyCode.N)
  			editBoardPieceType = PieceType.KNIGHT;
  		else if (keyCode == KeyCode.B)
  			editBoardPieceType = PieceType.BISHOP;
  		else if (keyCode == KeyCode.P)
  			editBoardPieceType = PieceType.PAWN;
  		updatePiecePreview();
  	}
  	else if (keyCode == KeyCode.MULTIPLY) {
  		if (pressedKeys.contains(KeyCode.CONTROL))
  			setRandomBoardTilesSprites();
  		else
  			setRandomPiecesSprites();
  	}
  	else if (keyCode == KeyCode.UP || keyCode == KeyCode.DOWN) {
	  	int inc = keyCode == KeyCode.UP ? 1 : -1;
	  	if (pressedKeys.contains(KeyCode.SHIFT) || pressedKeys.contains(KeyCode.CONTROL)) {
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

	private void updatePiecePreview() {
		GraphicsContext gc = canvasTurn.getGraphicsContext2D();
		PieceImage pieceImage = ChessSprites.pieceImages.get(piecePngType);
		int[] p;
		if (menuCheckItemEditMode.isSelected())
			p = ChessSprites.getXYFromPieceInfo(editBoardPieceType, PieceColor.WHITE, piecePngType);
		else
			p = ChessSprites.getXYFromPieceInfo(PieceType.PAWN, board.getCurrentColorTurn(), piecePngType);
		gc.clearRect(0, 0, 48, 80);
		gc.strokeRect(0, 0, 48, 80);
		gc.drawImage(ChessSprites.getPieceImageSet(piecePngType), p[0], p[1], pieceImage.getSourceW(), pieceImage.getSourceH(), 0, 80 - pieceImage.getTargetH() * 0.7, pieceImage.getTargetW() * 0.7, pieceImage.getTargetH() * 0.7);
	}

	public void init() {
		board = new Board();
		resumirCronometro(null);
		resetGame();
	  boardTimer();
	}
	
	private void setTitle() {
		String title = "Chess Game (" + chessPlayMode.getName() + ")";
		if (chessPlayMode == ChessPlayMode.PLAYER_VS_CPU)
			title += " - Cpu play speed: " + cpuPlaySpeed + "ms";
		title += " - Piece Sprite: [" + (piecePngType + 1) + "] - Board Sprite: [" + (boardPngTypeA + 1) + " / " + (boardPngTypeB + 1) + "]";
		Program.getMainStage().setTitle(title);
	}
	
	private void reloadBoard(String boardName) {
		if (configIniFile.read("SAVED_BOARDS", boardName) == null)
			boardName = CHESS_DEFAULT;
		try {
			if (boardName.equals(CHESS_DEFAULT))
				loadBoardFromString(DEFAULT_STRING_BOARD);
			else
				loadBoardFromString(configIniFile.read("SAVED_BOARDS", boardName));
			board.reset();
			board.setBoard(loadedBoard);
		}		
		catch (Exception e) {}
	}
	
	private void resetGame() {
		msg("");
		setTitle();
		try {
			disabledControlsWhileIsCpuTurn = false;
			reloadBoard(loadedBoardName);
			if (!justStarted) {
				board.setPlayMode(chessPlayMode);
				board.validateBoard();
				if (chessPlayMode == ChessPlayMode.PLAYER_VS_CPU) {
					List<String> options = Arrays.asList("Black", "White");
					String choice = Alerts.choiceCombo("Select", "Select CPU color", "Select which color the CPU will play as", options);
					board.setCpuColor(cpuColor = choice.equals(options.get(1)) ? PieceColor.WHITE : PieceColor.BLACK);
				}
			}
			if (menuCheckItemSwapBoard.isSelected() != board.isSwappedBoard())
				board.swapSides();
		  updateBoard();
			buttonUndo.setDisable(true);
			buttonRedo.setDisable(true);
			resumirCronometro(null);
			hoveredPiece = null;
			cpuPlay = 0;
			gameOver = justStarted;
			if (!justStarted) {
				boardTimer();
				cpuPlay();
			}
			justStarted = false;
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
		Boolean disableControls = !gameOver && !menuCheckItemEditMode.isSelected() &&
				(TravelingPiece.havePiecesTraveling() || (!board.canRedoMove() && board.isCpuTurn()));
		if (disableControls != disabledControlsWhileIsCpuTurn) {
			hBoxUndoControls.setDisable(disableControls);
			menuBar.setDisable(disableControls);
			buttonResetGame.setDisable(disableControls);
			disabledControlsWhileIsCpuTurn = disableControls;
		}
		if (clearMsg != 0 && System.currentTimeMillis() >= clearMsg) {
			clearMsg = 0;
			msg("");
		}
		if (TravelingPiece.havePiecesTraveling()) {
			TravelingPiece.runItOnEveryFrame();
			canvasMovePiece.getGraphicsContext2D().clearRect(0, 0, 512, 576);
			for (TravelingPiece travelingPiece : TravelingPiece.getTravelingPieces())
				if (!travelingPiece.isActive()) {
					if (travelingPiece.getPiece() == board.getSelectedPiece())
						movePieceTo(travelingPiece.getTargetPosition());
				}
				else {
					PieceImage pieceImage = ChessSprites.pieceImages.get(piecePngType);
					int[] p = ChessSprites.getXYFromPieceInfo(travelingPiece.getPiece(), piecePngType);
					canvasMovePiece.getGraphicsContext2D().drawImage(ChessSprites.getPieceImageSet(piecePngType), p[0], p[1], pieceImage.getSourceW(), pieceImage.getSourceH(), travelingPiece.getSourceX() + 32 - pieceImage.getTargetW() / 2, travelingPiece.getSourceY() + 128 - pieceImage.getTargetH(), pieceImage.getTargetW(), pieceImage.getTargetH());
				}
			if (!TravelingPiece.havePiecesTraveling()) {
				canvasMovePiece.getGraphicsContext2D().clearRect(0, 0, 512, 576);
				fpsHandler.setCPS(30);
			  checkIfPieceIsPromoted();
			}
		}
		else
			updateBoard();
		textCronometroGame.setText(cronometroGame.getDuracaoStr().substring(0, 10));
		textCronometroBlack.setText(cronometroBlack.getDuracaoStr().substring(0, 10));
		textCronometroWhite.setText(cronometroWhite.getDuracaoStr().substring(0, 10));
		if (!unknownError && !gameOver && !board.canRedoMove() && board.isCpuTurn() &&
				!menuCheckItemEditMode.isSelected() && System.currentTimeMillis() >= cpuPlay) {
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
					error(e.getMessage());
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
		if (Program.windowIsOpen() && (menuCheckItemEditMode.isSelected() ||
				!gameOver || TravelingPiece.havePiecesTraveling())) {
					if (gameOver && TravelingPiece.havePiecesTraveling())
						TravelingPiece.runItOnEveryFrame();
					GameTools.callMethodAgain(e -> boardTimer());
		}
	}

	private void checkUndoButtons() {
		buttonRedo.setDisable(!board.canRedoMove());
		buttonUndo.setDisable(!board.canUndoMove());
	}
	
	private void error(String text) {
		msg(text, Color.RED);
		playWav("error");
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
		GraphicsContext gc = canvasBoard.getGraphicsContext2D();
		gc.setEffect(null);
		gc.clearRect(0, 0, 512, 576);
		gc.setEffect(new BoxBlur(1, 1, linearFiltering));
		if (board.pieceIsSelected())
			hoveredPiece = null;
		for (int y = 0; y < 8; y++)
			for (int x = 0; x < 8; x++)
				gc.drawImage(ChessSprites.boardTilesImages.get((x + y) % 2 == 0 ? boardPngTypeA : boardPngTypeB), x * 150, y * 150, 150, 150, x * 64, y * 64 + 64, 64, 64);
		for (int y = 0; y < 8; y++)
			for (int x = 0; x < 8; x++)
				drawTile(x, y);
		updatePiecePreview();
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
		GraphicsContext gc = canvasBoard.getGraphicsContext2D();
		Position pos = new Position(x, y);

		Color rectangleColor = null;
		Piece piece = board.getPieceAt(pos);
		Boolean justHovered = hoveredPiece != null;
		Piece selectedPiece = justHovered ? hoveredPiece : board.getSelectedPiece();

		if (!menuCheckItemEditMode.isSelected()) {
			if (!TravelingPiece.havePiecesTraveling())
				if (piece != null) {
					if (board.pieceCanDoEnPassant(selectedPiece) &&
							board.getEnPassantPawn() == piece)
								rectangleColor = Color.ORANGERED;
					if (board.isChecked() &&
							(piece.is(PieceType.KING, board.getCurrentColorTurn()) ||
							!board.getPieceListByColor(piece.getOpponentColor(),
								p -> p.isKing() && piece.couldCapture(p)).isEmpty()))
									rectangleColor = Color.ORANGE; // Marca o rei com retângulo laranja, se ele estiver em check. Também marca a pedra que está ameaçando o rei.
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
				gc.setStroke(rectangleColor);
				for (int n = 0; n < 5; n++)
					gc.strokeRect(x * 64 + n, y * 64 + 64 + n, 64 - n * 2, 64 - n * 2);
			}
		}

		Boolean blink = (fpsHandler.getElapsedFrames() / 2) % 2 == 0;
		if (piece != null && !TravelingPiece.pieceIsTraveling(piece)) {
					int[] p = ChessSprites.getXYFromPieceInfo(piece, piecePngType);
					PieceImage pieceImage = ChessSprites.pieceImages.get(piecePngType);
					Boolean ok = (piece == selectedPiece && (menuCheckItemEditMode.isSelected() || piece.getColor() == board.getCurrentColorTurn()));
					int lift = menuCheckItemHoverLift.isSelected() && ok ? 8 : 0;
					if (ok && (menuCheckItemTransparent.isSelected() || 
							(menuCheckItemHoverBlink.isSelected() && !blink)))
								gc.setGlobalAlpha(0.5);
					gc.drawImage(ChessSprites.getPieceImageSet(piecePngType), p[0], p[1], pieceImage.getSourceW(), pieceImage.getSourceH(), x * 64 + 32 - pieceImage.getTargetW() / 2, y * 64 + 128 - pieceImage.getTargetH() - lift, pieceImage.getTargetW(), pieceImage.getTargetH());
					gc.setGlobalAlpha(1);
		}
	}

	private void canvasBoardMoved(int row, int column) {
    if (menuCheckItemEditMode.isSelected() || (!TravelingPiece.havePiecesTraveling() &&
    		!unknownError && !board.isGameOver() && !gameOver && !board.isCpuTurn())) {
			  	Position position = new Position(column, row - 1);
					if (!mouseHoverPos.equals(position)) {
						mouseHoverPos.setPosition(position);
						boardMouseHover(board.getPieceAt(position), position);
					}
    }
	}

	private void canvasBoardClicked(int row, int column, int button) {
    if (menuCheckItemEditMode.isSelected() || (!TravelingPiece.havePiecesTraveling() &&
    		!unknownError && !board.isGameOver() && !board.isCpuTurn())) {
		    	Position position = new Position(column, row - 1);
		    	boardClick(board.getPieceAt(position), position, button);
    }		
	}

	private void boardMouseHover(Piece piece, Position pos) {
    if (!board.pieceIsSelected() && (hoveredPiece == null || piece != hoveredPiece)) {
					if (piece != null && (menuCheckItemEditMode.isSelected() || !piece.isStucked()))
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

	private void boardClick(Piece piece, Position pos, int button) {
		if (menuCheckItemEditMode.isSelected()) {
			if (board.getPieceAt(pos) != null) {
				loadedBoard[pos.getY()][pos.getX()] = null;
				board.removePiece(piece);
				editBoardPieceType = piece.getType();
				updatePiecePreview();
				playWav("unselect");
			}
			else {
				try {
					Piece p = board.addNewPiece(pos, editBoardPieceType, button == 0 ? PieceColor.WHITE : PieceColor.BLACK);
					loadedBoard[pos.getY()][pos.getX()] = button == 0 ? p.getType().getLet() : Character.toLowerCase(p.getType().getLet());
					playWav("move");
				}
				catch (Exception e) {
					playWav("error");
	    		Alerts.error("Error", "Error on board layout:\n", e.getMessage());
				}
			}

			updateBoard();
			return;
		}
  	if (board.isGameOver() || gameOver) {
    	error("Reset the game");
  		return;
  	}
  	msg("");
  	if (checkIfPieceIsPromoted(true))
  		return;
  	try {
    	if (board.pieceIsSelected()) {
    		if (board.getSelectedPiece().isSamePosition(pos))
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
  		error(ex.getMessage());
			if (tryCatchOnConsole) {
				System.err.println("Error on catch 004");
				ex.printStackTrace();
			}
		}
	  updateBoard();
	}

	private void startPieceTravel(Position targetPosition) {
		Piece rook = null;
		Position rookTargetPositon = null;
		Board b = board.newClonedBoard();
		try
			{ board.movePieceTo(targetPosition, false); }
		catch (Exception e) {}
		if (board.lastMoveWasCastling()) {
			rook = board.getLastCastlingPiece();
			rookTargetPositon = new Position(rook.getPosition());
		}
		Board.cloneBoard(b, board);
		if (rook != null)
			TravelingPiece.add(rook, rookTargetPositon, movePieceDelay);
		TravelingPiece.add(board.getSelectedPiece(), targetPosition, movePieceDelay);
		playWav("clicked");
		fpsHandler.setCPS(120);
		updateBoard();
	}

	private void movePieceTo(Position pos) {
		Boolean wasCheckedBefore = board.isChecked();
		try {
			if (!board.isCpuTurn()) {
				board.movePieceTo(pos);
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
			error(e.getMessage());
			if (tryCatchOnConsole) {
				System.err.println("Error on catch 006");
				e.printStackTrace();
			}
			return;
		}
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
		}
		cpuPlay();
		cpuPlay += cpuPlaySpeed;
	}
	
	private Boolean gameOver() {
		if (cronometroGame.getDuracao() == 0)
			return false;
		if (!board.isGameOver() || gameOver || TravelingPiece.havePiecesTraveling())
			return false;
		if (board.checkMate() || board.deadlyKissMate()) {
			Boolean won = chessPlayMode != ChessPlayMode.PLAYER_VS_CPU ||
				board.getWinnerColor() != board.getCpuColor();
			playWav(won ? "checkmate" : "loose");
			cronometroBlack.setPausado(true);
			cronometroWhite.setPausado(true);
			cronometroGame.setPausado(true);
			msg((!board.deadlyKissMate() ? "Checkmate! " : "Kiss of death mate! ") +
					(won ? board.getWinnerColor().name() + " won!" : "You loose"), Color.BLUE);
			gameOver = true;
		}
		else {
			System.out.println("board.isDrawByFiftyMoveRule(): " + board.isDrawByFiftyMoveRule());
			System.out.println("board.isDrawByStalemate(): " + board.isDrawByStalemate());
			System.out.println("board.isDrawByInsufficientMatingMaterial(): " + board.isDrawByInsufficientMatingMaterial());
			System.out.println("board.isDrawByBareKings(): " + board.isDrawByBareKings());
			System.out.println("board.isDrawByThreefoldRepetition(): " + board.isDrawByThreefoldRepetition());
			playWav("loose");
			cronometroBlack.setPausado(true);
			cronometroWhite.setPausado(true);
			cronometroGame.setPausado(true);
			if (board.isDrawByFiftyMoveRule())
				msg("Draw game (Fifty-move rule)", Color.RED);
			else if (board.isDrawByStalemate())
				msg("Draw game (Stalemate)", Color.RED);
			else if (board.isDrawByInsufficientMatingMaterial())
				msg("Draw game (Insufficient mating material)", Color.RED);
			else if (board.isDrawByBareKings())
				msg("Draw game (Bare Kings)", Color.RED);
			else if (board.isDrawByThreefoldRepetition())
				msg("Draw game (Threefold-repetition)", Color.RED);
			else
				msg("Draw game", Color.RED);
			gameOver = true;
			boardTimer();
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
			error(e.getMessage());
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
    			cpuPlay = System.currentTimeMillis() + (long)cpuPlaySpeed * 2;
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
			loadedBoardName = configIniFile.read("CONFIG", "loadedBoardName");
			movePieceDelay = Integer.parseInt(configIniFile.read("CONFIG", "movePieceDelay"));
			piecePngType = Integer.parseInt(configIniFile.read("CONFIG", "piecePngType"));
			boardPngTypeA = Integer.parseInt(configIniFile.read("CONFIG", "boardPngTypeA"));
			boardPngTypeB = Integer.parseInt(configIniFile.read("CONFIG", "boardPngTypeB"));
			cpuPlaySpeed = Integer.parseInt(configIniFile.read("CONFIG", "cpuPlaySpeed"));
			soundEnabled = Boolean.parseBoolean(configIniFile.read("CONFIG", "soundEnabled"));
			chessPlayMode = ChessPlayMode.valueOf(configIniFile.read("CONFIG", "chessPlayMode"));
			cpuColor = PieceColor.valueOf(configIniFile.read("CONFIG", "cpuColor"));
			menuCheckItemHoverBlink.setSelected(configIniFile.read("CONFIG", "hoverPieceMode").equals("1"));
			menuCheckItemHoverLift.setSelected(configIniFile.read("CONFIG", "hoverPieceMode").equals("2"));
			menuCheckItemTransparent.setSelected(configIniFile.read("CONFIG", "hoverPieceMode").equals("3"));
			menuCheckItemLinearFilteringOff.setSelected(configIniFile.read("CONFIG", "linearFiltering").equals("0"));
			menuCheckItemLinearFilteringX1.setSelected(configIniFile.read("CONFIG", "linearFiltering").equals("1"));
			menuCheckItemLinearFilteringX2.setSelected(configIniFile.read("CONFIG", "linearFiltering").equals("2"));
			menuCheckItemLinearFilteringX3.setSelected(configIniFile.read("CONFIG", "linearFiltering").equals("3"));
			menuCheckItemSwapBoard.setSelected(Boolean.parseBoolean(configIniFile.read("CONFIG", "swapColors")));
		}
		catch (Exception e) {
			loadedBoardName = CHESS_DEFAULT;
			movePieceDelay = 20;
			piecePngType = 0;
			boardPngTypeA = 0;
			boardPngTypeB = 0;
			cpuPlaySpeed = 1000;
			soundEnabled = true;
			chessPlayMode = ChessPlayMode.PLAYER_VS_PLAYER;
			cpuColor = PieceColor.BLACK;
			menuCheckItemHoverBlink.setSelected(true);
			menuCheckItemLinearFilteringX2.setSelected(true);
			if (tryCatchOnConsole) {
				System.err.println("Error on catch 009");
				e.printStackTrace();
			}
		}
	}

	public void saveConfigsToDisk() {
		try {
			configIniFile.write("CONFIG", "loadedBoardName", loadedBoardName);
			configIniFile.write("CONFIG", "piecePngType", "" + piecePngType);
			configIniFile.write("CONFIG", "boardPngTypeA", "" + boardPngTypeA);
			configIniFile.write("CONFIG", "boardPngTypeB", "" + boardPngTypeB);
			configIniFile.write("CONFIG", "cpuPlaySpeed", "" + cpuPlaySpeed);
			configIniFile.write("CONFIG", "soundEnabled", soundEnabled.toString());
			configIniFile.write("CONFIG", "swapColors", "" + menuCheckItemSwapBoard.isSelected());
			configIniFile.write("CONFIG", "chessPlayMode", chessPlayMode.name());
			configIniFile.write("CONFIG", "cpuColor", cpuColor.name());
			configIniFile.write("CONFIG", "movePieceDelay", "" + movePieceDelay);
			configIniFile.write("CONFIG", "hoverPieceMode", menuCheckItemHoverBlink.isSelected() ? "1" : menuCheckItemHoverLift.isSelected() ? "2" : "3");
			configIniFile.write("CONFIG", "linearFiltering", menuCheckItemLinearFilteringOff.isSelected() ? "0" :
				menuCheckItemLinearFilteringX1.isSelected() ? "1" :
				menuCheckItemLinearFilteringX2.isSelected() ? "2" : "3");
			configIniFile.saveToDisk();
		}
		catch (Exception e) {
			if (tryCatchOnConsole) {
				System.err.println("Error on catch 010");
				e.printStackTrace();
			}
		}
	}	

}