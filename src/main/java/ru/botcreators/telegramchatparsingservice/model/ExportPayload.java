package ru.botcreators.telegramchatparsingservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class ExportPayload {
    private Instant exportInstant = Instant.now();
    private List<UserRecord> participants = new ArrayList<>();
    private List<UserRecord> mentions = new ArrayList<>();
    private List<ChannelRecord> channels = new ArrayList<>();

    public ExportPayload(Instant exportInstant,
                         List<UserRecord> participants,
                         List<UserRecord> mentions,
                         List<ChannelRecord> channels) {
        this.exportInstant = exportInstant;
        this.participants = participants != null ? participants : new ArrayList<>();
        this.mentions = mentions != null ? mentions : new ArrayList<>();
        this.channels = channels != null ? channels : new ArrayList<>();
    }
}
