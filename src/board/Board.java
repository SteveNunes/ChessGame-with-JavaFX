package board;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import exceptions.BoardException;
import exceptions.CheckException;
import exceptions.GameException;
import exceptions.InvalidMoveException;
import exceptions.PieceSelectionException;
import exceptions.PromotionException;
import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import piece.Position;
import pieces.Bishop;
import pieces.ChuckNorris;
import pieces.King;
import pieces.Knight;
import pieces.Pawn;
import pieces.Queen;
import pieces.Rook;

public class Board {
	
	private int turns;
	private List<Piece[][]> undoBoards;
	private List<Piece> capturedPieces;
	private Piece[][] board;
	private Piece selectedPiece;
	private PieceColor currentColorTurn;
	
	public Board() { 
		board = new Piece[8][8];
		undoBoards = new ArrayList<>();
		capturedPieces = new ArrayList<>();
		reset(); 
	}
	
	public Board(PieceColor startTurn) {
		this();
		currentColorTurn = startTurn;		
	}
	
	public void reset() {
		turns = 0;
		selectedPiece = null;
		currentColorTurn = new SecureRandom().nextInt(2) == 0 ? PieceColor.BLACK : PieceColor.WHITE;
		capturedPieces.clear();
		resetBoard(board);
		undoBoards.clear();
		setPiecesOnTheBoard();
	}
	
	public List<Piece> getPieceList(PieceColor color) {
		if (color == null)
			throw new GameException("color is null");
		List<Piece> pieceList = new ArrayList<>();
		for (Piece[] boardRow : board)
			for (Piece piece : boardRow)
				if (piece != null && (color == null || piece.getColor() == color))
					pieceList.add(piece);
		return pieceList;
	}
	
	public List<Piece> getPieceList()
		{ return getPieceList(null); }
	
	public void resetBoard(Piece[][] board) { 
		for (Piece[] b : board)
			Arrays.fill(b, null);
	}

	private void copyBoard(Piece[][] sourceBoard, Piece[][] targetBoard) {
		if (sourceBoard == null)
			throw new GameException("sourceBoard is null");
		if (targetBoard == null)
			throw new GameException("targetBoard is null");
		for (int y = 0; y < sourceBoard.length; y++)
			for (int x = 0; x < sourceBoard[y].length; x++)
				targetBoard[y][x] = sourceBoard[y][x]; 
	}
	
	public int getTurns()
		{ return turns; }
	
	public PieceColor getWinnerColor()
		{ return checkMate() ? opponentColor() : null; }
	
	public PieceColor getCurrentColorTurn()
		{ return currentColorTurn; }
	
	public Piece getPromotedPiece() {
		for (Piece piece : board[currentColorTurn == PieceColor.BLACK ? 0 : 7])
			if (piece != null && piece.getType() == PieceType.PAWN)
				return piece;
		return null;
	}
	
	public Boolean pieceWasPromoted() {
		{ return getPromotedPiece() != null; }
	}
	
	public void promotePiece(PieceType newType) throws BoardException {
		if (!pieceWasPromoted())
			throw new PromotionException("There is no promoted piece");
		if (newType == null)
			throw new GameException("newType is null");
		if (newType == PieceType.PAWN)
			throw new PromotionException("You can't promote a Pawn to a Pawn");
		if (newType == PieceType.KING)
			throw new PromotionException("You can't promote a Pawn to a King");
		Position pos = new Position(getPromotedPiece().getPosition());
		PieceColor color = getPromotedPiece().getColor();
		removePiece(getPromotedPiece().getPosition());
		addNewPiece(pos, newType, color);
		changeTurn();
	}
	
	public Piece getEnPassantPiece() {
		if (selectedPiece == null || selectedPiece.getType() != PieceType.PAWN)
			return null;

		for (int x = -1; x <= 1; x += 2) {
			Position pos = new Position(selectedPiece.getPosition().getRow(), selectedPiece.getPosition().getColumn() + x);
			Position targetPos = new Position(pos);
			targetPos.incValues(selectedPiece.getColor() == PieceColor.WHITE ? 1 : -1, 0);
			Piece piece = getPieceAt(pos);
			if (piece != null && piece.getType() == PieceType.PAWN &&
					((Pawn) piece).getEnPassant() && isFreeSlot(targetPos))
						return piece;
		}
		return null;
	}
	
