package ru.botcreators.telegramchatparsingservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.botcreators.telegramchatparsingservice.model.ExportPayload;
import ru.botcreators.telegramchatparsingservice.model.ExportResponse;
import ru.botcreators.telegramchatparsingservice.model.ExportedFile;
import ru.botcreators.telegramchatparsingservice.model.UserRecord;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ExportOrchestratorService {

    private final UserAggregationService userAggregationService;
    private final TextExportService textExportService;
    private final ExcelExportService excelExportService;

    public ExportResponse export(ExportPayload payload, String filenamePrefix) {
        Objects.requireNonNull(payload, "payload");

        List<UserRecord> uniqueUsers = userAggregationService.collectUniqueUsers(payload);
        int total = uniqueUsers.size();

        if (total < 50) {
            String text = textExportService.buildText(uniqueUsers);
            return ExportResponse.text(text);
        }

        ExportedFile file = excelExportService.buildXlsx(payload, filenamePrefix);
        return ExportResponse.excel(file);
    }
}
