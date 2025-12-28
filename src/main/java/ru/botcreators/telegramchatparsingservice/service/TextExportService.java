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
        sb.append("Найдено явных пользователей и упоминаний: ").append(total).append("\n");

        if (users == null || users.isEmpty()) {
            sb.append("Ничего не найдено (в истории нет авторов/упоминаний).");
            return sb.toString();
        }

        int numberOfUsers = 0;
        int numberOfMentions = 0;
        int i = 1;
        for (UserRecord u : users) {
            String handle = normUsername(u.getUsername());
            String name = safe(u.getFullName()).trim();

            String line;
            if (handle != null && !handle.isBlank()) {
                line = i + ". " + handle + (name.isBlank() ? "" : " — " + name);
                numberOfMentions++;
            } else {
                line = i + ". " + (name.isBlank() ? "(без username)" : name);
                numberOfUsers++;
            }

            if (sb.length() + line.length() + 1 > TELEGRAM_MAX_LEN) {
                addStatisticsDetails(sb, numberOfUsers, numberOfMentions);
                sb.append("\n…сообщение обрезано из-за лимита Telegram. Для полного списка сформируйте Excel.");
                break;
            }

            sb.append(line).append("\n");
            i++;
        }

        addStatisticsDetails(sb, numberOfUsers, numberOfMentions);

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

    private void addStatisticsDetails(StringBuilder sb, int numberOfUsers, int numberOfMentions) {
        StringBuilder statisticsDetails = new StringBuilder();
        statisticsDetails.append("Явных пользователей: ").append(numberOfUsers).append("\n");
        statisticsDetails.append("Упоминаний: ").append(numberOfMentions).append("\n\n");

        int insertIndex = sb.indexOf("\n");

        sb.insert(insertIndex + 1, statisticsDetails);
    }
}
