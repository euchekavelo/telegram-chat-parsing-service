package ru.botcreators.telegramchatparsingservice.service;

import org.junit.jupiter.api.Test;
import ru.botcreators.telegramchatparsingservice.model.UserRecord;
import ru.botcreators.telegramchatparsingservice.service.impl.TextExportServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextExportServiceImplTest {

    private final TextExportService service = new TextExportServiceImpl();

    @Test
    void buildText_shouldReturnNoFoundMessageOnEmpty() {
        String text = service.buildText(List.of());
        assertTrue(text.contains("Ничего не найдено"));
    }

    @Test
    void buildText_shouldFormatUsernameAndName() {
        UserRecord u = new UserRecord();
        u.setUsername("user1");
        u.setFullName("User One");

        String text = service.buildText(List.of(u));
        assertTrue(text.contains("@user1"));
        assertTrue(text.contains("User One"));
    }

    @Test
    void buildText_shouldHandleMissingUsername() {
        UserRecord u = new UserRecord();
        u.setFullName("No Username");

        String text = service.buildText(List.of(u));
        assertTrue(text.contains("No Username"));
    }

    @Test
    void buildText_shouldTruncateWhenTooLong() {
        List<UserRecord> users = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            UserRecord u = new UserRecord();
            u.setUsername("user" + i);
            u.setFullName("X".repeat(50));
            users.add(u);
        }

        String text = service.buildText(users);
        assertTrue(text.contains("обрезано"));
        assertTrue(text.length() <= 4100);
    }
}
