package de.uniks.stpmon.team_m.controller;

import de.uniks.stpmon.team_m.controller.subController.GroupCell;
import de.uniks.stpmon.team_m.controller.subController.MessagesBoxController;
import de.uniks.stpmon.team_m.controller.subController.UserCell;
import de.uniks.stpmon.team_m.dto.Group;
import de.uniks.stpmon.team_m.dto.User;
import de.uniks.stpmon.team_m.service.*;
import de.uniks.stpmon.team_m.utils.BestFriendUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import static de.uniks.stpmon.team_m.Constants.*;

public class MessagesController extends Controller {

    @FXML
    public Text currentFriendOrGroupText; //needs to be set each time a different chat is selected
    @FXML
    public Label friendsAndGroupText;
    @FXML
    public VBox friendsListViewVBox;
    @FXML
    public VBox groupsListViewVBox;
    @FXML
    public Button findNewFriendsButton;
    @FXML
    public Button newGroupButton;
    @FXML
    public Button mainMenuButton;
    @FXML
    public Button settingsButton;
    @FXML
    public TextArea messageTextArea;
    @FXML
    public Button sendButton;
    @FXML
    public VBox chatViewVBox;
    @FXML
    public ScrollPane chatScrollPane;
    @Inject
    Provider<MainMenuController> mainMenuControllerProvider;

    @Inject
    Provider<NewFriendController> newFriendControllerProvider;

    @Inject
    Provider<GroupController> groupControllerProvider;

    @Inject
    Provider<UserStorage> userStorageProvider;
    @Inject
    Provider<GroupStorage> groupStorageProvider;
    @Inject
    UsersService usersService;
    @Inject
    MessageService messageService;
    @Inject
    GroupService groupService;
    @Inject
    Preferences preferences;
    private final ObservableList<User> friends = FXCollections.observableArrayList();
    private final ObservableList<Group> groups = FXCollections.observableArrayList();
    private ListView<User> userListView;
    private ListView<Group> groupListView;
    private final List<Controller> subControllers = new ArrayList<>();
    Map<User, MessagesBoxController> messagesBoxControllerUserMap = new HashMap<>();
    Map<Group, MessagesBoxController> messagesBoxControllerGroupMap = new HashMap<>();

    @Inject
    public MessagesController() {
    }

    @Override
    public void init() {
        userListView = new ListView<>(friends);
        userListView.setId("friends");
        userListView.setPlaceholder(new Label(NO_FRIENDS_FOUND));
        userListView.setCellFactory(param -> new UserCell(preferences));
        if (!userStorageProvider.get().getFriends().isEmpty()) {
            disposables.add(usersService.getUsers(userStorageProvider.get().getFriends(), null).observeOn(FX_SCHEDULER)
                    .subscribe(users -> {
                        friends.setAll(users);
                        sortListView(userListView);
                        new BestFriendUtils(preferences).sortBestFriendTop(userListView);
                        userListView.refresh();
                    }));
        }

        listenToStatusUpdate(friends, userListView);

        groupListView = new ListView<>(groups);
        groupListView.setId("groups");
        groupListView.setCellFactory(param -> new GroupCell());
        groupListView.setPlaceholder(new Label(NO_GROUPS_FOUND));
        disposables.add(groupService.getGroups(null).observeOn(FX_SCHEDULER).subscribe(groups -> {
            groups.stream().filter(group -> {
                if (group.members().size() == 2 && group.name() == null) {
                    for (String id : userStorageProvider.get().getFriends()) {
                        if (group.members().contains(id)) {
                            return false;
                        }
                    }
                    return true;
                } else return group.name() != null;
            }).forEach(this.groups::add);
            groupListView.refresh();
        }));
    }

    @Override
    public String getTitle() {
        return MESSAGES_TITLE;
    }

