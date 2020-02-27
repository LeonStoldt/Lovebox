package de.stoldt.lovebox.controller;

import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Message;
import de.stoldt.lovebox.persistence.dao.BoxDao;
import de.stoldt.lovebox.telegram.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class BackendController {

    private static final String DEFAULT_MESSAGE = "Keine neuen Nachrichten...";

    private final List<Message> unreadMessages;
    private final List<File> unreadPictures;
    private final BoxDao boxDao;

    @Autowired
    public BackendController(TelegramService telegramService, BoxDao boxDao) {
        this.unreadMessages = telegramService.getUnreadMessages();
        this.unreadPictures = telegramService.getUnreadPictures();
        this.boxDao = boxDao;
    }

    @GetMapping("/")
    public String showMessage(Model model) {
        if (!unreadMessages.isEmpty()) {
            Message message = unreadMessages.remove(0);
            model.addAttribute("message", message.text());

        } else {

            if (boxDao.getBox().getPublisherId() == null) {
                model.addAttribute("uuid", boxDao.getBox().getToken());
                return "register";
            }

            if (!unreadPictures.isEmpty()) {
                File photo = unreadPictures.remove(0);
                model.addAttribute("picture", photo);
                return "picture";
            }

            model.addAttribute("message", DEFAULT_MESSAGE);
        }
        return "text";
    }
}
