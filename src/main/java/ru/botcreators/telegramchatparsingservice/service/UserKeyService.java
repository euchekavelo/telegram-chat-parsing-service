package ru.botcreators.telegramchatparsingservice.service;

import ru.botcreators.telegramchatparsingservice.model.UserRecord;

public interface UserKeyService {
    String key(UserRecord user);
}
