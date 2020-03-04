package de.stoldt.lovebox.controller;

import com.pengrad.telegrambot.response.GetFileResponse;
import de.stoldt.lovebox.persistence.dao.BoxDao;
import de.stoldt.lovebox.telegram.TelegramService;
import de.stoldt.lovebox.telegram.message.AbstractMessage;
import de.stoldt.lovebox.telegram.message.DataMessage;
import de.stoldt.lovebox.telegram.message.MessageType;
import de.stoldt.lovebox.telegram.message.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

    @Autowired
    public BackendController(TelegramService telegramService, BoxDao boxDao) {
        this.telegramService = telegramService;
        this.boxDao = boxDao;
    }

    @GetMapping("/")
    public String showMessage(Model model) {
        if (!telegramService.getUnreadAbstractMessages().isEmpty()) {
            AbstractMessage message = telegramService.removeMessage();

            if (message.getMessageType().equals(MessageType.TEXT)) {
                TextMessage textMessage = (TextMessage) message;
                model.addAttribute(THYMELEAF_VARIABLE_MESSAGE, textMessage.getText());
                return MessageType.TEXT.getTemplateName();
            } else {
                model.addAttribute(THYMELEAF_VARIABLE_FILE_URL, getFileUrl((DataMessage) message));
                return message.getMessageType().getTemplateName();
            }
        } else if (boxDao.getBox().getPublisherId() == null) {
            model.addAttribute(THYMELEAF_VARIABLE_UUID, boxDao.getBox().getToken());
            return "register";
        }
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
