package ru.botcreators.telegramchatparsingservice.service;

import org.junit.jupiter.api.Test;
import ru.botcreators.telegramchatparsingservice.model.UserRecord;
import ru.botcreators.telegramchatparsingservice.service.impl.UserKeyServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserKeyServiceImplTest {

    private final UserKeyService service = new UserKeyServiceImpl();

    @Test
    void key_shouldPreferUserId() {
        UserRecord u = new UserRecord();
        u.setUserId(" 123 ");
        u.setUsername("SomeUser");

        assertEquals("id:123", service.key(u));
    }

    @Test
    void key_shouldUseNormalizedUsernameWhenNoUserId() {
        UserRecord u = new UserRecord();
        u.setUserId("  ");
        u.setUsername(" @TeStUser ");

        assertEquals("u:testuser", service.key(u));
    }

    @Test
    void key_shouldFallbackToFullNameAndBioHash() {
        UserRecord u1 = new UserRecord();
        u1.setFullName("Ivan Ivanov");
        u1.setBio("Hello");

        UserRecord u2 = new UserRecord();
        u2.setFullName(" Ivan Ivanov ");
        u2.setBio(" hello ");

        assertEquals(service.key(u1), service.key(u2));
        assertTrue(service.key(u1).startsWith("w:"));
    }

    @Test
    void key_shouldReturnIdentityKeyWhenEverythingEmpty() {
        UserRecord u = new UserRecord();
        String k = service.key(u);
        assertTrue(k.startsWith("x:"));
    }

    @Test
    void key_nullUser_returnsLiteralNull() {
        assertEquals("null", service.key(null));
    }
}
