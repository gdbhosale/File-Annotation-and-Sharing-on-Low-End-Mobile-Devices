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
import java.util.Vector;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.obex.Operation;
import javax.obex.ResponseCodes;

/**
 * 
 * This class implements inquiry state. In inquiry state an active
 * device discovery is peformed. No OBEX server commands are served.
 * 
 * This class extends ExchangerState abstract class. This class also
 * implments DiscoveryListener interface to receive Bluetooth inquiry
 * callbacks.
 * 
 * @version 1.0 29.09.2005
 * @see bcexchanger.comm.ExchangerState
 *      javax.bluetooth.DiscoveryListener Design Patterns: State
 * 
 */
public class InquiryState extends ExchangerState
    implements DiscoveryListener {

  private DiscoveryAgent agent;
  private Vector remoteDevices; // vector of found devices
  private int operation = ServiceDiscoveryState.GET;
  
  /**
   * Constructor
   * 
   * @param _parent -
   *          the class which is nesting the current state of the
   *          state machine
   */
  public InquiryState(ExchangerStateParent _parent,int oper)
      throws IOException {
    super(_parent);
    operation = oper;
    remoteDevices = new Vector();
    // initiate Bluetooth
    LocalDevice local = LocalDevice.getLocalDevice();
    agent = local.getDiscoveryAgent();
    // start Bluetooth inquiry
    agent.startInquiry(DiscoveryAgent.GIAC, this);

  }

  /*
   * Inquiry state does not allow to start any other business card
   * exchange process.
   * 
   * @see bcexchanger.comm.ExchangerState#startSending()
   */
  public void startSending(int op) throws IOException {
    throw new IOException(
        "Inquiry is in progress. Inquiry has to be canceled before starting new sending process");
  }

  /*
   * InquiryState allows to cancel inquiry process.
   * 
   * @see bcexchanger.comm.ExchangerState#cancelSending()
   */
  public void cancelSending() {
    agent.cancelInquiry(this);
  }

  public void deviceDiscovered(RemoteDevice dev, DeviceClass devClass) 
  {
    remoteDevices.addElement(dev);
  }

  public void inquiryCompleted(int code) {
    try 
    {
    	int completionCode = ExchangerComm.ERROR;

      // convert the inquiry completion code to application completion
      // code
      switch (code) {
        case DiscoveryListener.INQUIRY_COMPLETED:
          completionCode = ExchangerComm.DONE;
          break;
        case DiscoveryListener.INQUIRY_TERMINATED:
          completionCode = ExchangerComm.CANCELED;
          break;
        case DiscoveryListener.INQUIRY_ERROR:
          completionCode = ExchangerComm.ERROR;
          break;
      }
      parent.getListener().onInquiryComplete(completionCode); // signal
                                                              // that
                                                              // inquiry
                                                              // is
                                                              // done

      if (code == DiscoveryListener.INQUIRY_COMPLETED) { // no errors
    	                                                 // or
    	                                                 // cancelations
        parent.setState(new ServiceDiscoveryState(parent,
            remoteDevices,operation));

      } else {
        parent.setState(new IdleState(parent));
      }

    } catch (Exception e) {
    	parent.setState(new IdleState(parent));
    }
  }

  /*
   * Service discovery callbacks are not handled and not supposed to
   * occur, since service discovery process is not started
   * 
   * @see javax.bluetooth.DiscoveryListener#servicesDiscovered(int,
   *      javax.bluetooth.ServiceRecord[])
   */
  public void servicesDiscovered(int arg0, ServiceRecord[] arg1) {
    throw new RuntimeException(
        "Internal error #4: InquiryState.servicesDiscovered() should not be called");

  }

  /*
   * Service discovery callbacks are not handled and not supposed to
   * occur, since service discovery process is not started
   * 
   * @see javax.bluetooth.DiscoveryListener#serviceSearchCompleted(int,
   *      int)
   */
  public void serviceSearchCompleted(int arg0, int arg1) {
    throw new RuntimeException(
        "Internal error #5: InquiryState.serviceSearchCompleted() should not be called");

  }

  /*
   * Serving OBEX GET operation is supported only in IdleState
   * 
   * @see bcexchanger.comm.ExchangerState#onGet(javax.obex.Operation)
   */
  public int onGet(Operation op) {

    return ResponseCodes.OBEX_HTTP_CONFLICT;
  }

  /*
   * Serving OBEX GET operation is supported only in
   * IdleState
   * 
   * @see bcexchanger.comm.ExchangerState#onPut(javax.obex.Operation)
   */
  public int onPut(Operation op) {
    // onPut is supported only in IdleState
    return ResponseCodes.OBEX_HTTP_CONFLICT;
  }

}
