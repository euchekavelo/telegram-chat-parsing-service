package ru.botcreators.telegramchatparsingservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.botcreators.telegramchatparsingservice.model.ChannelRecord;
import ru.botcreators.telegramchatparsingservice.model.ExportPayload;
import ru.botcreators.telegramchatparsingservice.model.ExportResponse;
import ru.botcreators.telegramchatparsingservice.model.UserRecord;
import ru.botcreators.telegramchatparsingservice.model.UserSource;
import ru.botcreators.telegramchatparsingservice.model.chatimport.TelegramChatExport;
import ru.botcreators.telegramchatparsingservice.model.chatimport.TelegramMessage;
import ru.botcreators.telegramchatparsingservice.model.chatimport.TelegramTextEntity;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final static String FILE_PREFIX = "chat_export";
    private final static String DISCOVERED_FROM = "Экспорт чата из Telegram";
    private final ObjectMapper objectMapper;
    private final ExportOrchestratorService orchestratorService;

    public ExportResponse importChatHistory(InputStream inputStream) throws IOException {
        TelegramChatExport telegramChatExport = objectMapper.readValue(inputStream, TelegramChatExport.class);
        ExportPayload exportPayload = processTelegramChatExport(telegramChatExport);
        String filename = getFilename(exportPayload);
        return orchestratorService.export(exportPayload, filename);
    }

    private ExportPayload processTelegramChatExport(TelegramChatExport telegramChatExport) {
        List<UserRecord> participants = retrieveParticipants(telegramChatExport);
        List<UserRecord> mentions = retrieveMentions(telegramChatExport);
        List<ChannelRecord> channels = retrieveChannels(telegramChatExport);
        Instant exportInstant = Instant.now();
        return new ExportPayload(exportInstant, participants, mentions, channels);
    }

    private List<ChannelRecord> retrieveChannels(TelegramChatExport telegramChatExport) {
        ChannelRecord channelRecord = new ChannelRecord();
        channelRecord.setTitle(telegramChatExport.getName());
        channelRecord.setUsername(telegramChatExport.getId().toString());
        channelRecord.setDiscoveredFrom(DISCOVERED_FROM);
        channelRecord.setUrl(getChatUrl(telegramChatExport));
        List<ChannelRecord> channels = new ArrayList<>();
        channels.add(channelRecord);
        return channels;
    }

    private String getChatUrl(TelegramChatExport telegramChatExport) {
        // todo скорее всего можно получить из telegram api по TelegramChatExport.id
        return null;
    }

    private List<UserRecord> retrieveMentions(TelegramChatExport telegramChatExport) {
        if (telegramChatExport == null || telegramChatExport.getMessages() == null) {
            return Collections.emptyList();
        }

        return Optional.of(telegramChatExport.getMessages())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(TelegramMessage::getTextEntities)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(this::isMention)
                .map(TelegramTextEntity::getText)
                .map(username -> {
                    UserRecord userRecord = new UserRecord();
                    userRecord.setUsername(username);
                    userRecord.setSource(UserSource.MENTIONED);
                    return userRecord;
                })
                .collect(Collectors.toList());
    }

    private boolean isMention(TelegramTextEntity telegramTextEntity) {
        return "mention".equals(telegramTextEntity.getType());
    }

    private List<UserRecord> retrieveParticipants(TelegramChatExport telegramChatExport) {
        if (telegramChatExport == null || telegramChatExport.getMessages() == null) {
            return Collections.emptyList();
        }

        return telegramChatExport.getMessages().stream()
                .filter(msg -> msg.getFromId() != null && msg.getFrom() != null)
                .collect(Collectors.toMap(
                        TelegramMessage::getFromId,
                        TelegramMessage::getFrom,
                        (existing, replacement) -> existing
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    UserRecord userRecord = new UserRecord();
                    userRecord.setUserId(entry.getKey());
                    userRecord.setFullName(entry.getValue());
                    userRecord.setSource(UserSource.MESSAGE_AUTHOR);
                    return userRecord;
                })
                .collect(Collectors.toList());
    }

    private String getFilename(ExportPayload exportPayload) {
        String title = exportPayload.getChannels().getFirst().getTitle();
        return title + "_" + FILE_PREFIX;
    }
}
