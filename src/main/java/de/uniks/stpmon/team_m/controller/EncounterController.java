package de.uniks.stpmon.team_m.controller;

import de.uniks.stpmon.team_m.controller.subController.*;
import de.uniks.stpmon.team_m.dto.*;
import de.uniks.stpmon.team_m.service.*;
import de.uniks.stpmon.team_m.utils.*;
import de.uniks.stpmon.team_m.ws.EventListener;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import javax.inject.Inject;
import javax.inject.Provider;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;

import static de.uniks.stpmon.team_m.Constants.*;
import static de.uniks.stpmon.team_m.Constants.BallType.*;
import static de.uniks.stpmon.team_m.Constants.SoundEffect.*;


public class EncounterController extends Controller {
    private final List<Controller> subControllers = new ArrayList<>();
    private final List<AbilityDto> abilityDtos = new ArrayList<>();
    public final HashMap<Integer, ItemTypeDto> itemTypeDtos = new HashMap<>();
    private final HashMap<String, Opponent> opponentsUpdate = new HashMap<>();
    private final HashMap<String, Opponent> opponentsDelete = new HashMap<>();
    private final HashMap<String, Boolean> monsterInEncounterHashMap = new HashMap<>();
    private final HashMap<String, Boolean> resultLevelUpHashMap = new HashMap<>();
    private final HashMap<String, Boolean> resultEvolvedHashMap = new HashMap<>();
    private final HashMap<String, Monster> oldMonsterHashMap = new HashMap<>();
    private final HashMap<String, ArrayList<Integer>> newAbilitiesHashMap = new HashMap<>();
    private final HashMap<String, EncounterOpponentController> encounterOpponentControllerHashMap = new HashMap<>();
    private final HashMap<String, Boolean> monsterInTeamHashMap = new HashMap<>();
    private final DecimalFormat formatter = new DecimalFormat("#,###.0");
    @FXML
    public StackPane rootStackPane;
    @FXML
    public Text battleDialogText;
    @FXML
    public TextFlow battleDialogTextFlow;
    @FXML
    public VBox battleMenuVBox;
    @FXML
    public VBox battleBackgroundVBox;
    @FXML
    public HBox enemyHBox;
    @FXML
    public HBox teamHBox;
    @Inject
    EncounterOpponentsService encounterOpponentsService;
    @Inject
    RegionEncountersService regionEncountersService;
    @Inject
    TrainersService trainersService;
    @Inject
    MonstersService monstersService;
    @Inject
    PresetsService presetsService;
    @Inject
    TrainerItemsService trainerItemsService;
    @Inject
    Provider<IngameController> ingameControllerProvider;
    @Inject
    Provider<EventListener> eventListener;
    @Inject
    EncounterOpponentStorage encounterOpponentStorage;
    @Inject
    BattleMenuController battleMenuController;
    @Inject
    EncounterResultController encounterResultController;
    @Inject
    AbilitiesMenuController abilitiesMenuController;
    @Inject
    Provider<TrainerStorage> trainerStorageProvider;
    @Inject
    Provider<ItemStorage> itemStorageProvider;
    @Inject
    Provider<MonsterStorage> monsterStorageProvider;
    @Inject
    Provider<MonstersDetailController> monstersDetailControllerProvider;
    @Inject
    Provider<ChangeMonsterListController> changeMonsterListControllerProvider;
    @Inject
    Provider<LevelUpController> levelUpControllerProvider;
    @Inject
    Provider<CaughtMonsterController> caughtMonsterControllerProvider;
    @Inject
    Provider<ItemMenuController> itemMenuControllerProvider;

    private EncounterOpponentController enemy1Controller;
    private EncounterOpponentController enemy2Controller;
    private EncounterOpponentController ownTrainerController;
    private EncounterOpponentController coopTrainerController;
    private int opponentsSize;
    private String regionId;
    private String encounterId;
    private String trainerId;
    private int repeatedTimes = 0;
    private int currentMonsterIndex = 0;
    private int deleteOpponents = 0;
    private ImageView ballImageView;
    private BallType selectedBallType;
    private boolean monsterCaught = false;
    public Monster caughtMonster;
    public MonsterTypeDto caughtMonsterType;
    public Image enemyMonsterImage;


    @Inject
    public EncounterController() {
    }

    public void init() {
        super.init();
        opponentsSize = encounterOpponentStorage.getEncounterSize();
        regionId = trainerStorageProvider.get().getRegion()._id();
        encounterId = encounterOpponentStorage.getEncounterId();
        trainerId = trainerStorageProvider.get().getTrainer()._id();
        listenToOpponents(encounterId);
        battleMenuController.setValues(resources, preferences, resourceBundleProvider, battleMenuController, app);
        battleMenuController.init();
        subControllers.add(battleMenuController);

        List<String> monsterInTeam = trainerStorageProvider.get().getTrainer().team();
        for (String monsterId : monsterInTeam) {
            monsterInTeamHashMap.put(monsterId, false);
        }

        if (!GraphicsEnvironment.isHeadless() && !AudioService.getInstance().checkMuted()) {
            AudioService.getInstance().stopSound();
            AudioService.getInstance().playSound(FIGHT_SOUND);
            AudioService.getInstance().setCurrentSound(FIGHT_SOUND);
            AudioService.getInstance().setVolume(preferences.getDouble("volume", AudioService.getInstance().getVolume()));
        }
    }

    public String getTitle() {
        return resources.getString("ENCOUNTER");
    }

    public Parent render() {
        Parent parent = super.render();
        // init battle menu
        battleMenuController.init(this, encounterOpponentStorage, app);
        battleMenuVBox.getChildren().add(battleMenuController.render());
        battleMenuController.onFleeButtonClick = this::onFleeButtonClick;

        // Init opponent controller for own trainer
        ownTrainerController = new EncounterOpponentController();
        Opponent selfOpponent = encounterOpponentStorage.getSelfOpponent();
        checkMoveAlreadyUsed(selfOpponent); // für Encounter wiederherstellen
        ownTrainerController.init(selfOpponent, false, false, true, false, true, this);
        encounterOpponentControllerHashMap.put(selfOpponent._id(), ownTrainerController);
        Parent ownTrainerParent = ownTrainerController.render();
        //HBox.setHgrow(ownTrainerParent, javafx.scene.layout.Priority.ALWAYS);

        //showMyMonster
        showTeamMonster(ownTrainerController, encounterOpponentStorage.getSelfOpponent());
        monsterInEncounterHashMap.put(selfOpponent._id(), false);
        // showMySprite
        ImageView sprite = ownTrainerController.getTrainerImageView();
        setTrainerSpriteImageView(trainerStorageProvider.get().getTrainer(), sprite, TRAINER_DIRECTION_UP);

        disposables.add(monstersService.getMonster(regionId, trainerId, encounterOpponentStorage.getSelfOpponent().monster()).observeOn(FX_SCHEDULER).subscribe(monster -> {
            oldMonsterHashMap.put(encounterOpponentStorage.getSelfOpponent()._id(), monster);
            encounterOpponentStorage.addCurrentMonsters(encounterOpponentStorage.getSelfOpponent()._id(), monster);
        }));
        newAbilitiesHashMap.put(encounterOpponentStorage.getSelfOpponent()._id(), new ArrayList<>());
        resultLevelUpHashMap.put(encounterOpponentStorage.getSelfOpponent()._id(), false);
        resultEvolvedHashMap.put(encounterOpponentStorage.getSelfOpponent()._id(), false);

        if (encounterOpponentStorage.isTwoMonster()) {
            disposables.add(monstersService.getMonster(regionId, trainerId, encounterOpponentStorage.getCoopOpponent().monster()).observeOn(FX_SCHEDULER).subscribe(monster -> {
                oldMonsterHashMap.put(encounterOpponentStorage.getCoopOpponent()._id(), monster);
                encounterOpponentStorage.addCurrentMonsters(encounterOpponentStorage.getCoopOpponent()._id(), monster);
            }));
            newAbilitiesHashMap.put(encounterOpponentStorage.getCoopOpponent()._id(), new ArrayList<>());
            resultLevelUpHashMap.put(encounterOpponentStorage.getCoopOpponent()._id(), false);
            resultEvolvedHashMap.put(encounterOpponentStorage.getCoopOpponent()._id(), false);
        }

        disposables.add(presetsService.getAbilities().observeOn(FX_SCHEDULER).subscribe(abilities -> {
            abilityDtos.addAll(abilities);
            Comparator<AbilityDto> abilityDtoComparator = Comparator.comparingInt(AbilityDto::id);
            abilityDtos.sort(abilityDtoComparator);
        }));

        disposables.add(presetsService.getItems().observeOn(FX_SCHEDULER).subscribe(items -> {
            for (ItemTypeDto itemTypeDto : items) {
                itemTypeDtos.put(itemTypeDto.id(), itemTypeDto);
            }
        }));

        disposables.add(regionEncountersService.getEncounter(regionId, encounterId).observeOn(FX_SCHEDULER).subscribe(encounter -> {
            encounterOpponentStorage.setWild(encounter.isWild());
            if (encounter.isWild()) {
                renderForWild(ownTrainerParent);
                battleMenuController.showFleeButton(true);
            } else {
                if (opponentsSize == 2) {
                    renderFor1vs1(ownTrainerParent);
                } else if (opponentsSize == 3) {
                    if (encounterOpponentStorage.getEnemyOpponents().size() == 2) {
                        renderFor1vs2(ownTrainerParent);
                    } else {
                        renderFor2vs1(ownTrainerParent);
                    }

                } else {
                    renderFor2vs2(ownTrainerParent);
                }
                battleMenuController.showFleeButton(false);
            }
        }, Throwable::printStackTrace));
        return parent;
    }

