package ru.botcreators.telegramchatparsingservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ChannelRecord {
    private String username;
    private String title;
    private String url;
    private String discoveredFrom;

    public ChannelRecord(String username, String title, String url, String discoveredFrom) {
        this.username = username;
        this.title = title;
        this.url = url;
        this.discoveredFrom = discoveredFrom;
    }
}
