package ru.botcreators.telegramchatparsingservice.service;

import org.springframework.stereotype.Service;
import ru.botcreators.telegramchatparsingservice.model.UserRecord;

import java.util.*;

@Service
public class TextExportService {

    private static final int TELEGRAM_MAX_LEN = 3900;

    public String buildText(List<UserRecord> users) {
        int total = users == null ? 0 : users.size();

        StringBuilder sb = new StringBuilder();
        sb.append("Найдено пользователей: ").append(total).append("\n\n");

        if (users == null || users.isEmpty()) {
            sb.append("Ничего не найдено (в истории нет авторов/упоминаний).");
            return sb.toString();
        }

        int i = 1;
        for (UserRecord u : users) {
            String handle = normUsername(u.getUsername());
            String name = safe(u.getFullName()).trim();

            String line;
            if (handle != null && !handle.isBlank()) {
                line = i + ". " + handle + (name.isBlank() ? "" : " — " + name);
            } else {
                line = i + ". " + (name.isBlank() ? "(без username)" : name);
            }

            if (sb.length() + line.length() + 1 > TELEGRAM_MAX_LEN) {
                sb.append("\n…сообщение обрезано из-за лимита Telegram. Для полного списка сформируйте Excel.");
                break;
            }

            sb.append(line).append("\n");
            i++;
        }

        return sb.toString().trim();
    }

    private String normUsername(String username) {
        if (username == null) return null;
        String u = username.trim();
        if (u.isBlank()) return null;
        return u.startsWith("@") ? u : "@" + u;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