    public void onFleeButtonClick() {
        rootStackPane.getChildren().add(this.buildFleePopup());
    }

    private void renderForWild(Parent ownTrainerParent) {
        // Wild situation

        // Wild monster (init opponent controller for enemy)
        Opponent enemyOpponent = encounterOpponentStorage.getEnemyOpponents().get(0);
        checkMoveAlreadyUsed(enemyOpponent); // für Encounter wiederherstellen
        enemy1Controller = new EncounterOpponentController();
        enemy1Controller.init(enemyOpponent, true, true, false, false, false, this);
        encounterOpponentControllerHashMap.put(enemyOpponent._id(), enemy1Controller);
        enemyHBox.setPadding(new Insets(0, 360, 0, 0));
        Parent enemy1Parent = enemy1Controller.render();
        enemyHBox.getChildren().add(enemy1Parent);
        targetOpponent(encounterOpponentStorage.getEnemyOpponents().get(0));
        showEnemyMonster(enemy1Controller, encounterOpponentStorage.getEnemyOpponents().get(0), true);
        monsterInEncounterHashMap.put(enemyOpponent._id(), false);

        // Own trainer
        teamHBox.setPadding(new Insets(0, 0, 0, 360));
        teamHBox.getChildren().add(ownTrainerParent);
    }

    private void renderFor1vs1(Parent ownTrainerParent) {
        // 1 vs 1 situation

        // Enemy as a trainer (init opponent controller for the enemy)
        Opponent enemyOpponent = encounterOpponentStorage.getEnemyOpponents().get(0);
        checkMoveAlreadyUsed(enemyOpponent); // für Encounter wiederherstellen
        enemy1Controller = new EncounterOpponentController();
        enemy1Controller.init(enemyOpponent, true, false, false, false, false, this);
        encounterOpponentControllerHashMap.put(enemyOpponent._id(), enemy1Controller);
        Parent enemy1Parent = enemy1Controller.render();
        enemyHBox.setPadding(new Insets(0, 360, 0, 0));
        enemyHBox.getChildren().add(enemy1Parent);
        targetOpponent(encounterOpponentStorage.getEnemyOpponents().get(0));
        showEnemyInfo(enemy1Controller, encounterOpponentStorage.getEnemyOpponents().get(0));
        monsterInEncounterHashMap.put(enemyOpponent._id(), false);

        // Own trainer
        teamHBox.setPadding(new Insets(0, 0, 0, 360));
        teamHBox.getChildren().add(ownTrainerParent);
    }

    private void renderFor1vs2(Parent ownTrainerParent) {
        // 1 vs 2 situation

        // 1st enemy as a trainer
        Opponent enemy1Opponent = encounterOpponentStorage.getEnemyOpponents().get(0);
        checkMoveAlreadyUsed(enemy1Opponent); // für Encounter wiederherstellen
        enemy1Controller = new EncounterOpponentController();
        enemy1Controller.init(enemy1Opponent, true, false, false, true, false, this);
        encounterOpponentControllerHashMap.put(enemy1Opponent._id(), enemy1Controller);
        VBox enemy1Parent = (VBox) enemy1Controller.render();
        HBox.setHgrow(enemy1Parent, javafx.scene.layout.Priority.ALWAYS);
        enemyHBox.getChildren().add(enemy1Parent);
        targetOpponent(encounterOpponentStorage.getEnemyOpponents().get(0));
        showEnemyInfo(enemy1Controller, encounterOpponentStorage.getEnemyOpponents().get(0));
        monsterInEncounterHashMap.put(enemy1Opponent._id(), false);

        // 2nd enemy as a trainer
        Opponent enemy2Opponent = encounterOpponentStorage.getEnemyOpponents().get(1);
        checkMoveAlreadyUsed(enemy2Opponent); // für Encounter wiederherstellen
        enemy2Controller = new EncounterOpponentController();
        enemy2Controller.init(enemy2Opponent, true, false, true, true, false, this);
        encounterOpponentControllerHashMap.put(enemy2Opponent._id(), enemy2Controller);
        VBox enemy2Parent = (VBox) enemy2Controller.render();
        enemyHBox.getChildren().add(enemy2Parent);
        showEnemyInfo(enemy2Controller, encounterOpponentStorage.getEnemyOpponents().get(1));
        monsterInEncounterHashMap.put(enemy2Opponent._id(), false);

        // Own trainer
        teamHBox.setPadding(new Insets(0, 0, 0, 360));
        teamHBox.getChildren().add(ownTrainerParent);
    }

    private void renderFor2vs1(Parent ownTrainerParent) {
        // 2 vs 1 Situation

        // 1st enemy as a trainer
        Opponent enemy1Opponent = encounterOpponentStorage.getEnemyOpponents().get(0);
        checkMoveAlreadyUsed(enemy1Opponent); // für Encounter wiederherstellen
        enemy1Controller = new EncounterOpponentController();
        enemy1Controller.init(enemy1Opponent, true, false, false, false, false, this);
        encounterOpponentControllerHashMap.put(enemy1Opponent._id(), enemy1Controller);
        VBox enemy1Parent = (VBox) enemy1Controller.render();
        HBox.setHgrow(enemy1Parent, javafx.scene.layout.Priority.ALWAYS);
        enemyHBox.getChildren().add(enemy1Parent);
        targetOpponent(encounterOpponentStorage.getEnemyOpponents().get(0));
        showEnemyInfo(enemy1Controller, encounterOpponentStorage.getEnemyOpponents().get(0));
        monsterInEncounterHashMap.put(enemy1Opponent._id(), false);

        // Coop Trainer
        Opponent coopOpponent = encounterOpponentStorage.getCoopOpponent();
        checkMoveAlreadyUsed(coopOpponent); // für Encounter wiederherstellen
        coopTrainerController = new EncounterOpponentController();
        coopTrainerController.init(coopOpponent, false, false, false, false, encounterOpponentStorage.isTwoMonster(), this);
        encounterOpponentControllerHashMap.put(coopOpponent._id(), coopTrainerController);
        Parent coopTrainerParent = coopTrainerController.render();
        HBox.setHgrow(coopTrainerParent, javafx.scene.layout.Priority.ALWAYS);
        teamHBox.getChildren().add(coopTrainerParent);
        if (!encounterOpponentStorage.isTwoMonster()) {
            showCoopImage(coopTrainerController, encounterOpponentStorage.getCoopOpponent());
        }
        showTeamMonster(coopTrainerController, encounterOpponentStorage.getCoopOpponent());
        monsterInEncounterHashMap.put(coopOpponent._id(), false);


        // Own trainer
        teamHBox.getChildren().add(ownTrainerParent);
    }

