package entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import objmoveutils.Position;
import piece.Piece;

public class TravelingPiece {

	private static List<TravelingPiece> travelingPieces = new ArrayList<>();
	private static Map<Piece, TravelingPiece> travelingPiecesMap = new HashMap<>();
	
	private Piece piece;
	private double initialX;
	private double initialY;
	private double sourceX;
	private double sourceY;
	private double targetX;
	private double targetY;
	private double incX;
	private double incY;
	private int frames;
	private Boolean isActive;
	
	private TravelingPiece(Piece piece, double targetX, double targetY, int frames) {
		this.piece = piece;
		sourceX = initialX = piece.getPosition().getX() * 64;
		sourceY = initialY = piece.getPosition().getY() * 64;
		this.targetX = targetX * 64;
		this.targetY = targetY * 64;
		this.frames = frames;
		incX = (this.targetX - sourceX) / frames;
		incY = (this.targetY - sourceY) / frames;
		isActive = true;
	}
	
	public static void add(Piece piece, double targetX, double targetY, int frames) {
		TravelingPiece travelingPiece = new TravelingPiece(piece, targetX, targetY, frames);
		travelingPieces.add(travelingPiece);
		travelingPiecesMap.put(piece, travelingPiece);
	}
	
	public static void add(Piece piece, Position targetPosition, int frames)
		{ add(piece, targetPosition.getX(), targetPosition.getY(), frames); }

	public static List<TravelingPiece> getTravelingPieces()
		{ return travelingPieces; }
	
	public static TravelingPiece getInstance(Piece piece) {
		if (travelingPiecesMap.containsKey(piece))
			return travelingPiecesMap.get(piece);
		return null;
	}
	
	public static Boolean pieceIsTraveling(Piece piece)
		{ return travelingPiecesMap.containsKey(piece) && getInstance(piece).isActive(); }

	public static void runItOnEveryFrame() {
		for (int n = 0; n < travelingPieces.size(); n++) {
			while (n < travelingPieces.size() && !travelingPieces.get(n).isActive) {
				travelingPiecesMap.remove(travelingPieces.get(n).piece);
				travelingPieces.remove(n);
			}
			if (n < travelingPieces.size()) {
				TravelingPiece travelingPiece = travelingPieces.get(n);
				travelingPiece.sourceX += travelingPiece.incX;
				travelingPiece.sourceY += travelingPiece.incY;
				if (--travelingPiece.frames == 0)
					travelingPiece.isActive = false;
			}
		}
	}
	
	public Piece getPiece()
		{ return piece; }

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
	
	public static Boolean havePiecesTraveling() {
		for (TravelingPiece tp : travelingPieces)
			if (tp.isActive())
				return true;
		return false;
	}

	public static void clear() {
		travelingPiecesMap.clear();
		travelingPieces.clear();
	}

}