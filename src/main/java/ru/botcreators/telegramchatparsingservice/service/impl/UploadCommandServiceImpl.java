package ru.botcreators.telegramchatparsingservice.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import ru.botcreators.telegramchatparsingservice.client.TelegramBotClient;
import ru.botcreators.telegramchatparsingservice.model.ExportResponse;
import ru.botcreators.telegramchatparsingservice.model.ExportResponseType;
import ru.botcreators.telegramchatparsingservice.service.CommandService;
import ru.botcreators.telegramchatparsingservice.service.ImportService;
import ru.botcreators.telegramchatparsingservice.service.LocalizationService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class UploadCommandServiceImpl implements CommandService {

    private final LocalizationService localizationService;
    private final ImportService importService;
    private final TelegramBotClient telegramBotClient;
    @Qualifier("taskExecutor")
    private final Executor executor;

    public UploadCommandServiceImpl(LocalizationService localizationService, ImportService importService,
                                    TelegramBotClient telegramBotClient, @Qualifier("taskExecutor") Executor executor) {

        this.telegramBotClient = telegramBotClient;
        this.localizationService = localizationService;
        this.importService = importService;
        this.executor = executor;
    }

    @Override
    public boolean canHandleCommand(Update update) {
        if (update.hasMessage() && update.getMessage().hasDocument()) {
            Document document = update.getMessage().getDocument();
            String mimeType = document.getMimeType();
            String fileName = document.getFileName();

            return mimeType.equals("application/json") || (fileName != null && fileName.endsWith(".json"));
        }

        return false;
    }

    @Override
    public void handleCommand(Update update) {
        ReplyParameters replyParameters = new ReplyParameters();
        replyParameters.setMessageId(update.getMessage().getMessageId());
        SendMessage infoMessage = SendMessage
                .builder()
                .chatId(update.getMessage().getChatId())
                .text(localizationService.getLocalizedMessage("system.process"))
                .replyParameters(replyParameters)
                .build();

        telegramBotClient.sendMessage(infoMessage);

        CompletableFuture.runAsync(() -> sendMessage(replyParameters, update), executor);
    }

    private void sendMessage(ReplyParameters replyParameters, Update update) {
        Document document = update.getMessage().getDocument();
        GetFile getFileRequest = new GetFile(document.getFileId());
        File fileMetadata = telegramBotClient.getDownloadedFileMetadata(getFileRequest);

        ExportResponse exportResponse;
        try (InputStream inputStream = telegramBotClient.getDownloadedFileContent(fileMetadata)) {
            exportResponse = importService.importChatHistory(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (exportResponse.getType().equals(ExportResponseType.TEXT)) {
            SendMessage message = SendMessage
                    .builder()
                    .chatId(update.getMessage().getChatId())
                    .text(exportResponse.getText())
                    .replyParameters(replyParameters)
                    .build();

            telegramBotClient.sendMessage(message);
            return;
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(exportResponse.getFile().getBytes());
        SendDocument sendDocument = new SendDocument(String.valueOf(update.getMessage().getChatId()),
                new InputFile(byteArrayInputStream, exportResponse.getFile().getFilename()));
        sendDocument.setReplyParameters(replyParameters);

        telegramBotClient.sendDocument(sendDocument);
    }
}