    private void renderFor2vs2(Parent ownTrainerParent) {
        // 2 vs 2 situation

        // 1st enemy as a trainer
        Opponent enemy1Opponent = encounterOpponentStorage.getEnemyOpponents().get(0);
        checkMoveAlreadyUsed(enemy1Opponent); // für Encounter wiederherstellen
        enemy1Controller = new EncounterOpponentController();
        enemy1Controller.init(enemy1Opponent, true, false, false, true, false, this);
        encounterOpponentControllerHashMap.put(enemy1Opponent._id(), enemy1Controller);
        VBox enemy1Parent = (VBox) enemy1Controller.render();
        enemyHBox.getChildren().add(enemy1Parent);
        targetOpponent(encounterOpponentStorage.getEnemyOpponents().get(0));
        showEnemyInfo(enemy1Controller, encounterOpponentStorage.getEnemyOpponents().get(0));
        monsterInEncounterHashMap.put(enemy1Opponent._id(), false);

        // 2nd enemy as a trainer
        Opponent enemy2Opponent = encounterOpponentStorage.getEnemyOpponents().get(1);
        checkMoveAlreadyUsed(enemy2Opponent); // für Encounter wiederherstellen
        enemy2Controller = new EncounterOpponentController();
        enemy2Controller.init(enemy2Opponent, true, false, true, true, false, this);
        encounterOpponentControllerHashMap.put(enemy2Opponent._id(), enemy2Controller);
        VBox enemy2Parent = (VBox) enemy2Controller.render();
        enemyHBox.getChildren().add(enemy2Parent);
        showEnemyInfo(enemy2Controller, encounterOpponentStorage.getEnemyOpponents().get(1));
        monsterInEncounterHashMap.put(enemy2Opponent._id(), false);

        // Coop Trainer
        Opponent coopOpponent = encounterOpponentStorage.getCoopOpponent();
        checkMoveAlreadyUsed(coopOpponent); // für Encounter wiederherstellen
        coopTrainerController = new EncounterOpponentController();
        coopTrainerController.init(coopOpponent, false, false, false, false, encounterOpponentStorage.isTwoMonster(), this);
        encounterOpponentControllerHashMap.put(coopOpponent._id(), coopTrainerController);
        Parent coopTrainerParent = coopTrainerController.render();
        teamHBox.getChildren().add(coopTrainerParent);
        if (!encounterOpponentStorage.isTwoMonster()) {
            showCoopImage(coopTrainerController, encounterOpponentStorage.getCoopOpponent());
        }
        showTeamMonster(coopTrainerController, encounterOpponentStorage.getCoopOpponent());
        monsterInEncounterHashMap.put(coopOpponent._id(), false);

        // Own trainer
        teamHBox.getChildren().add(ownTrainerParent);
    }

    private void onTargetChange() {
        if (encounterOpponentStorage.getLeastTargetOpponent() != null && encounterOpponentControllerHashMap.containsKey(encounterOpponentStorage.getLeastTargetOpponent()._id())) {
            encounterOpponentControllerHashMap.get(encounterOpponentStorage.getLeastTargetOpponent()._id()).unTarget();
        }
        if (encounterOpponentControllerHashMap.containsKey(encounterOpponentStorage.getTargetOpponent()._id())) {
            encounterOpponentControllerHashMap.get(encounterOpponentStorage.getTargetOpponent()._id()).onTarget();
        }
    }

    public void targetOpponent(Opponent opponent) {
        encounterOpponentStorage.setTargetOpponent(opponent);
        if (encounterOpponentStorage.getLeastTargetOpponent() == null) {
            if (encounterOpponentControllerHashMap.containsKey(opponent._id())) {
                encounterOpponentControllerHashMap.get(opponent._id()).onTarget();
            }
        } else {
            if (encounterOpponentStorage.getLeastTargetOpponent() != opponent && encounterOpponentControllerHashMap.containsKey(opponent._id()) && encounterOpponentControllerHashMap.get(opponent._id()).isMultipleEnemyEncounter) {
                onTargetChange();
            }
        }

    }

    // Hier soll allen Serveranfragen kommen
    private void showEnemyMonster(EncounterOpponentController encounterOpponentController, Opponent opponent, boolean isInit) {
        disposables.add(monstersService.getMonster(regionId, opponent.trainer(), opponent.monster()).observeOn(FX_SCHEDULER).subscribe(monster -> {
            encounterOpponentController.setLevelLabel("LVL " + monster.level()).setHealthBarValue((double) monster.currentAttributes().health() / monster.attributes().health());
            encounterOpponentStorage.addCurrentMonsters(opponent._id(), monster);
            caughtMonster = monster;

            listenToMonster(opponent.trainer(), monster._id(), encounterOpponentController, opponent);
            for (String effect : STATUS_EFFECTS) {
                encounterOpponentController.showStatus(effect, false);
            }
            for (String effect : monster.status()) {
                encounterOpponentController.showStatus(effect, true);
            }
            //write monster name
            disposables.add(presetsService.getMonster(monster.type()).observeOn(FX_SCHEDULER).subscribe(m -> {
                caughtMonsterType = m;
                encounterOpponentController.setMonsterNameLabel(m.name());
                encounterOpponentStorage.addCurrentMonsterType(opponent._id(), m);
                // only show the description if it is at start
                if (encounterOpponentStorage.isWild() && isInit) {
                    battleDialogText.setText(resources.getString("ENCOUNTER_DESCRIPTION_BEGIN") + " " + m.name());
                }
            }, Throwable::printStackTrace));
            if (monsterStorageProvider.get().imagesAlreadyFetched()) {
                enemyMonsterImage = monsterStorageProvider.get().getMonsterImage(monster.type());
                encounterOpponentController.setMonsterImage(enemyMonsterImage);
            }
        }, Throwable::printStackTrace));
    }

    private void showEnemyInfo(EncounterOpponentController encounterOpponentController, Opponent opponent) {
        if (opponent != null) {
            // Monster
            showEnemyMonster(encounterOpponentController, opponent, true);

            // Trainer Sprite
            disposables.add(trainersService.getTrainer(regionId, opponent.trainer()).observeOn(FX_SCHEDULER).subscribe(trainer -> {
                battleDialogText.setText(resources.getString("ENCOUNTER_DESCRIPTION_BEGIN") + " " + trainer.name());
                ImageView opponentTrainer = encounterOpponentController.getTrainerImageView();
                setTrainerSpriteImageView(trainer, opponentTrainer, TRAINER_DIRECTION_DOWN);
            }, Throwable::printStackTrace));
        }
    }

    private void showTeamMonster(EncounterOpponentController encounterOpponentController, Opponent opponent) {
        // Monster
        disposables.add(monstersService.getMonster(regionId, opponent.trainer(), opponent.monster()).observeOn(FX_SCHEDULER).subscribe(monster -> {
            encounterOpponentStorage.addCurrentMonsters(opponent._id(), monster);
            listenToMonster(opponent.trainer(), opponent.monster(), encounterOpponentController, opponent);
            double currentHealth = monster.currentAttributes().health();
            double maxHealth = monster.attributes().health();
            encounterOpponentController.setLevelLabel("LVL " + monster.level())
                    .setExperienceBarValue((double) monster.experience() / requiredExperience(monster.level() + 1))
                    .setHealthBarValue((double) monster.currentAttributes().health() / monster.attributes().health())
                    .setHealthLabel(formatter.format(currentHealth) + "/" + formatter.format(maxHealth) + " HP");
            for (String effect : STATUS_EFFECTS) {
                encounterOpponentController.showStatus(effect, false);
            }
            for (String effect : monster.status()) {
                encounterOpponentController.showStatus(effect, true);
            }
            //write monster name
            disposables.add(presetsService.getMonster(monster.type()).observeOn(FX_SCHEDULER).subscribe(m -> {
                encounterOpponentController.setMonsterNameLabel(m.name());
                encounterOpponentStorage.addCurrentMonsterType(opponent._id(), m);
            }, Throwable::printStackTrace));
            if (monsterStorageProvider.get().imagesAlreadyFetched()) {
                Image myMonsterImage = monsterStorageProvider.get().getMonsterImage(monster.type());
                encounterOpponentController.setMonsterImage(myMonsterImage);
            }
        }, Throwable::printStackTrace));
    }

