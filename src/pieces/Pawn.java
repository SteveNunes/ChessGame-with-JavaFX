package pieces;

import java.util.ArrayList;
import java.util.List;

import board.Board;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import piece.Position;

public class Pawn extends Piece {
	
	private Boolean enPassant;
	
	public Pawn(Board board, Position position, PieceColor color) {
		super(board, position, PieceType.PAWN, color);
		enPassant = false;
	}
	
	@Override
	public List<Position> getPossibleMoves() {
		List<Position> moves = new ArrayList<>();
		int inc = getColor() == PieceColor.WHITE ? 1 : -1;
		Position p = new Position(getPosition()), p2 = new Position(p);
		// Front check (1 or 2 steps further (2 if this piece was never moved before))
		for (int row = 1; row <= (wasMoved() ? 1 : 2); row++) {
			p.incValues(inc, 0);
			// Diagonal check for capture
			for (int i = -1; row == 1 && i <= 1; i += 2) {
				p2.setValues(p);
				p2.incValues(0, i);
				if (getBoard().isOpponentPiece(p2, getColor()))
					moves.add(new Position(p2));
				//En Passant special move
			}
			if (!getBoard().isValidBoardPosition(p) || !getBoard().isFreeSlot(p))
				break;
			moves.add(new Position(p));
		}
		if (getBoard().getSelectedPiece() == this && getBoard().checkEnPassant())
			moves.add(new Position(getBoard().getEnPassantCapturePosition()));
		return moves;
	}
	
	public Boolean getEnPassant()
		{ return enPassant; }
	
	public void setEnPassant(Boolean enPassant)
		{ this.enPassant = enPassant; }

	@Override
	public String toString()
		{ return "P"; }

}