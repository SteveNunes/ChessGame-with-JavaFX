package application;

import util.IniFile;

public class Test {

	public static void main(String[] args) {
		IniFile ini = IniFile.getNewIniFileInstance("D:\\Java\\ChessGame com JavaFX\\config.ini");
		ini.write("CONFIG", "teste", "master");
	}

}
