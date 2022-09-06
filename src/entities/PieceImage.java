package entities;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class PieceImage {
	
	private Image image;
	private Color transparent;
	private int toleranceThreshold;
	private int sourceW;
	private int sourceH;
	private int targetW;
	private int targetH;
	private String filePath;
	
	public PieceImage() {}
	
	public PieceImage(Image image, Color transparent, int toleranceThreshold, int sourceW, int sourceH, int targetW, int targetH, String filePath) {
		this.filePath = filePath;
		this.image = image;
		this.transparent = transparent;
		this.sourceW = sourceW;
		this.sourceH = sourceH;
		this.targetW = targetW;
		this.targetH = targetH;
		this.toleranceThreshold = toleranceThreshold;
	}

	public Image getImage()
		{ return image; }

	public Color getTransparent()
		{ return transparent; }

	public int getToleranceThreshold()	
		{ return toleranceThreshold; }	
	
	public String getFilePath()
		{ return filePath; }

	public int getSourceW()
		{ return sourceW; }

	public int getSourceH()
		{ return sourceH; }

	public int getTargetW()
		{ return targetW; }

	public int getTargetH()
		{ return targetH; }
	
}