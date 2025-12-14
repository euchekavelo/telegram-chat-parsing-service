package ru.botcreators.telegramchatparsingservice.service;

import org.junit.jupiter.api.Test;
import ru.botcreators.telegramchatparsingservice.model.ExportPayload;
import ru.botcreators.telegramchatparsingservice.model.UserRecord;
import ru.botcreators.telegramchatparsingservice.service.impl.UserAggregationServiceImpl;
import ru.botcreators.telegramchatparsingservice.service.impl.UserKeyServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserAggregationServiceImplTest {

    private final UserKeyService keyService = new UserKeyServiceImpl();
    private final UserAggregationService service = new UserAggregationServiceImpl(keyService);

    @Test
    void collectUniqueUsers_shouldDedupeAcrossParticipantsAndMentions_byUserId() {
        UserRecord p = new UserRecord();
        p.setUserId("1");
        p.setUsername("user1");

        UserRecord m = new UserRecord();
        m.setUserId("1");
        m.setUsername("user1_duplicated");

        ExportPayload payload = new ExportPayload();
        payload.setParticipants(List.of(p));
        payload.setMentions(List.of(m));

        var result = service.collectUniqueUsers(payload);
        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getUsername());
    }

    @Test
    void collectUniqueUsers_shouldIncludeBothWhenDifferentKeys() {
        UserRecord a = new UserRecord();
        a.setUserId("1");
        a.setUsername("a");

        UserRecord b = new UserRecord();
        b.setUserId("2");
        b.setUsername("b");

        ExportPayload payload = new ExportPayload();
        payload.setParticipants(List.of(a));
        payload.setMentions(List.of(b));

        var result = service.collectUniqueUsers(payload);
        assertEquals(2, result.size());
    }

    @Test
    void collectUniqueUsers_shouldNotFailOnNullLists() {
        ExportPayload payload = new ExportPayload();
        payload.setParticipants(null);
        payload.setMentions(null);

        var result = service.collectUniqueUsers(payload);
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
