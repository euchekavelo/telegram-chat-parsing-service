package ru.botcreators.telegramchatparsingservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.botcreators.telegramchatparsingservice.client.TelegramBotClient;
import ru.botcreators.telegramchatparsingservice.service.CommandService;
import ru.botcreators.telegramchatparsingservice.service.KeyboardService;
import ru.botcreators.telegramchatparsingservice.service.LocalizationService;

@Service
@RequiredArgsConstructor
public class StartCommandServiceImpl implements CommandService {

    private final LocalizationService localizationService;
    private final KeyboardService keyboardService;
    private final TelegramBotClient telegramBotClient;

    @Override
    public boolean canHandleCommand(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return false;
        }
        String localizedMessage = localizationService.getLocalizedMessage("menu.start");
        return update.getMessage().getText().equals(localizedMessage);
    }

    @Override
    public void handleCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        String localizedMessage = localizationService.getLocalizedMessage("menu.welcome");
        SendMessage message = SendMessage
                .builder()
                .chatId(chatId)
                .text(localizedMessage)
                .replyMarkup(keyboardService.mainMenu())
                .build();

        telegramBotClient.sendMessage(message);
    }
}
