package carsharingapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramNotificationService extends TelegramLongPollingBot {

    private final UserServiceImpl userService;

    @Value("${telegram.bot.token}")
    private String botToken;

    public void sendNotification(Long chatId, String message) {
        sendMessage(chatId, message);
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.info("Failed to send Telegram message", e);
        }
    }

    @Override
    public String getBotUsername() {
        return "CarSharingApp_bossBot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                userService.updateTelegramChatId(chatId);
                sendNotification(chatId, "Your chat ID has been saved.");
            }
        }
    }
}