    private void showCoopImage(EncounterOpponentController encounterOpponentController, Opponent opponent) {
        disposables.add(trainersService.getTrainer(regionId, opponent.trainer()).observeOn(FX_SCHEDULER).subscribe(trainer -> {
            ImageView opponentTrainer = encounterOpponentController.getTrainerImageView();
            setTrainerSpriteImageView(trainer, opponentTrainer, TRAINER_DIRECTION_UP);
        }, Throwable::printStackTrace));
    }

    public int requiredExperience(int currentLevel) {
        return (int) (Math.pow(currentLevel, 3) - Math.pow(currentLevel - 1, 3));
    }

    public void showMonsterDetailsInEncounter(Opponent currentOpponent, EncounterOpponentController encounterOpponentController) {
        VBox monsterDetailVBox = new VBox();
        monsterDetailVBox.setAlignment(Pos.CENTER);
        MonstersDetailController monstersDetailController = monstersDetailControllerProvider.get();
        Monster monster = encounterOpponentStorage.getCurrentMonsters(currentOpponent._id());
        MonsterTypeDto monsterTypeDto = encounterOpponentStorage.getCurrentMonsterType(currentOpponent._id());

        monstersDetailController.initFromBattleMenu(this, monsterDetailVBox, monster, monsterTypeDto, encounterOpponentController.getMonsterImage(), resources, presetsService);
        monsterDetailVBox.getChildren().add(monstersDetailController.render());
        rootStackPane.getChildren().add(monsterDetailVBox);
        monsterDetailVBox.requestFocus();
    }

    public void showMonsterDetails(Monster monster, MonsterTypeDto monsterTypeDto, Image monsterImage) {
        VBox monsterDetailVBox = new VBox();
        monsterDetailVBox.setAlignment(Pos.CENTER);
        MonstersDetailController monstersDetailController = monstersDetailControllerProvider.get();

        monstersDetailController.initFromBattleMenu(this, monsterDetailVBox, monster, monsterTypeDto, monsterImage, resources, presetsService);
        monsterDetailVBox.getChildren().add(monstersDetailController.render());
        rootStackPane.getChildren().add(monsterDetailVBox);
        monsterDetailVBox.requestFocus();
    }

    public void showChangeMonsterList() {
        VBox monsterListVBox = new VBox();
        monsterListVBox.setMinWidth(600);
        monsterListVBox.setMinHeight(410);
        monsterListVBox.setAlignment(Pos.CENTER);
        ChangeMonsterListController changeMonsterListController = changeMonsterListControllerProvider.get();
        changeMonsterListController.init(this, monsterListVBox, ingameControllerProvider.get(), null, null);
        monsterListVBox.getChildren().add(changeMonsterListController.render());
        rootStackPane.getChildren().add(monsterListVBox);
        monsterListVBox.requestFocus();
    }

    @Override
    public void destroy() {
        super.destroy();
        for (var controller : subControllers) {
            if (controller != null) {
                controller.destroy();
            }
        }
        encounterOpponentControllerHashMap.values().forEach(Controller::destroy);

    }

    public void listenToOpponents(String encounterId) {
        disposables.add(eventListener.get().listen("encounters." + encounterId + ".trainers.*.opponents.*.*", Opponent.class).observeOn(FX_SCHEDULER).subscribe(event -> {
            final Opponent opponent = event.data();
            if (event.suffix().contains("updated")) {
                updateOpponent(opponent);
            } else if (event.suffix().contains("deleted")) {
                if (deleteOpponents >= 0) {
                    opponentsDelete.put(opponent._id(), opponent);
                }
                deleteOpponents++;
                if (opponentsDelete.size() >= encounterOpponentStorage.getEncounterSize()) {
                    PauseTransition pause = new PauseTransition(Duration.millis((double) pauseDuration / 2));
                    pause.setOnFinished(evt -> showResult());
                    pause.play();
                }
            } else if (event.suffix().contains("created")) {
                if (encounterOpponentStorage.getEncounterSize() == 4) {
                    deleteOpponents--;
                }
                disposables.add(encounterOpponentsService.getEncounterOpponents(trainerStorageProvider.get().getRegion()._id(), encounterId).observeOn(FX_SCHEDULER).subscribe(opponents -> {
                    teamHBox.getChildren().clear();
                    ingameControllerProvider.get().initEncounterOpponentStorage(opponents);
                    for (Opponent o : opponents) {
                        if (o.isAttacker() == encounterOpponentStorage.isAttacker()) {
                            if (o.trainer().equals(trainerId)) {
                                encounterOpponentStorage.setSelfOpponent(o);
                                ownTrainerController = new EncounterOpponentController();
                                ownTrainerController.init(o, false, false, true, false, encounterOpponentStorage.isTwoMonster(), this);
                                encounterOpponentControllerHashMap.put(o._id(), ownTrainerController);
                            } else {
                                encounterOpponentStorage.setCoopOpponent(o);
                                coopTrainerController = new EncounterOpponentController();
                                coopTrainerController.init(o, false, false, false, false, encounterOpponentStorage.isTwoMonster(), this);
                                encounterOpponentControllerHashMap.put(o._id(), coopTrainerController);
                            }
                            showTeamMonster(encounterOpponentControllerHashMap.get(o._id()), o);
                            showCoopImage(encounterOpponentControllerHashMap.get(o._id()), o);
                            monsterInEncounterHashMap.put(o._id(), false);
                        }
                    }
                    teamHBox.getChildren().add(coopTrainerController.render());
                    teamHBox.getChildren().add(ownTrainerController.render());
                }, Throwable::printStackTrace));
            }
        }, Throwable::printStackTrace));
    }

    // manage the response of all the opponents (include the move and all results after the move, there are 2 Updates per opponent)
    private void updateOpponent(Opponent opponent) {
        if (opponent.move() != null) {
            opponentsUpdate.put(opponent._id() + "Move", opponent);
        } else {
            if (opponent._id().equals(encounterOpponentStorage.getSelfOpponent()._id())) {
                encounterOpponentStorage.setSelfOpponent(opponent);
            } else if (opponent.isAttacker() != encounterOpponentStorage.isAttacker()) {
                encounterOpponentStorage.getEnemyOpponents().removeIf(o -> o._id().equals(opponent._id()));
                encounterOpponentStorage.addEnemyOpponent(opponent);

            } else {
                encounterOpponentStorage.setCoopOpponent(opponent);
            }
            opponentsUpdate.put(opponent._id() + "Results", opponent);
            if (opponent.monster() == null) {
                monsterInEncounterHashMap.put(opponent._id(), true);
                if (opponent.trainer().equals(trainerId)) {
                    yourMonsterDefeated(opponent._id());
                }
            } else if (monsterInEncounterHashMap.get(opponent._id()) != null) {
                if (monsterInEncounterHashMap.get(opponent._id())) {
                    if (opponent.isAttacker() != encounterOpponentStorage.isAttacker()) {
                        showEnemyMonster(encounterOpponentControllerHashMap.get(opponent._id()), opponent, false);
                        updateDescription(resources.getString("ENEMY.CHANGED.MONSTER") + "\n", false);
                    } else {
                        showTeamMonster(encounterOpponentControllerHashMap.get(opponent._id()), opponent);
                        if (opponent.trainer().equals(trainerId)) {
                            updateDescription(resources.getString("YOU.CHANGED.MONSTER") + "\n", false);
                        } else {
                            updateDescription(resources.getString("ALLY.CHANGED.MONSTER") + "\n", false);
                        }
                    }
                    monsterInEncounterHashMap.put(opponent._id(), false);
                    opponentsUpdate.remove(opponent._id() + "Results");
                }
            }
        }

        // this magic number is two time the size of oppenents in this encounter
        if (opponentsUpdate.size() >= (encounterOpponentStorage.getEncounterSize() - deleteOpponents) * 2) {
            if (repeatedTimes == 0) {
                writeBattleDescription(opponentsUpdate);
            }
            repeatedTimes++;
        }
    }

