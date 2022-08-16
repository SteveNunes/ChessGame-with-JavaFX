package pieces;

import java.util.ArrayList;
import java.util.List;

import board.Board;
import piece.*;

public class King extends Piece  {

	public King(Board board, Position position, Color color)
		{ super(board, position, Type.KING, color); }

	@Override
	public List<Position> possibleMoves() {
		List<Position> moves = new ArrayList<>();
		Position p = new Position(getPosition());
		int[][] inc = {
			{-1,-1,-1,0,0,1,1,1,0,0},
			{-1,0,1,-1,1,-1,0,1,1,1}
		};
		// Castling special move
		for (int col = 0, i = 1; !wasMoved() && col <= 7; col += 7, i = -1) {
			p.setValues(getRow(), col);
			if (getBoard().thereHavePiece(p) &&
				!getBoard().isOpponentPiece(p, getColor()) &&
				getBoard().getPieceAtPosition(p) instanceof Rook) {
					p.incValues(0, i);
					while (!p.equals(getPosition())) {
						if (getBoard().thereHavePiece(p)) break;
						p.incValues(0, i);
					}
					if (p.equals(getPosition()))
						inc[1][col == 0 ? 8 : 9] = col == 0 ? -2 : 2;
			}
		}
		/* 10 directions check (9th and 10th is for castling special move
		 * (these values will be changed if castling check pass, otherwise
		 * they will be just repeated directions))
		 */
		for (int dir = 0; dir < 10; dir++) {
			p.setValues(getPosition());
			p.incValues(inc[0][dir], inc[1][dir]);
			if (getBoard().isValidBoardPosition(p) &&
				(!getBoard().thereHavePiece(p) || getBoard().isOpponentPiece(p, getColor())))
					moves.add(new Position(p));
		}
		return moves;
	}

	@Override
	public String toString() { return "K"; }

}