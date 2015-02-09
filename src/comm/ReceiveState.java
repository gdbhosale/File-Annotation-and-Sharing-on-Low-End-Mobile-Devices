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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;

/**
 * 
 * This class implements business card receiving state. In this state
 * an client OBEX GET operation is peformed. No OBEX server commands
 * are served.
 * 
 * This class extends ExchangerState abstract class.
 * 
 * @version 1.0 29.09.2005
 * @see bcexchanger.comm.ExchangerState Design Patterns: State
 */
public class ReceiveState extends ExchangerState{

  // Instance variables
  private ClientSession connection = null;
  private Operation operation = null;
  String url = null;

  boolean cancelInvoked = false; // true after cancelSending is
                                  // called

  /**
   * Constructor
   * <p>
   * description
   * 
   * @param _parent -
   *          the class which is nesting the current state of the
   *          state machine
	 * @param _url -
	 *            a URL of the remote OBEX service. This state is responsible
	 *            for establishin the connection
   * @exception
   * @see
   */
  public ReceiveState(ExchangerStateParent _parent,
      String url) 
  {
    super(_parent);
    this.url = url;
  }

  /*
   * ReceiveState does not allow to start any other business card
   * exchange process.
   * 
   * @see bcexchanger.comm.ExchangerState#startSending()
   */
  public void startSending(int to) throws IOException {

    throw new IOException(
        "Receiving is in progress. Receiving has to be canceled before starting new sending process");

  }

  /*
   * ReceiveState allows to cancel receiving process.
   * 
   * @see bcexchanger.comm.ExchangerState#cancelSending()
   */
  public void cancelSending() {
    cancelInvoked = true;

    try { // cancel any active operation running
      operation.abort();
    } catch (IOException e) {
      // Ignore, aborting operation anyway
    }

    try { // send DISCONNECT command to the remote peer
      connection.disconnect(null);
    } catch (IOException e1) {
        // Ignore, disconnecting anyway
    }

  }

  /*
   * This method implements the logic for
   * receving a business card
   * 
   */
  public void doGet()
  {
	  try 
	  {
		  connection = (ClientSession) Connector.open(url);
		  HeaderSet response = connection.connect(null);
		  operation = connection.get(null);
		  InputStream in = operation.openInputStream();
		  byte[] fullResult = null; 
		  {
			  byte[] buf = new byte[256];
			  ByteArrayOutputStream bout = new ByteArrayOutputStream(2048);
			  for (int len = in.read(buf); len >= 0; len = in.read(buf))
    		  {	
    		  	bout.write(buf, 0, len);
    		  } 
			  fullResult = bout.toByteArray();
		  }   
		  ByteArrayInputStream bin = new ByteArrayInputStream(fullResult);
		  DataInputStream din = new DataInputStream(bin);
		  int size = din.readInt();    
		  byte[] vCard = new byte[size];
		  din.read(vCard);
		  
		  parent.getListener().onReceiveBC(vCard);
		 
		  // End the transaction
		  in.close();
		  int responseCode = operation.getResponseCode();
	
		  operation.close();
		  
		  if (cancelInvoked) {
			  throw new Exception(
			  "Cancel did not invoke any exception; throw exception then");
		  }

		  // receive is done
		  try 
		  
		  {
			  parent.setState(new IdleState(parent));
			  parent.getListener().onReceiveComplete(ExchangerComm.DONE);
		  } 
		  catch (Exception e) 
		  {
		  }

    } 
	catch (Exception e1) 
	{
      if (cancelInvoked)
      { // if exception was caused by canceling
    	  parent.setState(new IdleState(parent));
        parent.getListener()
            .onReceiveComplete(ExchangerComm.CANCELED);
      } 
      else 
      { // if exception was caused by error
    	  
    	parent.setState(new IdleState(parent));
        parent.getListener().onReceiveComplete(ExchangerComm.ERROR);
      }
    } 
	finally 
	{
      try { // finalizing operation
        operation.close();
      } catch (IOException e3) 
      {
      }

      try { // sending DISCONNECT command
        connection.disconnect(null);
      } catch (IOException e2) 
      {
        // Ignore, disconnecting anyway
      }
      

    }
  }
  
  /*
   * Server OBEX GET command is supported only in
   * IdleState
   * 
   * @see bcexchanger.comm.ExchangerState#onGet(javax.obex.Operation)
   */
  public int onGet(Operation op) {
    return ResponseCodes.OBEX_HTTP_CONFLICT;
  }

  /*
   * Server OBEX PUT command is supported only in
   * IdleState
   * 
   * @see bcexchanger.comm.ExchangerState#onPut(javax.obex.Operation)
   */
  public int onPut(Operation op) {
    return ResponseCodes.OBEX_HTTP_CONFLICT;
  }


}
