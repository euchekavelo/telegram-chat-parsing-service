package ru.botcreators.telegramchatparsingservice.service;

import ru.botcreators.telegramchatparsingservice.model.ExportPayload;
import ru.botcreators.telegramchatparsingservice.model.ExportedFile;

public interface ExcelExportService {

    ExportedFile buildXlsx(ExportPayload payload, String filenamePrefix);
}
