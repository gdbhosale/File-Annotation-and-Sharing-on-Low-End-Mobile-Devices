import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

/*
 * Thread that waits for the Connection from client who wants to search for file over this deveice.
 * This server Thread response to that query
 */
public class BTConnThread extends Thread {
	private StreamConnectionNotifier service;
	private StreamConnection conn;
	private InputStream is;
	private OutputStream os;
	private OutputStreamWriter osw;
	private XMLJ2MEFileRead parent;

	public BTConnThread(XMLJ2MEFileRead parent) {
		this.parent = parent;
	}

	public void run() {
		parent.writeConsole("MyXMLFileServer starting...");
		String str = "", queryType = null, searchType = null, query = null;

		try {
			LocalDevice localDevice = LocalDevice.getLocalDevice();
			DiscoveryAgent agent = localDevice.getDiscoveryAgent();
			String url = "btspp://localhost:102030405060708090A1B1C1D1D1E100;name=MyXMLFileServer";
			service = (StreamConnectionNotifier) Connector.open(url);
		} catch (Exception e) {
			parent.writeConsole("Cant get Device");
		}

		byte[] data = new byte[1000];
		while (true) {
			try {
				parent.writeConsole("MyXMLFileServer waiting...");
				conn = service.acceptAndOpen();
				parent.writeConsole("Connected");
				is = conn.openInputStream();
				os = conn.openOutputStream();
			} catch (Exception e) {
				parent.writeConsole("Error in Accepting Connection"
						+ e.getMessage());
				conn = null;
				continue;
			}
			try {
				is.read(data);
				str = new String(data);
				str = str.trim();
				// is.close();
				parent.writeConsole("Q:" + str);
			} catch (Exception e) {
				parent.writeConsole("Error Receiving Data");
			}
			try {
				String[] q = parent.split(str, "&");
				if (q.length == 3) {
					queryType = q[0];
					searchType = q[1];
					query = q[2];
				}
			} catch (Exception e) {
				parent.writeConsole("Error Processing Query");
			}

			String sb = new String("");
			if (queryType == null || searchType == null || query == null) {
				try {
					os.write("Wrong Query".getBytes());
					os.flush();
				} catch (Exception e) {
					parent.writeConsole("Error Sending Wrong Result");
				}
			} else if (queryType.equals("Search")) {
				try {
					Vector result = parent.searchFiles(query, searchType);
					if (result.size() != 0) {
						for (int i = 0; i < result.size(); i++) {
							String fileName = (String) result.elementAt(i);
							sb = sb + fileName + "#";
						}
						if (result.size() != 0)
							sb = sb.substring(0, sb.length() - 1);
						os.write(sb.getBytes());
						os.flush();
						parent.writeConsole("Result Sent.");
					} else {
						os.write("Error While Processing".getBytes());
						os.flush();
						parent.writeConsole("Result Sent.");
					}
				} catch (Exception e) {
					parent.writeConsole("Error Sending Result: "
							+ e.getMessage());
				}
			}
			BTFileSendThread filetras = new BTFileSendThread(parent, is, os);
			filetras.start();
			/*
			 * try { Thread.sleep(2000); if (conn != null) conn.close();
			 * if(service != null) service.close();
			 * parent.writeConsole("Connection service Closed."); } catch
			 * (Exception e) { parent.writeConsole("Error Closing Connection");
			 * }
			 */
		}
	}
}