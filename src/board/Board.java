package board;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import piece.Piece;
import piece.PieceColor;
import piece.PieceType;
import piece.Position;
import pieces.Bishop;
import pieces.King;
import pieces.Knight;
import pieces.Pawn;
import pieces.Queen;
import pieces.Rook;

public class Board {
	
	private PieceColor currentColorTurn;
	private int turns;
	private Piece[][] board, undoBoard;
	private List<Piece> capturedPieces;
	private Piece selectedPiece, undoPiece;
	private Boolean canUndo;
	
	public Board() { 
		board = new Piece[8][8];
		undoBoard = new Piece[8][8];
		capturedPieces = new ArrayList<>();
		reset(); 
	}
	
	public Board(PieceColor startTurn) {
		this();
		currentColorTurn = startTurn;		
	}
	
	public void reset() {
		turns = 0;
		canUndo = false;
		selectedPiece = null;
		currentColorTurn = new SecureRandom().nextInt(1) == 0 ? PieceColor.BLACK : PieceColor.WHITE;
		capturedPieces.clear();
		resetBoard(board);
		resetBoard(undoBoard);
		setPiecesOnTheBoard();
	}
	
	public List<Piece> getPieceList(PieceColor color) {
		List<Piece> pieceList = new ArrayList<>();
		for (Piece[]vec : board)
			for (Piece p : vec)
				if (p != null && (color == null || p.getColor() == color))
					pieceList.add(p);
		return pieceList;
	}
	
	public List<Piece> getPieceList()
		{ return getPieceList(null); }
	
	public static void resetBoard(Piece[][] board) { 
		for (Piece[]b : board)
			Arrays.fill(b, null);
	}

	public static void copyBoard(Piece[][] sourceBoard, Piece[][] targetBoard) {
		for (int y = 0; y < 8; y++)
			for (int x = 0; x < 8; x++)
				targetBoard[y][x] = sourceBoard[y][x]; 
	}
	
	public int getTurns()
		{ return turns; }
	
	public PieceColor getWinnerColor()
		{ return checkMate() ? oppositeColor() : null; }
	
	public PieceColor getCurrentColorTurn()
		{ return currentColorTurn; }
	
	public Piece getPromotedPiece() {
		for (int x = 0; x < 8; x++) {
			Piece piece = board[currentColorTurn == PieceColor.BLACK ? 0 : 7][x];
			if (piece != null && piece.getType() == PieceType.PAWN)
				return piece;
		}
		return null;
	}
	
	public Boolean pieceWasPromoted() {
		{ return getPromotedPiece() != null; }
	}
	
