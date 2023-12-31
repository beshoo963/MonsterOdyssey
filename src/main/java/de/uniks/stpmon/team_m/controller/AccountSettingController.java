package de.uniks.stpmon.team_m.controller;

import de.uniks.stpmon.team_m.App;
import de.uniks.stpmon.team_m.controller.subController.AvatarSelectionController;
import de.uniks.stpmon.team_m.controller.subController.ChangeLanguageController;
import de.uniks.stpmon.team_m.service.UsersService;
import de.uniks.stpmon.team_m.utils.ImageProcessor;
import de.uniks.stpmon.team_m.utils.PasswordFieldSkin;
import de.uniks.stpmon.team_m.utils.UserStorage;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;

import javax.inject.Inject;
import javax.inject.Provider;
import java.awt.*;
import java.util.Objects;
import java.util.Optional;

import static de.uniks.stpmon.team_m.Constants.*;


public class AccountSettingController extends Controller {

    @FXML
    public Label informationLabel;
    @FXML
    public TextField usernameField;
    @FXML
    public Button usernameEditButton;
    @FXML
    public Button saveUsernameButton;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Button showPasswordButton;
    @FXML
    public Button passwordEditButton;
    @FXML
    public Button savePasswordButton;
    @FXML
    public Button cancelButton;
    @FXML
    public Button deleteAccountButton;
    @FXML
    public Label passwordErrorLabel;
    @FXML
    public Label usernameErrorLabel;
    @FXML
    public Label titleLabel;
    @FXML
    public ImageView avatarImageView;
    @FXML
    public Button editAvatarButton;
    @FXML
    public Button saveAvatarButton;
    @FXML
    public Label avatarErrorLabel;
    @FXML
    public Button changeLanguageButton;
    @FXML
    public ImageView accountSettingsMonsterImageView;
    @Inject
    Provider<MainMenuController> mainMenuControllerProvider;
    @Inject
    Provider<LoginController> loginControllerProvider;
    @Inject
    Provider<UserStorage> userStorageProvider;
    @Inject
    UsersService usersService;
    @Inject
    Provider<AvatarSelectionController> avatarSelectionControllerProvider;
    private PasswordFieldSkin skin;
    private final SimpleStringProperty username = new SimpleStringProperty();
    private final SimpleStringProperty password = new SimpleStringProperty();
    private String selectedFilePath;

    /**
     * AccountSettingController is used to edit the avatar, language, username and password of the user.
     */

    @Inject
    AccountSettingController() {
    }

    @Override
    public void init() {
        super.init();
        ChangeLanguageController changeLanguageController = new ChangeLanguageController();
        changeLanguageController.init();
    }

    /**
     * This method is used to define the title of the AccountSettingController
     *
     * @return The title of the AccountSettingController
     */

    @Override
    public String getTitle() {
        return resources.getString("ACCOUNT.SETTINGS.TITLE");
    }

    /**
     * This method is used to render JavaFX elements of the AccountSettingController.
     * It is also used to bind the username and password to the TextFields.
     * It disables the Buttons if the username is empty or password length is less than 8 characters.
     *
     * @return Parent node of the AccountSettingController
     */

