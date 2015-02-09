import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

public class BTFileRecThread extends Thread {
	byte[] data = new byte[1000];
	XMLJ2MEFileRead parent;
	InputStream is;
	OutputStreamWriter osw;
	String str, queryType, searchType, query;
	String downFileName;

	public BTFileRecThread(XMLJ2MEFileRead parent, InputStream is,
			OutputStreamWriter osw, String downFileName) {
		this.is = is;
		this.osw = osw;
		this.parent = parent;
		this.downFileName = downFileName;
	}

	public void run() {
		FileConnection fc = null;
		OutputStream os;
		try {
			System.gc();
			String query = "File&" + downFileName + "&OK";
			parent.writeConsole("Query=" + query + "...");
			osw.write(query);
			osw.flush();

			/*
			 * FileConnection fcFolder = (FileConnection) Connector
			 * .open("file://localhost/E:/download/"); if (!fcFolder.exists()) {
			 * fcFolder.create(); parent.writeConsole("Folder Created."); }
			 * fcFolder.close();
			 */

			String fileURL = "file://localhost/E:/" + downFileName.substring(downFileName.lastIndexOf('/') + 1);
			// file://localhost/E:/FileInfo.xml
			parent.writeConsole(fileURL);
			parent.writeConsole("Opening File...");

			// Way 1
			fc = (FileConnection) Connector.open(fileURL);
			if (!fc.exists()) {
				// This is creating Problem
				fc.create();
				parent.writeConsole("File Created...");
			}
			os = fc.openOutputStream();
			byte[] buffer = new byte[1024];
			parent.writeConsole("Receiving...");
			int bytes = 0;
			int totalBytesRec = 0;
			while ((bytes = is.read(buffer)) != -1) {
				os.write(buffer);
				totalBytesRec += bytes;
			}
			fc.close();

			// Way 2
			/*
			 * ImageCanvas imgCanvas = null; try { byte[] buffer = new
			 * byte[is.available()]; is.read(buffer);
			 * parent.writeConsole("Reading Stream done: " + buffer.length);
			 * imgCanvas = new ImageCanvas(parent, buffer,
			 * display.getCurrent()); parent.display.setCurrent(imgCanvas); }
			 * catch (IOException e) { e.printStackTrace();
			 * parent.alert("Streaming Image Error", "Error: " +
			 * e.getMessage()); }
			 */

			parent.writeConsole("File Received "+totalBytesRec);
			parent.alert("File Downloaded " + downFileName);

		} catch (Exception e) {
			e.printStackTrace();
			parent.writeConsole("Error Receiving File: " + e.getMessage());
			parent.alert("Error RX File", e.getMessage());
		}
	
	}
}