package ru.botcreators.telegramchatparsingservice.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CommandService {

    boolean canHandleCommand(Update update);

    void handleCommand(Update update);
}
