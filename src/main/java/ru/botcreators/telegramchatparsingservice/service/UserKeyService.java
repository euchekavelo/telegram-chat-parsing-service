package ru.botcreators.telegramchatparsingservice.service;

import org.springframework.stereotype.Service;
import ru.botcreators.telegramchatparsingservice.model.UserRecord;

import java.util.Optional;

@Service
public class UserKeyService {

    public String key(UserRecord u) {
        if (u == null) return "null";

        if (u.getUserId() != null && !u.getUserId().isBlank()) {
            return "id:" + u.getUserId().trim();
        }

        String username = u.getUsername();
        if (username != null && !username.isBlank()) {
            return "u:" + username.trim().toLowerCase().replaceFirst("^@", "");
        }

        String full = Optional.ofNullable(u.getFullName()).orElse("").trim().toLowerCase();
        String bio = Optional.ofNullable(u.getBio()).orElse("").trim().toLowerCase();

        if (full.isBlank() && bio.isBlank()) {
            return "x:" + System.identityHashCode(u);
        }

        return "w:" + full + "|" + bio.hashCode();
    }
}
