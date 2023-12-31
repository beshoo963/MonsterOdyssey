package de.uniks.stpmon.team_m.controller.subController;

import de.uniks.stpmon.team_m.controller.Controller;
import de.uniks.stpmon.team_m.controller.IngameController;
import de.uniks.stpmon.team_m.dto.Trainer;
import de.uniks.stpmon.team_m.service.PresetsService;
import de.uniks.stpmon.team_m.utils.SpriteAnimation;
import de.uniks.stpmon.team_m.utils.TrainerStorage;
import javafx.scene.Parent;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import javax.inject.Inject;
import javax.inject.Provider;

import static de.uniks.stpmon.team_m.Constants.*;

public class TrainerController extends Controller {
    private final IngameController ingameController;

    private SpriteAnimation spriteAnimation;
    private final GraphicsContext graphicsContext;
    private final GraphicsContext alternativeGraphicsContext;
    private final Image trainerChunk;
    private int trainerX;
    private int trainerY;
    private int trainerDirection;
    private int trainerTargetX;
    private int trainerTargetY;

    @Inject
    PresetsService presetsService;

    @Inject
    Provider<TrainerStorage> trainerStorageProvider;

    private Boolean isWalking = false;

    public int getTrainerX() {
        return trainerX;
    }

    public int getTrainerY() {
        return trainerY;
    }

    @Override
    public void init() {
        super.init();
        int duration;
        if (isWalking) {
            duration = DELAY;
        } else {
            duration = DELAY_LONG;
        }
        spriteAnimation = new SpriteAnimation(this, trainerChunk, duration, graphicsContext, alternativeGraphicsContext);
        spriteAnimation.stay(trainerDirection);
    }

    public void startAnimations() {
        spriteAnimation.start();
    }

    @Override
    public Parent render() {
        return null;
    }

    /**
     * Note: oldTrainerX, oldTrainerY, targetTrainerX and targetTrainerY are given in tiles, not in pixels!
     * BUT trainerTargetX and trainerTargetY are given in pixels!
     */
    public TrainerController(IngameController ingameController, Trainer trainer, Image trainerChunk, GraphicsContext graphicsContext, GraphicsContext alternateGraphicsContext) {
        this.ingameController = ingameController;
        this.trainerChunk = trainerChunk;
        this.graphicsContext = graphicsContext;
        this.alternativeGraphicsContext = alternateGraphicsContext;

        // Init trainer position and direction
        trainerTargetX = trainer.x();
        trainerTargetY = trainer.y();
        this.trainerX = trainer.x() * TILE_SIZE;
        this.trainerY = trainer.y() * TILE_SIZE;
        this.trainerDirection = trainer.direction();
    }

    /**
     * @param x x-coordinate of the trainer in tiles
     * @param y y-coordinate of the trainer in tiles
     */
    public void setTrainerTargetPosition(int x, int y) {
        trainerTargetX = x;
        trainerTargetY = y;
    }

    /**
     * This method is used to turn the trainer in a specific direction
     *
     * @param direction : 0 = right, 1 = up, 2 = left, 3 = down
     */
    public void turn(int direction) {
        trainerDirection = direction;
        spriteAnimation.stay(trainerDirection);
    }

    public void walk() {
        if (trainerX != trainerTargetX * TILE_SIZE || trainerY != trainerTargetY * TILE_SIZE) {
            isWalking = true;
            if (trainerX < trainerTargetX * TILE_SIZE) {
                trainerDirection = TRAINER_DIRECTION_RIGHT;
                trainerX++;
            } else if (trainerX > trainerTargetX * TILE_SIZE) {
                trainerDirection = TRAINER_DIRECTION_LEFT;
                trainerX--;
            } else if (trainerY < trainerTargetY * TILE_SIZE) {
                trainerDirection = TRAINER_DIRECTION_DOWN;
                trainerY++;
            } else if (trainerY > trainerTargetY * TILE_SIZE) {
                trainerDirection = TRAINER_DIRECTION_UP;
                trainerY--;
            }
            spriteAnimation.walk(trainerDirection);
        } else {
            isWalking = false;
            spriteAnimation.stay(trainerDirection);
        }
    }

    public void setTrainerDirection(int direction) {
        trainerDirection = direction;
    }

    @Override
    public void destroy() {
        spriteAnimation.stop();
        super.destroy();
    }

    public int getUserTrainerY() {
        return ingameController.getUserTrainerY();
    }

    public int getDirection() {
        return this.trainerDirection;
    }
}