    @Override
    public Parent render() {
        Parent parent = super.render();
        friendsListViewVBox.getChildren().add(userListView);
        groupsListViewVBox.getChildren().add(groupListView);
        messageTextArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                this.onSendMessage();
            }
        });
        userListView.setOnMouseClicked(event -> {
            if(!(userListView.getSelectionModel().getSelectedItem()==null)){
                openPrivateChat(userListView.getSelectionModel().getSelectedItem());
                currentFriendOrGroupText.setText(userListView.getSelectionModel().getSelectedItem().name());
            }
        });

        groupListView.setOnMouseClicked(event -> {
            if(!(groupListView.getSelectionModel().getSelectedItem()==null)){
                openGroupChat(groupListView.getSelectionModel().getSelectedItem());
                if (!(groupListView.getSelectionModel().getSelectedItem().name() == null)) {
                    currentFriendOrGroupText.setText(groupListView.getSelectionModel().getSelectedItem().name());
                }
                else{
                    for (String id: groupListView.getSelectionModel().getSelectedItem().members()){
                        if(!id.equals(userStorageProvider.get().get_id())){
                            disposables.add(usersService.getUser(id)
                                    .observeOn(FX_SCHEDULER).subscribe(user -> currentFriendOrGroupText.setText(user.name()), error -> currentFriendOrGroupText.setText("Unknown User")));
                        }
                    }
                }

            }
        });

        if (groupStorageProvider.get().get_id() != null) {
            for (User user : friends) {
                if (user._id().equals(groupStorageProvider.get().get_id())) {
                    openPrivateChat(user);
                }
            }
            for (Group group : groups) {
                if (group._id().equals(groupStorageProvider.get().get_id())) {
                    openGroupChat(group);
                }
            }
        }
        return parent;

    }

    @Override
    public void destroy() {
        super.destroy();
        subControllers.forEach(Controller::destroy);
        subControllers.clear();
        messagesBoxControllerUserMap.clear();
        messagesBoxControllerGroupMap.clear();
    }

    public void openPrivateChat(User user) {
        try {
            chatScrollPane.setContent(messagesBoxControllerUserMap.get(user).render());
        } catch (Exception ignored) {
            MessagesBoxController messagesBoxController = new MessagesBoxController(
                    messageService,
                    groupService,
                    eventListener,
                    usersService,
                    groupStorageProvider.get(),
                    userStorageProvider.get(),
                    user,
                    null,
                    currentFriendOrGroupText
            );
            messagesBoxController.init();
            messagesBoxControllerUserMap.put(user, messagesBoxController);
            subControllers.add(messagesBoxController);
            chatScrollPane.setContent(messagesBoxControllerUserMap.get(user).render());
        }
    }

    public void openGroupChat(Group group) {
        try {
            chatScrollPane.setContent(messagesBoxControllerGroupMap.get(group).render());
        } catch (Exception ignored) {
            MessagesBoxController messagesBoxController = new MessagesBoxController(
                    messageService,
                    groupService,
                    eventListener,
                    usersService,
                    groupStorageProvider.get(),
                    userStorageProvider.get(),
                    null,
                    group,
                    currentFriendOrGroupText
            );
            messagesBoxController.init();
            messagesBoxControllerGroupMap.put(group, messagesBoxController);
            subControllers.add(messagesBoxController);
            chatScrollPane.setContent(messagesBoxControllerGroupMap.get(group).render());
        }
    }

    public void changeToMainMenu() {
        destroy();
        app.show(mainMenuControllerProvider.get());
    }

    public void changeToFindNewFriends() {
        destroy();
        app.show(newFriendControllerProvider.get());
    }

    public void changeToNewGroup() {
        groupStorageProvider.get().set_id(EMPTY_STRING);
        destroy();
        app.show(groupControllerProvider.get());
    }

    public void changeToSettings() {
        if (groupListView.getSelectionModel().getSelectedItem() != null) {
            groupStorageProvider.get().set_id(groupListView.getSelectionModel().getSelectedItem()._id());
            destroy();
            app.show(groupControllerProvider.get());
        }
    }

    public void onSendMessage() {
        String groupID = groupStorageProvider.get().get_id();
        if (groupID == null) {
            return;
        }

        String messageBody = messageTextArea.getText();
        disposables.add(messageService.newMessage(groupID, messageBody, MESSAGE_NAMESPACE_GROUPS)
                .observeOn(FX_SCHEDULER).subscribe(
                        message -> {
                        }, Throwable::printStackTrace
                ));
        messageTextArea.setText("");
    }
}
