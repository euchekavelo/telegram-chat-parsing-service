package ru.botcreators.telegramchatparsingservice.model.chatimport;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramTextEntity {
    public String type;

    public String text;

    @JsonProperty("user_id")
    public Long user_id;
}
