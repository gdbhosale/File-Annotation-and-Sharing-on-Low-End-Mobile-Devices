/*
 * XML Parsing using kxml2
 * Author : Shweta Guja
 */

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import java.io.*;
import java.util.*;

/*
 * XML J2ME File Annotation Midlet
 */
public class XMLJ2MEFileRead extends MIDlet implements CommandListener {

	public String currDirName;

	FileConnection fc;
	List browser;
	FileConnection currDir = null;
	String fileName;

	private final static String UP_DIRECTORY = "..";
	private final static String MEGA_ROOT = "/";
	private final static String SEP_STR = "/";
	private final static char SEP = '/';

	private final static int BY_FILE_NAME = 0;
	private final static int BY_FILE_SIZE = 1;
	private final static int BY_FILE_DATE = 2;
	private final static int BY_FILE_KEYWORD = 3;
	private final static int BY_FILE_DESC = 4;

	BluetoothConnection btconn;

	Display display = null;
	Form annotateForm = new Form("File Annotate System");
	TextField filename, filesize, modifieddate, keywords, desc;
	public Form formsearch;
	public List formsearchresult;
	public TextField tdata, tdata2;
	public String searchType;

	// a menu with items
	public List mainMenu = null, annotatemenu = null, advancemenu = null;
	// main menu
	public List subMenu = null;

	// textbox
	TextBox input = null;

	// commands
	private Command viewFileCommand = new Command("Annotate Files",
			Command.ITEM, 1);
	private Command openFileCommand = new Command("Open", Command.ITEM, 1);
	private Command backFileCommand = new Command("Back", Command.BACK, 2);

	static final Command backCommand = new Command("Back", Command.BACK, 0);
	static final Command exitCommand = new Command("Exit", Command.EXIT, 2);
	// static final Command optionsCommand = new Command("Options",
	// Command.ITEM,
	// 1);
	// static final Command mainMenuCommand = new Command("Main",
	// Command.SCREEN, 1);
	private final static Command searchCommand = new Command("Search Files",
			Command.OK, 1);
	private final static Command searchPANCommand = new Command("Search PAN",
			Command.OK, 1);
	private final static Command annoteteCommand = new Command("Annotate",
			Command.OK, 1);

	String currentMenu = null;

	// Console List
	public static List console;
	Command backConsoleCommand = new Command("Back", Command.BACK, 0);

	// Location of xml file // not in use now
	static final String URL = "http://127.0.0.1:8080/MyServlet/FileInfo.xml";
	// Vector Storing FileInfo Objects retrieved from XML File
	Vector fileVector = new Vector();

