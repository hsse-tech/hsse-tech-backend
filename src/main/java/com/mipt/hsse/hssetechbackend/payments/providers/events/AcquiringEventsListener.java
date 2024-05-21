package com.mipt.hsse.hssetechbackend.payments.providers.events;

public interface AcquiringEventsListener {
  void onAcquiringNotificationReceived(MerchantNotification notification);
}
