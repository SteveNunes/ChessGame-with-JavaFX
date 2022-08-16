package application;

public final class AnsiColors {
	
	private static Boolean colored = false;
	public static String ANSI_RESET = "";
	public static String ANSI_BLACK = "";
	public static String ANSI_RED = "";
	public static String ANSI_GREEN = "";
	public static String ANSI_YELLOW = "";
	public static String ANSI_BLUE = "";
	public static String ANSI_PURPLE = "";
	public static String ANSI_CYAN = "";
	public static String ANSI_WHITE = "";
	public static String ANSI_BLACK_BACKGROUND = "";
	public static String ANSI_RED_BACKGROUND = "";
	public static String ANSI_GREEN_BACKGROUND = "";
	public static String ANSI_YELLOW_BACKGROUND = "";
	public static String ANSI_BLUE_BACKGROUND = "";
	public static String ANSI_PURPLE_BACKGROUND = "";
	public static String ANSI_CYAN_BACKGROUND = "";
	public static String ANSI_WHITE_BACKGROUND = "";
	public static String BLACK_PIECE = "";
	public static String WHITE_PIECE = "";
	public static String BLACK_PIECE_BACKGROUND = "";
	public static String WHITE_PIECE_BACKGROUND = "";
	
	public static void setToColored() {
		ANSI_RESET = "\u001B[0m";
		ANSI_BLACK = "\u001B[30m";
		ANSI_RED = "\u001B[31m";
		ANSI_GREEN = "\u001B[32m";
		ANSI_YELLOW = "\u001B[33m";
		ANSI_BLUE = "\u001B[34m";
		ANSI_PURPLE = "\u001B[35m";
		ANSI_CYAN = "\u001B[36m";
		ANSI_WHITE = "\u001B[37m";
		ANSI_BLACK_BACKGROUND = "\u001B[40m";
		ANSI_RED_BACKGROUND = "\u001B[41m";
		ANSI_GREEN_BACKGROUND = "\u001B[42m";
		ANSI_YELLOW_BACKGROUND = "\u001B[43m";
		ANSI_BLUE_BACKGROUND = "\u001B[44m";
		ANSI_PURPLE_BACKGROUND = "\u001B[45m";
		ANSI_CYAN_BACKGROUND = "\u001B[46m";
		ANSI_WHITE_BACKGROUND = "\u001B[47m";
		BLACK_PIECE = ANSI_YELLOW;
		WHITE_PIECE = ANSI_CYAN;
		BLACK_PIECE_BACKGROUND = ANSI_YELLOW_BACKGROUND;
		WHITE_PIECE_BACKGROUND = ANSI_CYAN_BACKGROUND;
		colored = true;
	}
	
	public static void setToPB() {
		ANSI_RESET = "";
		ANSI_BLACK = "";
		ANSI_RED = "";
		ANSI_GREEN = "";
		ANSI_YELLOW = "";
		ANSI_BLUE = "";
		ANSI_PURPLE = "";
		ANSI_CYAN = "";
		ANSI_WHITE = "";
		ANSI_BLACK_BACKGROUND = "";
		ANSI_RED_BACKGROUND = "";
		ANSI_GREEN_BACKGROUND = "";
		ANSI_YELLOW_BACKGROUND = "";
		ANSI_BLUE_BACKGROUND = "";
		ANSI_PURPLE_BACKGROUND = "";
		ANSI_CYAN_BACKGROUND = "";
		ANSI_WHITE_BACKGROUND = "";
		BLACK_PIECE = "";
		WHITE_PIECE = "";
		BLACK_PIECE_BACKGROUND = "";
		WHITE_PIECE_BACKGROUND = "";
		colored = false;
	}

	public static final void clearScreen() {
		if (colored) {
			System.out.print("\033[H\033[2J");
			System.out.flush();
		}
		else {
			for(int n = 0; n < 40; n++)
				System.out.println();
		}
	}

}