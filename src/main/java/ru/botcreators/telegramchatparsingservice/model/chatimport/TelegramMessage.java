package ru.botcreators.telegramchatparsingservice.model.chatimport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramMessage {

    public Long id;

    public String type;

    public String date;

    @JsonProperty("date_unixtime")
    public String dateUnixtime;

    public String from;

    @JsonProperty("from_id")
    public String fromId;

    public String actor;

    @JsonProperty("actor_id")
    public String actorId;

    public String action;

    public Object text;

    @JsonProperty("text_entities")
    public List<TelegramTextEntity> textEntities;

    public List<String> members;

    @JsonProperty("message_id")
    public Long messageId;

    @JsonProperty("reply_to_message_id")
    public Long replyToMessageId;

    public String edited;

    @JsonProperty("edited_unixtime")
    public String editedUnixtime;

    public List<TelegramReaction> reactions;
}
