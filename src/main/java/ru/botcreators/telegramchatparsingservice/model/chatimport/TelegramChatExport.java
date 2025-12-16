package ru.botcreators.telegramchatparsingservice.model.chatimport;

import lombok.Data;

import java.util.List;

@Data
public class TelegramChatExport {
    public String name;
    public String type;
    public Long id;

    public List<TelegramMessage> messages;
}
