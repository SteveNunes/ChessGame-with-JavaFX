package piece;

import java.util.List;

import board.Board;

public abstract class Piece {
	
	private PieceType type;
	private Board board;
	private PieceColor color;
	private Position position;
	private int movedTurns;
	
	public Piece(Board board, Position position, PieceType type, PieceColor color) {
		this.board = board;
		setPosition(position);
		movedTurns = 0;
		setType(type);
		setColor(color);
	}

	public int getRow()
		{ return position.getRow(); }

	public int getColumn()
		{ return position.getColumn(); }

	public Position getPosition()
		{ return position; }
	
	public void setPosition(Position position)
		{ this.position = position; }
	
	public PieceColor getColor()
		{ return color; }
	
	public void setColor(PieceColor color)
		{ this.color = color; }
	
	public PieceType getType()
		{ return type; }
	
	public void setType(PieceType type)
		{ this.type = type; }
	
	public int getMovedTurns()
		{ return movedTurns; }

	public void decMovedTurns()	
		{ movedTurns--; }
	
	public void incMovedTurns()	
		{ movedTurns++; }
	
	public Boolean wasMoved()
		{ return getMovedTurns() > 0; }
	
	public Board getBoard()
		{ return board; }
	
	public Boolean isStucked()
		{ return getPossibleMoves().size() == 0; }
	
	public Boolean canMoveToPosition(Position position) 
		{ return getPossibleMoves().contains(position); }
	
	public abstract List<Position> getPossibleMoves();

}