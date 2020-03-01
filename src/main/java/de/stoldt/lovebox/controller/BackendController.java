package de.stoldt.lovebox.controller;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.response.GetFileResponse;
import de.stoldt.lovebox.persistence.dao.BoxDao;
import de.stoldt.lovebox.telegram.TelegramService;
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

    private final TelegramService telegramService;

    private final List<Message> unreadMessages;
    private final List<GetFile> unreadPictures;
    private final List<GetFile> unreadVideos;
    private final List<GetFile> unreadAudios;
    private final BoxDao boxDao;

    @Autowired
    public BackendController(TelegramService telegramService, BoxDao boxDao) {
        this.telegramService = telegramService;
        this.unreadMessages = telegramService.getUnreadMessages();
        this.unreadPictures = telegramService.getUnreadPictures();
        this.unreadVideos = telegramService.getUnreadVideos();
        this.unreadAudios = telegramService.getUnreadAudios();
        this.boxDao = boxDao;
    }

    @GetMapping("/")
    public String showMessage(Model model) {
        if (!unreadMessages.isEmpty()) {
            model.addAttribute("message", unreadMessages.remove(0).text());

        } else {

            if (boxDao.getBox().getPublisherId() == null) {
                model.addAttribute("uuid", boxDao.getBox().getToken());
                return "register";
            }

            if (!unreadPictures.isEmpty()) {
                GetFile fileRequest = unreadPictures.remove(0);
                GetFileResponse fileResponse = telegramService.getFile(fileRequest);
                if (fileResponse.isOk()) {
                    String fileUrl = String.format(GET_FILE_URL, telegramService.getApiToken(), fileResponse.file().filePath());
                    model.addAttribute("fileUrl", fileUrl);
                    return "picture";
                } else {
                    LOGGER.warn("Received status code: {} from getFile {}", fileResponse.errorCode(), fileRequest);
                }
            }

            if (!unreadVideos.isEmpty()) {
                GetFile fileRequest = unreadVideos.remove(0);
                GetFileResponse fileResponse = telegramService.getFile(fileRequest);
                if (fileResponse.isOk()) {
                    String filePath = fileResponse.file().filePath();
                    String fileUrl = String.format(GET_FILE_URL, telegramService.getApiToken(), filePath);
                    model.addAttribute("fileUrl", fileUrl);
                    return "video";
                } else {
                    LOGGER.warn("Received status code: {} from getFile {}", fileResponse.errorCode(), fileRequest);
                }
            }

            if (!unreadAudios.isEmpty()) {
                GetFile fileRequest = unreadAudios.remove(0);
                GetFileResponse fileResponse = telegramService.getFile(fileRequest);
                if (fileResponse.isOk()) {
                    String filePath = fileResponse.file().filePath();
                    String fileUrl = String.format(GET_FILE_URL, telegramService.getApiToken(), filePath);
                    model.addAttribute("fileUrl", fileUrl);
                    return "audio";
                } else {
                    LOGGER.warn("Received status code: {} from getFile {}", fileResponse.errorCode(), fileRequest);
                }
            }

            model.addAttribute("message", DEFAULT_MESSAGE);
        }
        return "text";
    }
}
