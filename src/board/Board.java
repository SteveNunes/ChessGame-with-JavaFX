package board;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import piece.*;
import pieces.*;

public class Board {
	
	private Color currentColorTurn;
	private int turns;
	private Piece[][] board, undoBoard;
	private List<Piece> capturedPieces;
	private Piece selectedPiece, undoPiece, promotedPiece, enPassantPiece;
	private Boolean canUndo;
	
	public Board() { 
		board = new Piece[8][8];
		undoBoard = new Piece[8][8];
		capturedPieces = new ArrayList<>();
		reset(); 
	}
	
	public Board(Color startTurn) {
		this();
		currentColorTurn = startTurn;		
	}
	
	public void reset() {
		turns = 0;
		canUndo = false;
		promotedPiece = selectedPiece = promotedPiece = enPassantPiece = null;
		currentColorTurn = new SecureRandom().nextInt(1) == 0 ? Color.BLACK : Color.WHITE;
		capturedPieces.clear();
		resetBoard(board);
		resetBoard(undoBoard);
		setPiecesOnTheBoard();
	}
	
	public List<Piece> getPieceList(Color color) {
		List<Piece> pieceList = new ArrayList<>();
		for (Piece[]vec : board)
			for (Piece p : vec)
				if (p != null && (color == null || p.getColor() == color))
					pieceList.add(p);
		return pieceList;
	}
	
	public List<Piece> getPieceList() { return getPieceList(null); }
	
	public static void resetBoard(Piece[][] board) 
		{ for (Piece[]b : board) Arrays.fill(b, null); }

	public static void copyBoard(Piece[][] sourceBoard, Piece[][] targetBoard) {
		for (int y = 0; y < 8; y++)
			for (int x = 0; x < 8; x++)
				targetBoard[y][x] = sourceBoard[y][x]; 
	}
	
	public int getTurns() { return turns; }
	
	public Color getWinnerColor() { return checkMate() ? oppositeColor() : null; }
	
	public Color getCurrentColorTurn() { return currentColorTurn; }
	
	public Piece getPromotedPiece() { return promotedPiece; }
	
	public Boolean pieceWasPromoted() { return getPromotedPiece() != null; }
	
	public Piece getEnPassantPiece() { return enPassantPiece; }

	public Boolean checkEnPassant() { return enPassantPiece != null; }

	public void promotePiece(Type newType) throws BoardException {
		if (!pieceWasPromoted())
			throw new PromotionException("There is no promoted piece");
		if (newType == Type.PAWN)
			throw new PromotionException("You can't promote a Pawn to a Pawn");
		if (newType == Type.KING)
			throw new PromotionException("You can't promote a Pawn to a King");
		removePiece(getPromotedPiece().getPosition());
		addNewPiece(getPromotedPiece().getPosition(), newType, getPromotedPiece().getColor());
		promotedPiece = null;
	}

	public Boolean isValidBoardPosition(Position position) {
		return position.getColumn() >= 0 && position.getColumn() < 8 &&
				position.getRow() >= 0 && position.getRow() < 8;
	}
	
	public Boolean thereHavePiece(Position position)
		{ return getPieceAtPosition(position) != null; }
	
	public Piece getPieceAtPosition(Position position) {
		if (!isValidBoardPosition(position)) return null;
		return board[position.getRow()][position.getColumn()];
	}
	
	public Boolean isOpponentPiece(Position position, Color color) 
		{ return thereHavePiece(position) && getPieceAtPosition(position).getColor() != color; }
	
	public Boolean isOpponentPiece(Position position)
		{ return isOpponentPiece(position, getCurrentColorTurn()); }

	public Piece getSelectedPiece() { return selectedPiece; }
	
	public Boolean pieceIsSelected() { return getSelectedPiece() != null; }

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
	
	public Color oppositeColor() 
		{ return getCurrentColorTurn() == Color.BLACK ? Color.WHITE : Color.BLACK; }
	
	public void addCapturedPiece(Piece piece)
		{ if (piece != null) capturedPieces.add(piece); }

	public void removeCapturedPiece(Piece piece)
		{ if (piece != null) capturedPieces.remove(piece); }
	