    @Override
    public Parent render() {
        final Parent parent = super.render();

        if (!GraphicsEnvironment.isHeadless()) {
            accountSettingsMonsterImageView.setImage(new Image(Objects.requireNonNull(App.class.getResource(MONSTER1_WITHOUT)).toString()));
        }

        // Firstly disable the editfield
        usernameField.setDisable(true);
        passwordField.setDisable(true);
        showPasswordButton.setDisable(true);

        saveAvatarButton.setDisable(true);

        // Show the Current UserName
        usernameField.setPromptText(userStorageProvider.get().getName());

        // show password
        skin = new PasswordFieldSkin(passwordField);
        passwordField.setSkin(skin);

        // bind the username and password
        usernameField.textProperty().bindBidirectional(username);
        passwordField.textProperty().bindBidirectional(password);

        BooleanBinding isInvalidUsername = username.isEmpty();
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                saveUsername();
            }
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                savePassword();
            }
        });
        saveUsernameButton.disableProperty().bind(isInvalidUsername);

        BooleanBinding isInvalidPassword = password.length().lessThan(PASSWORD_CHARACTER_LIMIT);
        savePasswordButton.disableProperty().bind(isInvalidPassword);

        passwordField.setPromptText(resources.getString("PASSWORD.LESS.THAN.8.CHARACTERS"));

        // show Avatar if there is one
        if (userStorageProvider.get().getAvatar() != null) {
            String avatar = userStorageProvider.get().getAvatar();

            avatarImageView.setImage(ImageProcessor.fromBase64ToFXImage(avatar));
        }

        return parent;
    }

    /**
     * This method is used to re-enable the TextField for the username.
     */

    public void editUsername() {
        usernameField.setDisable(!usernameField.isDisabled());
    }

    /**
     * This method is used to save the username.
     * It also checks if the username is already taken.
     */

    public void saveUsername() {
        informationLabel.setText(EMPTY_STRING);
        usernameErrorLabel.setText(EMPTY_STRING);

        disposables.add(usersService.updateUser(username.get(), null, null, null, null)
                .observeOn(FX_SCHEDULER).subscribe(userResult -> {
                    userStorageProvider.get().setName(userResult.name());
                    informationLabel.setText(resources.getString("USERNAME.SUCCESS.CHANGED"));
                    usernameField.setDisable(true);
                    usernameField.setText(EMPTY_STRING);
                    usernameField.setPromptText(userStorageProvider.get().getName());
                }, error -> usernameErrorLabel.setText(errorHandle(error.getMessage()))));

    }

    /**
     * This method is used to show the password when clicking the show password button.
     */

    public void showPassword() {
        skin.setMask(skin.getNotMask());
        passwordField.setText(passwordField.getText());
    }

    /**
     * This method is used to re-enable the TextField for the password.
     */

    public void editPassword() {
        passwordField.setDisable(!passwordField.isDisabled());
        showPasswordButton.setDisable(!showPasswordButton.isDisabled());
    }

    /**
     * This method is used to save the password by sending a request to the server.
     */

    public void savePassword() {
        informationLabel.setText(EMPTY_STRING);
        passwordErrorLabel.setText(EMPTY_STRING);

        disposables.add(usersService.updateUser(null, null, null, null, password.get())
                .observeOn(FX_SCHEDULER).subscribe(userResult -> {
                    passwordField.setText(EMPTY_STRING);
                    passwordField.setPromptText(resources.getString("PASSWORD.LESS.THAN.8.CHARACTERS"));
                    passwordField.setDisable(true);
                    showPasswordButton.setDisable(true);
                    informationLabel.setText(resources.getString("PASSWORD.SUCCESS.CHANGED"));
                }, error -> passwordErrorLabel.setText(errorHandle(error.getMessage()))));
    }


    /**
     * This method opens a pop-up to the avatar selection.
     */
    public void editAvatar() {
        AvatarSelectionController avatarSelectionController = avatarSelectionControllerProvider.get();
        avatarSelectionController.init();
        ButtonType cancelButton = new ButtonType(resources.getString("Cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType okButton = new ButtonType(resources.getString("OK"), ButtonBar.ButtonData.OK_DONE);

        Dialog<?> dialog = new Dialog<>();
        dialog.setTitle(resources.getString("CHOOSE.YOUR.AVATAR"));
        avatarSelectionController.setValues(resources, preferences, resourceBundleProvider, this, app);
        dialog.getDialogPane().setContent(avatarSelectionController.render());
        dialog.getDialogPane().getButtonTypes().add(okButton);
        dialog.getDialogPane().getButtonTypes().add(cancelButton);
        dialog.initOwner(app.getStage());
        if (!GraphicsEnvironment.isHeadless()) {
            dialog.getDialogPane().getStyleClass().add("comicSans");
            Button ok = (Button) dialog.getDialogPane().lookupButton(okButton);
            ok.getStyleClass().add("buttonsWhite");
            Button cancel = (Button) dialog.getDialogPane().lookupButton(cancelButton);
            cancel.getStyleClass().add("buttonsWhite");
            avatarSelectionController.selectFileButton.getStyleClass().add("buttonsWhite");
        }
        dialog.showAndWait();

        if (dialog.getResult() == okButton) {
            saveAvatarButton.setDisable(false);
            Image image;
            try {
                image = new Image(Objects.requireNonNull(App.class.getResource(avatarSelectionController.selectedAvatar)).toString());
                selectedFilePath = Objects.requireNonNull(App.class.getResource(avatarSelectionController.selectedAvatar)).toURI().getPath();
            } catch (Exception e) {
                image = new Image("file:" + avatarSelectionController.selectedAvatar);
                selectedFilePath = avatarSelectionController.selectedAvatar;
            }
            avatarImageView.setImage(image);
        }
    }

    /**
     * This method is used to save the selected avatar by sending a request to the server.
     */
    public void saveAvatar() {
        informationLabel.setText(resources.getString("IMAGE.PROCESSING.ONGOING"));
        String base64Image = ImageProcessor.toBase64(selectedFilePath);
        if (base64Image.equals(resources.getString("IMAGE.PROCESSING.ERROR")))
            informationLabel.setText(resources.getString("IMAGE.PROCESSING.ERROR"));
        String avatarUpload = "data:image/png;base64, " + base64Image;
        disposables.add(usersService
                .updateUser(null, null, avatarUpload, null, null)
                .observeOn(FX_SCHEDULER)
                .subscribe(userResult -> {
                    userStorageProvider.get().setAvatar(userResult.avatar());
                    saveAvatarButton.setDisable(true);
                    informationLabel.setText(resources.getString("AVATAR.SUCCESS.CHANGED"));
                }, error -> avatarErrorLabel.setText(error.getMessage()))
        );

    }


    /**
     * This method is used to delete the account.
     * It also shows a pop-up to confirm the deletion.
     *
     * @param alert The pop-up to confirm the deletion
     */

    public void deleteAccount(Alert alert) {
        disposables.add(usersService.deleteUser().observeOn(FX_SCHEDULER).subscribe(delete -> {
            LoginController loginController = loginControllerProvider.get();
            loginController.setInformation(resources.getString("DELETE.SUCCESS"));
            app.show(loginController);
        }, error -> errorAlert(alert)));
    }

    /**
     * This method is used to leave the AccountSettingController and go back to the MainMenuController.
     */

    public void cancel() {
        app.show(mainMenuControllerProvider.get());
    }

    /**
     * This method is used to show a pop-up to confirm the deletion of the account.
     */

    public void showDeletePopUp() {
        Alert alert = new Alert(Alert.AlertType.WARNING, resources.getString("SURE"), ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(resources.getString("DELETE.ACCOUNT"));
        alert.setHeaderText(null);
        alert.initOwner(app.getStage());
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteAccount(alert);
        }
    }

    /**
     * This method is used to show a pop-up when an error occurs.
     *
     * @param alert The pop-up to show when an error occurs
     */

    private void errorAlert(Alert alert) {
        alert.setContentText(resources.getString("CUSTOM.ERROR"));
        alert.setTitle(resources.getString("ERROR"));
        alert.getButtonTypes().remove(ButtonType.CANCEL);
        alert.showAndWait();
    }

    /**
     * This method is used to handle the error message.
     *
     * @param error The error message
     * @return More precise error message
     */

    public String errorHandle(String error) {
        if (error.contains(HTTP_409)) {
            return resources.getString("USERNAME.TAKEN");
        } else {
            return resources.getString("CUSTOM.ERROR");
        }
    }
}
