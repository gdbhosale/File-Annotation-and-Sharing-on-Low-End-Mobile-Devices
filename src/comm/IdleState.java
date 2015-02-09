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
import java.io.InputStream;
import java.io.OutputStream;
import javax.obex.Operation;
import javax.obex.ResponseCodes;

/**
 * 
 * This class implements idle state of the communication state machine. In idle
 * state machine is waiting for external events (like incomming Bluetooth
 * connections or user command to start business card exchange)
 * 
 * This class extends ExchangerState abstract class.
 * 
 * @version 1.0 29.09.2005
 * @see bcexchanger.comm.ExchangerState Design Patterns: State
 * 
 */
public class IdleState extends ExchangerState {
	/**
	 * Constructor
	 * 
	 * @param _parent -
	 *            the class which is nesting the current state of the state
	 *            machine
	 */
	public IdleState(ExchangerStateParent _parent) {
		super(_parent);

	}

	public int onGet(Operation op) {

		try {

			OutputStream out = op.openOutputStream();
			byte[] vCard = parent.getListener().getOwnBC(); // getting the
			// own card

			int vlen = vCard.length;
			byte[] tmpBuf = new byte[vlen + 4];
			System.arraycopy(vCard, 0, tmpBuf, 4, vlen);
			tmpBuf[0] = (byte) ((vlen >>> 24) & 0xff);
			tmpBuf[1] = (byte) ((vlen >>> 16) & 0xff);
			tmpBuf[2] = (byte) ((vlen >>> 8) & 0xff);
			tmpBuf[3] = (byte) ((vlen >>> 0) & 0xff);

			out.write(tmpBuf); // sending data

			op.close();

			return ResponseCodes.OBEX_HTTP_OK;

		} catch (Exception e) {

			return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
		}

	}

	public int onPut(Operation op) {

		try 
		{
			InputStream in = op.openInputStream();
			byte[] fullResult = null;
			{
				byte[] buf = new byte[256];
				ByteArrayOutputStream bout = new ByteArrayOutputStream(2048);
				for (int len = in.read(buf); len >= 0; len = in.read(buf))
					bout.write(buf, 0, len);
				fullResult = bout.toByteArray();
			}
			ByteArrayInputStream bin = new ByteArrayInputStream(fullResult);
			DataInputStream din = new DataInputStream(bin);
			int size = din.readInt();
			byte[] vCard = new byte[size];
			din.read(vCard);
			// card is received

			op.close();
			parent.getListener().onReceiveBC(vCard);

			return ResponseCodes.OBEX_HTTP_OK;

		} 
		catch (Exception e) {

			return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
		}

	}

	public void startSending(int oper) throws Exception {
		parent.setState(new InquiryState(parent, oper));

	}

	public void cancelSending() {
		// Internal error, but not fatal
	}

}
