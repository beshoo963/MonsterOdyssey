package de.uniks.stpmon.team_m.controller.subController;

import de.uniks.stpmon.team_m.App;
import de.uniks.stpmon.team_m.dto.Group;
import de.uniks.stpmon.team_m.dto.Message;
import de.uniks.stpmon.team_m.dto.User;
import de.uniks.stpmon.team_m.service.GroupService;
import de.uniks.stpmon.team_m.service.MessageService;
import de.uniks.stpmon.team_m.utils.GroupStorage;
import de.uniks.stpmon.team_m.utils.UserStorage;
import de.uniks.stpmon.team_m.ws.EventListener;
import io.reactivex.rxjava3.core.Observable;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;

import javax.inject.Provider;
import java.util.List;

import static io.reactivex.rxjava3.core.Observable.just;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(MockitoExtension.class)
class MessagesBoxControllerTest extends ApplicationTest {

    @Mock
    MessageService messageService;
    @Mock
    GroupService groupService;
    @Mock
    Provider<GroupStorage> groupStorageProvider;
    @Spy
    GroupStorage groupStorage;
    @Mock
    Provider<UserStorage> userStorageProvider;
    @Spy
    UserStorage userStorage;
    @Mock
    Provider<EventListener> eventListenerProvider;
    @InjectMocks
    MessagesBoxController messagesBoxController;
    @Spy
    App app = new App(null);

    @Override
    public void start(Stage stage) {
        EventListener eventListener = mock(EventListener.class);
        when(groupStorageProvider.get()).thenReturn(groupStorage);
        when(userStorageProvider.get()).thenReturn(userStorage);
        when(groupService.getGroups(any())).thenReturn(just(List.of(
                new Group("6477bcb4634144ca9efec424", "TestGroup", List.of("6477bc8f27adf9b5b978401e"))
        )));
        when(messageService.getGroupMessages(any())).thenReturn(just(List.of()));
        when(eventListenerProvider.get()).thenReturn(eventListener);
        when(eventListener.listen(any(), any())).thenReturn(Observable.empty());
        userStorage.set_id("6477bc8f27adf9b5b978401e");
        groupStorage.set_id("6477bcb4634144ca9efec424");
        messagesBoxController.setGroup(null);
        messagesBoxController.setUser(new User("6477bc8f27adf9b5b978401e", "Rick Sanchez", "online", null, List.of()));
        messagesBoxController.setAllUsers(List.of(
                new User("6477bc8f27adf9b5b978401e", "Rick Sanchez", "online", null, List.of())
        ));
        app.start(stage);
        app.show(messagesBoxController);
        stage.requestFocus();
    }

    @Test
    void editMessage() {
        Message updatedMessage = new Message("2023-05-30T12:02:57.510Z", "2023-05-30T12:01:57.510Z", "6475e595ac3946b6a812d863",
                "6477bc8f27adf9b5b978401e", "Test1Test2");
        when(messageService.updateMessage(any(), any(), any(), any())).thenReturn(just(updatedMessage));
        Message message = new Message("2023-05-30T12:01:57.510Z", "2023-05-30T12:01:57.510Z", "6475e595ac3946b6a812d863",
                "6477bc8f27adf9b5b978401e", "Test1");
        ObservableList<Message> messageList = messagesBoxController.getMessages();
        interact(() -> messageList.add(message));
        waitForFxEvents();
        clickOn("Test1");
        clickOn("#editMessage");
        clickOn("#messageArea");
        write("Test2");
        clickOn("OK");
        interact(() -> messageList.stream().filter(m -> m._id().equals(updatedMessage._id())).findFirst()
                .ifPresent(m -> messageList.set(messageList.indexOf(m), updatedMessage)));
        assertNotNull(lookup("Test1Test2").query());
    }

    @Test
    void deleteMessage() {
        Message message = new Message("2023-05-30T12:01:57.510Z", "2023-05-30T12:01:57.510Z", "6475e595ac3946b6a812d863",
                "6477bc8f27adf9b5b978401e", "Test1");
        when(messageService.deleteMessage(any(), any(), any())).thenReturn(just(message));
        ObservableList<Message> messageList = messagesBoxController.getMessages();
        interact(() -> messageList.add(message));
        waitForFxEvents();
        clickOn("Test1");
        clickOn("#deleteMessage");
        clickOn("OK");
        interact(() -> messageList.remove(message));
        assertNull(lookup("Test1").tryQueryAs(javafx.scene.control.Label.class).orElse(null));
    }

}