	public Position getEnPassantCapturePosition() {
		if (!checkEnPassant())
			return null;
		Piece piece = getEnPassantPiece();
		Position position = new Position(piece.getPosition());
		position.incValues(piece.getColor() == PieceColor.WHITE ? -1 : -1, 0);
		return position;
	}

	public Boolean checkEnPassant()
		{ return getEnPassantPiece() != null; }

	public Boolean checkIfCastlingIsPossible(King king, Rook rook) {
		if (king == null)
			throw new GameException("king is null");
		if (rook == null)
			throw new GameException("rook is null");
		if (king.getColor() != rook.getColor())
			throw new GameException("king and rook must be from the same color");

		Position kingPosition = new Position(king.getPosition());
		Position rookPosition = new Position(rook.getPosition());
		Boolean toLeft = kingPosition.getColumn() > rookPosition.getColumn();
		
		if (king.wasMoved() || rook.wasMoved() || currentColorIsChecked())
			return false;

		while (!kingPosition.equals(rookPosition)) {
			kingPosition.incColumn(toLeft ? -1 : 1);
			if (isFreeSlot(kingPosition)) {
				addNewPiece(kingPosition, king);
				if (currentColorIsChecked()) {
					removePiece(kingPosition);
					return false;
				}
				removePiece(kingPosition);
			}
		}
		return kingPosition.equals(rookPosition);
	}

	public Boolean checkIfCastlingIsPossible(Piece king, Piece rook) {
		if (king.getType() != PieceType.KING || rook.getType() != PieceType.ROOK)
			return false;
		return checkIfCastlingIsPossible((King) king, (Rook) rook);
	}
	
	private void validatePosition(Position position, String varName) throws BoardException {
		if (position == null)
			throw new BoardException(varName + " is null");
		if (!isValidBoardPosition(position))
			throw new BoardException(varName + " - Invalid board position");
	}
	
	public Boolean isValidBoardPosition(Position position) {
		if (position == null)
			throw new GameException("position is null");
		return position.getColumn() >= 0 && position.getColumn() < 8 &&
			position.getRow() >= 0 && position.getRow() < 8;
	}
	
	public Boolean isFreeSlot(Position position) {
		validatePosition(position, "position");
		return isValidBoardPosition(position) && getPieceAt(position) == null;
	}
	
	public Piece getPieceAt(Position position)
		{ return board[position.getRow()][position.getColumn()]; }
	
	public Boolean isOpponentPiece(Position position, PieceColor color) { 
		if (color == null)
			throw new GameException("color is null");
		validatePosition(position, "position");
		return !isFreeSlot(position) && getPieceAt(position).getColor() != color;
	}
	
	public Boolean isOpponentPiece(Position position) {
		validatePosition(position, "position");
		return isOpponentPiece(position, getCurrentColorTurn());
	}

	public Piece getSelectedPiece()
		{ return selectedPiece; }
	
	public Boolean pieceIsSelected()
		{ return getSelectedPiece() != null; }

	public Piece selectPiece(Position position) throws PieceSelectionException {
		validatePosition(position, "position");
		if (getPieceAt(position) == null)
			throw new PieceSelectionException("There is no piece on that position");
		if (getPieceAt(position).getColor() != getCurrentColorTurn())
			throw new PieceSelectionException("This piece is not yours");
		if (getPieceAt(position).isStucked())
			throw new PieceSelectionException("This piece is stucked");
		return (selectedPiece = getPieceAt(position));
	}
	
	public PieceColor opponentColor(PieceColor color) { 
		if (color == null)
			throw new GameException("color is null");
		return color == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK;
	}
	
	public PieceColor opponentColor()
		{ return opponentColor(getCurrentColorTurn()); }
	
	public void addCapturedPiece(Piece piece) {
		if (piece == null)
			throw new GameException("piece is null");
		capturedPieces.add(piece);
	}

