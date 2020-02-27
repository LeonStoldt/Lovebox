package de.stoldt.lovebox.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
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
import java.util.Arrays;
import java.util.List;

@Service
public class TelegramService extends TelegramBot {

    private static final String REGISTER_COMMAND = "/register@Box";
    private static final String UNREGISTER_COMMAND = "/unregisterBox";
    public static final String ANSWER_REGISTERED_SUCCESSFULLY = "You have registered successfully. The following messages will be directly forwarded to the box";
    public static final String ANSWER_UNREGISTERED_BOX_SUCCESSFULLY = "Die Registrierung der Box wurde aufgehoben";
    public static final String ANSWER_ACCESS_DENIED = "Access denied";
    public static final String ANSWER_INSERT_TOKEN = "Please insert the generated Token.";
    public static final String ANSWER_UNREGISTERED_BOX_FAILED = "Du bist nicht registriert und kannst dich deshalb auch nicht abmelden.";
    private static Logger LOGGER = LoggerFactory.getLogger(TelegramService.class);

    private PublisherDao publisherDao;
    private BoxDao boxDao;
    private ArrayList<Message> unreadMessages;
    private ArrayList<File> unreadPictures;

    @Autowired
    public TelegramService(@Value("${telegram.token}") String botToken, BoxDao boxDao, PublisherDao publisherDao) {
        super(botToken);

        this.publisherDao = publisherDao;
        this.boxDao = boxDao;
        this.unreadMessages = new ArrayList<>();
        this.unreadPictures = new ArrayList<>();

        setUpdatesListener(this::processUpdates);
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
        } else {
            List<PhotoSize> photos = Arrays.asList(message.photo());
            PhotoSize photo = photos.get(photos.size() - 1);
            String fileId = photo.fileId();
            GetFile getFile = new GetFile(fileId);
            GetFileResponse response = execute(getFile);
            File picture = response.file();
            unreadPictures.add(picture);
        }
    }

    private void processTextMessage(Message message, String text) {
        Chat chat = message.chat();

        if (text.equals(REGISTER_COMMAND)) processRegisterRequest(chat);
        else if (text.equals(boxDao.getBox().getToken())) registerPublisherWithValidToken();
        else if (text.equals(UNREGISTER_COMMAND)) unRegisterPublisherWithValidToken(chat.id());
        else if (isValidPublisher(chat.id())) unreadMessages.add(message);
        else {
            LOGGER.info("Illegal Access to Bot from Chat: {}", chat);
            sendMessage(chat.id(), ANSWER_ACCESS_DENIED);
        }
    }

    private boolean isValidPublisher(Long publisherId) {
        return boxDao.getBox().getPublisherId() != null && boxDao.getBox().getPublisherId().equals(publisherId);
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

    private void registerPublisherWithValidToken() {
        Publisher publisher = publisherDao.getPublisher();
        Box box = boxDao.getBox();
        publisher.setToken(box.getToken());
        publisherDao.save(publisher);

        box.setPublisherId(publisher.getChatId());
        boxDao.save(box);

        sendMessage(publisher.getChatId(), ANSWER_REGISTERED_SUCCESSFULLY);
    }

    private void unRegisterPublisherWithValidToken(Long chatId) {
        if (chatId.equals(boxDao.getBox().getPublisherId())) {
            boxDao.initializeAndSaveNewBox();
            LOGGER.info("unregistered Box and deleted Publisher with id {}", chatId);
            sendMessage(chatId, ANSWER_UNREGISTERED_BOX_SUCCESSFULLY);
        } else {
            LOGGER.info("user with id {} tried to unregister box", chatId);
            sendMessage(chatId, ANSWER_UNREGISTERED_BOX_FAILED);
        }
    }

    public void sendMessage(Long chatId, String message) {
        execute(new SendMessage(chatId, message));
    }

    public List<Message> getUnreadMessages() {
        return unreadMessages;
    }

    public List<File> getUnreadPictures() {
        return unreadPictures;
    }
}
