package me.itsghost.jdiscord.internal.httprequestbuilders;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Header {
    private final String type;
    private final String data;
}
