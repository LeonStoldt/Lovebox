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
import de.stoldt.lovebox.telegram.message.AbstractMessage;
import de.stoldt.lovebox.telegram.message.DataMessage;
import de.stoldt.lovebox.telegram.message.MessageType;
import de.stoldt.lovebox.telegram.message.TextMessage;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramService.class);
    private static final int MAX_DISPLAYED_SIGNS = 750;

    // commands
    private static final String REGISTER_COMMAND = "/register@Box";
    private static final String UNREGISTER_COMMAND = "/unregisterBox";

    // answer
    private static final String ANSWER_REGISTERED_SUCCESSFULLY = "You have registered successfully. The following messages will be directly forwarded to the box";
    private static final String ANSWER_UNREGISTERED_BOX_SUCCESSFULLY = "Die Registrierung der Box wurde aufgehoben";
    private static final String ANSWER_ACCESS_DENIED = "Access denied";
    private static final String ANSWER_INSERT_TOKEN = "Please insert the generated Token.";
    private static final String ANSWER_UNREGISTERED_BOX_FAILED = "Du bist nicht registriert und kannst dich deshalb auch nicht abmelden.";

    private PublisherDao publisherDao;
    private BoxDao boxDao;
    private final List<AbstractMessage> unreadAbstractMessages;
    private final String apiToken;

    @Autowired

    public TelegramService(@Value("${telegram.token}") String apiToken, BoxDao boxDao, PublisherDao publisherDao) {
        super(apiToken);
        this.apiToken = apiToken;
        this.publisherDao = publisherDao;
        this.boxDao = boxDao;
        this.unreadAbstractMessages = new ArrayList<>();

        setUpdatesListener(this::processUpdates);
    }

    private int processUpdates(List<Update> updates) {
        updates.forEach(update -> {
            Message message = update.editedMessage() != null ? update.editedMessage() : update.message();
            processMessage(message);
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void processMessage(Message message) {
        if (message.text() != null) processTextMessage(message);
        else if (message.photo() != null) processPhotoMessage(message);
        else if (message.video() != null || message.animation() != null) processVideoMessage(message);
        else if (message.voice() != null || message.audio() != null) processAudioMessage(message);
    }

    // region process MessageTypes
    private void processTextMessage(Message message) {
        Chat chat = message.chat();
        String text = message.text();
        LOGGER.info("received text message: {} with data: {}", text, chat);

        if (text.equals(REGISTER_COMMAND)) processRegisterRequest(chat);
        else if (text.equals(boxDao.getBox().getToken())) registerPublisherWithValidToken();
        else if (text.equals(UNREGISTER_COMMAND)) unRegisterPublisherWithValidToken(chat.id());
        else if (isValidPublisher(chat.id())) addTextToUnreadMessages(text);
        else {
            LOGGER.info("Illegal Access to Bot from Chat: {}", chat);
            sendMessage(chat.id(), ANSWER_ACCESS_DENIED);
        }
    }

    private void addTextToUnreadMessages(String text) {
        if (text.length() > MAX_DISPLAYED_SIGNS) {
            Arrays.stream(text.split(String.format("(?<=\\G.{%d,}\\s)", MAX_DISPLAYED_SIGNS)))
                    .map(TextMessage::new)
                    .forEach(unreadAbstractMessages::add);
        } else {
            unreadAbstractMessages.add(new TextMessage(text));
        }
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

    private boolean isValidPublisher(Long publisherId) {
        return boxDao.getBox().getPublisherId() != null && boxDao.getBox().getPublisherId().equals(publisherId);
    }

    private void processPhotoMessage(Message message) {
        List<PhotoSize> photos = Arrays.asList(message.photo());
        if (!photos.isEmpty()) {
            PhotoSize photo = photos.get(photos.size() - 1);
            unreadAbstractMessages.add(new DataMessage(MessageType.PICTURE, new GetFile(photo.fileId())));
            LOGGER.info("received photo message: {} with data: {}", message.photo(), message.chat());
        }
    }

    private void processVideoMessage(Message message) {
        String fileId;
        if (message.video() != null) {
            fileId = message.video().fileId();
            LOGGER.info("received video message: {} with data: {}", message.video(), message);
        } else {
            fileId = message.animation().fileId();
            LOGGER.info("received animation message: {} with data: {}", message.animation(), message);
        }
        unreadAbstractMessages.add(new DataMessage(MessageType.VIDEO, new GetFile(fileId)));
    }

    private void processAudioMessage(Message message) {
        String fileId;

        if (message.voice() != null) {
            fileId = message.voice().fileId();
            LOGGER.info("received audio message: {} with data: {}", message.voice(), message);
        } else {
            fileId = message.audio().fileId();
            LOGGER.info("received audio message: {} with data: {}", message.audio(), message);
        }
        unreadAbstractMessages.add(new DataMessage(MessageType.AUDIO, new GetFile(fileId)));
    }
    // endregion

    public void sendMessage(Long chatId, String message) {
        execute(new SendMessage(chatId, message));
    }

    public GetFileResponse getFile(GetFile fileRequest) {
        return execute(fileRequest);
    }

    public List<AbstractMessage> getUnreadAbstractMessages() {
        return unreadAbstractMessages;
    }

    public String getApiToken() {
        return apiToken;
    }
}
