package ru.botcreators.telegramchatparsingservice.service;

import ru.botcreators.telegramchatparsingservice.model.ExportPayload;
import ru.botcreators.telegramchatparsingservice.model.UserRecord;

import java.util.List;

public interface UserAggregationService {
    List<UserRecord> collectUniqueUsers(ExportPayload payload);
}
