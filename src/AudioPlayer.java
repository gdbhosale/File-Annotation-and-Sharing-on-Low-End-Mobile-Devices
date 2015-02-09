

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.VolumeControl;

public class AudioPlayer extends Form implements CommandListener, Runnable, PlayerListener {
	XMLJ2MEFileRead midlet;
	private Display display;
	private Command play, back;
	private Player player;
	public String audioFile;
	public Displayable backDisplay;

	public AudioPlayer(XMLJ2MEFileRead midlet, Displayable displayable,
			String audioFile) {
		super("");
		this.midlet = midlet;
		this.display = midlet.display;
		this.audioFile = audioFile;
		this.backDisplay = displayable;

		play = new Command("Play", Command.OK, 0);
		back = new Command("Back", Command.BACK, 0);
		addCommand(play);
		addCommand(back);
		
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == play) {
			try {
				Thread t = new Thread(this);
				t.start();
			} catch (Exception e) {
			}
		} else if (c == back) {
			player.close();
			display.setCurrent(backDisplay);
		}
	}

	public void run() {
		VolumeControl vc;
		try {
			System.out.println("Creating Player...");
			// InputStream is = getClass().getResourceAsStream(videoFile); //BAD
			// player = Manager.createPlayer(is, "video/3gpp");

			// player = Manager.createPlayer(videoFile); // OK

			FileConnection fcin = (FileConnection) Connector.open(audioFile);
			player = Manager
					.createPlayer(fcin.openInputStream(), "audio/x-wav");
			System.out.println("Player Created.");
			player.addPlayerListener(this);
			player.setLoopCount(-1);
            player.prefetch();
            player.realize();
            vc = (VolumeControl) player.getControl("VolumeControl");
			if (vc != null) {
				vc.setLevel(100);
			}
            player.start();
            
		} catch (Exception e) {
			e.printStackTrace();
			Alert alert = new Alert("Audio Player","Cannot start Player: "+e.toString(),null,AlertType.ERROR);
			alert.setTimeout(3000);
			midlet.display.setCurrent(alert);
		}
	}

	public void playerUpdate(Player player, String event, Object eventData) {
		
	}
}