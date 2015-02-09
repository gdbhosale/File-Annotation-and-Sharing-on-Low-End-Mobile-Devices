import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;
import javax.bluetooth.*;

import java.util.Vector;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class BluetoothConnection extends List implements CommandListener, DiscoveryListener {

	String query = "Search&desc&io";

	XMLJ2MEFileRead parent;

	public static final String SERVER_NAME = "MyXMLFileServer";

	// Display Object
	public Display display;

	// Variable which defines pause status
	private boolean midletPaused = false;

	// back from BluetoothConnection
	private Command backCommand;

	// Start devices discover command
	private Command devicesDiscoveryCommand;

	// Start devices service command
	private Command serviceDiscoveryCommand;

	// The DiscoveryAgent for the local Bluetooth device.
	private DiscoveryAgent bluetoothDiscoveryAgent;

	// Keeps track of the devices found
	private Vector deviceList;

	private Vector serviceList;
	private Vector serviceRecordList;

	BTDevice lastContactedDevice;

	// Keeps track of the transaction ID returned from searchServices.
	private int transactionID;

	public boolean serviceDiscoveryCompleted = false;

	Command showConsoleCommand;

	// Result
	List resultList;
	Command backResultCommand;
	Command downloadResultCommand;

	Connection connection = null;
	StreamConnection conn = null;
	OutputStream os = null;
	OutputStreamWriter osw = null;
	InputStream is = null;
	InputStreamReader isr = null;

	public BluetoothConnection(XMLJ2MEFileRead parent) {
		super("BT Devices", List.IMPLICIT);
		this.parent = parent;
		display = parent.display;

		serviceList = new Vector();
		serviceRecordList = new Vector();

		backCommand = new Command("Back", Command.BACK, 0);
		devicesDiscoveryCommand = new Command("Search.Devices", Command.SCREEN, 0);
		serviceDiscoveryCommand = new Command("Search Query", Command.SCREEN, 0);
		showConsoleCommand = new Command("Console", Command.HELP, 0);
		backResultCommand = new Command("Back", Command.BACK, 0);
		downloadResultCommand = new Command("Download", Command.OK, 0);

		addCommand(backCommand);
		addCommand(devicesDiscoveryCommand);
		addCommand(showConsoleCommand);
		setCommandListener(this);
		display.setCurrent(this);
		parent.currentMenu = "BluetoothConnection";

		resultList = new List("Remote Files", List.IMPLICIT);
		resultList.addCommand(backResultCommand);
		resultList.setSelectCommand(downloadResultCommand);
		resultList.setCommandListener(this);
		// Retrieve the DiscoveryAgent object that allows us to perform device
		// and service discovery.
		try {
			bluetoothDiscoveryAgent = LocalDevice.getLocalDevice().getDiscoveryAgent();
		} catch (BluetoothStateException ex) {
			parent.writeConsole("Local Device Not Found\n" + ex.getMessage());
			// It means that the Bluetooth system could not be initialized
		}
		// Initialize the transactionID
		transactionID = -1;
		deviceList = new Vector();
	}

	public void setParam() {
		String data1 = parent.tdata.getString();
		String searchTypeStr, data;

		if (parent.searchType.equals("File Size")) {
			searchTypeStr = "size";
			String data2 = parent.tdata2.getString();
			data = data1 + "-" + data2;
		} else if (parent.searchType.equals("File Name")) {
			searchTypeStr = "name";
			data = data1;
		} else if (parent.searchType.equals("File Creation Date")) {
			searchTypeStr = "date";
			data = data1;
		} else if (parent.searchType.equals("Keyword")) {
			searchTypeStr = "key";
			data = data1;
		} else if (parent.searchType.equals("Description")) {
			searchTypeStr = "desc";
			data = data1;
		} else {
			searchTypeStr = "name";
			data = data1;
		}

		this.query = "Search&" + searchTypeStr + "&" + data;
	}

	public void commandAction(Command command, Displayable displayable) {

		if (displayable == this) {
			if (command == devicesDiscoveryCommand) {
				try {
					// Ui-components clearing from old data
					deviceList.removeAllElements();
					this.deleteAll();

					// start discover for new bluetooth devices
					bluetoothDiscoveryAgent.startInquiry(DiscoveryAgent.GIAC, this);
					setTitle("BT Devices: Searching...");
				} catch (BluetoothStateException e) {
					e.printStackTrace();
					parent.writeConsole("Device Discovery Error\n" + e.getMessage());
					// Failed to start device discovery
				}
			} else if (command == serviceDiscoveryCommand) {
				if (deviceList.size() == 0) {
					return;
				}
				try {
					// The class is used to represent a universally unique
					// identifier used widely as the value for a service
					// attribute.
					// Add the UUID for L2CAP to make sure that the service
					// record
					// found will support L2CAP.
					// You can change it according to The Bluetooth Assigned
					// Numbers document.
					UUID[] searchList = new UUID[1];
					searchList[0] = new UUID(0x0100);

					// Initialization of service attributes whose values will be
					// retrieved on services which have the UUIDs specified
					// in searchList
					int[] attributesList = new int[2];
					attributesList[0] = 3;
					attributesList[1] = 256;

					// get current selected remote device by index from
					// ChoiceGroup
					RemoteDevice currentDevice = (RemoteDevice) deviceList.elementAt(this.getSelectedIndex());
					if (currentDevice == null) {
						return;
					}
					if (serviceDiscoveryCompleted && currentDevice.getFriendlyName(false).equals(lastContactedDevice.getFriendlyName())) {

						parent.writeConsole("Already Contacted Device=" + lastContactedDevice.getFriendlyName());
						sendQuery(true);

					} else {
						serviceDiscoveryCompleted = false;
						lastContactedDevice = new BTDevice(currentDevice);
						lastContactedDevice.setFriendlyName(currentDevice.getFriendlyName(false));

						// start searching services on current device
						// and get transaction ID of the service search
						transactionID = bluetoothDiscoveryAgent.searchServices(attributesList, searchList, currentDevice, this);
					}
				} catch (Exception e) {
					e.printStackTrace();
					parent.writeConsole("Service Discovery Error\n" + "Failed to start the search on this device\n" + e.getMessage());
					// Failed to start service search on this device.
				}
			} else if (command == backCommand) {
				display.setCurrent(parent.subMenu);
			} else if (command == showConsoleCommand) {
				display.setCurrent(parent.console);
			}
		} else if (displayable == resultList) {
			if (command == backResultCommand) {
				display.setCurrent(this);
			} else if (command == downloadResultCommand) {
				String fileString = resultList.getString(resultList.getSelectedIndex());
				new BTFileRecThread(parent, is, osw, fileString.trim()).start();
			}
		}
	}

	/**
	 * From DiscoveryListener. Called when a device was found during an inquiry.
	 * An inquiry searches for devices that are discoverable. The same device
	 * may be returned multiple times.
	 * 
	 * @see DiscoveryAgent#startInquiry
	 * @param btDevice
	 *            the device that was found during the inquiry
	 * @param cod
	 *            the service classes, major device class, and minor device
	 *            class of the remote device being returned
	 */
	public synchronized void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		// adding new device object to ChoiceGroup and to Vector objects
		String friendlyName = null;
		try {
			friendlyName = btDevice.getFriendlyName(false);
		} catch (IOException e) {
			e.printStackTrace();
			// Failed to get device name
		}
		String choiceElementText = (friendlyName == null) ? btDevice.getBluetoothAddress() : friendlyName;
		deviceList.addElement(btDevice);
		append(choiceElementText, null);
	}

	/**
	 * From DiscoveryListener. Called when a device discovery transaction is
	 * completed.
	 * 
	 * @param discType
	 *            the type of request that was completed
	 */
	public void inquiryCompleted(int discType) {
		// No implementation required
		switch (discType) {
		case DiscoveryListener.INQUIRY_COMPLETED:
			parent.writeConsole("Device discovery Complete!");
			setSelectCommand(serviceDiscoveryCommand);
			break;
		case DiscoveryListener.INQUIRY_TERMINATED:
			parent.writeConsole("Device discovery terminated!");
			deleteAll();
			break;
		case DiscoveryListener.INQUIRY_ERROR:
			parent.writeConsole("Device discovery error!");
			deleteAll();
			break;
		default:
			throw new IllegalArgumentException("Unknown type of message!");
		}
		setTitle("BT Devices: Complete");
	}

	/**
	 * From DiscoveryListener. Called when service(s) are found during a service
	 * search. This method provides the array of services that have been found.
	 * 
	 * @param transID
	 *            the transaction ID of the service search that is posting the
	 *            result
	 * @param service
	 *            a list of services found during the search request
	 */
	public synchronized void servicesDiscovered(int transID, ServiceRecord[] serviceRecords) {
		parent.writeConsole("servicesDiscovered " + serviceRecords.length);
		for (int i = 0; i < serviceRecords.length; i++) {
			String serviceName = (String) (serviceRecords[i].getAttributeValue(256).getValue());

			serviceList.addElement(serviceName);
			serviceRecordList.addElement(serviceRecords[i]);

			parent.writeConsole("Add Service: " + lastContactedDevice.getFriendlyName() + ":" + serviceName);

			if (serviceName.equals(SERVER_NAME)) {
				bluetoothDiscoveryAgent.cancelServiceSearch(transID);
				lastContactedDevice.setServiceRecord(serviceRecords[i]);
			}
		}
	}

	/**
	 * From DiscoveryListener. The following method is called when a service
	 * search is completed or was terminated because of an error.
	 * 
	 * @param transID
	 *            the transaction ID identifying the request which initiated the
	 *            service search
	 * @param respCode
	 *            the response code which indicates the status of the
	 *            transaction; guaranteed to be one of the aforementioned only
	 */
	public void serviceSearchCompleted(int transID, int respCode) {
		parent.writeConsole("serviceSearchCompleted");
		switch (respCode) {
		case DiscoveryListener.SERVICE_SEARCH_COMPLETED:
			parent.writeConsole("Services discovering Complete on device:" + lastContactedDevice.getFriendlyName());
			parent.alert("Device not supports the Connection");
			break;
		case DiscoveryListener.SERVICE_SEARCH_TERMINATED:
			parent.writeConsole("Services discovering terminated!");
			if (serviceList.contains("MyXMLFileServer")) {
				parent.writeConsole("Device supports the Connection");
				sendQuery(false);
				serviceDiscoveryCompleted = true;
			} else {
				parent.writeConsole("Device not supports the Connection");
			}
			break;
		case DiscoveryListener.SERVICE_SEARCH_ERROR:
			parent.writeConsole("Sevice discovering error!");
			break;
		default:
			break;
		}
		transactionID = -1;
	}

	private void sendQuery(boolean isPreviousDevice) {
		try {
			parent.writeConsole("Connecting to " + lastContactedDevice.getConnURL());
			if (!isPreviousDevice) {
				connection = Connector.open(lastContactedDevice.getConnURL());
				conn = (StreamConnection) connection;
				parent.writeConsole("Connected. Opening Streams");
				// send string
				os = conn.openOutputStream();
				osw = new OutputStreamWriter(os);
				is = conn.openInputStream();
				// isr = new InputStreamReader(is);
			}

			parent.writeConsole("Writing Data=" + query + "...");
			osw.write(query);
			osw.flush();

			parent.writeConsole("Reading Data...");

			byte[] buffer = new byte[100];
			is.read(buffer);
			String lineRead = new String(buffer);
			parent.writeConsole("Result Received.");

			String[] results = XMLJ2MEFileRead.split(lineRead, "#");
			resultList.deleteAll();
			resultList.setTitle("Remote Files:" + lastContactedDevice.getFriendlyName());
			for (int i = 0; i < results.length; i++) {
				resultList.append(results[i], null);
			}
			/*
			 * result = new TextBox("Result", lineRead, 5000, TextField.ANY);
			 * result.addCommand(backResultCommand);
			 * result.setCommandListener(this); display.setCurrent(result);
			 */
			display.setCurrent(resultList);
			parent.writeConsole("OK");
			/*
			 * Thread.sleep(1000); conn.close();
			 */
		} catch (Exception e) {
			e.printStackTrace();
			parent.writeConsole("Error While Sending Query:-\n" + e.getMessage());
		}
	}
}