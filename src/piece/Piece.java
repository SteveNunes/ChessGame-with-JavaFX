package piece;

import java.util.List;

import board.Board;

public abstract class Piece {
	
	private Type type;
	private Board board;
	private Color color;
	private Position position;
	private int movedTurns;
	
	public Piece(Board board, Position position, Type type, Color color) {
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
	
	public Color getColor()
		{ return color; }
	
	public void setColor(Color color)
		{ this.color = color; }
	
	public Type getType()
		{ return type; }
	
	public void setType(Type type)
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
		{ return possibleMoves().size() == 0; }
	
	public Boolean canMoveToPosition(Position position) {
	  for (Position p : possibleMoves())
		  if (p.equals(position))
		  	return true;
	  return false;
	}
	
	public abstract List<Position> possibleMoves();
	
}