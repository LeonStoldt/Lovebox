package de.stoldt.lovebox.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import de.stoldt.lovebox.bo.Box;
import de.stoldt.lovebox.bo.Publisher;
import de.stoldt.lovebox.persistence.dao.BoxDao;
import de.stoldt.lovebox.persistence.dao.PublisherDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TelegramService extends TelegramBot {

    private static final String REGISTER_COMMAND = "/register@Box";
    public static final String ANSWER_REGISTERED_SUCCESSFULLY = "You have registered successfully. The following messages will be directly forwarded to the box";
    public static final String ANSWER_ACCESS_DENIED = "Access denied";
    public static final String ANSWER_INSERT_TOKEN = "Please insert the generated Token.";
    private static Logger LOGGER = LoggerFactory.getLogger(TelegramService.class);

    private PublisherDao publisherDao;
    private BoxDao boxDao;
    private Box box;
    private ArrayList<Message> unreadMessages;

    @Autowired
    public TelegramService(@Value("${telegram.token}") String botToken, BoxDao boxDao, PublisherDao publisherDao) {
        super(botToken);

        this.publisherDao = publisherDao;
        this.boxDao = boxDao;
        this.box = (boxDao.getBox() == null) ? createAndUpdateNewBox() : boxDao.getBox();
        this.unreadMessages = new ArrayList<>();

        setUpdatesListener(this::processUpdates);
    }

    private Box createAndUpdateNewBox() {
        Box newBox = new Box();
        boxDao.save(newBox);
        return newBox;
    }

    private int processUpdates(List<Update> updates) {
        updates.forEach(update -> {
            Message message = (update.editedMessage() != null)
                    ? update.editedMessage()
                    : update.message();
            LOGGER.info("received message: {} with data: {}", message.text(), message);
            processMessage(message);
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void processMessage(Message message) {
        String text = message.text();
        if (text != null) {
            processTextMessage(message, text);
        } // TODO handle pictures etc
    }

    private void processTextMessage(Message message, String text) {
        Chat chat = message.chat();

        if (text.equals(REGISTER_COMMAND)) processRegisterRequest(chat);
        else if (text.equals(box.getToken())) registerPublisherWithValidToken();
        else if (isValidPublisher(chat.id())) unreadMessages.add(message);
        else {
            LOGGER.info("Illegal Access to Bot from Chat: {}", chat);
            sendMessage(chat.id(), ANSWER_ACCESS_DENIED);
        }
    }

    private boolean isValidPublisher(Long publisherId) {
        return box.getPublisherId() != null && box.getPublisherId().equals(publisherId);
    }

    private void registerPublisherWithValidToken() {
        Publisher publisher = publisherDao.getPublisher();
        publisher.setToken(box.getToken());
        publisherDao.save(publisher);

        box.setPublisherId(publisher.getChatId());
        boxDao.save(box);

        sendMessage(publisher.getChatId(), ANSWER_REGISTERED_SUCCESSFULLY);
    }

    private void processRegisterRequest(Chat chat) {
        try {
            Publisher publisher = new Publisher(chat);
            publisherDao.save(publisher);
            sendMessage(publisher.getChatId(), ANSWER_INSERT_TOKEN);

        } catch (IllegalAccessException e) {
            LOGGER.info("Illegal Access to Bot from Chat: {}", chat);
            sendMessage(chat.id(), ANSWER_ACCESS_DENIED);
        }
    }

    public void sendMessage(Long chatId, String message) {
        execute(new SendMessage(chatId, message));
    }

    public ArrayList<Message> getUnreadMessages() {
        return unreadMessages;
    }
}
