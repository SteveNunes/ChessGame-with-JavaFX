package pieces;

import java.util.ArrayList;
import java.util.List;

import board.Board;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import piece.Position;

public class King extends Piece  {

	public King(Board board, Position position, PieceColor color)
		{ super(board, position, PieceType.KING, color); }

	@Override
	public List<Position> getPossibleMoves() {
		List<Position> moves = new ArrayList<>();
		Position p = new Position(getPosition());
		int[][] inc = {
			{-1,-1,-1,0,0,1,1,1,0,0},
			{-1,0,1,-1,1,-1,0,1,1,1}
		};
		
		// Castling special move
		Position p2 = new Position(getPosition());
		for (int c = 0; c <= 7; c += 7) {
			p2.setColumn(c);
			Piece rook = getBoard().getPieceAt(p2);
			if (rook != null && getBoard().getSelectedPiece() == this && getBoard().checkIfCastlingIsPossible(this, rook))
				inc[1][c == 0 ? 8 : 9] = c == 0 ? -2 : 2;
		}
		
		/* 10 directions check (9th and 10th is for castling special move
		 * (these values will be changed if castling check pass, otherwise
		 * they will be just repeated directions))
		 */
		for (int dir = 0; dir < 10; dir++) {
			p.setValues(getPosition());
			p.incValues(inc[0][dir], inc[1][dir]);
			if (getBoard().isValidBoardPosition(p) &&
					(getBoard().isFreeSlot(p) || getBoard().isOpponentPiece(p, getColor())))
						moves.add(new Position(p));
		}
		return moves;
	}

	@Override
	public String toString()	
		{ return "K"; }

}