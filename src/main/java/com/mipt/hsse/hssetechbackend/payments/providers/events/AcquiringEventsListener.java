package com.mipt.hsse.hssetechbackend.payments.providers.events;

public interface AcquiringEventsListener {
  void onAcquiringNoficationReceived(MerchantNotification notification);
}
