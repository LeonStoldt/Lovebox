package de.stoldt.lovebox.controller;

import com.pengrad.telegrambot.response.GetFileResponse;
import de.stoldt.lovebox.gpio.BashCallback;
import de.stoldt.lovebox.persistence.dao.BoxDao;
import de.stoldt.lovebox.telegram.TelegramService;
import de.stoldt.lovebox.telegram.message.AbstractMessage;
import de.stoldt.lovebox.telegram.message.DataMessage;
import de.stoldt.lovebox.telegram.message.MessageType;
import de.stoldt.lovebox.telegram.message.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

@Controller
public class BackendController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackendController.class);
    private static final String DEFAULT_TEXT = "Keine neuen Nachrichten...";
    private static final String GET_FILE_URL = "https://api.telegram.org/file/bot%s/%s";
    private static final String THYMELEAF_VARIABLE_MESSAGE = "message";
    private static final String THYMELEAF_VARIABLE_FILE_URL = "fileUrl";
    private static final String THYMELEAF_VARIABLE_UUID = "uuid";

    private final TelegramService telegramService;
    private final BoxDao boxDao;
    private final boolean isActiveDevProfile;
    private final BashCallback bashCallback;

    @Autowired
    public BackendController(TelegramService telegramService, BoxDao boxDao, Environment environment, BashCallback bashCallback) {
        this.telegramService = telegramService;
        this.boxDao = boxDao;
        this.isActiveDevProfile = Arrays.asList(environment.getActiveProfiles()).contains("dev");
        this.bashCallback = bashCallback;
    }

    @GetMapping("/")
    public String showMessage(Model model) {
        if (!telegramService.getUnreadAbstractMessages().isEmpty()) { // process next message
            AbstractMessage message = telegramService.removeMessage();

            if (message.getMessageType().equals(MessageType.TEXT)) {
                TextMessage textMessage = (TextMessage) message;
                model.addAttribute(THYMELEAF_VARIABLE_MESSAGE, textMessage.getText());
                return MessageType.TEXT.getTemplateName();
            } else if (!isActiveDevProfile && message.getMessageType().equals(MessageType.VIDEO)) { // handle video differently with inactive dev profile
                bashCallback.startVideoPlayer(getFileUrl((DataMessage) message));
            } else {
                model.addAttribute(THYMELEAF_VARIABLE_FILE_URL, getFileUrl((DataMessage) message));
                return message.getMessageType().getTemplateName();
            }

        } else if (boxDao.getBox().getPublisherId() == null) { // register at box if no one is registered yet
            model.addAttribute(THYMELEAF_VARIABLE_UUID, boxDao.getBox().getToken());
            return "register";
        }
        // no messages available
        model.addAttribute(THYMELEAF_VARIABLE_MESSAGE, DEFAULT_TEXT);
        return MessageType.TEXT.getTemplateName();
    }

    private String getFileUrl(DataMessage message) {
        String fileUrl;
        GetFileResponse fileResponse = telegramService.getFile(message.getFile());

        if (fileResponse.isOk()) {
            fileUrl = String.format(GET_FILE_URL, telegramService.getApiToken(), fileResponse.file().filePath());
        } else {
            LOGGER.warn("Received status code: {} from getFile {}", fileResponse.errorCode(), message.getFile());
            fileUrl = "/fallback picture";
        }
        return fileUrl;
    }
}
