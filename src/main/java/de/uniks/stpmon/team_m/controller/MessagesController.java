package de.uniks.stpmon.team_m.controller;

import de.uniks.stpmon.team_m.Constants;
import de.uniks.stpmon.team_m.controller.subController.GroupCell;
import de.uniks.stpmon.team_m.controller.subController.UserCell;
import de.uniks.stpmon.team_m.dto.Group;
import de.uniks.stpmon.team_m.dto.Message;
import de.uniks.stpmon.team_m.dto.User;
import de.uniks.stpmon.team_m.service.*;
import de.uniks.stpmon.team_m.utils.BestFriendUtils;
import de.uniks.stpmon.team_m.ws.EventListener;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    Provider<EventListener> eventListener;
    @Inject
    GroupService groupService;
    @Inject
    Preferences preferences;
    private final ObservableList<User> friends = FXCollections.observableArrayList();
    private final ObservableList<Group> groups = FXCollections.observableArrayList();
    private ListView<User> userListView;
    private ListView<Group> groupListView;
    private final ObservableList<Message> messages = FXCollections.observableArrayList();
    private String chatID;

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
            groups.stream().filter(group -> group.name() != null).forEach(group -> {
                this.groups.add(group);
            });
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
            if (userListView.getSelectionModel().getSelectedItem() != null) {
                Platform.runLater(() -> openFriendChat("userListView"));
            }
        });
        groupListView.setOnMouseClicked(event -> {
            if (groupListView.getSelectionModel().getSelectedItem() != null) {
                Platform.runLater(() -> openFriendChat("groupListView"));
            }
        });
        if (groupStorageProvider.get().get_id() != null) {
            openFriendChat("other");
        }

        // do something with filledListView
        return parent;

    }

    private void openFriendChat(String origin) {

        final Group chat;
        if (origin.equals("userListView")) {
            chat = new Group(null, null, List.of(userListView.getSelectionModel().getSelectedItem()._id(), userStorageProvider.get().get_id()));
        } else if (origin.equals("groupListView")) {
            chat = groupListView.getSelectionModel().getSelectedItem();
        } else {
            chat = new Group(null, null, List.of(groupStorageProvider.get().get_id(), userStorageProvider.get().get_id()));
        }

        disposables.add(groupService.getGroups(chat.membersToString())
                .observeOn(FX_SCHEDULER).subscribe(gotGroups -> {
                    if (chat.name() == null) {
                        for (Group group : gotGroups) {
                            System.out.println("for-loop");
                            if (group.name() == null) {
                                System.out.println("group.name() == null");
                                System.out.println(group._id());
                                chatID = group._id();
                                break;
                            }
                            chatID = null;
                        }
                    } else {
                        chatID = chat._id();
                    }

                    if (origin.equals("userListView")) {
                        this.currentFriendOrGroupText.setText(userListView.getSelectionModel().getSelectedItem().name());
                    } else if (origin.equals("groupListView")) {
                        this.currentFriendOrGroupText.setText(chat.name());
                    } else {
                        this.currentFriendOrGroupText.setText(groupStorageProvider.get().getName());
                    }
                    chatViewVBox.getChildren().clear();

                    System.out.println(chatID);
                    if (chatID != null) {
                        groupStorageProvider.get().set_id(chatID);

                        disposables.add(messageService.getGroupMessages(chatID)
                                .observeOn(FX_SCHEDULER).subscribe(messages -> {
                                    chatViewVBox.getChildren().clear();
                                    this.messages.setAll(messages);

                                    for (Message message : messages) {
                                        chatViewVBox.getChildren().add(this.createMessageNode(message));
                                    }

                                    chatScrollPane.setVvalue(1.0);
                                    System.out.println(chatID);
                                    System.out.println(eventListener.get());
                                    disposables.add(eventListener.get()
                                            .listen("groups." + chatID + ".messages.*.*", Message.class)
                                            .observeOn(FX_SCHEDULER)
                                            .subscribe(messageEvent -> {
                                                System.out.println(messageEvent);
                                                final Message message = messageEvent.data();
                                                switch (messageEvent.suffix()) {
                                                    case "created":
                                                        chatViewVBox.getChildren().add(createMessageNode(message));
                                                        chatScrollPane.setVvalue(1.0);
                                                        break;
                                                    case "updated":
                                                        for (Node messageNode : chatViewVBox.getChildren()) {
                                                            if (Objects.equals(messageNode.getId(), message._id())) {
                                                                VBox messageVBox = (VBox) messageNode;
                                                                HBox messageValueAreaContainer = (HBox) messageVBox.getChildren().get(0);
                                                                VBox messageValueArea = (VBox) messageValueAreaContainer.getChildren().get(0);
                                                                Text messageValueText = (Text) messageValueArea.getChildren().get(0);

                                                                messageValueText.setText(message.body());

                                                                HBox messageInfoArea = (HBox) messageVBox.getChildren().get(1);
                                                                Label edited = (Label) messageInfoArea.getChildren().get(1);
                                                                edited.setVisible(true);
                                                            }
                                                        }
                                                        break;
                                                    case "deleted":
                                                        chatViewVBox.getChildren().removeIf(
                                                                node -> Objects.equals(node.getId(), message._id())
                                                        );
                                                        break;
                                                }
                                            }));
                                }));
                    }
                }));

    }

    public VBox createMessageNode(Message message) {
        VBox newMessageNode = new VBox();
        HBox newMessageValueArea = new HBox();
        HBox newMessageInfoArea = new HBox();

        // @Cheng, so you can call delete and edit functionality on this message
        newMessageNode.setId(message._id());

        newMessageValueArea.maxWidthProperty().bind(chatViewVBox.widthProperty().map(number -> number.intValue() / 2 - 20));
        newMessageInfoArea.maxWidthProperty().bind(chatViewVBox.widthProperty().map(number -> number.intValue() / 2 - 20));

        boolean userIsSender = message.sender().equals(userStorageProvider.get().get_id());
        // if user is sender: message is on the right, else on the left
        newMessageNode.setAlignment(userIsSender ? Pos.TOP_RIGHT : Pos.TOP_LEFT);

        newMessageValueArea.setBorder(new Border(
                new BorderStroke(
                        Color.BLACK,
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(10),
                        new BorderWidths(2)
                )
        ));

        Text messageText = new Text();
        messageText.setText(message.body());
        messageText.wrappingWidthProperty().bind(chatViewVBox.widthProperty().map(number -> number.intValue() / 2 - 20));

        VBox newMessageValueContainer = new VBox();
        newMessageValueContainer.getChildren().add(messageText);
        newMessageValueArea.getChildren().add(newMessageValueContainer);

        newMessageValueArea.setPadding(new Insets(2));
        newMessageValueContainer.setPadding(new Insets(5));

        Label authorAndTime = new Label();
        Label edited = new Label();
        edited.setText("\u270F");

        if (userIsSender) {
            authorAndTime.setText(userStorageProvider.get().getName() +
                    " " +
                    formatTimeString(message.createdAt()) +
                    " ");

            newMessageValueContainer.setBackground(Background.fill(Paint.valueOf("lightblue")));
        } else {
            final String[] senderName = {""};
            usersService.getUser(message.sender()).map(
                    user -> {
                        senderName[0] = user.name();
                        return user;
                    }
            ).subscribe().dispose();

            authorAndTime.setText(senderName[0] +
                    " " +
                    formatTimeString(message.createdAt()) +
                    " ");
        }

        edited.setVisible(!Objects.equals(message.updatedAt(), message.createdAt()));

        newMessageInfoArea.getChildren().add(authorAndTime);
        newMessageInfoArea.getChildren().add(edited);

        Button editButton = new Button("\u270F");
        editButton.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Edit message");
            dialog.setHeaderText(null);
            dialog.setContentText("Message:");

            TextArea messageArea = new TextArea();
            messageArea.setText(message.body());
            messageArea.setPrefRowCount(4);

            dialog.getDialogPane().setContent(messageArea);
            Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            ok.setOnAction( event1 -> {
                disposables.add(messageService.updateMessage(chatID, message._id(), messageArea.getText(), MESSAGE_NAMESPACE_GROUPS)
                        .observeOn(FX_SCHEDULER).subscribe());
            });
            dialog.showAndWait();
        });

        Button deleteButton = new Button("\uD83D\uDDD1");
        deleteButton.setOnAction(event -> {
            Alert deleteAlert = new Alert(Alert.AlertType.CONFIRMATION);
            deleteAlert.setTitle("Confirm delete");
            deleteAlert.setHeaderText(null);
            deleteAlert.setContentText("Do you really want to delete this message?");

            deleteAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    disposables.add(messageService.deleteMessage(message._id(), chatID, Constants.MESSAGE_NAMESPACE_GROUPS)
                            .observeOn(FX_SCHEDULER).subscribe());
                }
                deleteAlert.close();
            });
        });
        newMessageInfoArea.getChildren().addAll(editButton, deleteButton);

        newMessageInfoArea.setFillHeight(true);
        HBox.setMargin(editButton, new Insets(0, 0, 0, 5));
        HBox.setMargin(deleteButton, new Insets(0, 0, 0, 5));
        editButton.setVisible(false);
        deleteButton.setVisible(false);

        newMessageNode.getChildren().add(newMessageValueArea);
        newMessageNode.getChildren().add(newMessageInfoArea);

        newMessageNode.hoverProperty().addListener((observable, oldValue, newValue) -> {
            editButton.setVisible(newValue);
            deleteButton.setVisible(newValue);
        });

        return newMessageNode;
    }

    private String formatTimeString(String dateTime) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.parse(dateTime), ZoneId.of("Europe/Berlin"));

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm, dd.MM.yy");
        return localDateTime.format(outputFormatter);
    }

    public void changeToMainMenu() {
        app.show(mainMenuControllerProvider.get());
    }

    public void changeToFindNewFriends() {
        app.show(newFriendControllerProvider.get());
    }

    public void changeToNewGroup() {
        groupStorageProvider.get().set_id(EMPTY_STRING);
        app.show(groupControllerProvider.get());
    }

    public void changeToSettings() {
        groupStorageProvider.get().set_id(groupListView.getSelectionModel().getSelectedItem()._id());
        app.show(groupControllerProvider.get());
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
                            //
                        }, Throwable::printStackTrace
                ));
        messageTextArea.setText("");
    }

    private void listenToMessages() {

    }
}