    public void writeBattleDescription(HashMap<String, Opponent> forDescription) {
        updateDescription(EMPTY_STRING, true);
        List<String> opponentsInStorage = encounterOpponentStorage.getOpponentsInStorage();
        for (String opponentId : opponentsInStorage) {
            Opponent o = forDescription.get(opponentId + "Move");
            if (o != null) {
                Move move = o.move();
                if (move instanceof AbilityMove abilityMove) {
                    if (o.trainer().equals(trainerStorageProvider.get().getTrainer()._id())) {
                        updateDescription(resources.getString("YOU.USED") + " " + abilityDtos.get(abilityMove.ability() - 1).name() + ". ", false);
                    } else if (o.isAttacker() == encounterOpponentStorage.isAttacker()) {
                        updateDescription(resources.getString("COOP.USED") + " " + abilityDtos.get(abilityMove.ability() - 1).name() + ". ", false);
                    } else {
                        updateDescription(resources.getString("ENEMY.USED") + " " + abilityDtos.get(abilityMove.ability() - 1).name() + ". ", false);
                    }
                }

                if (move instanceof ChangeMonsterMove) {
                    if (o.trainer().equals(trainerStorageProvider.get().getTrainer()._id())) {
                        updateDescription(resources.getString("YOU.CHANGED.MONSTER") + "\n", false);
                        showTeamMonster(encounterOpponentControllerHashMap.get(o._id()), o);
                    } else if (o.isAttacker() != encounterOpponentStorage.isAttacker()) {
                        updateDescription(resources.getString("ENEMY.CHANGED.MONSTER") + "\n", false);
                        showEnemyMonster(encounterOpponentControllerHashMap.get(o._id()), o, false);
                    } else {
                        updateDescription(resources.getString("ALLY.CHANGED.MONSTER") + "\n", false);
                        showTeamMonster(encounterOpponentControllerHashMap.get(o._id()), o);
                    }
                    listenToMonster(o.trainer(), o.monster(), encounterOpponentControllerHashMap.get(o._id()), o);
                }

                if (move instanceof UseItemMove useItemMove) {
                    if (o.trainer().equals(trainerStorageProvider.get().getTrainer()._id())) {
                        updateDescription(resources.getString("YOU.USED") + " " + itemTypeDtos.get(useItemMove.item()).name() + ". ", false);
                    } else if (o.isAttacker() == encounterOpponentStorage.isAttacker()) {
                        updateDescription(resources.getString("COOP.USED") + " " + itemTypeDtos.get(useItemMove.item()).name() + ". ", false);
                    } else {
                        updateDescription(resources.getString("ENEMY.USED") + " " + itemTypeDtos.get(useItemMove.item()).name() + ". ", false);
                    }
                }
            }

            Opponent oResults = forDescription.get(opponentId + "Results");
            if (oResults != null) {
                if (oResults.results() != null) {
                    for (Result r : oResults.results()) {

                        switch (r.type()) {
                            case ABILITY_SUCCESS -> {
                                updateDescription(abilityDtos.get(r.ability() - 1).name() + " " + resources.getString("IS") + " " + r.effectiveness() + ".\n", false);
                                if (!GraphicsEnvironment.isHeadless()) {
                                    AudioService.getInstance().playEffect(ATTACK, ingameControllerProvider.get());
                                }
                            }
                            case MONSTER_LEVELUP -> {
                                if (oResults.trainer().equals(trainerStorageProvider.get().getTrainer()._id())) {
                                    resultLevelUpHashMap.put(oResults._id(), true);
                                }
                            }
                            case MONSTER_LEARNED -> {
                                if (oResults.trainer().equals(trainerStorageProvider.get().getTrainer()._id())) {
                                    if (newAbilitiesHashMap.get(oResults._id()) == null) {
                                        newAbilitiesHashMap.put(oResults._id(), new ArrayList<>(List.of(r.ability())));
                                    } else {
                                        newAbilitiesHashMap.get(oResults._id()).add(r.ability());
                                    }
                                    updateDescription(resources.getString("YOUR.MONSTER.LEARNED") + " " + abilityDtos.get(r.ability() - 1).name() + ".\n", false);
                                }
                            }
                            case MONSTER_EVOLVED -> {
                                if (oResults.trainer().equals(trainerStorageProvider.get().getTrainer()._id())) {
                                    resultEvolvedHashMap.put(oResults._id(), true);
                                }
                            }
                            case TARGET_DEFEATED -> {
                                if (oResults.trainer().equals(trainerStorageProvider.get().getTrainer()._id())) {

                                    enemyMonsterDefeated();
                                    updateDescription(resources.getString("ENEMY.DEFEATED") + "\n", false);
                                } else if (oResults.isAttacker() == encounterOpponentStorage.isAttacker()) {

                                    updateDescription(resources.getString("ENEMY.DEFEATED") + "\n", false);
                                } else {

                                    updateDescription(resources.getString("TEAM.DEFEATED") + "\n", false);
                                }
                            }
                            case STATUS_ADDED -> {
                                EncounterOpponentController encounterOpponentController = encounterOpponentControllerHashMap.get(oResults._id());
                                encounterOpponentController.showStatus(r.status(), true);

                            }
                            case STATUS_REMOVED -> {
                                EncounterOpponentController encounterOpponentController = encounterOpponentControllerHashMap.get(oResults._id());
                                encounterOpponentController.showStatus(r.status(), false);
                            }
                            case MONSTER_CAUGHT -> {
                                throwSuccessfulMonBall();
                                monsterCaught = true;
                                updateDescription("3...\n", true);
                                PauseTransition pause = new PauseTransition(Duration.seconds(1));
                                PauseTransition pause2 = new PauseTransition(Duration.seconds(1));
                                PauseTransition pause3 = new PauseTransition(Duration.seconds(1));
                                pause.setOnFinished(evt -> {
                                    updateDescription("2...\n", true);
                                    pause2.play();
                                });
                                pause2.setOnFinished(evt -> {
                                    updateDescription("1...\n", true);
                                    pause3.play();
                                });
                                pause3.setOnFinished(evt -> updateDescription(resources.getString("MONSTER.CAUGHT") + "\n", false));

                                pause.play();
                            }
                            case ITEM_SUCCESS -> {
                                List<Integer> ballIds = new ArrayList<>(List.of(10, 11, 12, 13, 14, 15, 16, 17));
                                if (ballIds.contains(r.item())) {
                                    throwFailedMonBall();
                                } else {
                                    updateDescription(resources.getString("USE.OF") + " " + itemTypeDtos.get(r.item()).name() + " " + resources.getString("IS") + " " + resources.getString("SUCCEED") + ".\n", false);
                                }
                            }
                            case ITEM_FAILED ->
                                    updateDescription(resources.getString("USE.OF") + " " + itemTypeDtos.get(r.item()).name() + " " + resources.getString("IS") + " " + resources.getString("FAILED") + ".\n", false);
                        }
                    }
                }
            }

        }
        opponentsUpdate.clear();
        currentMonsterIndex = 0;
    }

    private void checkMoveAlreadyUsed(Opponent opponent) {
        if (opponent.move() != null) {
            updateOpponent(opponent);
        }
    }

    public void updateDescription(String information, boolean isUpdated) {
        if (isUpdated) {
            battleDialogText.setText(information);
        } else {
            if (battleDialogText.getText().contains(information)) {
                return;
            }
            battleDialogText.setText(battleDialogText.getText() + information);
        }
    }

