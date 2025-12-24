package ru.botcreators.telegramchatparsingservice.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.botcreators.telegramchatparsingservice.service.CommandService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class CommandHandler {

    private final Collection<CommandService> commandServices;

    public void handle(Update update) {
        for (CommandService commandService : commandServices) {
            if(commandService.canHandleCommand(update)) {
                commandService.handleCommand(update);
                return;
            }
        }
    }
}