	public void removeCapturedPiece(Piece piece) {
		if (piece == null)
			throw new GameException("piece is null");
		capturedPieces.remove(piece);
	}
	
	public List<Piece> getCapturedPieces(PieceColor color) {
		if (color == null)
			throw new GameException("color is null");
		return capturedPieces.stream()
			.filter(c -> c.getColor() == color)
			.collect(Collectors.toList());
	}

	private void addPiece(Position position, Piece piece) { 
		validatePosition(position, "position");
		if (piece == null)
			throw new GameException("piece is null");
		if (!isFreeSlot(position))
			throw new BoardException("The slot at this position is not free");
		board[position.getRow()][position.getColumn()] = piece;
		piece.setPosition(position);
	}

	private void removePiece(Position position) { 
		validatePosition(position, "position");
		if (isFreeSlot(position))
			throw new BoardException("There's no piece at this slot position");
		board[position.getRow()][position.getColumn()] = null;
	}
	
	private void undoMoves(int totalUndoMoves) {
		if (totalUndoMoves < 0)
			throw new GameException("totalUndoMoves must be 0>");
		if (undoBoards.isEmpty())
			throw new GameException("No available undo moves");
		while (totalUndoMoves-- > 0 && !undoBoards.isEmpty()) {
			copyBoard(undoBoards.get(undoBoards.size() - 1), board);
			for (int y = 0; y < 8; y++)
				for (int x = 0; x < 8; x++)
					if (board[y][x] != null) 
						board[y][x].setPosition(new Position(y, x));
			undoBoards.remove(undoBoards.size() - 1);
		}
	}
	
	private void undoMove()
		{ undoMoves(1); }
	
	public void cancelSelection() {
		if (pieceWasPromoted())
			throw new BoardException("You must promote your pawn");
		selectedPiece = null;
	}
	
	private Piece movePieceTo(Position sourcePos, Position targetPos, Boolean testingCheckMate) throws InvalidMoveException {
		validatePosition(sourcePos, "sourcePos");
		validatePosition(targetPos, "targetPos");
		if (!testingCheckMate && pieceWasPromoted())
			throw new InvalidMoveException("You must promote the pawn");

		if (selectedPiece != null && targetPos.equals(selectedPiece.getPosition())) {
			//Se o slot de destino for o mesmo da pedra selecionada, desseleciona ela
			selectedPiece = null;
			return null;
		}
		
		if (!testingCheckMate) {
			validatePosition(sourcePos, "sourcePos");
			validatePosition(targetPos, "targetPos");
		}

		Boolean checked = currentColorIsChecked();
		Piece sourcePiece = getPieceAt(sourcePos);
		Piece targetPiece = getPieceAt(targetPos);
		Piece[][] cloneBoard = new Piece[8][8];

		if (!testingCheckMate && !sourcePiece.canMoveToPosition(targetPos))
			throw new InvalidMoveException("Invalid move for this piece");

		copyBoard(board, cloneBoard);
		undoBoards.add(cloneBoard);

		// Castling special move
		Position rookPosition = new Position(sourcePos);
		Boolean rookAtLeft = targetPos.getColumn() < sourcePos.getColumn();
		rookPosition.setColumn(rookAtLeft ? 0 : 7);
		if (targetPiece != null && checkIfCastlingIsPossible(sourcePiece, targetPiece)) {
			removePiece(rookPosition);
			rookPosition = new Position(targetPos);
			rookPosition.incColumn(rookAtLeft ? 1 : -1);
			addNewPiece(rookPosition, (Rook) getPieceAt(rookPosition));
		}

		removePiece(sourcePos);
		
		if (sourcePiece.getType() == PieceType.PAWN) {
			if (Math.abs(sourcePos.getRow() - targetPos.getRow()) == 2) // Marca peão que iniciou movendo 2 casas como EnPassant
				((Pawn) sourcePiece).setEnPassant(true);
			if (checkEnPassant() && getEnPassantCapturePosition().equals(targetPos)) // Verifica se o peão atual realizou um movimento de captura EnPassant
				targetPiece = getEnPassantPiece();
		}

		if (!testingCheckMate) { 
			if (targetPiece != null) {
				removePiece(targetPiece.getPosition());
				addCapturedPiece(targetPiece);
			}

			addPiece(targetPos, sourcePiece);
			sourcePiece.incMovedTurns();

			if (currentColorIsChecked()) {
				undoMove();
				throw new CheckException(!checked ? "You can't put yourself in check" : "You'll still checked after this move");
			}

			if (!pieceWasPromoted())
				changeTurn();
			
			// Remove o status de EnPassant de todos os peôes marcados como tal
			for (int y = 0; y < 8; y++)
				for (int x = 0; x < 8; x++)
					if (board[y][x] != null &&
							board[y][x].getColor() == currentColorTurn &&
							board[y][x].getType() == PieceType.PAWN &&
							((Pawn)board[y][x]).getEnPassant())
								((Pawn)board[y][x]).setEnPassant(false);
		}
		return targetPiece;
	}

