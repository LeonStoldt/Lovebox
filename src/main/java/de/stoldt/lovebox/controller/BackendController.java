package de.stoldt.lovebox.controller;

import com.pengrad.telegrambot.model.Message;
import de.stoldt.lovebox.persistence.dao.BoxDao;
import de.stoldt.lovebox.persistence.dao.PublisherDao;
import de.stoldt.lovebox.telegram.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;

@Controller
public class BackendController {

    private static final String DEFAULT_MESSAGE = "Keine neuen Nachrichten...";

    private TelegramService telegramService;
    private ArrayList<Message> unreadMessages;

    @Autowired
    public BackendController(TelegramService telegramService, PublisherDao publisherDao, BoxDao boxDao) {
        this.telegramService = telegramService;
        this.unreadMessages = telegramService.getUnreadMessages();
    }

    @GetMapping("/")
    public String showMessage(Model model) {
        if (!unreadMessages.isEmpty()) {
            Message message = unreadMessages.remove(0);
            model.addAttribute("message", message.text());
        } else {
            model.addAttribute("message", DEFAULT_MESSAGE);
        }
        return "index";
    }
}
