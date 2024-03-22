package com.mipt.hsse.hssetechbackend.payments.acquiring;


public interface AcquiringProvider {
  /**
   * Initializes a new payment session
   * @param paymentInfo Information about current session
   * @return A session information
   */
  PaymentSessionInfo initializePaymentSession(PaymentInfo paymentInfo);
}