	private void changeTurn() {
		if (pieceWasPromoted())
			throw new GameException("You must promote the pawn");
		turns++;
		selectedPiece = null;
		currentColorTurn = opponentColor();
	}

	public Piece movePieceTo(Position targetPos) throws BoardException 
		{ return movePieceTo(getSelectedPiece().getPosition(), targetPos, false); }
	
	public Boolean pieceColdBeCaptured(Piece piece) {
		if (piece == null)
			throw new GameException("piece is null");
		List<Piece> opponentPieceList = getPieceList(piece.getColor() == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK);
		for (Piece opponentPiece : opponentPieceList)
			if (opponentPiece.getPossibleMoves().contains(piece.getPosition()))
				return true;
		return false;
	}
	
	public Boolean pieceCanDoSafeMove(Piece piece) {
		if (piece == null)
			throw new GameException("piece is null");
		for (Position myPos : piece.getPossibleMoves()) {
			movePieceTo(piece.getPosition(), myPos, true);
			if (!pieceColdBeCaptured(piece)) {
				undoMove();
				return true;
			}
			undoMove();
		}			
		return false;
	}

	public Boolean isChecked(PieceColor color) {
		if (color == null)
			throw new GameException("color is null");
		List<Piece> pieceList = getPieceList(color);
		for (Piece piece : pieceList)
			if (piece.getType() == PieceType.KING && pieceColdBeCaptured(piece))
				return true;
		return false;
	}
	
	public Boolean currentColorIsChecked()
		{ return isChecked(getCurrentColorTurn()); }
	
	public Boolean checkMate(PieceColor color) {
		if (color == null)
			throw new GameException("color is null");
		Piece king = null;
		List<Piece> pieceList = getPieceList(color);
		for (Piece piece : pieceList)
			if (piece.getType() == PieceType.KING && pieceCanDoSafeMove(king = piece))
				return false;

		for (Piece p : pieceList)
			if (p.getType() != PieceType.KING)
				for (Piece p2 : pieceList)
					for (Position pos : p2.getPossibleMoves()) {
						movePieceTo(p2.getPosition(), pos, true);
						for (Position pos2 : king.getPossibleMoves()) {
							movePieceTo(king.getPosition(), pos2, true);
							if (pieceCanDoSafeMove(king)) {
								undoMoves(2);
								return false;
							}
							undoMove();
						}
						undoMove();
					}
		return true;
	}
	
	public Boolean checkMate()
		{ return checkMate(getCurrentColorTurn()); }

	public void addNewPiece(Position position, PieceType type, PieceColor color) throws GameException,BoardException {
		validatePosition(position, "position");
		if (type == null)
			throw new GameException("type is null");
		if (color == null)
			throw new GameException("color is null");
		if (!isFreeSlot(position))
			throw new BoardException("This board position is not free");
		Piece piece;
		if (type == PieceType.KING)
			piece = new King(this, position, color);
		else if (type == PieceType.QUEEN) 
			piece = new Queen(this, position, color);
		else if (type == PieceType.ROOK) 
			piece = new Rook(this, position, color);
		else if (type == PieceType.BISHOP) 
			piece = new Bishop(this, position, color);
		else if (type == PieceType.KNIGHT) 
			piece = new Knight(this, position, color);
		else if (type == PieceType.CHUCKNORRIS) 
			piece = new ChuckNorris(this, position, color);
		else 
			piece = new Pawn(this, position, color);
		board[position.getRow()][position.getColumn()] = piece;
	}

