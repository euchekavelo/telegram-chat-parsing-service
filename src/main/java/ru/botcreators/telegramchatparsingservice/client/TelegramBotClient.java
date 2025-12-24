package ru.botcreators.telegramchatparsingservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.InputStream;
import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class TelegramBotClient {

    private final TelegramClient telegramClient;

    public void sendMessage(BotApiMethod<? extends Serializable> message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public File getDownloadedFileMetadata(GetFile getFileRequest) {
        try {
            return telegramClient.execute(getFileRequest);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream getDownloadedFileContent(File file) {
        try {
            return telegramClient.downloadFileAsStream(file);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendDocument(SendDocument sendDocument) {
        try {
            telegramClient.execute(sendDocument);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
