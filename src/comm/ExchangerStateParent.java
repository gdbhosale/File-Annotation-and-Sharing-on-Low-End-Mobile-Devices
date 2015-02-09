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

/**
 * 
 * This interface contains the methods which the states of the
 * communication state machine use to get needed functionality from
 * the parent
 * 
 * This interface is implemented by ExchangerCommImpl class.
 * 
 * @version 1.0 29.09.2005
 * 
 */
public interface ExchangerStateParent {

  /**
   * Current state setter
   * <p>
   * Sets the current state in the parent
   * 
   * @param state -
   *          state of communication machine
   */
  public void setState(ExchangerState state);

  /**
   * Listener getter
   * <p>
   * Returns the communication event listener which is stored in the
   * parent
   * 
   * @return communication event listener interface
   */
  public ExchangeListener getListener();

  /**
   * Communication state getter
   * <p>
   * Returns current state of the communication machine which is kept
   * in the parent
   * 
   * @return current state of the communication machine
   */
  public ExchangerState getState();

  /**
   * Returns "Business Card Exchanger Service" UUID
   * 
   * @return string containing UUID of the Bluetooth OBEX server
   *         connection
   */
  public String getUUID();

}
