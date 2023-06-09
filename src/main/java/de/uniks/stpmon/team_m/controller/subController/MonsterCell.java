package de.uniks.stpmon.team_m.controller.subController;

import de.uniks.stpmon.team_m.Main;
import de.uniks.stpmon.team_m.controller.MonstersDetailController;
import de.uniks.stpmon.team_m.controller.MonstersListController;
import de.uniks.stpmon.team_m.dto.Monster;
import de.uniks.stpmon.team_m.dto.MonsterTypeDto;
import de.uniks.stpmon.team_m.dto.Trainer;
import de.uniks.stpmon.team_m.service.PresetsService;
import de.uniks.stpmon.team_m.service.TrainersService;
import de.uniks.stpmon.team_m.utils.ImageProcessor;
import de.uniks.stpmon.team_m.utils.TrainerStorage;
import de.uniks.stpmon.team_m.utils.UserStorage;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import static de.uniks.stpmon.team_m.Constants.*;
;


public class MonsterCell extends ListCell<Monster> {

    @FXML
    Label monsterName;
    @FXML
    Label monsterType;
    @FXML
    Label monsterLevel;

    @FXML
    ImageView monsterImageView;

    @FXML
    HBox rootmonsterHBox;
    private ResourceBundle resources;
    @Inject
    Provider<TrainerStorage> trainerStorageProvider;

    @Inject
    Provider<UserStorage> userStorageProvider;
    @Inject
    public TrainersService trainersService;
    @Inject
    public UserStorage usersStorage;
    @Inject
    public PresetsService presetsService;
    @Inject
    Provider<TrainersService> trainersServiceProvider;
    private FXMLLoader loader;

    private final ObservableList<Monster> allMonsters = FXCollections.observableArrayList();
    protected final CompositeDisposable disposables = new CompositeDisposable();
    public static final Scheduler FX_SCHEDULER = Schedulers.from(Platform::runLater);
    private String regionId;
    private String trainerId;
    private Trainer trainer;
    private Monster monster;
    private MonsterTypeDto monsterTypeDto;
    private Image monsterImage;

    public MonsterCell(ResourceBundle resources) {
        this.resources = resources;
    }

    @Override
    protected void updateItem(Monster monster, boolean empty) {
        super.updateItem(monster, empty);
        if (monster == null || empty) {
            setText(null);
            setGraphic(null);
        } else {
            loadFXML();
            monsterName.setText(monster.createdAt());
            monsterType.setText("" + monster.type());
            monsterLevel.setText("" + monster.level());
            rootmonsterHBox.setOnMouseClicked(event -> {
                showDetails(monster);
            });
            setGraphic(rootmonsterHBox);
            setText(null);
        }
    }

    private void showDetails(Monster monster) {
        Dialog<?> monstersDialog = new Dialog<>();
        monstersDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Node closeButton = monstersDialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.managedProperty().bind(closeButton.visibleProperty());
        closeButton.setVisible(false);
        monstersDialog.setTitle(resources.getString("MONSTERS"));
        monstersDialog.getDialogPane().setContent(new MonstersDetailController().render());
        monstersDialog.showAndWait();
    }

    private void loadFXML() {
        if (loader == null) {
            loader = new FXMLLoader(Main.class.getResource("views/MonsterCell.fxml"));
            loader.setControllerFactory(c -> this);
            try {
                loader.load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}

