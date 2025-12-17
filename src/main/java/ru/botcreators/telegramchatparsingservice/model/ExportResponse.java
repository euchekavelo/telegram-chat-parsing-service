package ru.botcreators.telegramchatparsingservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExportResponse {
    private final ExportResponseType type;
    private final String text;
    private final ExportedFile file;

    public static ExportResponse text(String text) {
        return new ExportResponse(ExportResponseType.TEXT, text, null);
    }

    public static ExportResponse excel(ExportedFile file) {
        return new ExportResponse(ExportResponseType.EXCEL, null, file);
    }
}
