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

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ServerRequestHandler;
import javax.obex.SessionNotifier;

/**
 * 
 * This class is the central class in the OBEX communication module
 * (package bcexchanger.comm).
 * 
 * Class implements interface ExchangerComm and realizes the methods
 * for controling process of sending and receiving business cards. It
 * also is a parent for states of the BCExchanger communication state
 * machine. It keeps the current state of the state machine and
 * implements interface ExchangerStateParent. Using this interface
 * state classes access to the required functionality of the parent.
 * 
 * This class waits for Bluetooth incomming connection in the separate
 * thread and therefore it implments Runnable interface.
 * 
 * This class also works as a OBEX server. In order to serve OBEX
 * requests it extends the ServerRequestHandler class and overides
 * some of its methods.
 * 
 * @version 1.0 29.09.2005
 * @see bcexchanger.comm.ExchangerStateParent
 *      bcexchanger.comm.ExchangerComm javax.obex.ServerRequestHandler
 *      Design patterns: State
 * 
 */
public class ExchangerCommImpl extends ServerRequestHandler
    implements ExchangerStateParent, ExchangerComm, Runnable {

  // Instance variables
  final private String uuid = "ed495afe28ed11da94d900e08161165f";
  final private String serverURL = "btgoep://localhost:" + uuid;

  private boolean cancelWaitingInvoked = false; // becomes true if
                                                // cancelWaiting() is
                                                // called

  private Thread waitingThread;

  private ExchangeListener listener;
  private ExchangerState currentState;

  private SessionNotifier notifier = null;
  private Connection con = null;

  /**
   * Constructor
   * <p>
   * description
   * 
   * @param _listener -
   *          listener of the communication module events
   * @exception
   * @see
   */
  public ExchangerCommImpl(ExchangeListener _listener) 
  {
	listener = _listener;

    startWaiting();
    setState(new IdleState(this));
  }

  public void setState(ExchangerState state) {
	  
    currentState = state;
  }

  public ExchangeListener getListener() {
    return listener;
  }

  public ExchangerState getState() {
    return currentState;
  }

  public void startSending(int oper) throws Exception {
    getState().startSending(oper);
  }
  
  public void startSending() throws Exception {
	    getState().startSending(0);
	  }
  

  public void cancelSending()
  {
    getState().cancelSending();
  }

  public void startWaiting() {
    cancelWaitingInvoked = false;

    waitingThread = new Thread(this);
    waitingThread.start();
  }

  public void cancelWaiting() 
  {
    cancelWaitingInvoked = true;

    try 
    {
      notifier.close(); // indicate to acceptAndOpen that it is
                        // canceled
    } catch (IOException e) {
      // Ignore, we're closing anyways
    }
  }

  public synchronized void run() {

  	// initialize stack and make the device discoverable
	try 
	{
      LocalDevice local = LocalDevice.getLocalDevice();
	  local.setDiscoverable(DiscoveryAgent.GIAC); 
	} catch (Exception e) 
	{
	  // catching notifier exception
	  listener.onServerError();
	  return;
	}
	try 
	{
		notifier = (SessionNotifier) Connector.open(serverURL);
	} 
	catch (IOException e2) 
	{
		
	}
    // the cycle stops only if cancelWaiting() was called
    while (!cancelWaitingInvoked) 
    {
      try 
      {      

        try
        {
          con = notifier.acceptAndOpen(this);
          wait(); // wait until the remote peer disconnects
          try 
          {
        	con.close();
          } 
          catch (IOException e0) 
          { }
        } 
        catch (Exception e1) 
        {
        	listener.onServerError();
        	return;
        }
      } 
      catch (Exception e) 
      {
        listener.onServerError();
        return;
      }

    }

  }

  /*
   * This method is related to OBEX server functionality. This method
   * is delegating this execution to the current state
   * 
   * @see javax.obex.ServerRequestHandler#onGet()
   */
  public int onGet(Operation op) {
    return getState().onGet(op);
  }

  /*
   * This method is related to OBEX server functionality. This method
   * is delegating this execution to the current state
   * 
   * @see javax.obex.ServerRequestHandler#onPut()
   */
  public int onPut(Operation op) {
    return getState().onPut(op);

  }

  /*
   * This method is related to OBEX server functionality. This method
   * handles OBEX DISCONNECT command from the remote device.
   * 
   * @see javax.obex.ServerRequestHandler#onDisconnect()
   */
  public synchronized void onDisconnect(HeaderSet request,
      HeaderSet reply) {
    super.onDisconnect(request, reply);
    notify();// stops waiting in run()

  }

  public String getUUID() {
    return uuid;
  }

}
