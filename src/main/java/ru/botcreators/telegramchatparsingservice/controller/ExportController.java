package ru.botcreators.telegramchatparsingservice.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.botcreators.telegramchatparsingservice.model.*;
import ru.botcreators.telegramchatparsingservice.service.ExcelExportService;
import ru.botcreators.telegramchatparsingservice.service.ExportOrchestratorService;
import ru.botcreators.telegramchatparsingservice.service.TextExportService;
import ru.botcreators.telegramchatparsingservice.service.UserAggregationService;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/export")
public class ExportController {

    private final ExportOrchestratorService orchestratorService;
    private final UserAggregationService userAggregationService;
    private final TextExportService textExportService;
    private final ExcelExportService excelExportService;

    public ExportController(ExportOrchestratorService orchestratorService,
                            UserAggregationService userAggregationService,
                            TextExportService textExportService,
                            ExcelExportService excelExportService) {
        this.orchestratorService = orchestratorService;
        this.userAggregationService = userAggregationService;
        this.textExportService = textExportService;
        this.excelExportService = excelExportService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> exportAuto(
            @RequestBody ExportPayload payload,
            @RequestParam(name = "prefix", defaultValue = "chat_export") String filenamePrefix
    ) {
        Objects.requireNonNull(payload, "payload must not be null");

        ExportResponse response = orchestratorService.export(payload, filenamePrefix);

        if (response.getType() == ExportResponseType.TEXT) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(response.getText());
        }

        return buildExcelResponse(response.getFile());
    }

    @PostMapping(value = "/text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> exportText(@RequestBody ExportPayload payload) {
        Objects.requireNonNull(payload, "payload must not be null");

        List<UserRecord> users = userAggregationService.collectUniqueUsers(payload);
        String text = textExportService.buildText(users);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(text);
    }

    @PostMapping(value = "/excel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> exportExcel(
            @RequestBody ExportPayload payload,
            @RequestParam(name = "prefix", defaultValue = "chat_export") String filenamePrefix
    ) {
        Objects.requireNonNull(payload, "payload must not be null");

        ExportedFile file = excelExportService.buildXlsx(payload, filenamePrefix);
        return buildExcelResponse(file);
    }

    private ResponseEntity<byte[]> buildExcelResponse(ExportedFile file) {
        if (file == null) {
            return ResponseEntity.internalServerError().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));

        headers.add(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\""
        );

        headers.setContentLength(file.getBytes().length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(file.getBytes());
    }
}
