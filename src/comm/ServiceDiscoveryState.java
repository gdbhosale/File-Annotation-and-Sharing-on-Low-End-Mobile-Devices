// Copyright 2006 Nokia Corporation.
//
// THIS SOURCE CODE IS PROVIDED 'AS IS', WITH NO WARRANTIES WHATSOEVER,
// EXPRESS OR IMPLIED, INCLUDING ANY WARRANTY OF MERCHANTABILITY, FITNESS
// FOR ANY PARTICULAR PURPOSE, OR ARISING FROM A COURSE OF DEALING, USAGE
// OR TRADE PRACTICE, RELATING TO THE SOURCE CODE OR ANY WARRANTY OTHERWISE
// ARISING OUT OF ANY PROPOSAL, SPECIFICATION, OR SAMPLE AND WITH NO
// OBLIGATION OF NOKIA TO PROVIDE THE LICENSEE WITH ANY MAINTENANCE OR
// SUPPORT. FURTHERMORE, NOKIA MAKES NO WARRANTY THAT EXERCISE OF THE
// RIGHTS GRANTED HEREUNDER DOES NOT INFRINGE OR MAY NOT CAUSE INFRINGEMENT
// OF ANY PATENT OR OTHER INTELLECTUAL PROPERTY RIGHTS OWNED OR CONTROLLED
// BY THIRD PARTIES
//
// Furthermore, information provided in this source code is preliminary,
// and may be changed substantially prior to final release. Nokia Corporation
// retains the right to make changes to this source code at
// any time, without notice. This source code is provided for informational
// purposes only.
//
// Nokia and Nokia Connecting People are registered trademarks of Nokia
// Corporation.
// Java and all Java-based marks are trademarks or registered trademarks of
// Sun Microsystems, Inc.
// Other product and company names mentioned herein may be trademarks or
// trade names of their respective owners.
//
// A non-exclusive, non-transferable, worldwide, limited license is hereby
// granted to the Licensee to download, print, reproduce and modify the
// source code. The licensee has the right to market, sell, distribute and
// make available the source code in original or modified form only when
// incorporated into the programs developed by the Licensee. No other
// license, express or implied, by estoppel or otherwise, to any other
// intellectual property rights is granted herein.
package comm;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.obex.Operation;
import javax.obex.ResponseCodes;

/**
 * 
 * This class implements service discovery state. In this state an active
 * service discovery is peformed. No OBEX server commands are served.
 * 
 * This class extends ExchangerState abstract class. This class also implments
 * DiscoveryListener interface to receive Bluetooth service discovery callbacks.
 * Service discoveries are run from the separate thread therefore the class
 * implements Runnable interface
 * 
 * @version 1.0 29.09.2005
 * @see bcexchanger.comm.ExchangerState javax.bluetooth.DiscoveryListener Design
 *      Patterns: State
 * 
 */
