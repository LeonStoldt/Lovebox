package de.stoldt.lovebox.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
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

    private static Logger LOGGER = LoggerFactory.getLogger(TelegramService.class);

    private static final String REGISTER_COMMAND = "/register@Box";
    private static final String UNREGISTER_COMMAND = "/unregisterBox";
    public static final String ANSWER_REGISTERED_SUCCESSFULLY = "You have registered successfully. The following messages will be directly forwarded to the box";
    public static final String ANSWER_UNREGISTERED_BOX_SUCCESSFULLY = "Die Registrierung der Box wurde aufgehoben";
    public static final String ANSWER_ACCESS_DENIED = "Access denied";
    public static final String ANSWER_INSERT_TOKEN = "Please insert the generated Token.";
    public static final String ANSWER_UNREGISTERED_BOX_FAILED = "Du bist nicht registriert und kannst dich deshalb auch nicht abmelden.";

    private PublisherDao publisherDao;
    private BoxDao boxDao;
    private final List<Message> unreadMessages;
    private final List<GetFile> unreadPictures;
    private final List<GetFile> unreadVideos;
    private final List<GetFile> unreadAudios;

    private final String apiToken;

    @Autowired

    public TelegramService(@Value("${telegram.token}") String apiToken, BoxDao boxDao, PublisherDao publisherDao) {
        super(apiToken);
        this.apiToken = apiToken;
        this.publisherDao = publisherDao;
        this.boxDao = boxDao;
        this.unreadMessages = new ArrayList<>();
        this.unreadPictures = new ArrayList<>();
        this.unreadVideos = new ArrayList<>();
        this.unreadAudios = new ArrayList<>();

        setUpdatesListener(this::processUpdates);
    }

    private int processUpdates(List<Update> updates) {
        updates.forEach(update -> {
            Message message = (update.editedMessage() != null)
                    ? update.editedMessage()
                    : update.message();
            processMessage(message);
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void processMessage(Message message) {
        if (message.text() != null) {
            processTextMessage(message);
            LOGGER.info("received text message: {} with data: {}", message.text(), message);
        } else if (message.photo() != null) {
            processPhotoMessage(message);
            LOGGER.info("received photo message: {} with data: {}", message.photo(), message);
        } else if (message.video() != null || message.animation() != null) {
            processVideoMessage(message);
            LOGGER.info("received video message: {} with data: {}", message.video(), message);
        } else if (message.voice() != null || message.audio() != null) {
            processAudioMessage(message);
            LOGGER.info("received audio message: {} with data: {}", message.audio(), message);
        }
    }

    private void processTextMessage(Message message) {
        Chat chat = message.chat();
        String text = message.text();

        if (text.equals(REGISTER_COMMAND)) processRegisterRequest(chat);
        else if (text.equals(boxDao.getBox().getToken())) registerPublisherWithValidToken();
        else if (text.equals(UNREGISTER_COMMAND)) unRegisterPublisherWithValidToken(chat.id());
        else if (isValidPublisher(chat.id())) unreadMessages.add(message);
        else {
            LOGGER.info("Illegal Access to Bot from Chat: {}", chat);
            sendMessage(chat.id(), ANSWER_ACCESS_DENIED);
        }
    }

    private void processPhotoMessage(Message message) {
        List<PhotoSize> photos = Arrays.asList(message.photo());
        PhotoSize photo = photos.get(photos.size() - 1);
        unreadPictures.add(new GetFile(photo.fileId()));
    }

    private void processVideoMessage(Message message) {
        String fileId = message.video() != null
                ? message.video().fileId()
                : message.animation().fileId();
        unreadVideos.add(new GetFile(fileId));
    }

    private void processAudioMessage(Message message) {
        String fileId = message.voice() != null
                ? message.voice().fileId()
                : message.audio().fileId();
        unreadAudios.add(new GetFile(fileId));
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

    public List<GetFile> getUnreadPictures() {
        return unreadPictures;
    }

    public List<GetFile> getUnreadVideos() {
        return unreadVideos;
    }

    public GetFileResponse getFile(GetFile fileRequest) {
        return execute(fileRequest);
    }

    public String getApiToken() {
        return apiToken;
    }

    public List<GetFile> getUnreadAudios() {
        return unreadAudios;
    }
}