	public void addNewPiece(int row, int column, PieceType type, PieceColor color) throws GameException,BoardException
		{ addNewPiece(new Position(row, column), type, color); }

	public void addNewPiece(String position, PieceType type, PieceColor color) throws GameException,BoardException
		{ addNewPiece(Position.stringToPosition(position), type, color); }
	
	public void addNewPiece(Position position, Piece piece)
		{ addNewPiece(position, piece.getType(), piece.getColor()); }
	
	private void setPiecesOnTheBoardORIGINAL() throws GameException,BoardException {
		// White Pieces
		addNewPiece("a7", PieceType.PAWN, PieceColor.WHITE);
		addNewPiece("b7", PieceType.PAWN, PieceColor.WHITE);
		addNewPiece("c7", PieceType.PAWN, PieceColor.WHITE);
		addNewPiece("d7", PieceType.PAWN, PieceColor.WHITE);
		addNewPiece("e7", PieceType.PAWN, PieceColor.WHITE);
		addNewPiece("f7", PieceType.PAWN, PieceColor.WHITE);
		addNewPiece("g7", PieceType.PAWN, PieceColor.WHITE);
		addNewPiece("h7", PieceType.PAWN, PieceColor.WHITE);
		addNewPiece("a8", PieceType.ROOK, PieceColor.WHITE);
		addNewPiece("b8", PieceType.KNIGHT, PieceColor.WHITE);
		addNewPiece("c8", PieceType.BISHOP, PieceColor.WHITE);
		addNewPiece("d8", PieceType.QUEEN, PieceColor.WHITE);
		addNewPiece("e8", PieceType.KING, PieceColor.WHITE);
		addNewPiece("f8", PieceType.BISHOP, PieceColor.WHITE);
		addNewPiece("g8", PieceType.KNIGHT, PieceColor.WHITE);
		addNewPiece("h8", PieceType.ROOK, PieceColor.WHITE);
		// Black Pieces
		addNewPiece("a2", PieceType.PAWN, PieceColor.BLACK);
		addNewPiece("b2", PieceType.PAWN, PieceColor.BLACK);
		addNewPiece("c2", PieceType.PAWN, PieceColor.BLACK);
		addNewPiece("d2", PieceType.PAWN, PieceColor.BLACK);
		addNewPiece("e2", PieceType.PAWN, PieceColor.BLACK);
		addNewPiece("f2", PieceType.PAWN, PieceColor.BLACK);
		addNewPiece("g2", PieceType.PAWN, PieceColor.BLACK);
		addNewPiece("h2", PieceType.PAWN, PieceColor.BLACK);
		addNewPiece("a1", PieceType.ROOK, PieceColor.BLACK);
		addNewPiece("b1", PieceType.KNIGHT, PieceColor.BLACK);
		addNewPiece("c1", PieceType.BISHOP, PieceColor.BLACK);
		addNewPiece("d1", PieceType.QUEEN, PieceColor.BLACK);
		addNewPiece("e1", PieceType.KING, PieceColor.BLACK);
		addNewPiece("f1", PieceType.BISHOP, PieceColor.BLACK);
		addNewPiece("g1", PieceType.KNIGHT, PieceColor.BLACK);
		addNewPiece("h1", PieceType.ROOK, PieceColor.BLACK);
	}
	
	private void setPiecesOnTheBoard() throws BoardException {
		addNewPiece("h8", PieceType.KING, PieceColor.WHITE);
		addNewPiece("e7", PieceType.QUEEN, PieceColor.BLACK);
		addNewPiece("e1", PieceType.KING, PieceColor.BLACK);
		

	}
}