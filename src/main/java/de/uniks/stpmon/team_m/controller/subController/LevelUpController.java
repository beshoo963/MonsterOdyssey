package de.uniks.stpmon.team_m.controller.subController;

import de.uniks.stpmon.team_m.App;
import de.uniks.stpmon.team_m.controller.Controller;
import de.uniks.stpmon.team_m.dto.AbilityDto;
import de.uniks.stpmon.team_m.dto.Monster;
import de.uniks.stpmon.team_m.dto.MonsterTypeDto;
import de.uniks.stpmon.team_m.service.MonstersService;
import de.uniks.stpmon.team_m.utils.TrainerStorage;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import javax.inject.Inject;
import javax.inject.Provider;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class LevelUpController extends Controller {
    @FXML
    public VBox levelUpVBox;
    @FXML
    public TextFlow levelUpTextFlow;
    @FXML
    public Button okButton;
    @FXML
    public Label levelLabel;
    @FXML
    public Label healthLabel;
    @FXML
    public Label attackLabel;
    @FXML
    public Label defenseLabel;
    @FXML
    public Label speedLabel;
    @FXML
    public ImageView expImageView;
    @FXML
    public ImageView hpImageView;
    @FXML
    public ImageView atkImageView;
    @FXML
    public ImageView defImageView;
    @FXML
    public ImageView spImageView;
    @FXML
    public VBox abilityVBox;
    private VBox container;
    private StackPane root;
    private Monster currentMonster;
    private MonsterTypeDto currentMonsterTypeDto;
    private Monster oldMonster;
    private MonsterTypeDto oldMonsterTypeDto;
    private ArrayList<Integer> newAbilities;
    private List<AbilityDto> abilities;
    private boolean hasEvolved;
    private final DecimalFormat formatter = new DecimalFormat("#,###.0");
    @Inject
    Provider<EvolutionController> evolutionControllerProvider;
    @Inject
    MonstersService monstersService;
    @Inject
    TrainerStorage trainerStorage;


    @Inject
    public LevelUpController() {
    }

    public void init(VBox container, StackPane root, Monster currentMonster, MonsterTypeDto currentMonsterTypeDto,
                     Monster oldMonster, ArrayList<Integer> newAbilities, List<AbilityDto> abilities, boolean hasEvolved) {
        this.container = container;
        this.root = root;
        this.currentMonster = currentMonster;
        this.currentMonsterTypeDto = currentMonsterTypeDto;
        this.oldMonster = oldMonster;
        this.newAbilities = newAbilities;
        this.abilities = abilities;
        this.hasEvolved = hasEvolved;
        disposables.add(presetsService.getMonster(oldMonster.type()).observeOn(FX_SCHEDULER).subscribe(
                monsterTypeDto -> oldMonsterTypeDto = monsterTypeDto, Throwable::printStackTrace));
    }

    public Parent render() {
        final Parent parent = super.render();
        if (!GraphicsEnvironment.isHeadless()) {
            expImageView.setImage(new Image(Objects.requireNonNull(App.class.getResource("images/star.png")).toString()));
            hpImageView.setImage(new Image(Objects.requireNonNull(App.class.getResource("images/heart.png")).toString()));
            atkImageView.setImage(new Image(Objects.requireNonNull(App.class.getResource("images/attack.png")).toString()));
            defImageView.setImage(new Image(Objects.requireNonNull(App.class.getResource("images/defense.png")).toString()));
            spImageView.setImage(new Image(Objects.requireNonNull(App.class.getResource("images/speed.png")).toString()));
        }

        levelLabel.setText(oldMonster.level() + " -> " + currentMonster.level());
        healthLabel.setText(formatter.format(oldMonster.attributes().health()) + " -> " + formatter.format(currentMonster.attributes().health()));
        attackLabel.setText(oldMonster.attributes().attack() + " -> " + currentMonster.attributes().attack());
        defenseLabel.setText(oldMonster.attributes().defense() + " -> " + currentMonster.attributes().defense());
        speedLabel.setText(oldMonster.attributes().speed() + " -> " + currentMonster.attributes().speed());

        levelUpTextFlow.getChildren().add(new Text(resources.getString("LEVEL.UP!") + "\n"));
        levelUpTextFlow.getChildren().add(new Text(currentMonsterTypeDto.name() + " " + resources.getString("NOW.HAS.THE.FOLLOWING.ATTRIBUTES") + ":"));

        if (!newAbilities.isEmpty()) {
            Label label = new Label(resources.getString("NEW.ABILITIES") + ": ");
            label.setAlignment(Pos.CENTER);
            newAbilities.forEach(integer -> label.setText(label.getText() + abilities.get(integer - 1).name() + " "));
            abilityVBox.getChildren().add(label);
        }
        return parent;
    }

    public void okButtonPressed() {
        if (hasEvolved) {
            disposables.add(monstersService.getMonster(trainerStorage.getRegion()._id(), trainerStorage.getTrainer()._id(), currentMonster._id()).observeOn(FX_SCHEDULER).subscribe(monster -> {
                currentMonster = monster;
                disposables.add(presetsService.getMonster(monster.type()).observeOn(FX_SCHEDULER).subscribe(monsterTypeDto -> {
                    currentMonsterTypeDto = monsterTypeDto;
                    EvolutionController evolutionController = evolutionControllerProvider.get();
                    evolutionController.init(container, root, currentMonster, currentMonsterTypeDto, oldMonster, oldMonsterTypeDto);
                    container.getChildren().clear();
                    container.getChildren().add(evolutionController.render());
                }, Throwable::printStackTrace));
            }, Throwable::printStackTrace));
        } else {
            root.getChildren().remove(container);
        }
    }
}
