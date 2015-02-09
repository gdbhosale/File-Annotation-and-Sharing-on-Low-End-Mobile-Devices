

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.VideoControl;
import javax.microedition.media.control.VolumeControl;

public class VideoPlayer extends Form implements CommandListener,
		PlayerListener, Runnable {
	XMLJ2MEFileRead midlet;
	private Display display;
	private Command play, back;
	private Player player;
	public String videoFile;
	public Displayable backDisplay;

	public VideoPlayer(XMLJ2MEFileRead midlet, Displayable displayable,
			String videoFile) {
		super("");
		this.midlet = midlet;
		this.display = midlet.display;
		this.videoFile = videoFile;
		this.backDisplay = displayable;

		play = new Command("Play", Command.OK, 0);
		back = new Command("Back", Command.BACK, 0);
		addCommand(play);
		addCommand(back);
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == play) {
			Thread t = new Thread(this);
			t.start();
		} else if (c == back) {
			player.close();
			display.setCurrent(backDisplay);
		}
	}

	public void playerUpdate(Player player, String event, Object eventData) {
		if (event.equals(PlayerListener.STARTED)
				&& new Long(0L).equals((Long) eventData)) {
			VideoControl vc = null;
			if ((vc = (VideoControl) player.getControl("VideoControl")) != null) {
				Item videoDisp = (Item) vc.initDisplayMode(
						vc.USE_GUI_PRIMITIVE, null);
				append(videoDisp);
			}
			display.setCurrent(this);
		} else if (event.equals(PlayerListener.CLOSED)) {
			deleteAll();
		}
	}

	public void run() {
		try {
			System.out.println("Creating Player...");
			// InputStream is = getClass().getResourceAsStream(videoFile); //BAD
			// player = Manager.createPlayer(is, "video/3gpp");

			// player = Manager.createPlayer(videoFile); // OK

			FileConnection fcin = (FileConnection) Connector.open(videoFile);
			player = Manager.createPlayer(fcin.openInputStream(), "video/3gpp");

			System.out.println("Player Created.");

			player.addPlayerListener(this);
			player.setLoopCount(-1);
			System.out.println("Prefetching");
			player.prefetch();
			System.out.println("Realizing...");
			player.realize();
			VolumeControl vc = (VolumeControl) player
					.getControl("VolumeControl");
			if (vc != null) {
				vc.setLevel(100);
			}
			System.out.println("Starting...");
			player.start();
			System.out.println("Video Started");
		} catch (Exception e) {
			e.printStackTrace();
			Alert alert = new Alert("Open Video File", "Error: "
					+ e.getMessage(), null, AlertType.ERROR);
			alert.setTimeout(3000);
			display.setCurrent(alert);
		}
	}
}