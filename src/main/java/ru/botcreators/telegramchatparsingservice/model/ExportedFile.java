package ru.botcreators.telegramchatparsingservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExportedFile {
    private final String filename;
    private final String contentType;
    private final byte[] bytes;
}
