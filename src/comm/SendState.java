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
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;

/**
 * This class implements business card sending state. In this state a client
 * OBEX PUT operation is peformed. No OBEX server commands are served.
 * 
 * This class extends ExchangerState abstract class. 
 * 
 * @version 1.0 29.09.2005
 * @see bcexchanger.comm.ExchangerState Design Patterns: State
 */
public class SendState extends ExchangerState {

	// Instance variables

	private ClientSession connection = null;

	private String url = null;

	private Operation operation = null;

	private boolean cancelInvoked = false;

	/**
	 * Constructor
	 * 
	 * @param _parent -
	 *            the class which is nesting the current state of the state
	 *            machine
	 * @param _url -
	 *            a URL of the remote OBEX service. This state is responsible
	 *            for establishin the connection
	 */
	public SendState(ExchangerStateParent _parent, String _url) {
		super(_parent);

		url = _url;
	}

	/*
	 * SendState does not allow to start any other business card exchange
	 * process.
	 * 
	 * @see bcexchanger.comm.ExchangerState#startSending()
	 */
	public void startSending(int o) throws IOException {
		throw new IOException(
				"Sending is in progress. Sending has to be canceled before starting new sending process");

	}

	/*
	 * SendState allows to cancel sending process.
	 * 
	 * @see bcexchanger.comm.ExchangerState#cancelSending()
	 */
	public void cancelSending() {

		cancelInvoked = true;

		try { // cancel any active OBEX operation
			operation.abort();
		} catch (Exception e) { // catch NullPointer exception also
			// Ignore, aborting anyway
		}

		try { // send DISCONNECT to the remote peer

			connection.disconnect(null);
		} catch (Exception e1) { // catch NullPointer exception also
			// Ignore, disconnecting anyway
		}

		try { // close the connection
			connection.close(); // 

		} catch (Exception e2) { // catch NullPointer exception also
			// Ignore, closing anyway
		}

	}

	/*
	 * This method implements the logic for sending a
	 * business card
	 * 
	 */	
	public void doSend()
	{
		try 
		{
			connection = (ClientSession) Connector.open(url);
			HeaderSet response = connection.connect(null);

			// Initiate the PUT request
			operation = connection.put(null);
			OutputStream out = operation.openOutputStream();
			//getting the own card
			byte[] vCard = parent.getListener().getOwnBC(); 
			int vlen = vCard.length;
			byte[] tmpBuf = new byte[vlen + 4];
			System.arraycopy(vCard, 0, tmpBuf, 4, vlen);
			tmpBuf[0] = (byte) ((vlen >>> 24) & 0xff);
			tmpBuf[1] = (byte) ((vlen >>> 16) & 0xff);
			tmpBuf[2] = (byte) ((vlen >>> 8) & 0xff);
			tmpBuf[3] = (byte) ((vlen >>> 0) & 0xff);
			//sending data
			out.write(tmpBuf); 
			out.close();
			int responseCode = operation.getResponseCode();

            operation.close();

			if (cancelInvoked) {
				throw new Exception(
						"Cancel did not invoke any exception; throw exception then");
			}

			// send is done
			try {				
				parent.setState(new IdleState(parent));
			} catch (Exception e) 
			{
			}
		} 
		catch (Exception e1) 
		{
			parent.setState(new IdleState(parent));
			if (cancelInvoked) 
			{ // if exception is caused by cancelation
				parent.getListener().onSendComplete(ExchangerComm.CANCELED);
			} else 
			{ // if exception is caused by error
				parent.setState(new IdleState(parent));
				parent.getListener().onSendComplete(ExchangerComm.ERROR);
			}
		}
			finally 
			{
		      try { // finalizing operation
		        operation.close();
		      } 
		      catch (IOException e3) {
		    	
		      }

		      try { // sending DISCONNECT command
		        connection.disconnect(null);
		      } 
		      catch (IOException e2) 
		      {		    	  
		        // Ignore, disconnecting anyway
		      }
		    }
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
