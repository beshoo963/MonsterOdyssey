package de.uniks.stpmon.team_m.service;

import de.uniks.stpmon.team_m.dto.CreateUserDto;
import de.uniks.stpmon.team_m.dto.UpdateUserDto;
import de.uniks.stpmon.team_m.dto.User;
import de.uniks.stpmon.team_m.rest.UsersApiService;
import de.uniks.stpmon.team_m.utils.UserStorage;
import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static de.uniks.stpmon.team_m.Constants.USER_STATUS_ONLINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

    @Mock
    UsersApiService usersApiService;
    @Mock
    UserStorage userStorage;
    @InjectMocks
    UsersService usersService;

    @Test
    void createUser() {
        //Successful sign Up of user

        //define mocks
        when(usersApiService.createUser(ArgumentMatchers.any()))
                .thenReturn(Observable.just(new User(
                        "423f8d731c386bcd2204da39",
                        "UserCreate",
                        "online",
                        null,
                        null
                )));

        //Sign Up of User
        final User user = usersService.createUser("1", null, "12345678").blockingFirst();

        //Check for successful Sign Up
        assertEquals("423f8d731c386bcd2204da39", user._id());

        verify(usersApiService).createUser(new CreateUserDto("1", null, "12345678"));
    }

    @Test
    void updateUserTest() {
        // define mock
        when(userStorage.get_id()).thenReturn("423f8d731c386bcd2204da39");
        when(usersApiService.updateUser(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(Observable.just(new User(
                        null,
                        null,
                        "online",
                        null,
                        null
                )));

        // update user
        final User user = usersService.updateUser(null, "online", null, null, null).blockingFirst();

        // check for successful update
        assertEquals("online", user.status());

        verify(usersApiService).updateUser("423f8d731c386bcd2204da39", new UpdateUserDto(null, "online", null, null, null));
    }

    @Test
    void getUsersTest() {
        // define mock
        when(usersApiService.getUsers(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(Observable.just(Arrays.asList(
                        new User("1", "11", "1", "1", null),
                        new User("2", "22", "2", "2", null),
                        new User("3", "33", "3", "3", null)
                )));

        // get users
        final List<User> users = usersApiService.getUsers(ArgumentMatchers.any(), ArgumentMatchers.any()).blockingFirst();

        // check for successful get
        assertEquals("1", users.get(0)._id());

        verify(usersApiService).getUsers(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void updateUsername() {
        //Successful change the Username of user

        //define mocks

        when(userStorage.get_id()).thenReturn("423f8d731c386bcd2204da39");
        when(usersApiService.updateUser(any(), any()))
                .thenReturn(Observable.just(new User(
                        "423f8d731c386bcd2204da39",
                        "UserPatch",
                        "online",
                        null,
                        null)));
        final User user = usersService.updateUser("UserPatch", null, null, null, null).blockingFirst();

        assertEquals("UserPatch", user.name());

        verify(usersApiService).updateUser("423f8d731c386bcd2204da39", new UpdateUserDto("UserPatch", null, null, null, null));
    }

    @Test
    void updatePassword() {
        //Successful change the Username of user

        //define mocks
        when(userStorage.get_id()).thenReturn("423f8d731c386bcd2204da39");
        when(usersApiService.updateUser(any(), any()))
                .thenReturn(Observable.just(new User(
                        "423f8d731c386bcd2204da39",
                        "UserPatch",
                        USER_STATUS_ONLINE,
                        null,
                        null)));
        final User user = usersService.updateUser(null, null, null, null, "12345678").blockingFirst();

        assertEquals("UserPatch", user.name());

        verify(usersApiService).updateUser("423f8d731c386bcd2204da39", new UpdateUserDto(null, null, null, null, "12345678"));
    }

    @Test
    void deletePassword() {
        //Successful delete the user

        //define mocks
        when(userStorage.get_id()).thenReturn("423f8d731c386bcd2204da39");
        when(usersApiService.deleteUser(anyString()))
                .thenReturn(Observable.just(new User(
                        "423f8d731c386bcd2204da39",
                        "UserDelete",
                        USER_STATUS_ONLINE,
                        null,
                        null)));

        final User user = usersService.deleteUser().blockingFirst();

        assertEquals("UserDelete", user.name());

        verify(usersApiService).deleteUser("423f8d731c386bcd2204da39");
    }

    @Test
    void getUser() {
        when(usersApiService.getUser(anyString()))
                .thenReturn(Observable.just(new User(
                        "423f8d731c386bcd2204da39",
                        "UserGet",
                        USER_STATUS_ONLINE,
                        null,
                        null)));

        final User user = usersService.getUser("423f8d731c386bcd2204da39").blockingFirst();

        assertEquals("UserGet", user.name());

        verify(usersApiService).getUser("423f8d731c386bcd2204da39");
    }
}