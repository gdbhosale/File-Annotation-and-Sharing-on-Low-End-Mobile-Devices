import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import com.nokia.mid.impl.isa.io.protocol.external.apdu.IsaSession;


public class BTDevice {
	
	private String friendlyName;
	private ServiceRecord serviceRecord;
	private String connURL;
	private boolean hasXMLFileServer = false;
	public BTDevice(RemoteDevice currentDevice) {
		
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public ServiceRecord getServiceRecord() {
		return serviceRecord;
	}

	public void setServiceRecord(ServiceRecord serviceRecord) {
		this.serviceRecord = serviceRecord;
		String serviceName = (String) (serviceRecord.getAttributeValue(256).getValue());
		if(serviceName.equals(BluetoothConnection.SERVER_NAME)) {
			connURL = serviceRecord.getConnectionURL(0, false);
			hasXMLFileServer = true;
		}
		
	}

	public String getConnURL() {
		return connURL;
	}

	public void setConnURL(String connURL) {
		this.connURL = connURL;
	}
}