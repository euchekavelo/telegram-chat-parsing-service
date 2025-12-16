package ru.botcreators.telegramchatparsingservice.model.chatimport;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramReactionRecent {

    public String from;

    @JsonProperty("from_id")
    public String fromId;

    public String date;
}
