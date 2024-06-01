package com.mipt.hsse.hssetechbackend.payments.providers;

public interface AcquiringSessionInitializer {
  SessionData initialize(SessionParams sessionData);
}
