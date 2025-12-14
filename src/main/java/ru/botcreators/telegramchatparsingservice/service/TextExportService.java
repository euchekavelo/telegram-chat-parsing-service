package ru.botcreators.telegramchatparsingservice.service;

import ru.botcreators.telegramchatparsingservice.model.UserRecord;

import java.util.List;

public interface TextExportService {
    String buildText(List<UserRecord> users);
}
