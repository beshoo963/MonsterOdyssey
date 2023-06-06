package de.uniks.stpmon.team_m.controller.subController;

import de.uniks.stpmon.team_m.controller.Controller;
import de.uniks.stpmon.team_m.service.PresetsService;
import de.uniks.stpmon.team_m.service.RegionsService;
import de.uniks.stpmon.team_m.service.TrainersService;
import de.uniks.stpmon.team_m.utils.ImageProcessor;
import de.uniks.stpmon.team_m.utils.TrainerStorage;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class IngameTrainerSettingsController extends Controller {
    @FXML
    public ImageView trainerAvatarImageView;

    @FXML
    public Button cancelButton;

    @FXML
    public Button deleteTrainerButton;

    @Inject
    public PresetsService presetsService;

    @Inject
    public RegionsService regionsService;

    @Inject
    public TrainersService trainersService;
    @Inject
    public Provider<TrainerStorage> trainerStorageProvider;
    private Image trainerImage;


    @Inject
    public IngameTrainerSettingsController() {
    }

    @Override
    public void init() {
        super.init();
        loadAndSetTrainerImage();
    }

    @Override
    public Parent render() {
        return super.render();
    }

    public void onCancelButtonClick() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    public void onDeleteTrainerButtonClick() {
        // TODO: show alert dialog
    }

    private void loadAndSetTrainerImage() {
        disposables.add(presetsService.getCharacter(trainerStorageProvider.get().getTrainer().image()).observeOn(FX_SCHEDULER).subscribe(responseBody -> {
            try {
                trainerImage = ImageProcessor.responseBodyToJavaFXImage(responseBody);
                trainerAvatarImageView.setImage(trainerImage);
            } catch (Exception e) {
                this.showError(e.getMessage());
            }
        }, error -> this.showError(error.getMessage())));
    }
}
