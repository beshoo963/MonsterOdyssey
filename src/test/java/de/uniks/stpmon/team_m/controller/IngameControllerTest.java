package de.uniks.stpmon.team_m.controller;

import de.uniks.stpmon.team_m.App;
import de.uniks.stpmon.team_m.controller.subController.*;
import de.uniks.stpmon.team_m.dto.*;
import de.uniks.stpmon.team_m.service.*;
import de.uniks.stpmon.team_m.udp.UDPEventListener;
import de.uniks.stpmon.team_m.utils.EncounterOpponentStorage;
import de.uniks.stpmon.team_m.utils.MonsterData;
import de.uniks.stpmon.team_m.utils.MonsterStorage;
import de.uniks.stpmon.team_m.utils.TrainerStorage;
import de.uniks.stpmon.team_m.ws.EventListener;
import io.reactivex.rxjava3.core.Observable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;

import javax.inject.Provider;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import static io.reactivex.rxjava3.core.Observable.empty;
import static io.reactivex.rxjava3.core.Observable.just;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IngameControllerTest extends ApplicationTest {

    @Spy
    final
    App app = new App(null);
    @Mock
    Provider<MainMenuController> mainMenuControllerProvider;
    @Mock
    Provider<TrainerStorage> trainerStorageProvider;
    @Mock
    Provider<MonsterStorage> monsterStorageProvider;
    @Mock
    AreasService areasService;
    @Mock
    Provider<UDPEventListener> udpEventListenerProvider;
    @Mock
    Provider<MonstersListController> monstersListControllerProvider;
    @Mock
    Provider<NotificationListHandyController> notificationListHandyControllerProvider;
    @Mock
    Provider<IngameStarterMonsterController> ingameStarterMonsterControllerProvider;
    @Mock
    Provider<IngameMiniMapController> ingameMiniMapControllerProvider;
    @Mock
    Provider<ItemMenuController> itemMenuControllerProvider;
    @Mock
    Provider<PresetsService> presetsServiceProvider;

    // Please also keep the EncounterOpponentStorage Spy, it is needed for the tests
    @Spy
    EncounterOpponentStorage encounterOpponentStorage;
    @Mock
    Provider<IngamePauseMenuController> pauseMenuControllerProvider;
    @Mock
    TrainersService trainersService;
    @Mock
    MessageService messageService;
    @Mock
    MonstersService monstersService;
    @Mock
    EncounterOpponentsService encounterOpponentsService;
    @Mock
    PresetsService presetsService;
    @InjectMocks
    IngameController ingameController;
    @Mock
    Provider<EventListener> eventListener;
    @InjectMocks
    NotificationListHandyController notificationListHandyController;
    @InjectMocks
    IngameStarterMonsterController ingameStarterMonsterController;
    @InjectMocks
    IngamePauseMenuController pauseMenuController;
    @InjectMocks
    MonstersListController monstersListController;
    @InjectMocks
    MainMenuController mainMenuController;
    @Mock
    TrainerItemsService trainerItemsService;
    @Mock
    AuthenticationService authenticationService;
    @Mock
    Provider<IngameSettingsController> ingameSettingsControllerProvider;
    @InjectMocks
    IngameSettingsController ingameSettingsController;
    @Mock
    Provider<IngameKeybindingsController> ingameKeybindingsControllerProvider;
    @InjectMocks
    IngameKeybindingsController ingameKeybindingsController;
    @Mock
    Provider<ChangeAudioController> changeAudioControllerProvider;
    @InjectMocks
    ChangeAudioController changeAudioController;
    @Mock
    Provider<IngameTrainerSettingsController> ingameTrainerSettingsControllerProvider;
    @InjectMocks
    IngameTrainerSettingsController ingameTrainerSettingsController;

    @Override
    public void start(Stage stage) {
        ResourceBundle bundle = ResourceBundle.getBundle("de/uniks/stpmon/team_m/lang/lang", Locale.forLanguageTag("en"));
        Preferences preferences = Preferences.userNodeForPackage(IngameController.class);
        ingameController.setValues(bundle, preferences, null, ingameController, app);

        when(authenticationService.stayOnline()).thenReturn(Observable.just(new LoginResult(
                "423f8d731c386bcd2204da39",
                "1",
                "online",
                null,
                null,
                "a1a2",
                "a3a4")));
        UDPEventListener udpEventListener = mock(UDPEventListener.class);
        Mockito.when(udpEventListenerProvider.get()).thenReturn(udpEventListener);
        when(udpEventListener.ping()).thenReturn(empty());
        when(udpEventListener.listen(any(), any())).thenReturn(Observable.just(new Event<>("areas.*.trainers.*.moved", new MoveTrainerDto("6475e595ac3946b6a812d865", "6475e595ac3946b6a812d863", 5, 4, 0))));

        MonsterStorage monsterStorage = mock(MonsterStorage.class);
        when(monsterStorageProvider.get()).thenReturn(monsterStorage);
        when(monsterStorage.getMonsterImage(anyInt())).thenReturn(null);

        final TrainerStorage trainerStorage = mock(TrainerStorage.class);
        Mockito.when(trainerStorageProvider.get()).thenReturn(trainerStorage);
        when(trainerItemsService.getItems(any(), any(), any())).thenReturn(
                Observable.just(List.of(new Item("98759283759023874653", "Travis", 2, 2)))
        );
        when(trainerStorageProvider.get().getRegion()).thenReturn(
                new Region(
                        "2023-05-22T17:51:46.772Z",
                        "2023-05-22T17:51:46.772Z",
                        "646bab5cecf584e1be02598a",
                        "Albertina",
                        new Spawn("646bc3c0a9ac1b375fb41d93", 1, 1),
                        null));
        when(areasService.getArea(any(), any())).thenReturn(Observable.just(
                new Area(
                        "2023-05-22T17:51:46.772Z",
                        "2023-05-22T17:51:46.772Z",
                        "646bc3c0a9ac1b375fb41d93",
                        "646bc436cfee07c0e408466f",
                        "Albertina",
                        null,
                        null
                )));
        lenient().when(areasService.getAreas(any())).thenReturn(Observable.just(
                List.of(new Area(
                        "2023-05-22T17:51:46.772Z",
                        "2023-05-22T17:51:46.772Z",
                        "646bc3c0a9ac1b375fb41d93",
                        "646bc436cfee07c0e408466f",
                        "Albertina",
                        null,
                        null
                ))));
        doNothing().when(trainerStorage).setMonsters(any());
        lenient().when(presetsService.getCharacter(any())).thenReturn(Observable.empty());
        when(trainersService.getTrainers(any(), any(), any())).thenReturn(Observable.just(List.of(
                        new Trainer(
                                "2023-05-30T12:02:57.510Z",
                                "2023-05-30T12:01:57.510Z",
                                "6475e595ac3946b6a812d863",
                                "646bab5cecf584e1be02598a",
                                "6475e595ac3946b6a812d868",
                                "Hans",
                                "Premade_Character_01.png",
                                0,
                                List.of("63va3w6d11sj2hq0nzpsa20w", "86m1imksu4jkrxuep2gtpi4a"),
                                List.of(1, 2),
                                List.of("646bacc568933551792bf3d5"),
                                "6475e595ac3946b6a812d863",
                                33,
                                18,
                                0,
                                new NPCInfo(false, false, false, false, null, null, null),
                                null),
                new Trainer(
                        "2023-05-30T12:02:57.510Z",
                        "2023-05-30T12:01:57.510Z",
                        "6475e595ac3946b6a812d863",
                        "646bab5cecf584e1be02598a",
                        "6475e595ac3946b6a812d868",
                        "Krankenschwester Erna",
                        "Nurse_2_16x16.png",
                        0,
                        List.of(),
                        List.of(),
                        List.of("646bacc568933551792bf3d5"),
                        "6475e595ac3946b6a812d863",
                        20,
                        18,
                        2,
                        new NPCInfo(false, false, false, true, null, null, null),
                        null),
                new Trainer(
                        "2023-05-30T12:02:57.510Z",
                        "2023-05-30T12:01:57.510Z",
                        "6475e595ac3946b6a812d869",
                        "646bab5cecf584e1be02598a",
                        "6475e595ac3946b6a812d868",
                        "Prof. Testikus Maximus",
                        "Premade_Character_02.png",
                        0,
                        List.of(),
                        List.of(),
                        List.of("646bacc568933551792bf3d5"),
                        "6475e595ac3946b6a812d863",
                        69,
                        69,
                        2,
                        new NPCInfo(false, false, false, false, null, null, List.of("1", "3", "5")),
                        null),
                new Trainer(
                        "2023-05-30T12:02:57.510Z",
                        "2023-05-30T12:01:57.510Z",
                        "6475e595ac3946b6a812d811",
                        "646bab5cecf584e1be02598a",
                        "6475e595ac3946b6a812d868",
                        "Test Clerk",
                        "Premade_Character_02.png",
                        0,
                        List.of(),
                        List.of(),
                        List.of("646bacc568933551792bf3d5"),
                        "6475e595ac3946b6a812d863",
                        100,
                        100,
                        2,
                        new NPCInfo(false, false, false, false, List.of(1, 2, 3), null, null),
                        null),
                new Trainer(
                        "2023-05-30T12:02:57.510Z",
                        "2023-05-30T12:01:57.510Z",
                        "6475e595ac3946b6a812d867",
                        "646bab5cecf584e1be02598a",
                        "6475e595ac3946b6a812d868",
                        "OtherEncounter",
                        "Premade_Character_02.png",
                        0,
                        List.of(),
                        List.of(),
                        List.of("646bacc568933551792bf3d5"),
                        "6475e595ac3946b6a812d863",
                        200,
                        200,
                        2,
                        null,
                        null)
                )
        ));
        EventListener eventListenerMock = mock(EventListener.class);
        when(eventListener.get()).thenReturn(eventListenerMock);
        Message message = new Message("2023-05-30T12:01:57.510Z", "2023-05-30T12:01:57.510Z", "6475e595ac3946b6a812d863",
                "6475e595ac3946b6a812d868", "Test1");
        when(eventListener.get().listen("regions.646bab5cecf584e1be02598a.messages.*.*", Message.class)).thenReturn(just(
                new Event<>("regions.646bab5cecf584e1be02598a.messages.6475e595ac3946b6a812d863.created", message)
        ));
        Trainer trainer = new Trainer(
                "2023-05-30T12:02:57.510Z",
                "2023-05-30T12:01:57.510Z",
                "6475e595ac3946b6a812d865",
                "646bab5cecf584e1be02598a",
                "6475e595ac3946b6a812d868",
                "Peter",
                "Premade_Character_02.png",
                0,
                List.of("1", "2"),
                List.of(),
                List.of("646bacc568933551792bf3d5"),
                "6475e595ac3946b6a812d863",
                33,
                18,
                1,
                new NPCInfo(false, false, false, false, null, null, null),
                null);

        when(trainerStorageProvider.get().getY()).thenReturn(5);
        when(trainerStorageProvider.get().getX()).thenReturn(5);

        when(eventListener.get().listen("regions." + trainerStorageProvider.get().getRegion()._id() + ".trainers.*.*", Trainer.class)).thenReturn(just(
                new Event<>("regions.646bab5cecf584e1be02598a.trainers.6475e595ac3946b6a812d865.created", trainer)));

        MonstersListController monstersListController = mock(MonstersListController.class);
        when(trainerStorageProvider.get().getTrainer()).thenReturn(trainer);
        when(monstersListControllerProvider.get()).thenReturn(monstersListController);

        notificationListHandyController.setValues(bundle, null, null, notificationListHandyController, app);
        when(notificationListHandyControllerProvider.get()).thenReturn(notificationListHandyController);
        ingameStarterMonsterController.setValues(bundle, null, null, ingameStarterMonsterController, app);
        lenient().when(ingameStarterMonsterControllerProvider.get()).thenReturn(ingameStarterMonsterController);

        final List<MonsterTypeDto> monsterTypeDtos = List.of(
                new MonsterTypeDto(
                        1,
                        "Monster1",
                        "Flamander_1.png",
                        List.of("fire"),
                        "Monster1lololol"
                ),
                new MonsterTypeDto(
                        2,
                        "Monster2",
                        "Flamander_1.png",
                        List.of("fire"),
                        "ich bin ein echter gangster"
                ),
                new MonsterTypeDto(
                        3,
                        "Monster3",
                        "Flamander_1.png",
                        List.of("fire"),
                        "waaas willst du tun"
                ),
                new MonsterTypeDto(
                        4,
                        "Monster4",
                        "Flamander_1.png",
                        List.of("fire"),
                        "dieser big tasty hat nichts mehr mit einem big tasty zu tun"
                ),
                new MonsterTypeDto(
                        5,
                        "Monster5",
                        "Flamander_1.png",
                        List.of("fire"),
                        "auch ein blindes schaf findet manchmal ein huhn"
                )
        );
        lenient().when(presetsService.getMonsters()).thenReturn(Observable.just(monsterTypeDtos));


        ResponseBody responseBody = mock(ResponseBody.class);

        lenient().when(presetsService.getMonsterImage(anyInt())).thenReturn(Observable.just(responseBody));

        //Mocking the opponent (Situation)
        when(eventListener.get().listen("encounters.*.trainers." + trainerStorageProvider.get().getTrainer()._id() + ".opponents.*.*", Opponent.class)).thenReturn(just(
                new Event<>("encounters.*.trainers.6475e595ac3946b6a812d865,opponents.*.nothappening", null)));

        when(eventListenerMock.listen("trainers." + trainer._id() + ".monsters.*.created", Monster.class)).thenReturn(Observable.empty());
        when(eventListenerMock.listen("trainers." + trainer._id() + ".items.*.created", Item.class)).thenReturn(Observable.empty());

        when(encounterOpponentsService.getTrainerOpponents(anyString(), anyString())).thenReturn(Observable.just(List.of()));
        lenient().when(encounterOpponentsService.getTrainerOpponents("646bab5cecf584e1be02598a", "6475e595ac3946b6a812d867")).thenReturn(Observable.just(List.of(
                new Opponent(
                        "2023-07-09T11:52:17.658Z",
                        "2023-07-09T11:52:35.578Z",
                        "64aa9f7132eb8b56aa9eb20f",
                        "64aa9f7132eb8b56aa9eb208",
                        "64abfde932eb8b56aac8efac",
                        true,
                        true,
                        "64aa9f7132eb8b56aa9eb20c",
                        null,
                        List.of(),
                        0),
                new Opponent(
                        "2023-07-09T11:52:17.658Z",
                        "2023-07-09T11:52:35.578Z",
                        "64aa9f7132eb8b56aa9eb20d",
                        "64aa9f7132eb8b56aa9eb208",
                        "64abfde932eb8b56aac8efad",
                        true,
                        true,
                        "64aa9f7132eb8b56aa9eb20d",
                        null,
                        List.of(),
                        0
                ))));
        final MonsterStorage monsterStorageMock = mock(MonsterStorage.class);
        when(monsterStorageProvider.get()).thenReturn(monsterStorageMock);
        LinkedHashMap<String, Integer> abilities = new LinkedHashMap<>();
        abilities.put("1", 35);
        abilities.put("3", 20);
        abilities.put("6", 25);
        abilities.put("7", 15);
        final Monster monster = new Monster("2023-06-05T17:02:40.357Z",
                "023-06-05T17:02:40.357Z",
                "647e1530866ace3595866db2",
                "647e15308c1bb6a91fb57321",
                1,
                1,
                0,
                abilities,
                new MonsterAttributes(14, 8, 8, 5),
                new MonsterAttributes(14, 8, 8, 5),
                List.of("poisoned")
        );
        when(monsterStorageProvider.get().getMonsterData(any())).thenReturn(new MonsterData(monster, monsterTypeDtos.get(0), null));
        when(trainersService.getTrainer(anyString(), anyString())).thenReturn(Observable.just(trainer));

        app.start(stage);
        app.show(ingameController);
        stage.requestFocus();
    }

    @Test
    void testHelp() throws InterruptedException {
        clickOn("#smallHandyButton");

        Thread.sleep(1000);
        clickOn("close");
    }

    @Test
    void testMessage() {
        // ingame chat
        final TextField messageField = lookup("#messageField").query();
        when(messageService.newMessage(any(), any(), any()))
                .thenReturn(Observable.just(new Message(
                        "2023-05-30T12:03:57.510Z",
                        "2023-05-30T12:01:57.510Z",
                        "6475e595ac3946b6a812d868",
                        "6477bc8f27adf9b5b978401f",
                        "Hello World")));


        // Send with enter key
        clickOn("#messageField");
        write("Hello World");
        type(KeyCode.ENTER);
        assertEquals("", messageField.getText());
        //verify(messageService).newMessage(any(), any(), any());
        assertFalse(messageField.isFocused());

        // Send with button
        clickOn("#messageField");
        write("Hello World");
        clickOn("#sendMessageButton");
        assertEquals("", messageField.getText());

        type(KeyCode.ENTER);
        assertTrue(messageField.isFocused());
        type(KeyCode.ENTER);
        assertFalse(messageField.isFocused());

        ListView<Message> chat = lookup("#chatListView").query();
        assertEquals(chat.getOpacity(), 0);
        clickOn("#showChatButton");
        assertEquals(chat.getOpacity(), 1);
        moveTo("Test1");
        clickOn("#showChatButton");
        assertEquals(chat.getOpacity(), 0);
    }

    @Test
    void testMiniMap() {
        IngameMiniMapController ingameMini = mock(IngameMiniMapController.class);
        when(ingameMiniMapControllerProvider.get()).thenReturn(ingameMini);
        doNothing().when(ingameMini).init(any(), any(), any(), any(), any(), any());

        Button close = new Button("Close");
        close.setOnAction(event -> {
            StackPane stackPane = lookup("#stackPane").query();
            VBox miniMapVBox = lookup("#miniMapVBox").query();
            stackPane.getChildren().remove(stackPane.getChildren().size() - 1);
            miniMapVBox.setVisible(false);
            miniMapVBox.toBack();
            ingameController.buttonsDisable(false);
        });
        when(ingameMini.render()).thenReturn(close);

        clickOn("#mapSymbol");
        clickOn(close);
    }

    @Test
    void testInventory() {
        ItemMenuController itemMenuController = mock(ItemMenuController.class);
        when(itemMenuControllerProvider.get()).thenReturn(itemMenuController);
        doNothing().when(itemMenuController).init(any(), any(), any(), any(), any(), any(), any());

        Button close = new Button("Close");
        close.setOnAction(event -> {
            StackPane stackPane = lookup("#stackPane").query();
            VBox itemMenuBox = lookup("#itemMenuBox").query();
            stackPane.getChildren().remove(stackPane.getChildren().size() - 1);
            itemMenuBox.setVisible(false);
            itemMenuBox.toBack();
            ingameController.buttonsDisable(false);
        });
        when(itemMenuController.render()).thenReturn(close);

        clickOn("#coinsButton");
        clickOn(close);
    }

    @Test
    void testDialog() throws InterruptedException {
        Mockito.when(trainerStorageProvider.get().getX()).thenReturn(33);
        Mockito.when(trainerStorageProvider.get().getY()).thenReturn(19);
        Mockito.when(trainerStorageProvider.get().getDirection()).thenReturn(1);
        when(udpEventListenerProvider.get().talk(any(), any())).thenReturn(empty());

        type(KeyCode.E);
        final TextFlow dialogTextFlow = lookup("#dialogTextFlow").query();
        final Text dialogText = (Text) dialogTextFlow.getChildren().get(0);
        final String firstDefaultText = dialogText.getText();
        assertNotEquals("", firstDefaultText);

        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);

        assertNotEquals(firstDefaultText, dialogText.getText());

        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);

        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);

        Thread.sleep(30);
    }


    @Test
    void testTalkToNPC2TilesAway() {
        Mockito.when(trainerStorageProvider.get().getX()).thenReturn(33);
        Mockito.when(trainerStorageProvider.get().getY()).thenReturn(20);
        when(udpEventListenerProvider.get().talk(any(), any())).thenReturn(empty());

        press(KeyCode.E);
        release(KeyCode.E);

        final StackPane stackPane = lookup("#stackPane").query();
        final Node node = stackPane.getChildren().get(stackPane.getChildren().size() - 1);

        assertNotEquals("dialogStackPane", node.getId());
    }

    @Test
    void testTalkDirection2() {
        Mockito.when(trainerStorageProvider.get().getX()).thenReturn(33);
        Mockito.when(trainerStorageProvider.get().getY()).thenReturn(19);
        Mockito.when(trainerStorageProvider.get().getDirection()).thenReturn(2);
        when(udpEventListenerProvider.get().talk(any(), any())).thenReturn(empty());

        press(KeyCode.E);
        release(KeyCode.E);

        final StackPane stackPane = lookup("#stackPane").query();
        final Node node = stackPane.getChildren().get(stackPane.getChildren().size() - 1);

        assertNotEquals("dialogStackPane", node.getId());
    }

    @Test
    void testTalkDirection3() {
        Mockito.when(trainerStorageProvider.get().getX()).thenReturn(33);
        Mockito.when(trainerStorageProvider.get().getY()).thenReturn(19);
        Mockito.when(trainerStorageProvider.get().getDirection()).thenReturn(3);
        when(udpEventListenerProvider.get().talk(any(), any())).thenReturn(empty());

        press(KeyCode.E);
        release(KeyCode.E);

        final StackPane stackPane = lookup("#stackPane").query();
        final Node node = stackPane.getChildren().get(stackPane.getChildren().size() - 1);

        assertNotEquals("dialogStackPane", node.getId());
    }


    @Test
    void testNurseDialogWithNoMons() throws InterruptedException {
        Mockito.when(trainerStorageProvider.get().getX()).thenReturn(20);
        Mockito.when(trainerStorageProvider.get().getY()).thenReturn(20);    // two tiles apart from Nurse
        when(udpEventListenerProvider.get().talk(any(), any())).thenReturn(empty());
        when(trainerStorageProvider.get().getTrainer()).thenReturn(new Trainer(
                "2023-05-30T12:02:57.510Z",
                "2023-05-30T12:01:57.510Z",
                "6475e595ac3946b6a812d865",
                "646bab5cecf584e1be02598a",
                "6475e595ac3946b6a812d868",
                "Peter",
                "Premade_Character_02.png",
                0,
                List.of(),
                null,
                List.of("646bacc568933551792bf3d5"),
                "6475e595ac3946b6a812d863",
                33,
                18,
                0,
                new NPCInfo(false, false, false, false, null, null, null),
                null));

        press(KeyCode.E);
        release(KeyCode.E);

        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);

        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);

        Thread.sleep(30);

        final StackPane stackPane = lookup("#stackPane").query();
        final Node node = stackPane.getChildren().get(stackPane.getChildren().size() - 1);

        assertNotEquals("dialogStackPane", node.getId());
    }

    @Test
    void testMovement() {
        when(udpEventListenerProvider.get().move(any())).thenReturn(empty());
        when(trainerStorageProvider.get().getDirection()).thenReturn(1);
        when(udpEventListenerProvider.get().listen(any(), any())).thenReturn(Observable.just(new Event<>("areas.*.trainers.*.moved", new MoveTrainerDto("6475e595ac3946b6a812d865", "6475e595ac3946b6a812d863", 6, 5, 0))));
        type(KeyCode.W);
        sleep(200);
        type(KeyCode.A);
        sleep(200);
        type(KeyCode.S);
        sleep(200);
        type(KeyCode.D);
    }

    @Test
    void testNurseDialogWithMons() throws InterruptedException {
        // test nurse dialog
        Mockito.when(trainerStorageProvider.get().getX()).thenReturn(20);
        Mockito.when(trainerStorageProvider.get().getY()).thenReturn(20);    // two tiles apart from Nurse
        Mockito.when(trainerStorageProvider.get().getDirection()).thenReturn(1);
        when(udpEventListenerProvider.get().talk(any(), any())).thenReturn(empty());
        type(KeyCode.E);

        final TextFlow dialogTextFlow = lookup("#dialogTextFlow").query();
        final Text dialogText = (Text) dialogTextFlow.getChildren().get(0);
        final String firstNurseText = dialogText.getText();

        assertNotEquals("", firstNurseText);

        press(KeyCode.E);
        release(KeyCode.E);

        Thread.sleep(30);

        assertNotEquals(firstNurseText, dialogText.getText());

        press(KeyCode.E);
        release(KeyCode.E);

        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);

        Thread.sleep(30);

        clickOn("No");

        final StackPane rootStackPane = lookup("#root").query();
        final Node node = rootStackPane.getChildren().get(rootStackPane.getChildren().size() - 1);

        assertNotEquals("nurseVBox", node.getId());

        press(KeyCode.E);
        release(KeyCode.E);

        Thread.sleep(30);


        final StackPane stackPane = lookup("#stackPane").query();
        final Node node2 = stackPane.getChildren().get(stackPane.getChildren().size() - 1);

        assertNotEquals("dialogStackPane", node2.getId());

        for (int i = 0; i < 4; i++) {
            press(KeyCode.E);
            release(KeyCode.E);

            Thread.sleep(30);
        }

        clickOn("Yes");
        // healing of monsters cannot be tested, since this should happen on the server, when you encounter the nurse

        press(KeyCode.E);
        release(KeyCode.E);

        Thread.sleep(30);
    }

    @Test
    void testPauseMenu() {

        // test pause and settings

        ResourceBundle bundle = ResourceBundle.getBundle("de/uniks/stpmon/team_m/lang/lang", Locale.forLanguageTag("en"));
        Preferences preferences = Preferences.userNodeForPackage(IngameController.class);
        pauseMenuController.setValues(bundle, null, null, pauseMenuController, app);
        when(pauseMenuControllerProvider.get()).thenReturn(pauseMenuController);

        ingameSettingsController.setValues(bundle, null, null, ingameSettingsController, app);
        when(ingameSettingsControllerProvider.get()).thenReturn(ingameSettingsController);

        ingameKeybindingsController.setValues(bundle, preferences, null, ingameKeybindingsController, app);
        when(ingameKeybindingsControllerProvider.get()).thenReturn(ingameKeybindingsController);

        changeAudioController.setValues(bundle, preferences, null, changeAudioController, app);
        when(changeAudioControllerProvider.get()).thenReturn(changeAudioController);

        ingameTrainerSettingsController.setValues(bundle, null, null, ingameTrainerSettingsController, app);
        when(ingameTrainerSettingsControllerProvider.get()).thenReturn(ingameTrainerSettingsController);
        when(trainerStorageProvider.get().getTrainerSprite()).thenReturn("Premade_Character_01.png");

        mainMenuController.setValues(bundle, null, null, mainMenuController, app);
        when(mainMenuControllerProvider.get()).thenReturn(mainMenuController);
        doNothing().when(app).show(mainMenuController);

        clickOn("#pauseButton");
        clickOn("#resumeGameButton");

        type(KeyCode.ESCAPE);
        clickOn("#settingsButton");
        type(KeyCode.ESCAPE);
        clickOn("#keybindingsButton");
        clickOn(ingameKeybindingsController.goBackButton);


        clickOn("#audioSettingsButton");
        clickOn("#closeButton");

        clickOn("#trainerSettingsButton");
        clickOn("<");


        clickOn("#goBackButton");
        type(KeyCode.ESCAPE);
        type(KeyCode.ESCAPE);

        clickOn("#leaveGameButton");
        verify(app).show(mainMenuController);
    }

    @Test
    void testTrainerSettingsUpdate() {
        ResourceBundle bundle = ResourceBundle.getBundle("de/uniks/stpmon/team_m/lang/lang", Locale.forLanguageTag("en"));
        pauseMenuController.setValues(bundle, null, null, pauseMenuController, app);
        when(pauseMenuControllerProvider.get()).thenReturn(pauseMenuController);

        ingameSettingsController.setValues(bundle, null, null, ingameSettingsController, app);
        when(ingameSettingsControllerProvider.get()).thenReturn(ingameSettingsController);

        ingameTrainerSettingsController.setValues(bundle, null, null, ingameTrainerSettingsController, app);
        when(ingameTrainerSettingsControllerProvider.get()).thenReturn(ingameTrainerSettingsController);
        when(trainerStorageProvider.get().getTrainerSprite()).thenReturn("Premade_Character_01.png");

        mainMenuController.setValues(bundle, null, null, mainMenuController, app);
        when(mainMenuControllerProvider.get()).thenReturn(mainMenuController);

        when(trainersService.updateTrainer(any(), any(), any(), any(), any(), any(), any())).thenReturn(Observable.just(new Trainer(
                "2023-05-30T12:02:57.510Z",
                "2023-05-30T12:01:57.510Z",
                "6475e595ac3946b6a812d863",
                "646bab5cecf584e1be02598a",
                "6475e595ac3946b6a812d868",
                "Test",
                "Premade_Character_01.png",
                0,
                List.of("63va3w6d11sj2hq0nzpsa20w", "86m1imksu4jkrxuep2gtpi4a"),
                List.of(1, 2),
                List.of("646bacc568933551792bf3d5"),
                "6475e595ac3946b6a812d863",
                33,
                18,
                0,
                new NPCInfo(false, false, false, false, null, null, null),
                null)));
        doNothing().when(app).show(mainMenuController);

        type(KeyCode.ESCAPE);
        clickOn("#settingsButton");
        clickOn("#trainerSettingsButton");
        clickOn("#trainerNameEditButton");
        clickOn("#trainerNameTextfield");
        write("Test");
        clickOn("#updateTrainerButton");
        clickOn("OK");
        verify(app).show(mainMenuController);
    }

    @Test
    void showMonsterTest() {
        ResourceBundle bundle = ResourceBundle.getBundle("de/uniks/stpmon/team_m/lang/lang", Locale.forLanguageTag("en"));
        monstersListController.setValues(bundle, null, null, monstersListController, app);
        when(monstersListControllerProvider.get()).thenReturn(monstersListController);
        when(presetsServiceProvider.get()).thenReturn(presetsService);
        LinkedHashMap<String, Integer> abilities = new LinkedHashMap<>();
        abilities.put("1", 1);
        abilities.put("23", 2);
        abilities.put("4", 3);
        when(monstersService.getMonsters(any(), any())).thenReturn(Observable.just(List.of(
                new Monster(
                        "2023-05-22T17:51:46.772Z",
                        "2023-05-22T17:51:46.772Z",
                        "646bac223b4804b87c0b8054",
                        "646bac8c1a74032c70fffe24",
                        1,
                        3,
                        56,
                        abilities,
                        new MonsterAttributes(40, 23, 45, 67),
                        new MonsterAttributes(20, 23, 45, 67),
                        List.of()
                )
        )));
        // test monster list
        clickOn("#monstersButton");

        clickOn("Other");
        //moveTo("Salamander (Level 3)");
        sleep(5000);
    }

    @Test
    void testStarter1MonsterNpcDialog() throws InterruptedException {
        Mockito.when(trainerStorageProvider.get().getX()).thenReturn(69);
        Mockito.when(trainerStorageProvider.get().getY()).thenReturn(70);
        Mockito.when(trainerStorageProvider.get().getDirection()).thenReturn(1);

        when(udpEventListenerProvider.get().talk(any(), any())).thenReturn(empty());
        when(trainerStorageProvider.get().getTrainer()).thenReturn(new Trainer(
                "2023-05-30T12:02:57.510Z",
                "2023-05-30T12:01:57.510Z",
                "6475e595ac3946b6a812d865",
                "646bab5cecf584e1be02598a",
                "6475e595ac3946b6a812d868",
                "Peter",
                "Premade_Character_02.png",
                0,
                List.of(),
                null,
                List.of("646bacc568933551792bf3d5"),
                "6475e595ac3946b6a812d863",
                33,
                18,
                0,
                new NPCInfo(false, false, false, false, null, null, null),
                null));

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        final VBox starterSelectionVBox = lookup("#starterSelectionVBox").query();
        assertNotNull(starterSelectionVBox);

        clickOn("#starterSelectionOkButton");
        clickOn("#starterSelectionOkButton");

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);
    }

    @Test
    void testStarter2MonsterNpcDialog() throws InterruptedException {
        Mockito.when(trainerStorageProvider.get().getX()).thenReturn(69);
        Mockito.when(trainerStorageProvider.get().getY()).thenReturn(70);
        Mockito.when(trainerStorageProvider.get().getDirection()).thenReturn(1);

        when(udpEventListenerProvider.get().talk(any(), any())).thenReturn(empty());
        when(trainerStorageProvider.get().getTrainer()).thenReturn(new Trainer(
                "2023-05-30T12:02:57.510Z",
                "2023-05-30T12:01:57.510Z",
                "6475e595ac3946b6a812d865",
                "646bab5cecf584e1be02598a",
                "6475e595ac3946b6a812d868",
                "Peter",
                "Premade_Character_02.png",
                0,
                List.of(),
                null,
                List.of("646bacc568933551792bf3d5"),
                "6475e595ac3946b6a812d863",
                33,
                18,
                0,
                new NPCInfo(false, false, false, false, null, null, null),
                null));

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        final VBox starterSelectionVBox = lookup("#starterSelectionVBox").query();
        assertNotNull(starterSelectionVBox);

        final Label starterSelectionLabel0 = lookup("#starterSelectionLabel").query();
        final String starterSelectionLabelText0 = starterSelectionLabel0.getText();

        clickOn("#arrowRight");

        final Label starterSelectionLabel1 = lookup("#starterSelectionLabel").query();
        final String starterSelectionLabelText1 = starterSelectionLabel1.getText();

        assertNotEquals(starterSelectionLabelText0, starterSelectionLabelText1);


        clickOn("#starterSelectionOkButton");
        clickOn("#starterSelectionOkButton");

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);
    }

    @Test
    void testStarter3MonsterNpcDialog() throws InterruptedException {
        Mockito.when(trainerStorageProvider.get().getX()).thenReturn(69);
        Mockito.when(trainerStorageProvider.get().getY()).thenReturn(70);
        Mockito.when(trainerStorageProvider.get().getDirection()).thenReturn(1);

        when(udpEventListenerProvider.get().talk(any(), any())).thenReturn(empty());
        when(trainerStorageProvider.get().getTrainer()).thenReturn(new Trainer(
                "2023-05-30T12:02:57.510Z",
                "2023-05-30T12:01:57.510Z",
                "6475e595ac3946b6a812d865",
                "646bab5cecf584e1be02598a",
                "6475e595ac3946b6a812d868",
                "Peter",
                "Premade_Character_02.png",
                0,
                List.of(),
                null,
                List.of("646bacc568933551792bf3d5"),
                "6475e595ac3946b6a812d863",
                33,
                18,
                0,
                new NPCInfo(false, false, false, false, null, null, null),
                null));

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        final VBox starterSelectionVBox = lookup("#starterSelectionVBox").query();
        assertNotNull(starterSelectionVBox);

        final Label starterSelectionLabel0 = lookup("#starterSelectionLabel").query();
        final String starterSelectionLabelText0 = starterSelectionLabel0.getText();

        clickOn("#arrowLeft");

        final Label starterSelectionLabel2 = lookup("#starterSelectionLabel").query();
        final String starterSelectionLabelText2 = starterSelectionLabel2.getText();

        assertNotEquals(starterSelectionLabelText0, starterSelectionLabelText2);

        clickOn("#starterSelectionOkButton");
        clickOn("#starterSelectionOkButton");

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);
    }

    @Test
    void testClerkDialog() throws InterruptedException {
        // test will be expanded after implementing the rest of the features
        Mockito.when(trainerStorageProvider.get().getX()).thenReturn(100);
        Mockito.when(trainerStorageProvider.get().getY()).thenReturn(102);
        Mockito.when(trainerStorageProvider.get().getDirection()).thenReturn(1);

        ItemMenuController itemMenuController = mock(ItemMenuController.class);
        when(itemMenuControllerProvider.get()).thenReturn(itemMenuController);
        doNothing().when(itemMenuController).init(any(), any(), any(), any(), any(), any(), any());

        Button close = new Button("Close");
        close.setOnAction(event -> {
            StackPane stackPane = lookup("#stackPane").query();
            VBox itemMenuBox = lookup("#itemMenuBox").query();
            stackPane.getChildren().remove(stackPane.getChildren().size() - 1);
            itemMenuBox.setVisible(false);
            itemMenuBox.toBack();
            ingameController.buttonsDisable(false);
        });
        when(itemMenuController.render()).thenReturn(close);


        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        clickOn("Leave");

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        type(KeyCode.E);
        Thread.sleep(30);

        type(KeyCode.E);
        Thread.sleep(30);

        type(KeyCode.E);
        Thread.sleep(30);

        type(KeyCode.E);
        Thread.sleep(30);

        clickOn("Buy");
        clickOn(close);

        type(KeyCode.E);
        Thread.sleep(30);

        type(KeyCode.E);
        Thread.sleep(30);

        type(KeyCode.E);
        Thread.sleep(30);

        type(KeyCode.E);
        Thread.sleep(30);

        clickOn("Sell");
        clickOn(close);

    }

    /*          no idea how to make the inventory testing work. I think if the inventory itself would be tested,
                I could get *inspiration* there, but the inventory test is a requirement before these tests I'd say
    @Test
    void testBuyItem() throws InterruptedException {
        Mockito.when(trainerStorageProvider.get().getX()).thenReturn(100);
        Mockito.when(trainerStorageProvider.get().getY()).thenReturn(102);
        Mockito.when(trainerStorageProvider.get().getDirection()).thenReturn(1);

        ResourceBundle bundle = ResourceBundle.getBundle("de/uniks/stpmon/team_m/lang/lang", Locale.forLanguageTag("en"));
        Preferences preferences = Preferences.userNodeForPackage(ItemMenuController.class);
        ItemMenuController itemMenuController = new ItemMenuController();
        itemMenuController.setValues(bundle, preferences, null, ingameController, app);

        when(itemMenuControllerProvider.get()).thenReturn(itemMenuController);
        when(presetsService.getItems()).thenReturn(Observable.just(
                List.of()
        ));

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        press(KeyCode.E);
        release(KeyCode.E);
        Thread.sleep(30);

        Thread.sleep(100000000);
    }
*/
    @Test
    void testOtherTrainerInEncounter() {
        Mockito.when(trainerStorageProvider.get().getX()).thenReturn(200);
        Mockito.when(trainerStorageProvider.get().getY()).thenReturn(201);
        Mockito.when(trainerStorageProvider.get().getDirection()).thenReturn(1);

        when(udpEventListenerProvider.get().talk(any(), any())).thenReturn(empty());

        type(KeyCode.E);
        sleep(30);

        type(KeyCode.E);
        sleep(30);

        clickOn("Leave");

        type(KeyCode.E);
        sleep(30);

        type(KeyCode.E);
        sleep(30);

        clickOn("Join encounter");

    }

    @Test
    void testgetUserTrainerY() {
        when(ingameController.getUserTrainerY()).thenReturn(13);

        int y = ingameController.getUserTrainerY();
        assertEquals(13, y);

    }

    @Test
    void testCoinsEarned() {
        ingameController.setCoinsEarned(true);
        assertTrue(ingameController.getCoinsEarned());

    }

    @Test
    void CoinsAmount() {
        ingameController.setCoinsAmount(100);
        assertEquals(100, ingameController.getCoinsAmount());
    }
}
