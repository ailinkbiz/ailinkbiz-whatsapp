package com.br.ailinkbiz.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class TwilioSender implements MessageSender {

    @Override
    public void send(String to, String message) {

        Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(System.getenv("TWILIO_WHATSAPP_FROM")),
                message
        ).create();

    }

}