package application;

import java.util.Scanner;

import board.Board;
import enums.ChessPlayMode;
import enums.PieceColor;
import enums.PieceType;
import gameutil.Position;
import piece.Piece;

public class BoardTest {
	
	private static Board board;
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		while (true) {
			board = new Board();
			Character[][] pieces = new Character[][] {
//				{'r','n','b','q','k','b','n','r'},
//				{'p','p','p','p','p','p','p','p'},
//				{' ',' ',' ',' ',' ',' ',' ',' '},
//				{' ',' ',' ',' ',' ',' ',' ',' '},
//				{' ',' ',' ',' ',' ',' ',' ',' '},
//				{' ',' ',' ',' ',' ',' ',' ',' '},
//				{'P','P','P','P','P','P','P','P'},
//				{'R','N','B','Q','K','B','N','R'}
			{'k','p',' ',' ',' ',' ',' ','r'},
			{'p','p',' ',' ',' ',' ',' ','p'},
			{' ',' ',' ',' ',' ',' ','Q',' '},
			{' ',' ',' ',' ',' ',' ',' ',' '},
			{' ',' ',' ',' ',' ',' ',' ',' '},
			{'P',' ',' ',' ',' ',' ',' ',' '},
			{' ',' ',' ',' ',' ',' ',' ',' '},
			{'K',' ',' ',' ',' ',' ',' ',' '}
			};
			Piece[][] b = new Piece[8][8];
			try
				{ board.setBoard(pieces);	}
			catch (Exception e) {}
			board.validateBoard();
			board.setPlayMode(ChessPlayMode.PLAYER_VS_CPU);
			board.setCpuColor(PieceColor.WHITE);
			board.getChessAI().doCpuSelectAPiece();
			for (int y = 0; y < 8; y++)
				for (int x = 0; x < 8; x++)
						b[y][x] = board.getPieceAt(new Position(x, y));
						
			try { board.getChessAI().doCpuMoveSelectedPiece(); } catch (Exception e) {}
			
			System.out.println();
			for (int y = -1; y < 8; y++) {
				for (int z = 0; z < 2; z++) {
					for (int x = -1; x < 8; x++) {
						if (x == -1)
							System.out.print((y == -1 ? " " : y) + " ");
						else if (y == -1)
							System.out.print(" " + x + " ");
						else if (z == 0)
							System.out.print(qt(b[y][x]));
						else {
							Piece piece = board.getPieceAt(new Position(x,y));
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
				(piece == null ? " " : piece.isWhite() ? piece.let() : Character.toLowerCase(piece.let())) +
				(piece == board.getLastMovedPiece() ? ">" : "]"); }

}
