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
import de.stoldt.lovebox.gpio.BashCallback;
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
import java.util.Optional;

@Service
public class TelegramService extends TelegramBot implements MessageCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramService.class);
    private static final int MAX_DISPLAYED_SIGNS = 750;

    // answer
    private static final String ANSWER_REGISTERED_SUCCESSFULLY = "Du hast dich erfolgreich registriert. Die kommenden Nachrichten werden ab sofort direkt an die Box weitergeleitet";
    private static final String ANSWER_UNREGISTERED_BOX_SUCCESSFULLY = "Die Registrierung der Box wurde aufgehoben";
    private static final String ANSWER_ACCESS_DENIED = "Zugriff verweigert";
    private static final String ANSWER_INSERT_TOKEN = "Bitte gebe das generierte Token ein:";
    private static final String ANSWER_UNREGISTERED_BOX_FAILED = "Du bist nicht registriert und kannst dich deshalb auch nicht abmelden.";
    private static final String ANSWER_UNSUPPORTED_MEDIA_TYPE = "Das Dateiformat wird leider nicht unterstützt";
    private static final String ANSWER_BOX_OPENED = "Die Box wurde geöffnet.";

    private final String apiToken;
    private final BoxDao boxDao;
    private final PublisherDao publisherDao;
    private final GpioCallback gpioCallback;
    private final BashCallback bashCallback;
    private final List<AbstractMessage> unreadAbstractMessages;
    private boolean requestedRegister;

    @Autowired
    public TelegramService(@Value("${telegram.token}") String apiToken, BoxDao boxDao,
                           PublisherDao publisherDao, GpioCallback gpioCallback, BashCallback bashCallback) {
        super(apiToken);
        this.apiToken = apiToken;
        this.publisherDao = publisherDao;
        this.boxDao = boxDao;
        this.gpioCallback = gpioCallback;
        this.bashCallback = bashCallback;
        this.unreadAbstractMessages = new ArrayList<>();
        this.requestedRegister = false;

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

        Optional<Commands> optionalCommand = Commands.of(text);
        if (optionalCommand.isPresent()) executeCommand(chat, optionalCommand.get());
        else if (isValidToken(text)) registerPublisherWithValidToken(text, chat.id());
        else if (isValidPublisher(chat.id())) addTextToUnreadMessages(text);
        else {
            LOGGER.info("Illegal Access to Bot from Chat: {}", chat);
            sendMessage(chat.id(), ANSWER_ACCESS_DENIED);
        }
    }

    private boolean isValidToken(String text) {
        return boxDao.getAll().anyMatch(box -> box.getToken().equals(text));
    }

    private void executeCommand(Chat chat, Commands command) {
        switch (command) {
            case REGISTER:
                processRegisterRequest(chat);
                break;
            case UNREGISTER:
                unRegisterPublisherWithValidToken(chat.id());
                break;
            case SHUTDOWN:
                bashCallback.shutdownSystem();
                break;
            case RESTART:
                bashCallback.rebootSystem();
                break;
            case UPDATE:
                bashCallback.upgradePackages();
                break;
            default:
                LOGGER.info("Could not find given command: {}", command);
        }
    }

    private void addTextToUnreadMessages(String text) {
        if (text.length() > MAX_DISPLAYED_SIGNS) {
            addMessage(new TextMessage(Arrays
                    .stream(text.split("(?<= )"))
                    .reduce((firstText, secondText) -> {
                        if (firstText.length() + secondText.length() <= 500) {
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
            boxDao.save(new Box());
            requestedRegister = true;
            sendMessage(publisher.getChatId(), ANSWER_INSERT_TOKEN);
        } catch (IllegalAccessException e) {
            LOGGER.info("Illegal Access to Bot from Chat: {}", chat);
            sendMessage(chat.id(), ANSWER_ACCESS_DENIED);
        }
    }

    private void registerPublisherWithValidToken(String token, Long chatId) {
        String answer = ANSWER_ACCESS_DENIED;
        Optional<Publisher> optionalPublisher = publisherDao.getPublisherFor(chatId);
        if (optionalPublisher.isPresent()) {
            Publisher publisher = optionalPublisher.get();
            boxDao.getBoxBy(token).map(box -> box.withPublisherId(chatId)).ifPresent(boxDao::save);
            publisher.withToken(token);
            publisherDao.save(publisher);
            answer = ANSWER_REGISTERED_SUCCESSFULLY;
        }
        sendMessage(chatId, answer);
    }

    private void unRegisterPublisherWithValidToken(Long chatId) {
        if (boxDao.getBoxBy(chatId).isPresent()) {
            boxDao.remove(chatId);
            publisherDao.getPublisherFor(chatId).map(publisher -> publisher.withToken(null)).ifPresent(publisherDao::save);
            LOGGER.info("unregistered Box and deleted Publisher with id {}", chatId);
            sendMessage(chatId, ANSWER_UNREGISTERED_BOX_SUCCESSFULLY);
        } else {
            LOGGER.info("user with id {} tried to unregister box", chatId);
            sendMessage(chatId, ANSWER_UNREGISTERED_BOX_FAILED);
        }
    }

    private boolean isValidPublisher(Long publisherId) {
        Optional<Publisher> optionalPublisher = publisherDao.getPublisherFor(publisherId);
        return boxDao.getBoxBy(publisherId).isPresent()
                && optionalPublisher.isPresent()
                && optionalPublisher.get().getToken() != null;
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
        gpioCallback.notifyLeds();
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

    public boolean isRegisterRequested() {
        boolean isRegisterRequested = this.requestedRegister;
        this.requestedRegister = false;
        return boxDao.getAll().count() == 0 || isRegisterRequested;
    }

    public String getToken() {
        return boxDao.getNextAvailableToken();
    }

    public List<AbstractMessage> getUnreadAbstractMessages() {
        return unreadAbstractMessages;
    }

    public String getApiToken() {
        return apiToken;
    }

    @Override
    public void sendConfirmation() {
        publisherDao.getAll().forEach(publisher -> sendMessage(publisher.getChatId(), ANSWER_BOX_OPENED));
    }
}
