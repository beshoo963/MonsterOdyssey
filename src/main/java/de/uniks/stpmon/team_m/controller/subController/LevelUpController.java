package de.uniks.stpmon.team_m.controller.subController;

import de.uniks.stpmon.team_m.App;
import de.uniks.stpmon.team_m.controller.Controller;
import de.uniks.stpmon.team_m.controller.EncounterController;
import de.uniks.stpmon.team_m.dto.Monster;
import de.uniks.stpmon.team_m.dto.MonsterTypeDto;
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
import java.awt.*;
import java.util.ArrayList;
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
    private EncounterController encounterController;
    private StackPane root;
    private Monster monster;
    private MonsterTypeDto monsterTypeDto;
    private Monster oldMonster;
    private ArrayList<Integer> newAbilities;


    @Inject
    public LevelUpController() {

    }

    public void init(VBox container, StackPane root, EncounterController encounterController, Monster currentMonster, MonsterTypeDto currentMonsterTypeDto, Monster oldMonster, ArrayList<Integer> newAbilities) {
        this.container = container;
        this.root = root;
        this.encounterController = encounterController;
        this.monster = currentMonster;
        this.monsterTypeDto = currentMonsterTypeDto;
        this.oldMonster = oldMonster;
        this.newAbilities = newAbilities;
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

        levelLabel.setText(oldMonster.level() + " -> " + monster.level());
        healthLabel.setText(oldMonster.attributes().health() + " -> " + monster.attributes().health());
        attackLabel.setText(oldMonster.attributes().attack() + " -> " + monster.attributes().attack());
        defenseLabel.setText(oldMonster.attributes().defense() + " -> " + monster.attributes().defense());
        speedLabel.setText(oldMonster.attributes().speed() + " -> " + monster.attributes().speed());

        levelUpTextFlow.getChildren().add(new Text(resources.getString("LEVEL.UP!") + "\n"));
        levelUpTextFlow.getChildren().add(new Text(monsterTypeDto.name() + " " + resources.getString("NOW.HAS.THE.FOLLOWING.ATTRIBUTES") + ":"));

        if (!newAbilities.isEmpty()) {
            Label label = new Label("new Ability\n");
            label.setAlignment(Pos.CENTER);
            newAbilities.forEach(integer -> {


            });
            abilityVBox.getChildren().add(label);
        }
        return parent;
    }

    public void okButtonPressed() {
        root.getChildren().remove(container);
        encounterController.continueBattle();
    }
}