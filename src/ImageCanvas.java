

import javax.microedition.lcdui.*;

public class ImageCanvas extends Canvas {
	public XMLJ2MEFileRead midlet;
	public byte[] img;
	public String url;
	Displayable backDisplay;
	public String source = "";

	public ImageCanvas(XMLJ2MEFileRead midlet2, byte[] b, Displayable current) {
		this.midlet = midlet2;
		this.img = b;
		this.backDisplay = current;
		source = "bytes";
		setFullScreenMode(true);
	}

	public void paint(Graphics g) {
		Image image = null;
		// image = Image.createImage(img, 0, img.length);
		if (source.equals("url")) {
			// FileConnection fcin = (FileConnection) Connector.open(url);
			// image = Image.createImage(fcin.openInputStream());
		} else if (source.equals("bytes")) {
			image = Image.createImage(img, 0, img.length);
		} else {
			return;
		}
		g.drawImage(image, 0, 0, 0);
		g.setColor(0, 0, 0);
		g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
				Font.SIZE_SMALL));
		g.drawString("Back", 204, 293, 0);
	}

	protected void keyPressed(int paramInt) {
		if (paramInt == -7) {
			midlet.display.setCurrent(backDisplay);
		} else if (paramInt == -5) {
			// saveImage();
		}
	}
}