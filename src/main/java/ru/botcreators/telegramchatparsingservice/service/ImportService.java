package ru.botcreators.telegramchatparsingservice.service;

import org.springframework.web.multipart.MultipartFile;
import ru.botcreators.telegramchatparsingservice.model.ExportResponse;

import java.io.IOException;

public interface ImportService {
    ExportResponse importChatHistory(MultipartFile file) throws IOException;
}
