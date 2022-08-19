package exceptions;

public class PieceSelectionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PieceSelectionException(String msg)
		{	super(msg); }
	
}