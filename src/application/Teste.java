package application;

import java.util.Scanner;

import board.Board;
import enums.ChessPlayMode;
import enums.PieceColor;
import enums.PieceType;
import piece.Piece;
import piece.PiecePosition;
import util.MyFiles;

public class Teste {
	
	private static Board board;
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		while (true) {
			board = new Board();
			Character[][] pieces = new Character[][] {
				{'k',' ',' ',' ',' ',' ',' ',' '},
				{' ',' ',' ',' ',' ',' ',' ',' '},
				{' ',' ',' ',' ',' ',' ',' ',' '},
				{'r',' ','R',' ',' ',' ',' ',' '},
				{' ',' ',' ','P',' ',' ',' ',' '},
				{' ',' ',' ',' ',' ',' ',' ',' '},
				{' ',' ',' ',' ',' ',' ','P','P'},
				{' ',' ',' ',' ',' ',' ','R','K'}
			};
			Piece[][] b = new Piece[8][8];
			try
				{ board.setBoard(pieces);	}
			catch (Exception e) {}
			board.validateBoard();
			board.setPlayMode(ChessPlayMode.CPU_VS_CPU);
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
