package piece;

import java.util.Objects;

import exceptions.BoardException;

public class Position {

	private int row, column;
	
	public Position(int row, int column)
		{ setValues(row, column); }
	
	public Position(Position position)
		{ setValues(position); }
	
	public Position(String position)
		{ setValues(position); }
	
	public int getRow()
		{ return row; }
	
	public int getColumn()
		{ return column; }
	
	public void setRow(int row)
		{ this.row = row; }
	
	public void setColumn(int column)
		{ this.column = column; }
	
	public void incRow(int value)
		{ row += value; }
	
	public void incColumn(int value)
		{ column += value; }

	public void setValues(int row, int column) {
		setRow(row);
		setColumn(column);
	}
	
	public void setValues(Position position)
		{ setValues(position.getRow(), position.getColumn()); }
	
	public void setValues(String position) 
		{ setValues(stringToPosition(position)); }
	
	public void incValues(int incRow, int incColumn) {
		incRow(incRow);
		incColumn(incColumn);
	}
	
	@Override
	public int hashCode()
		{ return Objects.hash(column, row); }

	@Override
	public boolean equals(Object obj) {
		Position other = (Position) obj;
		return obj != null && column == other.column && row == other.row;
	}

	public static Boolean equals(Position position1, Position position2)
		{ return position1.getRow() == position2.getRow() && position1.getColumn() == position2.getColumn(); }
	
	public static Position stringToPosition(String position) throws RuntimeException {
		position = position.toLowerCase();
		int row = 7 - (Integer.parseInt(position.substring(1)) - 1);
		int column = position.charAt(0) - 'a';
		if (column < 0 || column >= 8 || row < 0 || row >= 8)
			throw new BoardException("Invalid board position! The value must be between 'a1' and 'h8'");
		return new Position(row, column);
	}
	@Override
	public String toString()
		{ return "[" + getRow() + ", " + getColumn() + "]"; }
	
}
