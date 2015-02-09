import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

public class BTFileSendThread extends Thread {
	byte[] data = new byte[1000];
	XMLJ2MEFileRead parent;
	InputStream is;
	OutputStream os;
	String str, queryType, searchType, query;

	public BTFileSendThread(XMLJ2MEFileRead parent, InputStream is,
			OutputStream os) {
		this.is = is;
		this.os = os;
		this.parent = parent;
		parent.writeConsole("BTFileSendThread const");
	}

	public void run() {
		try {
			parent.writeConsole("BTFileSendThread start");
			while (true) {
				String sb = new String("");
				is.read(data);
				str = new String(data);
				if (str == null) {
					Thread.sleep(1000);
					continue;
				} else {
					parent.writeConsole("Result 2 Get Data:" + str);
					str = str.trim();
					String[] q = XMLJ2MEFileRead.split(str, "&");
					if (q.length == 3) {
						queryType = q[0];
						searchType = q[1];
						query = q[2];
						if (queryType.equals("Search")) {
							Vector result = parent.searchFiles(query,
									searchType);
							for (int i = 0; i < result.size(); i++) {
								String fileName = (String) result.elementAt(i);
								sb = sb + fileName + "#";
							}
							if (result.size() != 0)
								sb = sb.substring(0, sb.length() - 1);
							os.write(sb.getBytes());
							os.flush();
							parent.writeConsole("Result 2 Sent.");
						} else if (queryType.equals("File")) {
							sendFile(searchType, os);
							//os.write("Jai Shri Mataji".getBytes());
							os.flush();
							// Must remove after.
							break;
						}
					}
				}// else ends
			}// while ends
			parent.writeConsole("BTFileSendThread ends");
		} catch (Exception e) {
			parent.alert("Error Processing second Request:"+e.getMessage());
		}
	}
	
	void sendFile(String fileName, OutputStream os) {
		//fileName = fileName.replace('/', '\\');
		parent.writeConsole("Send File:"+fileName);
		//String url = "file://localhost/" + fileName;
		
		String currDirName = fileName.substring(0, fileName.lastIndexOf('/')).trim();
		String fileName2 = fileName.substring(fileName.lastIndexOf('/')).trim();
		
		
		String url = "file://localhost/" + currDirName + fileName2;
		//String url = "file://" + fileName;
		FileConnection fc = null;
		System.gc();
		byte[] buffer = new byte[5120];
		
		try {
			parent.writeConsole("Opening File..."+url);
			fc = (FileConnection) Connector.open(url.trim(), Connector.READ);
			if (!fc.exists()) {
				parent.writeConsole("File does not exists");
				return;
			}
			
			is = fc.openInputStream();
			parent.writeConsole("Sending File...");
			int sendBytes = 0;
			int totalBytesSent = 0;
			while((sendBytes = is.read(buffer)) != -1) {
				os.write(buffer);
				totalBytesSent += sendBytes;
			}
			os.flush();
			parent.writeConsole("File Sent. Bytes:" + totalBytesSent);
			parent.alert("File: "+currDirName + fileName2, "File Sent: Bytes:"+totalBytesSent);
			fc.close();
		} catch (Exception e) {
			parent.writeConsole("Error while sending: " + e.getMessage());
			//parent.alert("Open Text File", "Error: " + e.getMessage());
			return;
		}
	}
}