package application;

import java.util.Scanner;

import board.Board;
import piece.*;

public class UI {

	Board board;

	public UI(Board board) { this.board = board; }
	
	private String colorLet(Piece piece) {
		String str;
		if (piece == null) str = "-";
		else if (board.pieceWasPromoted() && piece == board.getPromotedPiece())
			str = AnsiColors.ANSI_PURPLE_BACKGROUND + AnsiColors.ANSI_WHITE + piece.toString();
		else if (piece == board.getSelectedPiece())
			str = (piece.getColor() == Color.BLACK ? AnsiColors.BLACK_PIECE_BACKGROUND : AnsiColors.WHITE_PIECE_BACKGROUND) + AnsiColors.ANSI_BLACK + piece.toString();
		else str = (piece.getColor() == Color.BLACK ? AnsiColors.BLACK_PIECE : AnsiColors.WHITE_PIECE) + piece.toString();
		return str + AnsiColors.ANSI_RESET;
	}
	
	public String turnColor(Color color)
		{ return (color == Color.BLACK ? AnsiColors.BLACK_PIECE : AnsiColors.WHITE_PIECE) + color + AnsiColors.ANSI_RESET; }
	
	public void drawInfos() {
		System.out.println(turnColor(Color.WHITE) + " captured Pieces: " + AnsiColors.ANSI_PURPLE + board.getCapturedPieces(Color.WHITE) + AnsiColors.ANSI_RESET);
		System.out.println(turnColor(Color.BLACK) + " captured Pieces: " + AnsiColors.ANSI_PURPLE + board.getCapturedPieces(Color.BLACK) + AnsiColors.ANSI_RESET);
		System.out.println();
		if (board.currentColorIsInCheck())
			System.out.println(AnsiColors.ANSI_CYAN + "CHECK!" + AnsiColors.ANSI_RESET);
		System.out.println("Turn: " + turnColor(board.getCurrentColorTurn()));
	}

	public void drawBoard() {
		String[] pieceName = {"", "P = Pawn","R = Rook","B = Bishop","N = Knight","Q = Queen","K = King",""};
		Position position;
		Piece piece = board.getSelectedPiece(), piece2;
		
		System.out.print("   ");
		for(int col = 0; col < 8; col++)
			System.out.print(AnsiColors.ANSI_GREEN + (char)('a' + col) + " ");
		System.out.println(AnsiColors.ANSI_RESET + "\n");
		for(int row = 0; row < 8; row++) {
			System.out.print(AnsiColors.ANSI_GREEN + (8 - row) + "  " + AnsiColors.ANSI_RESET);
			for(int col = 0; col < 8; col++) {
				position = new Position(row, col);
				piece2 = board.getPieceAtPosition(position);
				if (piece != null && piece.canMoveToPosition(position)) {
					if (piece2 != null) System.out.print(AnsiColors.ANSI_RED_BACKGROUND);
					else System.out.print(AnsiColors.ANSI_BLUE_BACKGROUND);
				}
				System.out.print(colorLet(piece2) + " ");
			}
			System.out.println(" " + AnsiColors.ANSI_GREEN + (8 - row) + "   " + AnsiColors.ANSI_RESET + pieceName[row]);
		}
		System.out.print("\n   ");
		for(int col = 0; col < 8; col++)
			System.out.print(AnsiColors.ANSI_GREEN + (char)('a' + col) + " ");
		System.out.println(AnsiColors.ANSI_RESET);
	}
	
	public void promotePiece(Scanner sc) {
		AnsiColors.clearScreen();
		drawBoard();
		String imput, error = "";
		System.out.print("\n" + AnsiColors.ANSI_CYAN + "Promoted piece! ");
		while (board.pieceWasPromoted()) {
			if (!error.isEmpty()) {
				AnsiColors.clearScreen();
				drawBoard();
				System.out.println("\n" + AnsiColors.ANSI_RED + error + AnsiColors.ANSI_RESET);
				error = "";
			}
			System.out.print(AnsiColors.ANSI_CYAN +
							 "Select the promotion level\n" +
							 AnsiColors.ANSI_YELLOW +
							 "(Q = Queen, B = Bishop, K = Knight, R = Rook): " +
							 AnsiColors.ANSI_GREEN);
			try {
				imput = sc.nextLine().toLowerCase();
				if (imput.equals("q")) board.promotePiece(Type.QUEEN);
				else if (imput.equals("b")) board.promotePiece(Type.BISHOP);
				else if (imput.equals("n")) board.promotePiece(Type.KNIGHT);
				else if (imput.equals("r")) board.promotePiece(Type.ROOK);
				else error = "Invalid promotion level";
			}
			catch (Exception e) { error = "Invalid promotion level"; }
		}
	}

}
