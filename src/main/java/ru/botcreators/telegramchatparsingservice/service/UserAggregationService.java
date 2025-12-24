package ru.botcreators.telegramchatparsingservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.botcreators.telegramchatparsingservice.model.ExportPayload;
import ru.botcreators.telegramchatparsingservice.model.UserRecord;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserAggregationService {

    private final UserKeyService userKeyService;

    public List<UserRecord> collectUniqueUsers(ExportPayload payload) {
        Objects.requireNonNull(payload, "payload");

        List<UserRecord> participants = Optional.ofNullable(payload.getParticipants()).orElse(List.of());
        List<UserRecord> mentions = Optional.ofNullable(payload.getMentions()).orElse(List.of());

        Map<String, UserRecord> map = new LinkedHashMap<>();

        Stream.concat(participants.stream(), mentions.stream())
                .filter(Objects::nonNull)
                .forEach(u -> map.putIfAbsent(userKeyService.key(u), u));

        return map.values().stream()
                .sorted(Comparator
                        .comparing((UserRecord u) -> u.getUsername() == null || u.getUsername().isBlank())
                        .thenComparing(u -> Optional.ofNullable(normUsername(u.getUsername())).orElse(""))
                        .thenComparing(u -> Optional.ofNullable(u.getFullName()).orElse("")))
                .collect(Collectors.toList());
    }

    private String normUsername(String username) {
        if (username == null) return null;
        String u = username.trim();
        if (u.isBlank()) return null;
        return u.startsWith("@") ? u : "@" + u;
    }
}
