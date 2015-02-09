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

import javax.obex.Operation;

/**
 * 
 * This is the abstract base class for the all states of the
 * communication state machine. Each state is implementing methods
 * which are can be executed in that particular case.
 * 
 * Classes IdleState, InquiryState, ServiceDiscoveryState,
 * ReceiveState and SendState inherit from this class
 * 
 * @see Design patterns: State
 * 
 */
public abstract class ExchangerState {

  protected ExchangerStateParent parent;

  /**
   * Implements OBEX GET command
   * <p>
   * Implements server handling of the OBEX GET command
   * 
   * @param op -
   *          OBEX operation class
   * @return - OBEX response code
   * @see javax.obex.ServerRequestHandler
   */
  abstract public int onGet(Operation op);

  /**
   * Implements OBEX PUT command
   * <p>
   * Implements server handling of the OBEX PUT command
   * 
   * @param op -
   *          OBEX operation class
   * @return - OBEX response code
   * @see javax.obex.ServerRequestHandler
   */
  abstract public int onPut(Operation op);

  /**
   * Implements actions related to starting sending process
   * <p>
   * Implements reaction of the state on command to start sending
   * process
   * 
   * @exception Exception -
   *              if any immediate error occur
   * @see example.BCExchanger.comm.ExchangerComm
   */
  abstract public void startSending(int oper) throws Exception;

  /**
   * Implements actions related to canceling sending process
   * <p>
   * Implements reaction of the state on command to cancel sending
   * process
   * 
   * @see example.BCExchanger.comm.ExchangerComm
   */
  abstract public void cancelSending();

  /**
   * Constructor
   * 
   * @param -
   *          _parent - the class which nests the current state of the
   *          communication machine
   */
  public ExchangerState(ExchangerStateParent _parent) {
    parent = _parent;
  }

}
