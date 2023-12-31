package de.uniks.stpmon.team_m;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import javafx.stage.FileChooser;

import javax.inject.Singleton;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

@Module
public class MainModule {

    @Provides
    ResourceBundle bundle(Preferences preferences){
        final String locale = preferences.get("locale", Locale.ENGLISH.toLanguageTag());
        return ResourceBundle.getBundle("de/uniks/stpmon/team_m/lang/lang", Locale.forLanguageTag(locale));
    }

    @Provides
    @Singleton
    ObjectMapper mapper() {
        return new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    }

    @Provides
    @Singleton
    FileChooser fileChooser() {
        return new FileChooser();
    }
}