    private void listenToMonster(String trainerId, String monsterId, EncounterOpponentController encounterOpponentController, Opponent opponent) {
        disposables.add(eventListener.get().listen("trainers." + trainerId + ".monsters." + monsterId + ".*", Monster.class).observeOn(FX_SCHEDULER).subscribe(event -> {
            final Monster monster = event.data();
            if (monster != null) {
                if (monster._id().equals(encounterOpponentStorage.getSelfOpponent().monster())) {
                    if (monster.currentAttributes().health() / monster.attributes().health() <= 0.2) {
                        if (!GraphicsEnvironment.isHeadless()) {
                            AudioService.getInstance().playEffect(LOW_HEALTH, ingameControllerProvider.get());
                        }
                    }
                }
                if (event.suffix().contains("updated")) {
                    double currentHealth = monster.currentAttributes().health();
                    double maxHealth = monster.attributes().health();
                    if (!GraphicsEnvironment.isHeadless()) {
                        AnimationBuilder.buildShakeAnimation(encounterOpponentController.monsterImageView, 50, 3, 1).play();
                        if (currentHealth > 0) {
                            AnimationBuilder.buildProgressBarAnimation(encounterOpponentController.HealthBar, 1500, monster.currentAttributes().health() / monster.attributes().health()).play();
                        } else {
                            encounterOpponentController.setHealthBarValue(monster.currentAttributes().health() / monster.attributes().health());
                        }
                    } else {
                        encounterOpponentController.setHealthBarValue(monster.currentAttributes().health() / monster.attributes().health());
                    }
                    encounterOpponentController
                            .setHealthLabel(formatter.format(currentHealth) + "/" + formatter.format(maxHealth) + " HP")
                            .setLevelLabel("LVL " + monster.level())
                            .setExperienceBarValue((double) monster.experience() / requiredExperience(monster.level()));
                    if (trainerId.equals(trainerStorageProvider.get().getTrainer()._id())) {
                        encounterOpponentStorage.addCurrentMonsters(opponent._id(), monster);
                        // if health is 0, then add to the team the information that the monster is died.
                        // if the type of the monster changed, then make a server call and update in the opponent storage
                        if (currentHealth == 0) {
                            monsterInTeamHashMap.put(monster._id(), true);
                        }
                    }
                }
            }
        }, Throwable::printStackTrace));
    }

    private void showResult() {
        List<String> opponentsInStorage = encounterOpponentStorage.getOpponentsInStorage();
        for (String id : opponentsInStorage) {
            Opponent o = opponentsDelete.get(id);
            if (monsterCaught) {
                PauseTransition pause = new PauseTransition(Duration.seconds(5));
                pause.play();
            } else if (o.monster() == null) {
                if (o.trainer().equals(trainerStorageProvider.get().getTrainer()._id())) {
                    showResultPopUp(resources.getString("YOU.FAILED"), false);
                    if (!GraphicsEnvironment.isHeadless()) {
                        AudioService.getInstance().playEffect(LOSE, ingameControllerProvider.get());
                    }
                } else {
                    showResultPopUp(resources.getString("YOU.WON"), true);
                    if (!GraphicsEnvironment.isHeadless()) {
                        AudioService.getInstance().playEffect(WIN, ingameControllerProvider.get());
                    }
                }
            }
        }
    }

    private void showResultPopUp(String string, boolean isWin) {
        Opponent selfOpponent = encounterOpponentStorage.getSelfOpponent();
        if (selfOpponent.coins() != 0 && isWin) {
            encounterResultController.setCoinsAmount(selfOpponent.coins());
            encounterResultController.setCoinsEarned(true);
        }
        VBox resultBox = new VBox();
        resultBox.setAlignment(Pos.CENTER);
        encounterResultController.init(app);
        resultBox.getChildren().add(encounterResultController.render());
        rootStackPane.getChildren().add(resultBox);
        resultBox.requestFocus();
        buttonsDisableEncounter(true);
        encounterResultController.setInformationText(string);
    }

    public void showIngameController() {
        destroy();
        if (trainerStorageProvider.get().getMonsters() != null) {
            trainerStorageProvider.get().getTrainer().team().stream().flatMap(teamMonsterId -> trainerStorageProvider.get().getMonsters().stream().filter(trainerMonster -> teamMonsterId.equals(trainerMonster._id())).filter(trainerMonster -> (double) trainerMonster.currentAttributes().health() / trainerMonster.attributes().health() <= 0.2)).forEach(trainerMonster -> this.ingameControllerProvider.get().showLowHealthNotification());
        }
        app.show(ingameControllerProvider.get());
    }

    public void showAbilities() {
        battleMenuVBox.getChildren().clear();
        Monster monster = null;
        Opponent opponent = encounterOpponentStorage.getSelfOpponent();
        subControllers.add(abilitiesMenuController);

        if (encounterOpponentStorage.isTwoMonster()) {
            if (currentMonsterIndex % 2 == 0 && encounterOpponentStorage.getCoopOpponent().monster() != null) {
                opponent = encounterOpponentStorage.getCoopOpponent();
            } else if (currentMonsterIndex % 2 == 1 && encounterOpponentStorage.getSelfOpponent().monster() != null) {
                opponent = encounterOpponentStorage.getSelfOpponent();
            } else if (encounterOpponentStorage.getSelfOpponent().monster() == null) {
                opponent = encounterOpponentStorage.getCoopOpponent();
            } else if (encounterOpponentStorage.getCoopOpponent().monster() == null) {
                opponent = encounterOpponentStorage.getSelfOpponent();
            }
            monster = encounterOpponentStorage.getCurrentMonsters(opponent._id());
        } else {
            if (encounterOpponentStorage.getSelfOpponent().monster() != null) {
                opponent = encounterOpponentStorage.getSelfOpponent();
                monster = encounterOpponentStorage.getCurrentMonsters(opponent._id());
            }
        }

        abilitiesMenuController.init(monster, this, opponent, abilityDtos);

        battleMenuVBox.getChildren().add(abilitiesMenuController.render());
    }

    public void goBackToBattleMenu() {
        battleMenuVBox.getChildren().clear();
        battleMenuController.init(this, encounterOpponentStorage, app);
        battleMenuVBox.getChildren().add(battleMenuController.render());
    }

    public void enemyMonsterDefeated() {
        // pause to wait for possible level up result which comes after defeat result, else if is always false
        if (!GraphicsEnvironment.isHeadless()) {
            AudioService.getInstance().playEffect(DEATH, ingameControllerProvider.get());
        }
        PauseTransition pause = new PauseTransition(Duration.millis(pauseDuration));
        pause.setOnFinished(evt -> {
            if (resultLevelUpHashMap.get(encounterOpponentStorage.getSelfOpponent()._id())) {
                showLevelUpPopUp(encounterOpponentStorage.getSelfOpponent()._id());
            }
            if (encounterOpponentStorage.isTwoMonster()) {
                if (resultLevelUpHashMap.get(encounterOpponentStorage.getCoopOpponent()._id())) {
                    showLevelUpPopUp(encounterOpponentStorage.getCoopOpponent()._id());
                }
            }
        });
        pause.play();
    }

    public void yourMonsterDefeated(String opponentId) {
        if (!GraphicsEnvironment.isHeadless()) {
            AudioService.getInstance().playEffect(DEATH, ingameControllerProvider.get());
        }
        monsterInTeamHashMap.forEach((monsterId, isDied) -> {
            if (!isDied && !Objects.equals(monsterId, encounterOpponentStorage.getSelfOpponent().monster())) {
                if (!encounterOpponentStorage.isTwoMonster() || !Objects.equals(monsterId, encounterOpponentStorage.getCoopOpponent().monster())) {
                    disposables.add(encounterOpponentsService.updateOpponent(regionId, encounterId, opponentId, monsterId, null).observeOn(FX_SCHEDULER).subscribe(opponent -> resetRepeatedTimes(), Throwable::printStackTrace));
                }
            } else if (isDied) {
                Trainer trainer = trainerStorageProvider.get().getTrainer();
                if (trainer.settings() != null && trainer.settings().monsterPermaDeath() != null && trainer.settings().monsterPermaDeath()) {
                    disposables.add(monstersService.deleteMonster(trainer.region(), trainer._id(), monsterId).observeOn(FX_SCHEDULER).subscribe(result -> disposables.add(trainersService.getTrainer(trainer.region(), trainer._id()).observeOn(FX_SCHEDULER).subscribe(trainer1 -> {
                        trainerStorageProvider.get().setTrainer(trainer1);
                        resetRepeatedTimes();
                        monsterInTeamHashMap.remove(monsterId);
                        monsterInEncounterHashMap.remove(monsterId);
                        oldMonsterHashMap.remove(monsterId);
                    }, Throwable::printStackTrace)), Throwable::printStackTrace));
                }
            }
        });
    }


