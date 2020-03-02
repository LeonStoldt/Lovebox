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

import java.util.List;

@Controller
public class BackendController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackendController.class);
    private static final String DEFAULT_MESSAGE = "Keine neuen Nachrichten...";
    private static final String GET_FILE_URL = "https://api.telegram.org/file/bot%s/%s";
    private static final String THYMELEAF_VARIABLE_MESSAGE = "message";
    private static final String THYMELEAF_VARIABLE_FILE_URL = "fileUrl";
    private static final String THYMELEAF_VARIABLE_UUID = "uuid";

    private final TelegramService telegramService;

    private final List<AbstractMessage> unreadAbstractMessages;
    private final BoxDao boxDao;

    @Autowired
    public BackendController(TelegramService telegramService, BoxDao boxDao) {
        this.telegramService = telegramService;
        this.unreadAbstractMessages = telegramService.getUnreadAbstractMessages();
        this.boxDao = boxDao;
    }

    @GetMapping("/")
    public String showMessage(Model model) {
        if (!unreadAbstractMessages.isEmpty()) {
            AbstractMessage message = unreadAbstractMessages.remove(0);

            switch (message.getMessageType()) {
                case TEXT:
                    TextMessage textMessage = (TextMessage) message;
                    model.addAttribute(THYMELEAF_VARIABLE_MESSAGE, textMessage.getText());
                    return MessageType.TEXT.getTemplateName();
                case PICTURE:
                    model.addAttribute(THYMELEAF_VARIABLE_FILE_URL, getFileUrl((DataMessage) message));
                    return MessageType.PICTURE.getTemplateName();
                case AUDIO:
                    model.addAttribute(THYMELEAF_VARIABLE_FILE_URL, getFileUrl((DataMessage) message));
                    return MessageType.AUDIO.getTemplateName();
                case VIDEO:
                    model.addAttribute(THYMELEAF_VARIABLE_FILE_URL, getFileUrl((DataMessage) message));
                    return MessageType.VIDEO.getTemplateName();
                default:
                    break;
            }
        } else if (boxDao.getBox().getPublisherId() == null) {
            model.addAttribute(THYMELEAF_VARIABLE_UUID, boxDao.getBox().getToken());
            return "register";
        }
        model.addAttribute(THYMELEAF_VARIABLE_MESSAGE, DEFAULT_MESSAGE);
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
