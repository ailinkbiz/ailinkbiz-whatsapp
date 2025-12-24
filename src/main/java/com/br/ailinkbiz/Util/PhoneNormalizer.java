package com.br.ailinkbiz.Util;

public final class PhoneNormalizer {

    private PhoneNormalizer() {}

    // Entrada vinda de payload, logs, endpoints
    public static String toUserId(String raw) {
        if (raw == null) return null;

        String phone = raw
                .replace("whatsapp:", "")
                .replace(" ", "")
                .trim();

        if (!phone.startsWith("+")) {
            phone = "+" + phone;
        }

        return phone;
    }

    // Saída para WhatsApp (Twilio)
    public static String toWhatsApp(String userId) {
        if (userId == null) return null;
        return "whatsapp:" + userId;
    }

}