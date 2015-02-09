

/**
 *
 * @author Shweta Guja
 */
import java.io.IOException;
import javax.microedition.lcdui.Ticker;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;
import javax.microedition.midlet.MIDlet;


/**
 * A simple splash screen.
 */
public class SplashScreen extends Canvas implements Runnable {

	private Image mImage;
	private MIDlet projectMIDlet;

	/**
	 * The constructor attempts to load the named image and begins a timeout
	 * thread. The splash screen can be dismissed with a key press, a pointer
	 * press, or a timeout
	 * 
	 * @param projectMIDlet instance of MIDlet
	 */
	public SplashScreen(MIDlet projectMIDlet) {
		this.projectMIDlet = projectMIDlet;
		try {
			mImage = Image.createImage("/folder-find-icon.png");
			Thread t = new Thread(this);
			t.start();
			setFullScreenMode(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Paints the image centered on the screen.
	 */
	public void paint(Graphics g) {
		int width = getWidth();
		int height = getHeight();

		// set background color to overdraw what ever was previously displayed
		g.setColor(234, 194, 235);
		g.fillRect(0, 0, width, height);
		g.setColor(68, 38, 46);
		g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD,
				Font.SIZE_MEDIUM));
		g.drawString("FILE ANNOTATION SYSTEM", width / 2, (height / 2)+30,
				Graphics.TOP | Graphics.HCENTER);
		g.setColor(85, 0, 176);
		g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD,
				Font.SIZE_SMALL));
		g.drawImage(mImage, width / 2, 65, Graphics.HCENTER
				| Graphics.VCENTER);
		g.drawString("version 1.0", (width / 2), (height / 2) + 70,
				Graphics.TOP | Graphics.HCENTER);
		this.setTicker(new Ticker("Loading..............."));
	}

	/**
	 * Dismisses the splash screen with a key press or a pointer press
	 */
	public void dismiss() {
		if (isShown()) {
			Display.getDisplay(projectMIDlet);
		}
	}

	/**
	 * Default timeout with thread
	 */
	public void run() {
		try {
			Thread.sleep(3000);// set for 3 seconds
		} catch (InterruptedException e) {
			System.out.println("InterruptedException");
			e.printStackTrace();
		}
		dismiss();
	}

	/**
	 * A key release event triggers the dismiss() method to be called.
	 */
	public void keyReleased(int keyCode) {
		dismiss();
	}

	/**
	 * A pointer release event triggers the dismiss() method to be called.
	 */
	public void pointerReleased(int x, int y) {
		dismiss();
	}
}