	/*
	 * Constructor of this Class
	 */
	public XMLJ2MEFileRead() {
		currDirName = MEGA_ROOT;
		/*
		 * mainForm.append (resultItem); mainForm.addCommand (xmlCommand);
		 * mainForm.setCommandListener (this);
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.microedition.midlet.MIDlet#startApp()
	 */
	public void startApp() {
		display = Display.getDisplay(this);
		display.setCurrent(new SplashScreen(this));
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		console = new List("Console", List.IMPLICIT);
		console.addCommand(backConsoleCommand);
		console.setCommandListener(this);
		btconn = new BluetoothConnection(this);

		mainMenu = new List("J2ME XML File Read", Choice.IMPLICIT);
		mainMenu.append("Annotate Files", null);
		mainMenu.append("Updated Metadata", null);
		mainMenu.append("Advance Search", null);
		// smainMenu.append("Search PAN", null);
		mainMenu.append("See Console", null);
		// menu.append("Meta Data list", null);
		// menu.append("File Annotation", null);
		mainMenu.addCommand(exitCommand);
		mainMenu.setCommandListener(this);

		displayMainMenu();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		new ReadXML().start();
		new BTConnThread(this).start();
		writeConsole("All Thread Started");
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) {
	}

	public void commandAction(Command command, Displayable d) {
		String label = command.getLabel();

		if (d == browser && command == openFileCommand) {
			List curr = (List) d;
			final String currFile = curr.getString(curr.getSelectedIndex());
			showFile(currFile);
			return;
		}
		if (command == viewFileCommand || command == openFileCommand) {
			List curr = (List) d;
			final String currFile = curr.getString(curr.getSelectedIndex());
			writeConsole("View File:" + currFile);
			if (curr == formsearchresult) {
				currDirName = currFile.substring(0, currFile.lastIndexOf('/'));
				showFile(currFile.substring(currFile.lastIndexOf('/')));
				return;
			}
			new Thread(new Runnable() {
				public void run() {
					if (currFile.endsWith(SEP_STR)
							|| currFile.equals(UP_DIRECTORY)) {
						traverseDirectory(currFile);
					} else {
						// showFile(currFile);
						fileName = currFile;
						annotateFile(currDirName + fileName);
					}
				}
			}).start();

		} else if (command == backFileCommand) {
			showCurrDir();

		} else if (command == backConsoleCommand) {
			if (currentMenu.equals("BluetoothConnection"))
				display.setCurrent(btconn);
			else
				display.setCurrent(mainMenu);

		} else if (command == searchCommand) {
			writeConsole("Searching Files...");
			searchFiles(tdata.getString(), subMenu.getSelectedIndex());
			currentMenu = "itemAdvSearch";

		} else if (command == searchPANCommand) {
			writeConsole("Searching PAN Files...");
			currentMenu = "itemAdvSearch";
			btconn.setParam();
			display.setCurrent(btconn);

		} else if (command == annoteteCommand) {
			try {
				FileConnection fc = (FileConnection) Connector.open(
						"file://localhost/E:/FileInfo.xml",
						Connector.READ_WRITE);
				fc.setWritable(true);
				fc.setReadable(true);
				// keywords.setString("file Opened");
				// fc.setFileConnection("file://localhost/E:/FileInfo.xml");
				OutputStream out = fc.openOutputStream(fc.fileSize() - 8);
				PrintStream ps = new PrintStream(out);
				ps.print("<File>" + "\n");
				ps.print("<FileName>" + filename.getString() + "</FileName>"
						+ "\n");
				ps.print("<FileSize>" + filesize.getString() + "</FileSize>"
						+ "\n");
				ps.print("<FileDate>" + modifieddate.getString()
						+ "</FileDate>" + "\n");
				ps.print("<keywords>" + keywords.getString() + "</keywords>"
						+ "\n");
				ps.print("<description>" + desc.getString() + "</description>"
						+ "\n");
				ps.print("</File>" + "\n");
				ps.print("</start>");
				out.flush();
				out.close();
				fc.close();
				// Reload the XML data for Search
				writeConsole("Annotation complete");
				new ReadXML().start();
				writeConsole("XML Reload Complete\nDisplaying Main Menu");
				displayMainMenu();
				return;
			} catch (Exception ex) {
				ex.printStackTrace();
				writeConsole("File Annotate Error\n" + ex.getMessage());
			}
		} else if (label.equals("Exit")) {
			notifyDestroyed();
		} else if (label.equals("Back")) {
			if (currentMenu.equals("item1") || currentMenu.equals("item2")
					|| currentMenu.equals("itemAnnotateFiles")
					|| currentMenu.equals("itemAdvSearch")
					|| currentMenu.equals("itemUpdatedMeta")
					|| currentMenu.equals("Sub")) {
				displayMainMenu();
			} else if (currentMenu.equals("item11ByFileName")
					|| currentMenu.equals("item12ByFileSize")
					|| currentMenu.equals("item13ByDate")
					|| currentMenu.equals("item14ByKeyWord")
					|| currentMenu.equals("item15ByDescription")
					|| currentMenu.equals("displaysub")
					|| currentMenu.equals("searchmenu")) {
				displaySubMenu();
			}
		} else {
			List down = (List) display.getCurrent();
			if (down == mainMenu) {
				// Main Menu Processing
				switch (down.getSelectedIndex()) {
				case 0:
					testItemAnnotateFiles();
					break;
				case 1:
					testItemUpdatedMeta();
					break;
				case 2:
					testItemAdvSearch();
					break;
				case 3:
					display.setCurrent(console);
					break;
				}
				return;
			} else if (down == subMenu) {
				// Submenu Processing
				switch (down.getSelectedIndex()) {
				case 0:
					displayAdvanceSearchForm("File Name");
					currentMenu = "item11ByFileName";
					break;
				case 1:
					displayAdvanceSearchForm("File Size");
					currentMenu = "item12ByFileSize";
					break;
				case 2:
					displayAdvanceSearchForm("File Creation Date");
					currentMenu = "item13ByDate";
					break;
				case 3:
					displayAdvanceSearchForm("Keyword");
					currentMenu = "item14ByKeyWord";
					break;
				case 4:
					displayAdvanceSearchForm("Description");
					currentMenu = "item15ByDescription";
					break;
				}
				return;
			}
		}
	}

	/*
	 * Class used for Reading the XML File
	 */
	class ReadXML extends Thread {
		public void run() {
			try {
				// Open http connection
				// HttpConnection httpConnection = (HttpConnection)
				// Connector.open(URL);

				FileConnection fcin = (FileConnection) Connector
						.open("file://localhost/E:/FileInfo.xml");
				if (!fcin.exists()) {
					writeConsole("File exists: " + fcin.exists());
					//alert("Open XML File", "XML File not found in Directory E:/");
					fcin.create();
					OutputStream os = fcin.openOutputStream();
					os.write("<?xml version=\"1.0\"  encoding=\"UTF-8\"?>\n<start>\n</start>".getBytes());
					
					fc.close();
					//return;
				}

				// Initialize XML parser
				KXmlParser parser = new KXmlParser();
				InputStream in = fcin.openInputStream();
				InputStreamReader ir = new InputStreamReader(in);
				parser.setInput(ir);
				// parser.setInputStream(httpConnection.openInputStream());
				parser.nextTag();
				parser.require(XmlPullParser.START_TAG, null, "start");

				// Iterate through our XML file
				while (parser.nextTag() != XmlPullParser.END_TAG) {
					readXMLData(parser);
				}

				parser.require(XmlPullParser.END_TAG, null, "start");
				parser.next();

				parser.require(XmlPullParser.END_DOCUMENT, null, null);
				in.close();
				ir.close();
				fcin.close();

			} catch (Exception e) {
				e.printStackTrace();
				writeConsole("ReadXML: Open XML File Error:" + e.getMessage());
			}
		}
	}

	/*
	 * Function that reads XML File with given Parser.
	 */
	private void readXMLData(KXmlParser parser) throws IOException,
			XmlPullParserException {

		// Parse our XML file
		parser.require(XmlPullParser.START_TAG, null, "File");

		FileInfo fileinfo = new FileInfo();

		while (parser.nextTag() != XmlPullParser.END_TAG) {

			parser.require(XmlPullParser.START_TAG, null, null);
			String name = parser.getName();

			String text = parser.nextText();

			System.out.println("<" + name + ">" + text);

			if (name.equals("FileName"))
				fileinfo.setFileName(text);
			else if (name.equals("FileSize"))
				fileinfo.setFileSize(Long.parseLong(text));
			else if (name.equals("FileDate"))
				fileinfo.setFileDate(text);
			else if (name.equals("keywords"))
				fileinfo.setKeyword(text);
			else if (name.equals("description"))
				fileinfo.setDescription(text);

			parser.require(XmlPullParser.END_TAG, null, name);
		}

		fileVector.addElement(fileinfo);
		System.out.println("Actual Elements=" + fileVector.size());

		parser.require(XmlPullParser.END_TAG, null, "File");

	}

	/*
	 * Function used to display Main Menu
	 */
	public void displayMainMenu() {
		display.setCurrent(mainMenu);
		currentMenu = "Main";
	}

	/*
	 * Function used to Display SubMenu .
	 */
	public void displaySubMenu() {
		display.setCurrent(subMenu);
		currentMenu = "Sub";
	}

	/*
	public void prepare() {
		input = new TextBox("Enter some text: ", "", 5, TextField.ANY);
		input.addCommand(backCommand);
		input.setCommandListener(this);
		input.setString("");
		display.setCurrent(input);
	}
	*/
	
	void showCurrDir() {
		Enumeration e;
		try {
			if (MEGA_ROOT.equals(currDirName)) {
				e = FileSystemRegistry.listRoots();
				browser = new List(currDirName, List.IMPLICIT);
			} else {
				currDir = (FileConnection) Connector.open("file://localhost/"
						+ currDirName);
				e = currDir.list();
				browser = new List(currDirName, List.IMPLICIT);
				browser.append(UP_DIRECTORY, null);
			}
			while (e.hasMoreElements()) {
				fileName = (String) e.nextElement();
				if (fileName.charAt(fileName.length() - 1) == SEP) {
					browser.append(fileName, null);

				} else {
					browser.append(fileName, null);
				}
			}
			browser.setSelectCommand(viewFileCommand);
			browser.addCommand(openFileCommand);
			browser.addCommand(backCommand);
			browser.setCommandListener(this);
			if (currDir != null) {
				currDir.close();
			}
			Display.getDisplay(this).setCurrent(browser);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			writeConsole("Error showCurrDir" + ioe.getMessage());
		}
	}

	void traverseDirectory(String fileName) {
		if (currDirName.equals(MEGA_ROOT)) {
			if (fileName.equals(UP_DIRECTORY)) {
				// can not go up from MEGA_ROOT
				return;
			}
			currDirName = fileName;
		} else if (fileName.equals(UP_DIRECTORY)) {
			// Go up one directory
			int i = currDirName.lastIndexOf(SEP, currDirName.length() - 2);
			if (i != -1) {
				currDirName = currDirName.substring(0, i + 1);
			} else {
				currDirName = MEGA_ROOT;
			}
		} else {
			currDirName = currDirName + fileName;
		}
		showCurrDir();
	}

	public void showFile(String fileName) {
		String url = "file://localhost/" + currDirName + fileName;
		writeConsole("Show File: " + url);
		System.gc();
		FileConnection fc = null;
		InputStream is = null;
		
		if (fileName.endsWith(".txt") || fileName.endsWith(".xml")) {

			byte[] b = new byte[1024];
			int length = 0;
			try {
				fc = (FileConnection) Connector.open(url);
				if (!fc.exists()) {
					throw new IOException("File does not exists");
				}
				is = fc.openInputStream();
				length = is.read(b, 0, 1024);
			} catch (IOException e) {
				e.printStackTrace();
				alert("Open Text File", "Error: " + e.getMessage());
				return;
			}

			TextBox tb = new TextBox("View File: " + fileName, null, 1024,
					TextField.ANY | TextField.UNEDITABLE);
			tb.addCommand(backCommand);
			tb.addCommand(exitCommand);
			tb.setCommandListener(this);
			tb.setString(new String(b, 0, length));

			display.setCurrent(tb);

		} else if (fileName.endsWith("jpg") || fileName.endsWith("png")) {
			ImageCanvas imgCanvas = null;
			try {
				fc = (FileConnection) Connector.open(url);
				if (!fc.exists()) {
					throw new IOException("File does not exists");
				}
				is = fc.openInputStream();

				byte[] buffer = new byte[100000];
				is.read(buffer);
				writeConsole("Reading File done.");

				imgCanvas = new ImageCanvas(this, buffer, display.getCurrent());
				display.setCurrent(imgCanvas);
			} catch (IOException e) {
				e.printStackTrace();
				alert("Open Image File", "Error: " + e.getMessage());
			}

		} else if (fileName.endsWith(".3gp") || fileName.endsWith(".mp4")) {
			VideoPlayer player = new VideoPlayer(this, display.getCurrent(),
					url);
			display.setCurrent(player);
		} else if (fileName.endsWith(".mp3") || fileName.endsWith(".wav")) {
			AudioPlayer player = new AudioPlayer(this, display.getCurrent(),
					url);
			display.setCurrent(player);
		}

		try {
			if (is != null)
				is.close();
			if (fc != null)
				fc.close();
		} catch (Exception e) {
			e.printStackTrace();
			writeConsole("Error closing connections" + e.getMessage());
		}
	}

	/*
	 * File to be added into XML File
	 */
	public void annotateFile(String annotateFileName) {
		try {
			String fileURL = "file://localhost/" + annotateFileName;
			writeConsole("Annotate File:" + fileURL);
			FileConnection f = (FileConnection) Connector.open(fileURL);
			if (f == null || !f.exists()) {
				writeConsole("File does not exists\n" + fileURL);
				alert("File does not Exist", "File does not Exist\n" + fileURL);
				return;
			}
			FileInfo fi = new FileInfo(f, this);

			filename = new TextField("FileName:", "", 100, TextField.ANY);
			filesize = new TextField("FileSize(KB):", "", 30, TextField.ANY);
			modifieddate = new TextField("Modified Date:", "", 40,
					TextField.ANY);
			keywords = new TextField("Keywords:", "", 100, TextField.ANY);
			desc = new TextField("Description:", "", 100, TextField.ANY);

			// filename.setString(currDirName + "/" + fileName);
			filename.setString(fi.getFileName());
			filesize.setString("" + fi.getFileSize());
			modifieddate.setString(fi.getFileDate());
			annotateForm.deleteAll();
			annotateForm.append(filename);
			annotateForm.append(filesize);
			annotateForm.append(modifieddate);
			annotateForm.append(keywords);
			annotateForm.append(desc);
			annotateForm.setCommandListener(this);
			annotateForm.addCommand(backCommand);
			annotateForm.addCommand(annoteteCommand);
			display.setCurrent(annotateForm);
			f.close();
			writeConsole("Annotation Form Displayed...");
		} catch (Exception e) {
			e.printStackTrace();
			writeConsole("Error annotateFile " + e.getMessage());
		}
	}

	public void displayAdvanceSearchForm(String data) {
		searchType = data;
		formsearch = new Form("File Search By " + data);

		if (data.equals("File Size")) {
			formsearch.setTitle("Search By File Size(KB)");
			tdata = new TextField("Size From", "", 40, TextField.ANY);
			tdata2 = new TextField("Size To", "", 40, TextField.ANY);
			formsearch.append(tdata);
			formsearch.append(tdata2);
		} else {
			tdata = new TextField(data, "", 40, TextField.ANY);
			formsearch.append(tdata);
		}
		formsearch.setCommandListener(this);
		formsearch.addCommand(backCommand);
		formsearch.addCommand(searchCommand);
		formsearch.addCommand(searchPANCommand);
		formsearch.setCommandListener(this);
		display.setCurrent(formsearch);
		// currentMenu="displaysub";
		System.out.println("Sub Menu=" + currentMenu);
	}

	public void getFileList(String path) {

		Enumeration fileroot = FileSystemRegistry.listRoots();
		String rootname;
		while (fileroot.hasMoreElements()) {
			rootname = (String) fileroot.nextElement();
			System.out.println(rootname);
		}
		try {
			// Opens a file connection in READ mode
			FileConnection fc = (FileConnection) Connector.open(path,
					Connector.READ);
			writeConsole("Can Read=" + fc.canRead());
			Enumeration filelist = fc.list("*", false);
			// fc.list("*.*", true)
			String filename;
			while (filelist.hasMoreElements()) {
				filename = (String) filelist.nextElement();
				fc = (FileConnection) Connector.open(path + filename,
						Connector.READ);
				if (fc.isDirectory()) { // checks if fc is a directory
					long size = fc.directorySize(false);
					annotatemenu.append(
							filename + " - " + Integer.toString((int) size)
									+ "B\n", null);
				} else { // otherwise, is a file
					long size = fc.fileSize();
					annotatemenu.append(
							filename + " - " + Integer.toString((int) size)
									+ "B\n", null);
				}
				writeConsole(filename);
			}
			fc.close();
		} catch (IOException ioe) {
			writeConsole("IOException: " + ioe.getMessage());
		} catch (SecurityException se) {
			writeConsole("SecurityException: " + se.getMessage());
		} catch (Exception e) {
			writeConsole("Error " + e.getMessage());
		}
	}

	// public void testItem1()
	// {
	// // prepare();
	// currentMenu = "item1";
	// }
	// public void testItem2()
	// {
	// //displayannotateinfo();
	// currentMenu = "item2";
	// }

	/*
	 * Function create File Explorer Form to add new Files into XML File.
	 */
	private void testItemAnnotateFiles() {
		annotatemenu = new List("Annotate File Names", Choice.IMPLICIT);
		boolean isAPIAvailable = false;
		if (System.getProperty("microedition.io.file.FileConnection.version") != null) {
			isAPIAvailable = true;
			showCurrDir();
		} else {
			StringBuffer splashText = new StringBuffer(
					getAppProperty("MIDlet-Name"))
					.append("\n")
					.append(getAppProperty("MIDlet-Vendor"))
					.append(isAPIAvailable ? ""
							: "\nFileConnection API not available");
			Alert splashScreen = new Alert(null, splashText.toString(), null,
					AlertType.INFO);
			splashScreen.setTimeout(3000);
			Display.getDisplay(this).setCurrent(splashScreen);
		}

		/*
		 * annotatemenu.append("File1", null); annotatemenu.append("File2",
		 * null); annotatemenu.append("File3", null);
		 * annotatemenu.append("File4", null); annotatemenu.append("File5",
		 * null); annotatemenu.append("File6", null);
		 * annotatemenu.append("File7", null); annotatemenu.append("File8",
		 * null); annotatemenu.append("File9  ", null);
		 */
		// getFileList("file:///root1/");
		// annotatemenu.addCommand(backCommand);
		// annotatemenu.addCommand(optionsCommand);
		// annotatemenu.setCommandListener(this);
		// display.setCurrent(annotatemenu);
		currentMenu = "itemAnnotateFiles"; // item3
	}

	/*
	 * Create form for Displaying Adv. Search Options
	 */
	private void testItemAdvSearch() {
		subMenu = new List("Advance Search Form", Choice.IMPLICIT);
		subMenu.append("By File Name", null); // 0
		subMenu.append("By File Size", null); // 1
		subMenu.append("By File Date", null); // 2
		subMenu.append("By File Keyword", null);// 3
		subMenu.append("By File Description", null);// 4
		subMenu.addCommand(exitCommand);
		subMenu.addCommand(backCommand);
		subMenu.setCommandListener(this);
		display.setCurrent(subMenu);
		// currentMenu = "Main";
		// currentMenu="Sub";
		currentMenu = "itemAdvSearch";
	}

	/*
	 * Function Update the Meta
	 */
	private void testItemUpdatedMeta() {
		writeConsole("testItemUpdatedMeta start");
		/*
		 * form.append (resultItem); form.addCommand (xmlCommand);
		 * form.setCommandListener (this);
		 * resultItem.setLabel("File  Information"); display.setCurrent(form);
		 */
		System.out.println("-------xxxxxxxxxxxxxxxxxxxx-------");
		System.out.println("----------------------------------");
		annotatemenu = new List("Annotate File Names", Choice.IMPLICIT);
		annotatemenu.addCommand(backCommand);
		// annotatemenu.addCommand(optionsCommand);
		annotatemenu.setCommandListener(this);
		display.setCurrent(annotatemenu);
		if (fileVector.size() == 0) {
			annotatemenu.append("Not File Found", null);
		}

		// Display parsed XML file
		System.out.println(fileVector.size());
		for (int i = 0; i < fileVector.size(); i++) {
			FileInfo file = (FileInfo) fileVector.elementAt(i);

			annotatemenu.append("File Name :" + file.getFileName(), null);
			annotatemenu.append("File Size :" + file.getFileSize(), null);
			annotatemenu.append("File Date :" + file.getFileDate(), null);
			annotatemenu.append("Keyword :" + file.getKeyword(), null);
			annotatemenu.append("Description :" + file.getDescription(), null);
			annotatemenu.append("----xxxxxxxxxxxxxxxxxxxxxxxx-----", null);

			printFileDesc(file);
		}
		currentMenu = "itemUpdatedMeta";
	}

	/*
	 * Function that Searches the data into FileVector Created with the Help of
	 * XML File.
	 */
	public void searchFiles(String data, int type) {
		writeConsole("searchFiles(data, type) start");
		currentMenu = "searchmenu";
		formsearchresult = new List("File Search Result", List.IMPLICIT);
		formsearchresult.setSelectCommand(openFileCommand);
		formsearchresult.addCommand(backCommand);
		formsearchresult.setCommandListener(this);
		formsearchresult.deleteAll();

		for (int i = 0; i < fileVector.size(); i++) {
			FileInfo file = (FileInfo) fileVector.elementAt(i);
			if (type == BY_FILE_NAME) {
				if (file.getFileName().indexOf(data) != -1) {
					formsearchresult.append(file.getFileName(), null);
					printFileDesc(file);
				}
			} else if (type == BY_FILE_SIZE) {
				int size = (int) file.getFileSize();
				int sFrom, sTo;
				String sizeFrom = tdata.getString();
				String sizeTo = tdata2.getString();
				if (sizeFrom != null && !sizeFrom.equals("") && sizeTo != null
						&& !sizeTo.equals("")) {
					try {
						sFrom = Integer.parseInt(sizeFrom);
						sTo = Integer.parseInt(sizeTo);
					} catch (Exception e) {
						alert("Pleasem write Correct Size Values");
						return;
					}
					if (size > sFrom && size < sTo) {
						formsearchresult.append(file.getFileName(), null);
						printFileDesc(file);
					}
				} else if (sizeFrom != null && !sizeFrom.equals("")
						&& (sizeTo == null || sizeTo.equals(""))) {
					try {
						sFrom = Integer.parseInt(sizeFrom);
					} catch (Exception e) {
						alert("Pleasem write Correct Size Values");
						return;
					}
					if (size > sFrom) {
						formsearchresult.append(file.getFileName(), null);
						printFileDesc(file);
					}
				} else if ((sizeFrom == null || sizeFrom.equals(""))
						&& sizeTo != null && !sizeTo.equals("")) {
					try {
						sTo = Integer.parseInt(sizeTo);
					} catch (Exception e) {
						alert("Pleasem write Correct Size Values");
						return;
					}
					if (size > sTo) {
						formsearchresult.append(file.getFileName(), null);
						printFileDesc(file);
					}
				}
			} else if (type == BY_FILE_DATE) {
				if (file.getFileDate().indexOf(data) != -1) {
					formsearchresult.append(file.getFileName(), null);
					printFileDesc(file);
				}
			} else if (type == BY_FILE_KEYWORD) {
				if (file.getKeyword() != null
						&& file.getKeyword().indexOf(data) != -1) {
					formsearchresult.append(file.getFileName(), null);
					printFileDesc(file);
				}
			} else if (type == BY_FILE_DESC) {
				if (file.getDescription() != null
						&& file.getDescription().indexOf(data) != -1) {
					formsearchresult.append(file.getFileName(), null);
					printFileDesc(file);
				}
			}
		}
		display.setCurrent(formsearchresult);
		currentMenu = "itemAdvSearch";
	}

	public Vector searchFiles(String data, String typeStr) {
		writeConsole("searchFiles(data, typeStr) start");
		Vector resultVector = new Vector();
		String sizeFrom = null, sizeTo = null;
		int sFrom, sTo;
		int type = 0;
		
		if (typeStr.equals("file"))
			type = BY_FILE_NAME;
		else if (typeStr.equals("size")) {
			type = BY_FILE_SIZE;
			String[] dataSplited = split(data, "-");
			
			if (dataSplited.length == 1) {
				if (data.startsWith("-")) {
					sizeTo = dataSplited[0];
				} else {
					sizeFrom = dataSplited[0];
				}
			} else if (dataSplited.length == 2) {
				sizeFrom = dataSplited[0];
				sizeTo = dataSplited[1];
			} else {
				return resultVector;
			}
		} else if (typeStr.equals("date"))
			type = BY_FILE_DATE;
		else if (typeStr.equals("key"))
			type = BY_FILE_KEYWORD;
		else if (typeStr.equals("desc"))
			type = BY_FILE_DESC;

		for (int i = 0; i < fileVector.size(); i++) {
			FileInfo file = (FileInfo) fileVector.elementAt(i);

			if (type == BY_FILE_NAME) {
				if (file.getFileName().indexOf(data) != -1) {
					resultVector.addElement(file.getFileName());
					printFileDesc(file);
				}
			} else if (type == BY_FILE_SIZE) {

				int size = (int) file.getFileSize();
				if (sizeFrom != null && sizeTo != null) {
					try {
						sFrom = Integer.parseInt(sizeFrom);
						sTo = Integer.parseInt(sizeTo);
					} catch (Exception e) {
						alert("Pleasem write Correct Size Values");
						return resultVector;
					}
					if (size > sFrom && size < sTo) {
						resultVector.addElement(file.getFileName());
						printFileDesc(file);
					}
				} else if (sizeFrom != null && sizeTo == null) {
					try {
						sFrom = Integer.parseInt(sizeFrom);
					} catch (Exception e) {
						alert("Pleasem write Correct Size Values");
						return resultVector;
					}
					if (size > sFrom) {
						resultVector.addElement(file.getFileName());
						printFileDesc(file);
					}
				} else if (sizeFrom == null && sizeTo != null) {
					try {
						sTo = Integer.parseInt(sizeTo);
					} catch (Exception e) {
						alert("Pleasem write Correct Size Values");
						return resultVector;
					}
					if (size < sTo) {
						resultVector.addElement(file.getFileName());
						printFileDesc(file);
					}
				}
			} else if (type == BY_FILE_DATE) {
				if (file.getFileDate().indexOf(data) != -1) {
					resultVector.addElement(file.getFileName());
					printFileDesc(file);
				}
			} else if (type == BY_FILE_KEYWORD) {
				if (file.getKeyword() != null
						&& file.getKeyword().indexOf(data) != -1) {
					resultVector.addElement(file.getFileName());
					printFileDesc(file);
				}
			} else if (type == BY_FILE_DESC) {
				if (file.getDescription() != null
						&& file.getDescription().indexOf(data) != -1) {
					resultVector.addElement(file.getFileName());
					printFileDesc(file);
				}
			}
		}
		writeConsole("searchFiles(data, typeStr) end vectorSize:"
				+ resultVector.size());
		return resultVector;
	}

	public void alert(String title, String message) {
		Alert alert = new Alert(title, message, null, AlertType.ERROR);
		alert.setTimeout(3000);
		display.setCurrent(alert);
	}

	public void alert(String message) {
		Alert alert = new Alert("Alert", message, null, AlertType.INFO);
		alert.setTimeout(3000);
		display.setCurrent(alert);
	}

	public void writeConsole(String Message) {
		console.append(Message, null);
	}

	public void printFileDesc(FileInfo file) {
		System.out.println("Search by Description---------------");
		System.out.println("File Name : " + file.getFileName());
		System.out.println("File Size : " + file.getFileSize());
		System.out.println("File Date : " + file.getFileDate());
		System.out.println("Keyword : " + file.getKeyword());
		System.out.println("Description : " + file.getDescription());
	}

	public static String[] split(String splitStr, String delimiter) {
		StringBuffer token = new StringBuffer();
		Vector tokens = new Vector();
		// split
		char[] chars = splitStr.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (delimiter.indexOf(chars[i]) != -1) {
				// we bumbed into a delimiter
				if (token.length() > 0) {
					tokens.addElement(token.toString());
					token.setLength(0);
				}
			} else {
				token.append(chars[i]);
			}
		}
		// don't forget the "tail"...
		if (token.length() > 0) {
			tokens.addElement(token.toString());
		}
		// convert the vector into an array
		String[] splitArray = new String[tokens.size()];
		for (int i = 0; i < splitArray.length; i++) {
			splitArray[i] = (String) tokens.elementAt(i);
		}
		return splitArray;
	}
}