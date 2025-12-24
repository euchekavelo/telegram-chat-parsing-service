package ru.botcreators.telegramchatparsingservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.botcreators.telegramchatparsingservice.client.TelegramBotClient;
import ru.botcreators.telegramchatparsingservice.service.CommandService;
import ru.botcreators.telegramchatparsingservice.service.LocalizationService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LanguageCommandServiceImpl implements CommandService {

    public static final String LANG_RUS = "lang_rus";
    public static final String LANG_EN = "lang_en";
    private final LocalizationService localizationService;
    private final TelegramBotClient telegramBotClient;

    @Override
    public boolean canHandleCommand(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return false;
        }
        String localizedMessage = localizationService.getLocalizedMessage("menu.language");

        return update.getMessage().getText().equals(localizedMessage);
    }

    @Override
    public void handleCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        String localizedMessage = localizationService.getLocalizedMessage("language.select");

        SendMessage message = SendMessage
                .builder()
                .chatId(chatId)
                .text(localizedMessage)
                .replyMarkup(languageInline())
                .build();

        telegramBotClient.sendMessage(message);
    }

    private ReplyKeyboard languageInline() {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder()
                .text(localizationService.getLocalizedMessage("language.ru"))
                .callbackData(LANG_RUS)
                .build()));
        rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder()
                .text(localizationService.getLocalizedMessage("language.en"))
                .callbackData(LANG_EN)
                .build()));

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}
