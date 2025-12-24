package ru.botcreators.telegramchatparsingservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.botcreators.telegramchatparsingservice.client.TelegramBotClient;
import ru.botcreators.telegramchatparsingservice.service.CommandService;
import ru.botcreators.telegramchatparsingservice.service.KeyboardService;
import ru.botcreators.telegramchatparsingservice.service.LocalizationService;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LanguageCallBackCommandServiceImpl implements CommandService {

    private final LocalizationService localizationService;
    private final KeyboardService keyboardService;
    private final TelegramBotClient telegramBotClient;

    @Override
    public boolean canHandleCommand(Update update) {
        if (!update.hasCallbackQuery()) {
            return false;
        }
        String callbackData = update.getCallbackQuery().getData();

        return callbackData.equals(LanguageCommandServiceImpl.LANG_RUS)
                || callbackData.equals(LanguageCommandServiceImpl.LANG_EN);
    }

    @Override
    public void handleCommand(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackData = update.getCallbackQuery().getData();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        if (callbackData.equals(LanguageCommandServiceImpl.LANG_RUS)) {
            localizationService.setLocale(new Locale("ru"));
            changeLanguage(chatId, messageId);
        } else if (callbackData.equals(LanguageCommandServiceImpl.LANG_EN)) {
            localizationService.setLocale(Locale.ENGLISH);
            changeLanguage(chatId, messageId);
        }
    }

    private void changeLanguage(Long chatId, int messageId) {
        String switched = localizationService.getLocalizedMessage("language.switched");
        EditMessageText editMessage = EditMessageText
                .builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(switched)
                .build();

        telegramBotClient.sendMessage(editMessage);

        SendMessage message = SendMessage
                .builder()
                .chatId(chatId)
                .text(localizationService.getLocalizedMessage("menu.welcome"))
                .replyMarkup(keyboardService.mainMenu())
                .build();

        telegramBotClient.sendMessage(message);
    }
}