public class ServiceDiscoveryState extends ExchangerState implements
		DiscoveryListener, Runnable {

	// Instance variables
	private Thread serviceDiscoveryThread;

	private Vector services;

	private DiscoveryAgent agent;

	private int serviceDiscoveryID;

	private Vector devices;

	private boolean canceled = false;
	
	public static int GET = 0;
	public static int PUT = 1;
	
	/**
	 * Constructor
	 * 
	 * @param _parent -
	 *            the class which is nesting the current state of the state
	 *            machine
	 * @param _devices -
	 *            a vecotor of RemoteDevice object representing devices found
	 *            during inquiry
	 */
	public ServiceDiscoveryState(ExchangerStateParent _parent, Vector _devices,int oper)
			throws IOException {
		super(_parent);
		canceled = false;

		services = new Vector();
		devices = _devices;

		// initiate Bluetooth
		LocalDevice local = LocalDevice.getLocalDevice();
		agent = local.getDiscoveryAgent();

		serviceDiscoveryThread = new Thread(this);
		serviceDiscoveryThread.start();

	}

	/*
	 * ServiceDiscoveryState does not allow to start any other business card
	 * exchange process.
	 * 
	 * @see bcexchanger.comm.ExchangerState#startSending()
	 */
	public void startSending(int op) throws Exception {
		throw new IOException(
				"Service discovery is in progress. Service discovery has to be canceled before starting new sending process");

	}

	/*
	 * ServiceDiscoveryState allows to cancel discovery process.
	 * 
	 * @see bcexchanger.comm.ExchangerState#cancelSending()
	 */
	public void cancelSending() {

		canceled = true;
		agent.cancelServiceSearch(serviceDiscoveryID);

	}

	/*
	 * 
	 * Inquiry callbacks are not handled and not supposed to occur, since
	 * inquiry process is not started
	 * 
	 * @see javax.bluetooth.DiscoveryListener#deviceDiscovered(javax.bluetooth.RemoteDevice,
	 *      javax.bluetooth.DeviceClass)
	 */
	public void deviceDiscovered(RemoteDevice arg0, DeviceClass arg1) {

		throw new RuntimeException(
				"Internal error #8: ServiceDiscoveryState.deviceDiscovered() should not be called");
	}

	/*
	 * 
	 * Inquiry callbacks are not handled and not supposed to occur, since
	 * inquiry process is not started
	 * 
	 * @see javax.bluetooth.DiscoveryListener#inquiryCompleted(int)
	 */
	public void inquiryCompleted(int arg0) {

		throw new RuntimeException(
				"Internal error #9: ServiceDiscoveryState.inquiryCompleted() should not be called");

	}

	public void servicesDiscovered(int id, ServiceRecord[] _services) {

		for (int i = 0; i < _services.length; i++) {
			services.addElement(_services[i]);
		}

	}

	public synchronized void serviceSearchCompleted(int arg0, int arg1) {
		notify();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * This methods implements logic of the service discovery process
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public synchronized void run() {

		Enumeration e = devices.elements();
		// Business card exchanger service UUID
		UUID[] uuids = new UUID[1];
		uuids[0] = new UUID(parent.getUUID(), false);

		// proceeded with all devices if not canceled
		while (e.hasMoreElements() && !canceled) {

			RemoteDevice device = (RemoteDevice) e.nextElement();
			try {
				serviceDiscoveryID = agent.searchServices(null, uuids, device,
						this);
			} catch (Exception e0) {
				// signal error to the MIDlet
				parent.getListener().onServiceDiscoveryComplete(
						ExchangerComm.ERROR);
				parent.setState(new IdleState(parent));
				return;
			}

			try {
				wait(); // wait until service search is done on this device
			} catch (Exception e1) {
				// Ignore
			}
		}

		if (canceled) { // handle user's cancelation
			try {
				parent.getListener().onServiceDiscoveryComplete(
						ExchangerComm.CANCELED);
				parent.setState(new IdleState(parent));
			} catch (Exception e1) {
				throw new RuntimeException(
						"Internal error #10: ServiceDicoveryState.run()");
			}
		} else 
		{ // not canceled
			if (services.isEmpty()) { // no services on devices
				try {
					parent.getListener().onServiceDiscoveryComplete(
							ExchangerComm.NO_RECORDS);
					parent.setState(new IdleState(parent));

				} catch (Exception e1) {

				}
			} 
			else if (services.size() == 1) 
			{ // one service found,
				// connect to it
				try {
					ServiceRecord serviceRecord = (ServiceRecord) services
							.firstElement();
					parent.getListener().onServiceDiscoveryComplete(
							ExchangerComm.DONE);
						SendState state = new SendState(parent, serviceRecord
								.getConnectionURL(
										ServiceRecord.NOAUTHENTICATE_NOENCRYPT,
										false));
						
						parent.setState(state);
						state.doSend();
						state = null;
						Thread.sleep(new Long(5000).longValue()); 

						ReceiveState rstate = new ReceiveState(parent, serviceRecord
								.getConnectionURL(
										ServiceRecord.NOAUTHENTICATE_NOENCRYPT,
										false));
						parent.setState(rstate); 
						rstate.doGet();
						rstate = null;
						Thread.sleep(new Long(5000).longValue()); 
					
				} 
				catch (Exception e2) 
				{
				
				}
			} 
			else 
			{ // several services found, let user choose
				try {
					// list of friendly names of devices which contain services
					Vector friendlyNames = createFriendlyNamesList(services);

					int index = parent.getListener().resolveMultipleServices(
							friendlyNames);

					if (!canceled) 
					{ // if not canceled during resolving
						ServiceRecord serviceRecord = (ServiceRecord) services
								.elementAt(index);

						parent.getListener().onServiceDiscoveryComplete(
								ExchangerComm.DONE);

							SendState state = new SendState(parent, serviceRecord
									.getConnectionURL(
											ServiceRecord.NOAUTHENTICATE_NOENCRYPT,
											false));
							parent.setState(state);
							state.doSend();
							state = null;
							Thread.sleep(new Long(5000).longValue()); 

							ReceiveState rstate = new ReceiveState(parent, serviceRecord
									.getConnectionURL(
											ServiceRecord.NOAUTHENTICATE_NOENCRYPT,
											false));
							parent.setState(rstate);
							rstate.doGet();
							
							rstate = null;
							Thread.sleep(new Long(5000).longValue()); 

					} 
					else 
					{ // if canceled during resolving
						parent.getListener().onServiceDiscoveryComplete(
								ExchangerComm.CANCELED);
						parent.setState(new IdleState(parent));
					}
				
					} 
					catch (Exception e1) {
					}

			}

		}
	}

	/*
	 * This method creates a list of the friendly names of devices that contain
	 * the servicies
	 * 
	 * @param _services - vector of ServiceRecord objects representing found
	 * services @return - vectors of strings containing friendly names of
	 * devices
	 */
	private Vector createFriendlyNamesList(Vector _services) {

		Vector friendlyNames = new Vector();
		Enumeration e = _services.elements();
		while (e.hasMoreElements()) {
			ServiceRecord serviceRecord = (ServiceRecord) e.nextElement();
			RemoteDevice device = serviceRecord.getHostDevice();

			try {
				friendlyNames.addElement(device.getFriendlyName(false));
			} catch (IOException e1) {
				// If there is an excption getting the friendly name
				// use the address
				friendlyNames.addElement(device.getBluetoothAddress());
			}
		}
		return friendlyNames;
	}

	/*
	 * Server OBEX GET command is supported only in IdleState
	 * 
	 * @see bcexchanger.comm.ExchangerState#onGet(javax.obex.Operation)
	 */
	public int onGet(Operation op) {
		return ResponseCodes.OBEX_HTTP_CONFLICT;
	}

	/*
	 * Server OBEX PUT command is supported only in IdleState
	 * 
	 * @see bcexchanger.comm.ExchangerState#onPut(javax.obex.Operation)
	 */
	public int onPut(Operation op) {
		return ResponseCodes.OBEX_HTTP_CONFLICT;
	}
}
