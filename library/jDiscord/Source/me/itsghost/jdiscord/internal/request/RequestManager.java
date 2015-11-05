package me.itsghost.jdiscord.internal.request;

import lombok.Getter;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;
import me.itsghost.jdiscord.exception.DiscordFailedToConnectException;
import me.itsghost.jdiscord.internal.httprequestbuilders.PacketBuilder;
import me.itsghost.jdiscord.internal.httprequestbuilders.RequestType;
import org.json.JSONObject;

public class RequestManager {
    @Getter private WebSocketClient socketClient;

    public RequestManager(DiscordAPIImpl api) throws DiscordFailedToConnectException {
        try {
            PacketBuilder pb = new PacketBuilder(api);
            pb.setType(RequestType.GET);
            pb.setUrl("https://discordapp.com/api/gateway");
            String response = pb.makeRequest();

            if (response == null)
                throw new DiscordFailedToConnectException();

            socketClient = new WebSocketClient(api, new JSONObject(response).getString("url"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new DiscordFailedToConnectException();
        }
    }

}
