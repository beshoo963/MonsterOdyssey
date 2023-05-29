package de.uniks.stpmon.team_m;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import de.uniks.stpmon.team_m.dto.*;
import de.uniks.stpmon.team_m.rest.*;
import de.uniks.stpmon.team_m.utils.UserStorage;
import de.uniks.stpmon.team_m.ws.EventListener;
import io.reactivex.rxjava3.core.Observable;

import java.util.List;
import java.util.prefs.Preferences;

import static org.mockito.Mockito.mock;

@Module
public class TestModule {
    @Provides
    static Preferences prefs() {
        return mock(Preferences.class);
    }

    @Provides
    ObjectMapper mapper() {
        return new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    }

    @Provides
    static EventListener eventListener() {
        return new EventListener(null, null) {
            @Override
            public <T> Observable<Event<T>> listen(String pattern, Class<T> type) {
                return Observable.empty();
            }

            @Override
            public void send(Object object) {
            }
        };
    }

    @Provides
    static AuthApiService authApiService() {
        return new AuthApiService() {
            @Override
            public Observable<LoginResult> login(LoginDto dto) {
                return Observable.just(new LoginResult("1",
                        "1",
                        "online",
                        null,
                        null,
                        "a1a2",
                        "a3a4"));
            }

            @Override
            public Observable<LoginResult> refresh(RefreshDto dto) {
                return Observable.just(new LoginResult("42", "42", "online", null, null, "123", "456"));
            }

            @Override
            public Observable<LogoutResult> logout() {
                return Observable.just(new LogoutResult());
            }
        };
    }

    @Provides
    static GroupsApiService groupsApiService() {
        return new GroupsApiService() {
            @Override
            public Observable<Group> create(CreateGroupDto dto) {
                return Observable.empty();
            }

            @Override
            public Observable<List<Group>> getGroups(String ids) {
                return Observable.just(
                        List.of(new Group("64610ec8420b3d786212aea8", "", List.of("64610e7b82ca062bfa5b7231", "64610e7b82ca062bfa5b7232")))
                );
            }

            @Override
            public Observable<Group> getGroup(String id) {
                return Observable.empty();
            }

            @Override
            public Observable<Group> update(String _id, UpdateGroupDto dto) {
                return Observable.empty();
            }

            @Override
            public Observable<Group> delete(String _id) {
                return Observable.empty();
            }
        };
    }

    @Provides
    static MessagesApiService messagesApiService() {
        return new MessagesApiService() {
            @Override
            public Observable<Message> create(String namespace, String parent, CreateMessageDto dto) {
                return Observable.empty();
            }

            @Override
            public Observable<List<Message>> getMessages(String namespace, String parent) {
                return Observable.empty();
            }

            @Override
            public Observable<Message> getMessage(String namespace, String parent, String id) {
                return Observable.empty();
            }

            @Override
            public Observable<Message> update(String namespace, String parent, String id, UpdateMessageDto dto) {
                return Observable.empty();
            }

            @Override
            public Observable<Message> delete(String namespace, String parent, String id) {
                return Observable.empty();
            }
        };
    }

    @Provides
    static RegionsApiService regionsApiService() {
        return new RegionsApiService() {
            @Override
            public Observable<List<Region>> getRegions() {
                return Observable.empty();
            }

            @Override
            public Observable<Region> getRegion(String id) {
                return Observable.empty();
            }
        };
    }

    @Provides
    static UsersApiService usersApiService() {
        return new UsersApiService() {
            @Override
            public Observable<User> createUser(CreateUserDto dto) {
                return Observable.just(new User("42", "Rick", "offline", null, null));
            }

            @Override
            public Observable<List<User>> getUsers(List<String> ids, String status) {
                return Observable.just(List.of(
                        new User("0815", "Morty", "online", null, null),
                        new User("Owen", "Morty", "online", null, null),
                        new User("2", "Morty", "online", null, null)
                ));
            }

            @Override
            public Observable<User> getUser(String id) {
                if (id.equals("0815")) {
                    return Observable.just(new User("0815", "Morty", "online", null, null));
                } else {
                    return Observable.just(new User("42", "Rick", "online", null, null));
                }
            }

            @Override
            public Observable<User> updateUser(String id, UpdateUserDto dto) {
                if (dto.status().equals("offline")) {
                    return Observable.just(new User("42", "Rick", "online", null, null));
                } else {
                    return Observable.just(new User("42", "Rick", "offline", null, null));
                }
            }

            @Override
            public Observable<User> deleteUser(String id) {
                return Observable.just(new User("423f8d731c386bcd2204da39", "Rick", "offline", null, null));
            }
        };
    }

    @Provides
    static UserStorage userStorage() {
        return new UserStorage() {
            @Override
            public List<String> getFriends() {
                return List.of("645cd04c11b590456276e9d9", "645cd086f249626b1eefa92e", "645cd0a34389d5c06620fe64");
            }

            @Override
            public String get_id() {
                return "645cd04c11b590456276e9d6";
            }
        };
    }

}
