package ru.botcreators.telegramchatparsingservice.service;

import ru.botcreators.telegramchatparsingservice.model.ExportPayload;
import ru.botcreators.telegramchatparsingservice.model.ExportResponse;

public interface ExportOrchestratorService {
    ExportResponse export(ExportPayload payload, String filenamePrefix);
}
