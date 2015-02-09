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
 * This is an interface to OBEX communication module. OBEX
 * communication module implemented by the classes from
 * bcexchanger.comm package realize exhange of the business card over
 * JSR82 OBEX. The controls of the module is abstracted in this
 * interface.
 * 
 * BCExchanger is calling the methods of this interface implemented by
 * ExchangerCommImpl class to control OBEX communication.
 * 
 * @version 1.0 27.09.2005
 * 
 */
public interface ExchangerComm {

  // Constant field declaration
  static final int DONE = 0;
  static final int CANCELED = 1;
  static final int ERROR = 2;
  static final int NO_RECORDS = 3;

  /**
   * Intitiate whole procedure of seding business card to a remote
   * device
   * <p>
   * The method start the procedure of inquiry, service discovery,
   * tranfering own card and receiving the card from the remote
   * device. This method is asynchrounous and it returns immediately
   * after operation is started.
   * 
   * @exception Exception -
   *              if any immediate errors occur while the process is
   *              started. In practice it is thrown when inquiry
   *              cannot start or if this method is called while
   *              sending process is in progress
   */
  public void startSending() throws Exception;

  /**
   * Cancels the process of sending
   * <p>
   * The method cancels the current operation of the sending: inquiry,
   * service discovery, OBEX send or receive and returns to the idle
   * state.
   * 
   */
  public void cancelSending();

  /**
   * Starts to listen for incomming connections
   * <p>
   * The method start listening for incomming connections. This method
   * is asynchronous and it returns immediately after the connection
   * listening is started.
   * 
   */
  public void startWaiting();

  /**
   * This method stops listening for incomming connection
   * <p>
   * The method stops listening for incomming connection if
   * startWaiting() was called before. Otherwise method does nothing.
   * 
   */
  public void cancelWaiting();

}
