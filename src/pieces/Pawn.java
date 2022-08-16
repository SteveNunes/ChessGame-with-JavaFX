package pieces;

import java.util.ArrayList;
import java.util.List;

import board.Board;
import piece.*;

public class Pawn extends Piece {
	
	public Pawn(Board board, Position position, Color color)
		{ super(board, position, Type.PAWN, color); }
	
	@Override
	public List<Position> possibleMoves() {
		List<Position> moves = new ArrayList<>();
		int inc = getColor() == Color.WHITE ? 1 : -1;
		Position p = new Position(getPosition()), p2 = new Position(p);
		// Front check (1 or 2 steps further (2 if this piece was never moved before))
		for (int row = 1; row <= (wasMoved() ? 1 : 2); row++) {
			p.incValues(inc, 0);
			// Diagonal check for capture
			for (int i = -1; row == 1 && i <= 1; i += 2) {
				p2.setValues(p);
				p2.incValues(0, i);
				if (getBoard().isOpponentPiece(p2, getColor())) moves.add(new Position(p2));
				//En Passant special move
				p2.incValues(-inc, 0);
				if (getBoard().thereHavePiece(p2) &&
					getBoard().getPieceAtPosition(p2) == getBoard().getEnPassantPiece())
						moves.add(new Position(p2.getRow() + inc, p2.getColumn()));
			}
			if (!getBoard().isValidBoardPosition(p) || getBoard().thereHavePiece(p)) break;
			moves.add(new Position(p));
		}
		return moves;
	}

	@Override
	public String toString() { return "P"; }

}