	public void promotePiece(PieceType newType) throws BoardException {
		if (!pieceWasPromoted())
			throw new PromotionException("There is no promoted piece");
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
		if (selectedPiece == null ||
				selectedPiece.getType() != PieceType.PAWN ||
				selectedPiece.getRow() != (selectedPiece.getColor() == PieceColor.WHITE ? 4 : 3))
					return null;

		for (int x = -1; x <= 1; x += 2) {
			Position pos = new Position(selectedPiece.getPosition().getRow(), selectedPiece.getPosition().getColumn() + x);
			Position targetPos = new Position(pos);
			targetPos.incValues(selectedPiece.getColor() == PieceColor.WHITE ? 1 : -1, 0);
			Piece piece = getPieceAtPosition(pos);
			if (piece != null && piece.getType() == PieceType.PAWN &&
					piece.getColor() != selectedPiece.getColor() &&
					piece.getMovedTurns() == 1 && !thereHavePiece(targetPos))
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

	public Boolean checkIfCastlingIsPossible(Position kingPosition, Position rookPosition) {
		kingPosition = new Position(kingPosition);
		rookPosition = new Position(rookPosition);
		Piece king = getPieceAtPosition(kingPosition);
		Piece rook = getPieceAtPosition(rookPosition);
		Boolean toLeft = kingPosition.getColumn() > rookPosition.getColumn();
		
		if (king == null || rook == null || king.getColor() != rook.getColor() ||
				king.getType() != PieceType.KING || rook.getType() != PieceType.ROOK ||
				king.wasMoved() || rook.wasMoved() || currentColorIsInCheck())
					return false;

		while (!kingPosition.equals(rookPosition)) {
			kingPosition.incColumn(toLeft ? -1 : 1);
			if (!thereHavePiece(kingPosition)) {
				addNewPiece(kingPosition, king);
				if (currentColorIsInCheck()) {
					removePiece(kingPosition);
					return false;
				}
				removePiece(kingPosition);
			}
		}
		return kingPosition.equals(rookPosition);
	}

	public Boolean isValidBoardPosition(Position position) {
		return position.getColumn() >= 0 && position.getColumn() < 8 &&
				position.getRow() >= 0 && position.getRow() < 8;
	}
	
	public Boolean thereHavePiece(Position position)
		{ return getPieceAtPosition(position) != null; }
	
	public Piece getPieceAtPosition(Position position) {
		if (!isValidBoardPosition(position))
			return null;
		return board[position.getRow()][position.getColumn()];
	}
	
	public Boolean isOpponentPiece(Position position, PieceColor color) 
		{ return thereHavePiece(position) && getPieceAtPosition(position).getColor() != color; }
	
	public Boolean isOpponentPiece(Position position)
		{ return isOpponentPiece(position, getCurrentColorTurn()); }

	public Piece getSelectedPiece()
		{ return selectedPiece; }
	
	public Boolean pieceIsSelected()
		{ return getSelectedPiece() != null; }

	public Piece selectPiece(Position position) throws PieceSelectionException {
		if (!isValidBoardPosition(position))
			throw new PieceSelectionException("Invalid position");
		if (getPieceAtPosition(position) == null)
			throw new PieceSelectionException("There is no piece on that position");
		if (getPieceAtPosition(position).getColor() != getCurrentColorTurn())
			throw new PieceSelectionException("This piece is not yours");
		if (getPieceAtPosition(position).isStucked())
			throw new PieceSelectionException("This piece is stucked");
		return (selectedPiece = getPieceAtPosition(position));
	}
	
	public PieceColor oppositeColor() 
		{ return getCurrentColorTurn() == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK; }
	
	public void addCapturedPiece(Piece piece) {
		if (piece != null)
			capturedPieces.add(piece);
	}

	public void removeCapturedPiece(Piece piece) {
		if (piece != null) 
			capturedPieces.remove(piece);
	}
	
	public List<Piece> getCapturedPieces(PieceColor color) {
		return capturedPieces.stream()
				.filter(c -> c.getColor() == color)
				.collect(Collectors.toList());
	}

	private void addPiece(Position position, Piece piece) { 
		if (piece != null && isValidBoardPosition(position)) {
			board[position.getRow()][position.getColumn()] = piece;
			piece.setPosition(position);
		}
	}

	private void removePiece(Position position) { 
		if (isValidBoardPosition(position))
			board[position.getRow()][position.getColumn()] = null;
	}
	
	private void undoMove() {
		if (canUndo) {
			copyBoard(undoBoard, board);
			for (int y = 0; y < 8; y++)
				for (int x = 0; x < 8; x++)
					if (board[y][x] != null) 
						board[y][x].setPosition(new Position(y, x));
			if (undoPiece != null)
				addPiece(undoPiece.getPosition(), undoPiece);
			canUndo = false;
			undoPiece = null;
		}
	}
	
	public void cancelSelection() {
		if (pieceWasPromoted())
			throw new InvalidMoveException("You must promote your pawn");
		selectedPiece = null;
	}
	
	private Piece movePieceTo(Position sourcePos, Position targetPos, Boolean testingCheckMate) throws InvalidMoveException {
		if (!testingCheckMate && pieceWasPromoted())
			throw new InvalidMoveException("You must promote the pawn");

			if (selectedPiece != null && targetPos.equals(selectedPiece.getPosition())) {
			selectedPiece = null;
			return null;
		}
		
		Piece sourcePiece = getPieceAtPosition(sourcePos);
		Piece targetPiece = getPieceAtPosition(targetPos);

		if (!testingCheckMate) {
			if (!isValidBoardPosition(sourcePos))
				throw new InvalidMoveException("Invalid source position");
			if (!isValidBoardPosition(targetPos))
				throw new InvalidMoveException("Invalid target position");
			if (!sourcePiece.canMoveToPosition(targetPos))
				throw new InvalidMoveException("Invalid move for this piece");
		}

		canUndo = true;
		copyBoard(board, undoBoard);

		// Castling special move
		Position rookPosition = new Position(sourcePos);
		Boolean rookAtLeft = targetPos.getColumn() < sourcePos.getColumn();
		rookPosition.setColumn(rookAtLeft ? 0 : 7);
		if (checkIfCastlingIsPossible(sourcePos, rookPosition)) {
			Rook rook = (Rook) getPieceAtPosition(rookPosition);
			removePiece(rookPosition);
			rookPosition = new Position(targetPos);
			rookPosition.incColumn(rookAtLeft ? 1 : -1);
			addNewPiece(rookPosition, rook);
		}

		removePiece(sourcePos);
		
		// EnPassant move
		if (sourcePiece.getType() == PieceType.PAWN &&
				checkEnPassant() && getEnPassantCapturePosition().equals(targetPos))
					targetPiece = getEnPassantPiece();

		undoPiece = targetPiece;

		if (!testingCheckMate) {

			if (currentColorIsInCheck()) {
				undoMove();
				throw new InvalidMoveException("You are in check");
			}

			if (targetPiece != null) {
				removePiece(targetPiece.getPosition());
				addCapturedPiece(targetPiece);
			}

			addPiece(targetPos, sourcePiece);
			sourcePiece.incMovedTurns();

			if (!pieceWasPromoted())
				changeTurn();
			
			// Incrementa o número de turnos dos peões adversários que tiverem movido apenas 1 casa, cancelando assim o status de EnPassant dos que estavam nesse statos mas não foram capturados de imediato.
			for (int y = 0; y < 8; y++)
				for (int x = 0; x < 8; x++)
					if (board[y][x] != null &&
							board[y][x].getColor() == currentColorTurn &&
							board[y][x].getType() == PieceType.PAWN &&
							board[y][x].getMovedTurns() == 1)
								board[y][x].incMovedTurns();
		}
		return targetPiece;
	}

	private void changeTurn() {
		selectedPiece = null;
		turns++;
		currentColorTurn = oppositeColor();
	}

	public Piece movePieceTo(Position targetPos) throws BoardException 
		{ return movePieceTo(getSelectedPiece().getPosition(), targetPos, false); }
	
	public Boolean currentColorIsInCheck() {
		List<Piece> pieceList1 = getPieceList(getCurrentColorTurn());
		List<Piece> pieceList2 = getPieceList(oppositeColor());
		for (Piece p : pieceList1)
			if (p.getType() == PieceType.KING) {
				for (Piece p2 : pieceList2)
					if (p2.canMoveToPosition(p.getPosition()))
						return true;
				break;
			}
		return false;
	}
	
	public Boolean checkMate() {
		if (currentColorIsInCheck()) {
			List<Piece> pieceList = getPieceList(getCurrentColorTurn());
			for (Piece p : pieceList)
				if (p.getType() == PieceType.KING) {
					for (Piece p2 : pieceList) {
						for (Position pos : p2.possibleMoves()) {
							movePieceTo(p2.getPosition(), pos, true);
							if (!currentColorIsInCheck()) {
								undoMove();
								return false;
							}
							undoMove();
						}
					}
					break;
				}			
			return true;
		}
		return false;
	}

	public void addNewPiece(Position position, PieceType type, PieceColor color) throws BoardException {
		if (!isValidBoardPosition(position))
			throw new BoardException("Invalid position");
		if (thereHavePiece(position))
			throw new BoardException("This board position is occupied already");
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
		else 
			piece = new Pawn(this, position, color);
		board[position.getRow()][position.getColumn()] = piece;
	}

	public void addNewPiece(int row, int column, PieceType type, PieceColor color) throws BoardException
		{ addNewPiece(new Position(row, column), type, color); }

	public void addNewPiece(String position, PieceType type, PieceColor color) throws BoardException
		{ addNewPiece(Position.stringToPosition(position), type, color); }
	
	public void addNewPiece(Position position, Piece piece)
		{ addNewPiece(position, piece.getType(), piece.getColor()); }
	
	public void setPiecesOnTheBoardORIGINAL() throws BoardException {
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
	
	public void setPiecesOnTheBoard() throws BoardException {
		// White Pieces
		addNewPiece("d3", PieceType.ROOK, PieceColor.WHITE);
		// Black Pieces
		addNewPiece("a7", PieceType.PAWN, PieceColor.BLACK);
		addNewPiece("b7", PieceType.PAWN, PieceColor.BLACK);
		addNewPiece("c7", PieceType.PAWN, PieceColor.BLACK);
		addNewPiece("d7", PieceType.PAWN, PieceColor.BLACK);
		addNewPiece("e7", PieceType.PAWN, PieceColor.BLACK);
		addNewPiece("f7", PieceType.PAWN, PieceColor.BLACK);
		addNewPiece("g7", PieceType.PAWN, PieceColor.BLACK);
		addNewPiece("h7", PieceType.PAWN, PieceColor.BLACK);
	}
}