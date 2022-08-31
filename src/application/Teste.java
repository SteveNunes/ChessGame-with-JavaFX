package application;

import java.util.Scanner;

import board.Board;
import enums.ChessPlayMode;
import enums.PieceColor;
import enums.PieceType;
import piece.Piece;
import piece.PiecePosition;

public class Teste {
	
	private static Board board;
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		try {
			int a = 1 + Integer.parseInt("a");
			System.out.println("Ola " + a);
			a += Integer.parseInt("2");
			System.out.println("Ola2 " + a);
		}
		catch (Exception e) {}
		
		while (true) {
			board = new Board(PieceColor.WHITE);
			board.setPlayMode(ChessPlayMode.CPU_VS_CPU);
			Character[][] pieces = new Character[][] {
				{'K','R',' ',' ',' ',' ',' ',' '},
				{'P','P',' ',' ','p',' ',' ',' '},
				{' ',' ',' ','b',' ',' ',' ',' '},
				{' ',' ',' ',' ',' ',' ',' ',' '},
				{' ',' ',' ',' ',' ',' ',' ',' '},
				{' ',' ',' ','R',' ',' ',' ',' '},
				{' ',' ',' ',' ',' ',' ','p','p'},
				{' ',' ',' ',' ',' ',' ','p','k'}
			};
			Piece[][] b = new Piece[8][8];
			try
				{ board.setBoard(pieces);	}
			catch (Exception e) {}
			board.validateBoard();
			board.getChessAI().doCpuSelectAPiece();
			for (int y = 0; y < 8; y++)
				for (int x = 0; x < 8; x++)
						b[y][x] = board.getPieceAt(new PiecePosition(y, x));
						
			try { board.getChessAI().doCpuMoveSelectedPiece(); } catch (Exception e) {}
			
			System.out.println();
			for (int y = 0; y < 8; y++) {
				for (int z = 0; z < 2; z++) {
					for (int x = 0; x < 8; x++) {
						if (z == 0)
							System.out.print(qt(b[y][x]));
						else {
							Piece piece = board.getPieceAt(new PiecePosition(y,x));
							System.out.print(qt(piece));
						}
					}
					System.out.print("      ");
				}
				System.out.println();
			}
			if (board.pawnWasPromoted())
				try { board.promotePawnTo(PieceType.QUEEN); } catch (Exception e) {}
			sc.nextLine();
		}
	}
	
	private static String qt(Piece piece)
		{ return (piece == board.getLastMovedPiece() ? "<" : "[") +
				(piece == null ? " " : piece.getColor() == PieceColor.WHITE ? piece.let() : Character.toLowerCase(piece.let())) +
				(piece == board.getLastMovedPiece() ? ">" : "]"); }

}
