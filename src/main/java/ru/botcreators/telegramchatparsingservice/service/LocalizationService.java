package ru.botcreators.telegramchatparsingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LocalizationService {

    private final MessageSource messageSource;
    @Setter
    private Locale locale;

    public String getLocalizedMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }
}
