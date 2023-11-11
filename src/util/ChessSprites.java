package util;

import java.util.ArrayList;
import java.util.List;

import entities.PieceImage;
import enums.PieceColor;
import enums.PieceType;
import gui.util.ImageUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import piece.Piece;

public class ChessSprites {
	
	public static List<PieceImage> pieceImages;
	public static List<Image> boardTilesImages;
	private static Boolean initialized = false;
	private static Image moved;
	private static Image enPassant;

	public static void initialize() {
		if (!initialized) {
			pieceImages = new ArrayList<>();
			FindFile.findFile("Sprites\\pieces","*.png").forEach(file -> {
				final int n = Integer.parseInt(file.getName().replace(".png", ""));
				IniFile ini = IniFile.getNewIniFileInstance(file.getParent() + "\\config.ini");
				String[] split = ini.read("CONFIG", "" + n).split(" ");
				Image image = new Image("file:" + file.getAbsolutePath());
				Color color = Color.valueOf(split[0]);
				int toleranceThreshold = Integer.parseInt(split[1]);
				int sourceW = Integer.parseInt(split[2]);
				int sourceH = Integer.parseInt(split[3]);
				int targetW = Integer.parseInt(split[4]);
				int targetH = Integer.parseInt(split[5]);
				image = ImageUtils.removeBgColor(image, color, toleranceThreshold);
				while (pieceImages.size() < n)
					pieceImages.add(new PieceImage());
				pieceImages.set(n - 1, new PieceImage(image, color, toleranceThreshold, sourceW, sourceH, targetW, targetH, file.getAbsolutePath()));
			});
			boardTilesImages = new ArrayList<>();
			FindFile.findFile("Sprites\\boards","*.png").forEach(file ->
				boardTilesImages.add(new Image("file:" + file.getAbsolutePath())));
			initialized = true;
			moved = ImageUtils.removeBgColor(new Image("file:Sprites\\moved.png"), Color.WHITE, 10);
			enPassant = new Image("file:Sprites\\enpassant.png");
		}
	}
	
	public static Image getMovedIcon()
		{ return moved; }
	
	public static Image getEnPassantIcon()
		{ return enPassant; }

	private static Image canvasToImage(Canvas canvas) {
    WritableImage writableImage = new WritableImage((int)canvas.getWidth(), (int)canvas.getHeight());
   	return canvas.snapshot(new SnapshotParameters(), writableImage);
	}

	public static int[] getXYFromPieceInfo(PieceType type, PieceColor color, int pieceType) {
		PieceType[] types = {PieceType.KING, PieceType.QUEEN, PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.PAWN}; 
		int y = color == PieceColor.BLACK ? 0 : pieceImages.get(pieceType).getSourceH(), x = 0;
		for (; types[x] != type; x++);
		return new int[] {x * pieceImages.get(pieceType).getSourceW(), y};
	}
	
	public static Image getPieceImageSet(int pieceType)
		{ return pieceImages.get(pieceType).getImage(); }
	
	public static int[] getXYFromPieceInfo(Piece piece, int pieceType)
		{ return getXYFromPieceInfo(piece.getType(), piece.getColor(), pieceType); }
	
	public static Canvas getPieceImage(PieceType type, PieceColor color, int pieceType, int width, int height) {
		initialize();
		Canvas canvas = new Canvas(width, height);
		PieceImage pi = pieceImages.get(pieceType);
		int[] pos = getXYFromPieceInfo(type, color, pieceType);
		canvas.getGraphicsContext2D().drawImage(pi.getImage(), pos[0], pos[1], pi.getSourceW(), pi.getSourceH(), 0, 0, width, height);
		return canvas;
	}

	public static Canvas getPieceImage(Piece piece, int pieceType, int width, int height)
		{ return getPieceImage(piece.getType(), piece.getColor(), pieceType, width, height); }

	public static Image getBoardTileImage(int boardType, int row, int column, int width, int height) {
		initialize();
		Canvas canvas = new Canvas(width, height);
		canvas.getGraphicsContext2D().drawImage(boardTilesImages.get(boardType), column * 150, row * 150, 150, 150, 0, 0, width, height);
		return canvasToImage(canvas);
	}

	public static ImageView getBoardTileImageView(int boardType, int row, int column, int width, int height)
		{ return imageViewWithAnImage(getBoardTileImage(boardType, row, column, width, height)); }

	private static ImageView imageViewWithAnImage(Image image) {
		ImageView imageView = new ImageView(image);
		imageView.resize(image.getWidth(), image.getHeight());
		return imageView;
	}
	
}