	public List<Piece> getCapturedPieces(Color color) {
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
	
	public void cancelSelection()
		{ selectedPiece = null; }
	
	private Piece movePieceTo(Position sourcePos, Position targetPos, Boolean testingCheckMate) throws InvalidMoveException {
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

		int dis;
		canUndo = true;
		copyBoard(board, undoBoard);
		promotedPiece = null;
		removePiece(sourcePos);
		
		if (sourcePiece instanceof Pawn) {
			// En Passant special move
			dis = sourcePos.getRow() - targetPos.getRow();
			if (Math.abs(dis) == 2) enPassantPiece = sourcePiece;
			else if (checkEnPassant()) {
				if (targetPos.getColumn() == enPassantPiece.getColumn()) {
					Position pos = new Position(targetPos);
					pos.incValues(sourcePiece.getColor() == Color.BLACK ? 1 : -1, 0);
					if (enPassantPiece.getPosition().equals(pos))
						targetPiece = enPassantPiece;
				}
				enPassantPiece = null;
			}
			// Promotion special move
			if (targetPos.getRow() == (getCurrentColorTurn() == Color.BLACK ? 0 : 7)) 
				promotedPiece = sourcePiece;
		}

		// Castling special move
		if (sourcePiece instanceof King && !currentColorIsInCheck() && !sourcePiece.wasMoved()) {
			dis = targetPos.getColumn() - sourcePos.getColumn();
			if (Math.abs(dis) == 2) {
				Position rookSourcePos = new Position(sourcePos.getRow(), dis == -2 ? 0 : 7);
				Position rookTargetPos = new Position(sourcePos);
				rookTargetPos.incValues(0, dis == -2 ? -1 : 1);
				Piece rookPiece = getPieceAtPosition(rookSourcePos); 
				addPiece(rookTargetPos, rookPiece);
				removePiece(rookSourcePos);
				rookPiece.incMovedTurns();
			}
		}
		
		undoPiece = targetPiece;

		if (!testingCheckMate) {
			if (currentColorIsInCheck()) {
				undoMove();
				throw new InvalidMoveException("You can't put yourself in check");
			}
			if (targetPiece != null) {
				removePiece(targetPiece.getPosition());
				addCapturedPiece(targetPiece);
			}
			addPiece(targetPos, sourcePiece);
			selectedPiece = null;
			turns++;
			currentColorTurn = oppositeColor();
			sourcePiece.incMovedTurns();
		}
		return targetPiece;
	}

	public Piece movePieceTo(Position targetPos) throws BoardException 
		{ return movePieceTo(getSelectedPiece().getPosition(), targetPos, false); }
	
	public Boolean currentColorIsInCheck() {
		List<Piece> pieceList1 = getPieceList(getCurrentColorTurn());
		List<Piece> pieceList2 = getPieceList(oppositeColor());
		for (Piece p : pieceList1)
			if (p instanceof King) {
				for (Piece p2 : pieceList2)
					if (p2.canMoveToPosition(p.getPosition())) return true;
				break;
			}
		return false;
	}
	
	public Boolean checkMate() {
		if (currentColorIsInCheck()) {
			List<Piece> pieceList = getPieceList(getCurrentColorTurn());
			for (Piece p : pieceList)
				if (p instanceof King) {
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

	public void addNewPiece(Position position, Type type, Color color) throws BoardException {
		if (!isValidBoardPosition(position))
			throw new BoardException("Invalid position");
		if (thereHavePiece(position))
			throw new BoardException("This board position is occupied already");
		Piece piece;
		if (type == Type.KING) piece = new King(this, position, color);
		else if (type == Type.QUEEN) piece = new Queen(this, position, color);
		else if (type == Type.ROOK) piece = new Rook(this, position, color);
		else if (type == Type.BISHOP) piece = new Bishop(this, position, color);
		else if (type == Type.KNIGHT) piece = new Knight(this, position, color);
		else piece = new Pawn(this, position, color);
		board[position.getRow()][position.getColumn()] = piece;
	}

	public void addNewPiece(int row, int column, Type type, Color color) throws BoardException
		{ addNewPiece(new Position(row, column), type, color); }

	public void addNewPiece(String position, Type type, Color color) throws BoardException
		{ addNewPiece(Position.stringToPosition(position), type, color); }
	
	public void setPiecesOnTheBoard() throws BoardException {
		// White Pieces
		addNewPiece("a7", Type.PAWN, Color.WHITE);
		addNewPiece("b7", Type.PAWN, Color.WHITE);
		addNewPiece("c7", Type.PAWN, Color.WHITE);
		addNewPiece("d7", Type.PAWN, Color.WHITE);
		addNewPiece("e7", Type.PAWN, Color.WHITE);
		addNewPiece("f7", Type.PAWN, Color.WHITE);
		addNewPiece("g7", Type.PAWN, Color.WHITE);
		addNewPiece("h7", Type.PAWN, Color.WHITE);
		addNewPiece("a8", Type.ROOK, Color.WHITE);
		addNewPiece("b8", Type.KNIGHT, Color.WHITE);
		addNewPiece("c8", Type.BISHOP, Color.WHITE);
		addNewPiece("d8", Type.QUEEN, Color.WHITE);
		addNewPiece("e8", Type.KING, Color.WHITE);
		addNewPiece("f8", Type.BISHOP, Color.WHITE);
		addNewPiece("g8", Type.KNIGHT, Color.WHITE);
		addNewPiece("h8", Type.ROOK, Color.WHITE);
		// Black Pieces
		addNewPiece("a2", Type.PAWN, Color.BLACK);
		addNewPiece("b2", Type.PAWN, Color.BLACK);
		addNewPiece("c2", Type.PAWN, Color.BLACK);
		addNewPiece("d2", Type.PAWN, Color.BLACK);
		addNewPiece("e2", Type.PAWN, Color.BLACK);
		addNewPiece("f2", Type.PAWN, Color.BLACK);
		addNewPiece("g2", Type.PAWN, Color.BLACK);
		addNewPiece("h2", Type.PAWN, Color.BLACK);
		addNewPiece("a1", Type.ROOK, Color.BLACK);
		addNewPiece("b1", Type.KNIGHT, Color.BLACK);
		addNewPiece("c1", Type.BISHOP, Color.BLACK);
		addNewPiece("d1", Type.QUEEN, Color.BLACK);
		addNewPiece("e1", Type.KING, Color.BLACK);
		addNewPiece("f1", Type.BISHOP, Color.BLACK);
		addNewPiece("g1", Type.KNIGHT, Color.BLACK);
		addNewPiece("h1", Type.ROOK, Color.BLACK);
	}
	
}