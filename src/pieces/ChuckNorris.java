package pieces;

import java.util.ArrayList;
import java.util.List;

import board.Board;
import piece.*;

public class ChuckNorris extends Piece  {
	
	public ChuckNorris(Board board, Position position, PieceColor color)
		{ super(board, position, PieceType.CHUCKNORRIS, color); }
	
	@Override
	public List<Position> getPossibleMoves() {
		List<Position> moves = new ArrayList<>();
		for (int y = 0; y < 8; y++)
			for (int x = 0; x < 8; x++)
				moves.add(new Position(y,x));
		return moves;
	}
	
	@Override
	public String toString()
		{ return "C"; }
	
}