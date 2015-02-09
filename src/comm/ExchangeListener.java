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

import java.util.Vector;

/**
 * 
 * ExchangeListner is an callback interface. By means of the interface
 * function OBEX communication module signal to a listener about
 * various networking events.
 * 
 * This interface is implemented by the BCExchangerMIDlet and OBEX
 * communication classes located in the package bcexchanger.comm use
 * it for signaling
 * 
 * @version 1.0 27.09.2005
 */
public interface ExchangeListener {

  /**
   * Called when the OBEX communication module completes the inquiry
   * 
   * @param code -
   *          inquiry completion code
   */
  public void onInquiryComplete(int code);

  /**
   * Called when the OBEX communication module completes service
   * discoveries
   * <p>
   * This method is called when service discovery on all devices is
   * complete
   * 
   * @param
   * @param code -
   *          service discovery completion code
   */
  public void onServiceDiscoveryComplete(int code);

  /**
   * This method is called when OBEX communication module needs to
   * resolve services
   * <p>
   * If several services are found, the OBEX communcation module asks
   * the MIDlet to resolve the services and choose the only one to
   * connect to
   * 
   * @param friendlyNames -
   *          list of friendly names of devices which has the service
   * @return index of the chosen device/service from the vector
   * @throws InterruptedException
   */
  public int resolveMultipleServices(Vector friendlyNames) throws InterruptedException;

  /**
   * This method returns own business card serialized to vCard/2.1
   * format
   * <p>
   * When OBEX module needs to send the own business card to a remote
   * device it uses this method to request own business card from the
   * MIDlet
   * 
   * @return byte array containing own business card serialized to
   *         vCard/2.1 format null if there were errors or cancelation
   * @throws Exception
   */
  public byte[] getOwnBC() throws Exception;

  /**
   * Called when business card send (Client OBEX PUT) is finished
   * 
   * @param code -
   *          completion code
   */
  public void onSendComplete(int code);

  /**
   * Called when business card receive (Client OBEX GET) is finished
   * 
   * @param code -
   *          completion code
   */
  public void onReceiveComplete(int code);

  /**
   * Called when Server OBEX GET operation is finished
   * 
   * @param code -
   *          completion code
   */
  public void onGetComplete(int code);

  /**
   * Called when Server OBEX PUT operation is finished
   * 
   * @param code -
   *          completion code
   */
  public void onPutComplete(int code);

  /**
   * Called when a business card from a remote device is received
   * <p>
   * This method allows MIDlet to save received business card.
   * 
   * @param vCard -
   *          byte array containing receive business card serialized
   *          to vCard/2.1 format
   * @throws Exception -
   *           if saving of the business card fails
   */
  public void onReceiveBC(byte[] vCard) throws Exception;

  /**
   * Called in case of problems with Bluetooth OBEX server accepting connection
   * 
   */
  public void onServerError();
    
  
}
