package com.br.ailinkbiz.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

public class TwilioService {

    static {
        Twilio.init(
                System.getenv("TWILIO_ACCOUNT_SID"),
                System.getenv("TWILIO_AUTH_TOKEN")
        );
    }

    public static void sendMessage(String to, String body) {
        Message.creator(
                new com.twilio.type.PhoneNumber(to),
                new com.twilio.type.PhoneNumber(System.getenv("TWILIO_WHATSAPP_FROM")),
                body
        ).create();
    }

    static {
        Twilio.init(
                System.getenv("TWILIO_ACCOUNT_SID"),
                System.getenv("TWILIO_AUTH_TOKEN")
        );
    }

}