    public void fleeFromBattle() {
        SequentialTransition fleeAnimation = AnimationBuilder.buildFleeAnimation(trainerStorageProvider.get().getTrainerSpriteChunk(), ownTrainerController);
        PauseTransition firstPause = new PauseTransition(Duration.millis(500));
        battleDialogText.setText(resources.getString("ENCOUNTER_DESCRIPTION_FLEE"));
        firstPause.setOnFinished(evt -> {
            ownTrainerController.monsterImageView.setVisible(false);
            fleeAnimation.play();
            if (!GraphicsEnvironment.isHeadless()) {
                AudioService.getInstance().playEffect(FLEE, ingameControllerProvider.get());
            }
        });
        fleeAnimation.setOnFinished(evt -> disposables.add(encounterOpponentsService.deleteOpponent(encounterOpponentStorage.getRegionId(), encounterOpponentStorage.getEncounterId(), encounterOpponentStorage.getSelfOpponent()._id()).observeOn(FX_SCHEDULER).subscribe(result -> showIngameController(), error -> {
            showError(error.getMessage());
            System.out.println(error.getMessage());
        })));
        firstPause.play();
    }

    private VBox buildFleePopup() {
        // base VBox
        VBox fleeVBox = new VBox();
        fleeVBox.setId("fleePopup");
        fleeVBox.setMaxWidth(fleePopupWidth);
        fleeVBox.setMaxHeight(fleePopupHeight);
        fleeVBox.getStyleClass().add("dialogTextFlow");
        fleeVBox.setAlignment(Pos.CENTER);

        // flee TextFlow
        TextFlow fleeTextFlow = new TextFlow();
        fleeTextFlow.setMaxWidth(fleePopupWidth);
        fleeTextFlow.setMaxHeight(fleeTextHeight);
        fleeTextFlow.setPrefWidth(fleePopupWidth);
        fleeTextFlow.setPrefHeight(fleeTextHeight);
        fleeTextFlow.setPadding(fleeTextInsets);
        fleeTextFlow.setTextAlignment(TextAlignment.CENTER);

        // flee Text
        Text fleeText = new Text(this.resources.getString("ENCOUNTER_FLEE_TEXT"));
        fleeTextFlow.getChildren().add(fleeText);

        // buttons HBox
        HBox buttonHBox = new HBox();
        buttonHBox.setMaxWidth(fleePopupWidth);
        buttonHBox.setMaxHeight(fleeButtonsHBoxHeight);
        buttonHBox.setPrefWidth(fleePopupWidth);
        buttonHBox.setPrefHeight(fleeButtonsHBoxHeight);
        buttonHBox.setPadding(fleeButtonsHBoxInsets);
        buttonHBox.setAlignment(Pos.TOP_CENTER);
        buttonHBox.setSpacing(buttonsHBoxSpacing);

        // yes Button
        Button yesButton = new Button(this.resources.getString("ENCOUNTER_FLEE_CONFIRM_BUTTON"));
        yesButton.setId("fleePopupYesButton");
        yesButton.setMaxWidth(fleeButtonWidth);
        yesButton.setMinHeight(fleeButtonHeight);
        yesButton.setPrefWidth(fleeButtonWidth);
        yesButton.setPrefHeight(fleeButtonHeight);
        yesButton.getStyleClass().add("hBoxRed");
        yesButton.setOnAction(event -> {
            rootStackPane.getChildren().remove(fleeVBox);
            this.fleeFromBattle();
        });

        // no Button
        Button noButton = new Button(this.resources.getString("ENCOUNTER_FLEE_CANCEL_BUTTON"));
        noButton.setId("fleePopupNoButton");
        noButton.setMaxWidth(fleeButtonWidth);
        noButton.setMinHeight(fleeButtonHeight);
        noButton.setPrefWidth(fleeButtonWidth);
        noButton.setPrefHeight(fleeButtonHeight);
        noButton.getStyleClass().add("hBoxYellow");
        noButton.setOnAction(event -> rootStackPane.getChildren().remove(fleeVBox));

        // add buttons to hbox
        buttonHBox.getChildren().addAll(yesButton, noButton);

        // add textFlow and buttonHBox to VBox
        fleeVBox.getChildren().addAll(fleeTextFlow, buttonHBox);

        return fleeVBox;
    }

    public void resetRepeatedTimes() {
        this.repeatedTimes = 0;
    }

    public void increaseCurrentMonsterIndex() {
        this.currentMonsterIndex++;
    }

    /*
     * Disable the buttons in the encounter when a Popup is shown
     */
    public void buttonsDisableEncounter(boolean isDisable) {
        battleMenuController.buttonDisable(isDisable);
    }

    public void showLevelUpPopUp(String opponentId) {
        if (!GraphicsEnvironment.isHeadless()) {
            AudioService.getInstance().playEffect(LEVEL_UP, ingameControllerProvider.get());
        }
        resultLevelUpHashMap.put(opponentId, false);
        LevelUpController levelUpController = levelUpControllerProvider.get();
        VBox popUpVBox = new VBox();
        popUpVBox.getStyleClass().add("miniMapContainer");

        Monster oldMonster = oldMonsterHashMap.get(opponentId);
        ArrayList<Integer> newAbilities = newAbilitiesHashMap.get(opponentId);

        levelUpController.init(popUpVBox, rootStackPane, encounterOpponentStorage.getCurrentMonsters(opponentId),
                encounterOpponentStorage.getCurrentMonsterType(opponentId), oldMonster, newAbilities, abilityDtos, resultEvolvedHashMap.get(opponentId));
        popUpVBox.getChildren().add(levelUpController.render());
        rootStackPane.getChildren().add(popUpVBox);
        newAbilitiesHashMap.put(opponentId, new ArrayList<>());
        resultEvolvedHashMap.put(opponentId, false);
    }

    public void showCaughtMonsterPopUp() {
        CaughtMonsterController caughtMonsterController = caughtMonsterControllerProvider.get();
        VBox caughtMonsterVbox = new VBox();
        caughtMonsterVbox.setAlignment(Pos.CENTER);
        caughtMonsterController.setValues(resources, preferences, resourceBundleProvider, caughtMonsterController, app);
        caughtMonsterController.init(caughtMonsterVbox, rootStackPane, caughtMonster, caughtMonsterType, enemyMonsterImage, this);
        caughtMonsterVbox.getChildren().add(caughtMonsterController.render());
        rootStackPane.getChildren().add(caughtMonsterVbox);
    }

    public void changeMonster(Monster monster) {
        Move move = new ChangeMonsterMove(CHANGE_MONSTER, monster._id());
        String opponentId = getCorrectOpponentId();

        disposables.add(encounterOpponentsService.updateOpponent(regionId, encounterId, opponentId, null, move).observeOn(FX_SCHEDULER).subscribe(opponent -> {
            resetRepeatedTimes();
            setCorrectOpponent(opponent);
            updateDescription(resources.getString("YOU.CHANGED.MONSTER") + ". ", true);
            increaseCurrentMonsterIndex();
        }));
    }

    public void showItems() {
        VBox itemMenuBox = new VBox();
        itemMenuBox.setId("itemMenuBox");
        itemMenuBox.setAlignment(Pos.CENTER);
        ItemMenuController itemMenuController = itemMenuControllerProvider.get();
        itemMenuController.initFromEncounter(this, trainersService, trainerStorageProvider, itemMenuBox, InventoryType.showItems, List.of(), rootStackPane);
        itemMenuBox.getChildren().add(itemMenuController.render());
        rootStackPane.getChildren().add(itemMenuBox);
        itemMenuBox.requestFocus();
        battleMenuController.buttonDisable(true);
    }

