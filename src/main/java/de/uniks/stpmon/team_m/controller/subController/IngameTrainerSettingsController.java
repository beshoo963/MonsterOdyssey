package de.uniks.stpmon.team_m.controller.subController;

import de.uniks.stpmon.team_m.controller.Controller;
import de.uniks.stpmon.team_m.controller.IngameController;
import de.uniks.stpmon.team_m.controller.MainMenuController;
import de.uniks.stpmon.team_m.service.PresetsService;
import de.uniks.stpmon.team_m.service.RegionsService;
import de.uniks.stpmon.team_m.service.TrainersService;
import de.uniks.stpmon.team_m.utils.ImageProcessor;
import de.uniks.stpmon.team_m.utils.TrainerStorage;
import de.uniks.stpmon.team_m.utils.UserStorage;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.awt.*;

import static de.uniks.stpmon.team_m.Constants.PREMADE_CHARACTERS;

@Singleton
public class IngameTrainerSettingsController extends Controller {
    @FXML
    public VBox trainerSettingsVbox;
    @FXML
    public Button goBackButton;
    @FXML
    public TextField trainerNameTextfield;
    @FXML
    public Button trainerNameEditButton;
    @FXML
    public Button arrowLeftButton;
    @FXML
    public Button arrowRightButton;
    @FXML
    public ImageView trainerAvatarImageView;
    @FXML
    public HBox trainerSettingsHbox;
    @FXML
    public Button updateTrainerButton;
    @FXML
    public Button deleteTrainerButton;
    @FXML
    public StackPane trainerSettingsStackpane;

    @Inject
    Provider<MainMenuController> mainMenuControllerProvider;
    private IngameController ingameController;
    @Inject
    public PresetsService presetsService;

    @Inject
    public RegionsService regionsService;

    @Inject
    public TrainersService trainersService;

    @Inject
    public Provider<TrainerStorage> trainerStorageProvider;
    @Inject
    public UserStorage usersStorage;
    @Inject
    Provider<IngameDeleteTrainerWarningController> ingameDeleteTrainerWarningControllerProvider;

    protected final CompositeDisposable disposables = new CompositeDisposable();
    private VBox ingameVbox;
    private IngameDeleteTrainerWarningController ingameDeleteTrainerWarningController;
    public int index = 1;
    final private String[] characters = PREMADE_CHARACTERS;
    public String selectedCharacter = characters[index - 1];


    @Inject
    public IngameTrainerSettingsController() {
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public Parent render() {
        Parent parent = super.render();
        loadAndSetTrainerImage();
        return parent;
    }

    public void onDeleteTrainerButtonClick() {
        ingameDeleteTrainerWarningController = ingameDeleteTrainerWarningControllerProvider.get();
        VBox deleteTrainerWarningVbox = new VBox();
        deleteTrainerWarningVbox.setAlignment(Pos.CENTER);
        ingameDeleteTrainerWarningController.init(this, deleteTrainerWarningVbox);
        deleteTrainerWarningVbox.getChildren().add(ingameDeleteTrainerWarningController.render());
        trainerSettingsStackpane.getChildren().add(deleteTrainerWarningVbox);
        deleteTrainerWarningVbox.requestFocus();
        buttonsDisableTrainer(true);
    }

    public void deleteTrainer() {
        disposables.add(trainersService.deleteTrainer(trainerStorageProvider.get().getRegion()._id(), trainerStorageProvider.get().getTrainer()._id()).
                observeOn(FX_SCHEDULER).subscribe(end -> {
                    trainerStorageProvider.get().setTrainer(null);
                    trainerStorageProvider.get().setTrainerSprite(null);
                    trainerStorageProvider.get().setTrainerName(null);
                    trainerStorageProvider.get().setRegion(null);
                }, error -> this.showError(error.getMessage())));
        MainMenuController mainMenuController = mainMenuControllerProvider.get();
        mainMenuController.setTrainerDeletion();
        ingameController.destroy();
        app.show(mainMenuController);
    }

    public void buttonsDisableTrainer(boolean set) {
        deleteTrainerButton.setDisable(set);
        updateTrainerButton.setDisable(set);
        trainerNameEditButton.setDisable(set);
        arrowLeftButton.setDisable(set);
        arrowRightButton.setDisable(set);
        goBackButton.setDisable(set);
    }

    private void loadAndSetTrainerImage() {
        String sprite = trainerStorageProvider.get().getTrainerSprite();
        String subString = sprite.substring(sprite.length() - 6, sprite.length() - 4);
        index = Integer.parseInt(subString);
        if (!GraphicsEnvironment.isHeadless()) {
            trainerAvatarImageView.setImage(ImageProcessor.showScaledFrontCharacter(sprite));
        }
    }

    public void initIngame(IngameController ingameController, VBox ingameVbox) {
        this.ingameController = ingameController;
        this.ingameVbox = ingameVbox;
    }

    public void goBackToSettings() {
        ingameController.root.getChildren().remove(ingameVbox);
    }

    public void editTrainerName() {
    }

    public void showCharacter() {
        trainerAvatarImageView.setImage(ImageProcessor.showScaledFrontCharacter(characters[index - 1]));
        selectedCharacter = characters[index - 1];
    }

    public void arrowLeftClick() {
        index--;
        if (index < 1) {
            index = index + characters.length;
        }
        showCharacter();
    }

    public void arrowRightClick() {
        index++;
        if (index > characters.length) {
            index = index - characters.length;
        }
        showCharacter();
    }

    public void updateTrainer() {
    }
}
