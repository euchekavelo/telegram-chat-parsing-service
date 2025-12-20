package ru.botcreators.telegramchatparsingservice.model.chatimport;

import lombok.Data;

import java.util.List;

@Data
public class TelegramReaction {

    public String type;

    public Integer count;

    public String emoji;

    public List<TelegramReactionRecent> recent;
}