    private void throwSuccessfulMonBall() {
        ballImageView = null;
        if (encounterOpponentStorage.isWild()) {
            if (!GraphicsEnvironment.isHeadless()) {
                AudioService.getInstance().playEffect(CATCH, ingameControllerProvider.get());
            }
            ballImageView = AnimationBuilder.throwMonBall(
                    selectedBallType,
                    rootStackPane,
                    ownTrainerController.trainerImageView,
                    enemy1Controller.monsterImageView,
                    3,
                    this::showCaughtMonsterPopUp
            );
        }
    }

    private void throwFailedMonBall() {
        ballImageView = null;
        if (encounterOpponentStorage.isWild()) {
            Random random = new Random();
            int ticks = random.nextInt(3) + 1;
            ballImageView = AnimationBuilder.throwMonBall(
                    selectedBallType,
                    rootStackPane,
                    ownTrainerController.trainerImageView,
                    enemy1Controller.monsterImageView,
                    ticks,
                    () -> onMonsterBreakout(ballImageView)
            );
            PauseTransition initialPause = new PauseTransition(Duration.seconds(1));
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            PauseTransition pause2 = new PauseTransition(Duration.seconds(1));
            PauseTransition pause3 = new PauseTransition(Duration.seconds(1));
            PauseTransition pause4 = new PauseTransition(Duration.seconds(1));
            initialPause.setOnFinished(event -> {
                updateDescription("3...", true);
                pause.play();
            });

            pause.setDelay(Duration.seconds(1));
            pause.setOnFinished(event -> {
                if (ticks > 1) {
                    updateDescription("2...", true);
                    pause2.play();
                } else {
                    updateDescriptionForMonsterBreakout();
                }
            });
            pause2.setOnFinished(event -> {
                if (ticks > 2) {
                    updateDescription("2...", true);
                    pause3.play();
                } else {
                    updateDescriptionForMonsterBreakout();
                }
            });
            pause3.setOnFinished(event -> {
                updateDescription("1...", true);
                pause4.play();
            });
            pause4.setOnFinished(event -> updateDescription(resources.getString("MONSTER.BROKE.OUT"), true));

            initialPause.play();

        }
    }

    private void updateDescriptionForMonsterBreakout() {
        updateDescription(resources.getString("MONSTER.BROKE.OUT"), true);
    }

    public void useMonBall(Item item) {
        if (encounterOpponentStorage.isWild()) {
            switch (item.type()) {
                case 10 -> selectedBallType = NORMAL;
                case 11 -> selectedBallType = SUPER;
                case 12 -> selectedBallType = HYPER;
                case 13 -> selectedBallType = MASTER;
                case 14 -> selectedBallType = NET;
                case 15 -> selectedBallType = WATER;
                default -> selectedBallType = HEAL;
            }
            Item itemCopy = new Item(
                    item._id(),
                    item.trainer(),
                    item.type(),
                    item.amount() - 1
            );
            itemStorageProvider.get().updateItemData(itemCopy, null, null);
            disposables.add(encounterOpponentsService.updateOpponent(
                    regionId,
                    encounterId,
                    encounterOpponentStorage.getSelfOpponent()._id(),
                    null,
                    new UseItemMove(ITEM_ACTION_USE_ITEM_MOVE, item.type(), encounterOpponentStorage.getTargetOpponent().monster())
            ).observeOn(FX_SCHEDULER).subscribe(result -> {
                resetRepeatedTimes();
                setCorrectOpponent(result);
                itemStorageProvider.get().updateItemData(itemCopy, null, null);
                UseItemMove useItemMove = (UseItemMove) result.move();
                trainerStorageProvider.get().useItem(useItemMove.item());
            }, Throwable::printStackTrace));
        }
    }

    private void onMonsterBreakout(ImageView ballImageView) {
        ballImageView.setVisible(false);
        enemy1Controller.monsterImageView.setVisible(true);
    }
    /*
    public void showMonsterReceivedPopUp() {
        receivedMonsterPopUp = new VBox();
        receivedMonsterPopUp.setMaxWidth(popupWidth);
        receivedMonsterPopUp.setMaxHeight(popupWidth);
        receivedMonsterPopUp.setAlignment(Pos.CENTER);
        receivedMonsterPopUp.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-border-style: solid; -fx-border-radius: 10px;");
        receivedMonsterPopUp.getStyleClass().add("hBoxLightGreen");
        String opponentId = encounterOpponentStorage.getTargetOpponent()._id();
        ReceiveObjectController receivedMonsterController = new ReceiveObjectController(encounterOpponentStorage.getCurrentMonsters(opponentId), encounterOpponentStorage.getCurrentMonsterType(opponentId), enemy1Controller.monsterImageView.getImage(), this::closeMonsterReceivedPopUp, trainerStorageProvider);
        receivedMonsterController.setValues(resources, preferences, resourceBundleProvider, this, app);
        receivedMonsterController.init();
        receivedMonsterPopUp.getChildren().add(receivedMonsterController.render());
        rootStackPane.getChildren().add(receivedMonsterPopUp);
    }
     */


    public boolean isWildEncounter() {
        return encounterOpponentStorage.isWild();
    }

    public ChangeMonsterListController getChangeMonsterListController() {
        return changeMonsterListControllerProvider.get();
    }

    public void useItem(Item item, Monster monster) {
        if (monster == null) {
            useMonBall(item);
        } else {
            // use item
            // Fit for two monsters
            UseItemMove move = new UseItemMove(USE_ITEM, item.type(), monster._id());

            String opponentId = getCorrectOpponentId();

            disposables.add(encounterOpponentsService.updateOpponent(regionId, encounterId, opponentId, null, move).observeOn(FX_SCHEDULER).subscribe(opponent -> {
                resetRepeatedTimes();
                setCorrectOpponent(opponent);
                UseItemMove useItemMove = (UseItemMove) opponent.move();
                updateDescription(resources.getString("YOU.USED") + " " + itemTypeDtos.get(useItemMove.item()).name() + ". ", true);
                trainerStorageProvider.get().useItem(useItemMove.item());
                increaseCurrentMonsterIndex();
            }));
        }
    }

    private void setCorrectOpponent(Opponent opponent) {
        if (encounterOpponentStorage.isTwoMonster()) {
            if (Objects.equals(encounterOpponentStorage.getSelfOpponent()._id(), opponent._id())) {
                encounterOpponentStorage.setSelfOpponent(opponent);
            } else if (Objects.equals(encounterOpponentStorage.getCoopOpponent()._id(), opponent._id())) {
                encounterOpponentStorage.setCoopOpponent(opponent);
            }
        } else {
            encounterOpponentStorage.setSelfOpponent(opponent);
        }
    }

    private String getCorrectOpponentId() {
        String opponentId = encounterOpponentStorage.getSelfOpponent()._id();

        if (encounterOpponentStorage.isTwoMonster()) {
            if (currentMonsterIndex % 2 == 0 && encounterOpponentStorage.getCoopOpponent().monster() != null) {
                opponentId = encounterOpponentStorage.getCoopOpponent()._id();
            } else if (currentMonsterIndex % 2 == 1 && encounterOpponentStorage.getSelfOpponent().monster() != null) {
                opponentId = encounterOpponentStorage.getSelfOpponent()._id();
            } else if (encounterOpponentStorage.getSelfOpponent().monster() == null) {
                opponentId = encounterOpponentStorage.getCoopOpponent()._id();
            } else if (encounterOpponentStorage.getCoopOpponent().monster() == null) {
                opponentId = encounterOpponentStorage.getSelfOpponent()._id();
            }
        } else {
            if (encounterOpponentStorage.getSelfOpponent().monster() != null) {
                opponentId = encounterOpponentStorage.getSelfOpponent()._id();
            }
        }

        return opponentId;
    }

    public IngameController getIngameController() {
        return ingameControllerProvider.get();
    }
}
    
