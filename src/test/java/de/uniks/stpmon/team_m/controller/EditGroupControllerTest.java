package de.uniks.stpmon.team_m.controller;

import de.uniks.stpmon.team_m.App;
import de.uniks.stpmon.team_m.dto.Group;
import de.uniks.stpmon.team_m.service.GroupService;
import de.uniks.stpmon.team_m.service.GroupStorage;
import io.reactivex.rxjava3.core.Observable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;

import javax.inject.Provider;

import java.util.List;

import static de.uniks.stpmon.team_m.Constants.DELETE_ERROR_403;
import static de.uniks.stpmon.team_m.Constants.GENERIC_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EditGroupControllerTest extends ApplicationTest {
    @Mock
    GroupService groupService;
    @Spy
    App app = new App(null);
    @Mock
    Provider<GroupStorage> groupStorageProvider;
    @InjectMocks
    GroupController groupController;
    @Mock
    Provider<MessagesController> messagesControllerProvider;

    @Override
    public void start(Stage stage) throws Exception {
        GroupStorage groupStorage = mock(GroupStorage.class);
        Mockito.when(groupStorage.get_id()).thenReturn("645f8d731c386bcd2204da39");
        Mockito.when(groupStorageProvider.get()).thenReturn(groupStorage);

        when(groupService.getGroup(any())).thenReturn(Observable.just(new Group("645f8d731c386bcd2204da39",
                "TestGroup", List.of("645f8d731c386bcd2204da40"))));

        app.start(stage);
        app.show(groupController);
        stage.requestFocus();
    }

    @Test
    void deleteGroupTest() {
        final MessagesController messagesController = mock(MessagesController.class);
        when(messagesControllerProvider.get()).thenReturn(messagesController);
        doNothing().when(app).show(messagesController);
        when(groupService.delete(any())).thenReturn(Observable.just(new Group("645f8d731c386bcd2204da39",
                "TestGroup", List.of("645f8d731c386bcd2204da40"))));

        final TextField groupNameInput = lookup("#groupNameInput").query();

        assertEquals("TestGroup", groupNameInput.getText());

        clickOn("Delete group");
        clickOn("No");
        assertEquals("Monster Odyssey - Edit Group", app.getStage().getTitle());
        clickOn("Delete group");
        clickOn("Yes");
        assertEquals("Monster Odyssey - Edit Group", app.getStage().getTitle());
    }

    @Test
    void errorAlertTest() {
        when(groupService.delete(any())).thenReturn(Observable.error(new Exception("Test")));
        clickOn("Delete group");
        clickOn("Yes");
        clickOn(GENERIC_ERROR);
        clickOn("OK");
        assertEquals("Monster Odyssey - Edit Group", app.getStage().getTitle());
        when(groupService.delete(any())).thenReturn(Observable.error(new Exception("HTTP 403")));
        clickOn("Delete group");
        clickOn("Yes");
        clickOn(DELETE_ERROR_403);
        clickOn("OK");
        assertEquals("Monster Odyssey - Edit Group", app.getStage().getTitle());
    }
}