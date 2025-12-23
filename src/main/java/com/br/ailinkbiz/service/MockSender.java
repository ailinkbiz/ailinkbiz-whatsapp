package com.br.ailinkbiz.service;


public class MockSender implements MessageSender {

    @Override
    public void send(String to, String message) {
//        System.out.println(
//                "[MOCK SEND] → " + to + " | " + message
//        );
    }

}