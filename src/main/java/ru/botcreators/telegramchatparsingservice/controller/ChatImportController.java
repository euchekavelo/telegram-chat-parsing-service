package ru.botcreators.telegramchatparsingservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.botcreators.telegramchatparsingservice.model.ExportResponse;
import ru.botcreators.telegramchatparsingservice.model.ExportResponseType;
import ru.botcreators.telegramchatparsingservice.model.ExportedFile;
import ru.botcreators.telegramchatparsingservice.service.ImportService;

import java.io.IOException;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatImportController {

    private final ImportService importService;

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ResponseEntity<?> importChat(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Файл пустой");
        }
        if (!file.getOriginalFilename().endsWith(".json")) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Ожидается JSON-файл");
        }

        ExportResponse response = importService.importChatHistory(file);

        if (response.getType() == ExportResponseType.TEXT) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(response.getText());
        }

        return buildExcelResponse(response.getFile());
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
