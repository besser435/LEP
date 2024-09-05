package org.besser.lep;

import org.bukkit.configuration.file.FileConfiguration;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.UUID;

import static java.util.logging.Level.*;
import static org.besser.lep.LepLogger.log;

public class SendBuyTelemetry {
    private final String apiUrl;

    public SendBuyTelemetry(FileConfiguration config) {
        this.apiUrl = config.getString("telemetry.api_url");
    }

    public void sendTelemetry(String playerName, UUID playerUUID, String purchasedItem, int purchasedQuantity, String paymentItem, int paymentQuantity, long time) {
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("player_name", playerName);
        jsonPayload.put("player_uuid", playerUUID);
        jsonPayload.put("purchased_item", purchasedItem);
        jsonPayload.put("purchased_quantity", purchasedQuantity);
        jsonPayload.put("payment_item", paymentItem);
        jsonPayload.put("payment_quantity", paymentQuantity);
        jsonPayload.put("time", time);
        String jsonString = jsonPayload.toString();

        try {
            URL obj = new URI("https://usa-industries.net/api/lep/update_telemetry").toURL();   // TODO use URL from config, add toggle

            HttpURLConnection connection = (HttpURLConnection)obj.openConnection();


            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            connection.setRequestProperty("Content-Type", "application/json");

            try (DataOutputStream os = new DataOutputStream(connection.getOutputStream())) {
                os.writeBytes(jsonString);
                os.flush();
                log(INFO, jsonString);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                log(INFO, "Response: " + responseCode);
            }
            else {
                log(WARNING, "Error: HTTP Response code - " + responseCode);
            }
            connection.disconnect();
        }
        catch (IOException | URISyntaxException e) {
            log(SEVERE, "An error occurred while sending telemetry: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
