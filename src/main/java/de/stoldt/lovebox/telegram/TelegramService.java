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
import de.stoldt.lovebox.gpio.GpioCallback;
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
    private static final String ANSWER_UNSUPPORTED_MEDIA_TYPE = "Unsupported Media Type";

    private final String apiToken;
    private BoxDao boxDao;
    private PublisherDao publisherDao;
    private final GpioCallback gpioCallback;
    private final List<AbstractMessage> unreadAbstractMessages;

    @Autowired
    public TelegramService(@Value("${telegram.token}") String apiToken, BoxDao boxDao, PublisherDao publisherDao, GpioCallback gpioCallback) {
        super(apiToken);
        this.apiToken = apiToken;
        this.publisherDao = publisherDao;
        this.boxDao = boxDao;
        this.gpioCallback = gpioCallback;
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
        else sendMessage(message.chat().id(), ANSWER_UNSUPPORTED_MEDIA_TYPE);
    }

    // region process MessageTypes
    private void processTextMessage(Message message) {
        Chat chat = message.chat();
        String text = message.text();
        LOGGER.info("received text message: {} with data: {}", text, chat);

        if (text.equals(REGISTER_COMMAND)) processRegisterRequest(chat);
        else if (text.equals(boxDao.getBox().getToken())) registerPublisherWithValidToken(chat.id());
        else if (text.equals(UNREGISTER_COMMAND)) unRegisterPublisherWithValidToken(chat.id());
        else if (isValidPublisher(chat.id())) addTextToUnreadMessages(text);
        else {
            LOGGER.info("Illegal Access to Bot from Chat: {}", chat);
            sendMessage(chat.id(), ANSWER_ACCESS_DENIED);
        }
    }

    private void addTextToUnreadMessages(String text) {
        if (text.length() > MAX_DISPLAYED_SIGNS) {
            addMessage(new TextMessage(Arrays
                    .stream(text.split("(?<= )"))
                    .reduce((firstText, secondText) -> {
                        if (firstText.length() + secondText.length() <= 750) {
                            return firstText + secondText;
                        }
                        addMessage(new TextMessage(firstText));
                        return secondText;
                    }).orElse(null)));
        } else {
            addMessage(new TextMessage(text));
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

    private void registerPublisherWithValidToken(Long chatId) {
        String answer;
        Publisher publisher = publisherDao.getPublisher();
        if (publisher != null) {
            Box box = boxDao.getBox();
            publisher.setToken(box.getToken());
            publisherDao.save(publisher);

            box.setPublisherId(publisher.getChatId());
            boxDao.save(box);
            answer = ANSWER_REGISTERED_SUCCESSFULLY;
        } else {
            answer = ANSWER_ACCESS_DENIED;
        }
        sendMessage(chatId, answer);
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
            addMessage(new DataMessage(MessageType.PICTURE, new GetFile(photo.fileId())));
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
        addMessage(new DataMessage(MessageType.VIDEO, new GetFile(fileId)));
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
        addMessage(new DataMessage(MessageType.AUDIO, new GetFile(fileId)));
    }
    // endregion

    private void addMessage(AbstractMessage message) {
        unreadAbstractMessages.add(message);
        gpioCallback.setHasUnreadMessages(true);
        LOGGER.info("Added message to unread messages.");
        if (!gpioCallback.ledsAreActive() && gpioCallback.isBoxClosed()) {
            gpioCallback.startLeds();
        }
    }

    public AbstractMessage removeMessage() {
        AbstractMessage removedMessage = unreadAbstractMessages.remove(0);
        LOGGER.info("Removed message of unread messages.");
        if (unreadAbstractMessages.isEmpty()) {
            gpioCallback.setHasUnreadMessages(false);
        }
        return removedMessage;
    }

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
