import java.io.IOException;
import java.util.Date;

import javax.microedition.io.file.FileConnection;

public class FileInfo {
	XMLJ2MEFileRead parent;
	private FileConnection f = null;
	private String fileName = "";
	private long fileSize = 0;
	private String fileDate = "";
	private String keyword = "";
	private String description = "";

	public FileInfo(FileConnection fc, XMLJ2MEFileRead parent) throws IOException {
		try {
			this.parent = parent;
			this.f = fc;
			fileName = f.getURL().substring(17);
			fileSize = f.fileSize() / 1024;
			System.out.println("f.lastModified(): " + f.lastModified());
			Date d = new Date(f.lastModified());
			fileDate = d.toString();
		} catch (Exception e) {
			parent.writeConsole("Error While Retriving File Info "
					+ e.getMessage());
		}
	}

	public FileInfo() {

	}

	/**
	 * @return keyword of File
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 * @return Modified Date of File
	 */
	public String getFileDate() {
		return fileDate;
	}

	/**
	 * @return Description of File
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return Size of File
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * @return File Name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param string
	 *            File Keyword
	 */
	public void setKeyword(String string) {
		keyword = string;
	}

	/**
	 * @param string
	 */
	public void setFileDate(String string) {
		fileDate = string;
	}

	/**
	 * @param string
	 */
	public void setDescription(String string) {
		description = string;
	}

	/**
	 * @param string
	 */
	public void setFileSize(long size) {
		fileSize = size;
	}

	/**
	 * @param string
	 */
	public void setFileName(String string) {
		fileName = string;
	}

	public String toString() {
		return "FileInfo [f=" + f + ", fileName=" + fileName + ", fileSize="
				+ fileSize + ", fileDate=" + fileDate + ", keyword=" + keyword
				+ ", description=" + description + "]";
	}
}
