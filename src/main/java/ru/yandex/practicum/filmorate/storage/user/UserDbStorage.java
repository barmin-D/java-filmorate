package ru.yandex.practicum.filmorate.storage.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@Component("UserDbStorage")
public class UserDbStorage implements UserStorage {
    private static int idCounter = 1;
    private final String USER_REQUEST_SQL = "SELECT * FROM USERS WHERE id = ?";
    private final String USERS_ALL_SQL = "SELECT id, login, name, email, birthday FROM USERS";
    private final String INSERT_USERS_SQL = "INSERT INTO  USERS (login,name,email,birthday)" + "VALUES(?,?,?,?)";
    private final String UPDATE_USERS_SQL = "UPDATE USERS SET" +
            " login = ?, name = ?, email = ? , birthday= ? WHERE id = ?";
    private final String INSERT_FRIENDS_SQL = "INSERT INTO FRIENDS(user_id,friend_id,status) VALUES(?,?,0)";
    private final String DELETE_FRIENDS_SQL = "DELETE FROM FRIENDS WHERE (user_id = ? AND friend_id = ?) OR " +
            "(user_id = ? AND friend_id = ?)";
    private final String FRIENDS_BY_ID_SQL = "SELECT * FROM FRIENDS  fr LEFT JOIN USERS  u ON " +
            "fr.friend_id=u.id WHERE fr.user_id =?";
    private final String FRIENDS_SQL = "SELECT u.id, u.login, u.name, u.email, u.birthday FROM FRIENDS fr " +
            "LEFT JOIN FRIENDS f ON fr.friend_id = f.friend_id " +
            "LEFT JOIN USERS u ON fr.friend_id = u.id " +
            "WHERE fr.user_id=? AND f.user_id=? AND fr.friend_id=f.friend_id";
    private final Logger log = LoggerFactory.getLogger(UserDbStorage.class);
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static int getIdCounter() {
        return idCounter++;
    }

    @Override
    public Optional<User> getUserById(Integer userId) {

        SqlRowSet userRows = jdbcTemplate.queryForRowSet(USER_REQUEST_SQL, userId);
        if (userRows.next()) {
            log.info("Найден пользователь: {} {}", userRows.getString("id"),
                    userRows.getString("name"));
            User user = new User(
                    userRows.getInt("id"),
                    userRows.getString("login"),
                    userRows.getString("name"),
                    userRows.getString("email"),
                    LocalDate.parse(userRows.getString("birthday")));
            return Optional.of(user);
        } else {
            log.error("Пользователь с идентификатором {} не найден.", userId);
            throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
        }
    }

    @Override
    public Collection<User> findAll() {
        return jdbcTemplate.query(USERS_ALL_SQL, this::makeUser);
    }

    private User makeUser(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getInt("id"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .email(rs.getString("email"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }

    @Override
    public User create(User user) {
        validateUser(user);
        user.setId(getIdCounter());
        log.debug("Пользователь сохранен");
        jdbcTemplate.update(INSERT_USERS_SQL,
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                user.getBirthday());
        return user;
    }

    @Override
    public User update(User user) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(USER_REQUEST_SQL, user.getId());
        if (userRows.next()) {
            validateUser(user);
            log.debug("Пользователь изменен");
            jdbcTemplate.update(UPDATE_USERS_SQL,
                    user.getLogin(),
                    user.getName(),
                    user.getEmail(),
                    user.getBirthday(),
                    user.getId());
        } else {
            log.error("Такого пользователя нет");
            throw new UserNotFoundException("Такого пользователя нет");
        }
        return user;
    }

    private void validateUser(User user) {
        if (user.getLogin().length() == 0 || user.getLogin().contains(" ")) {
            log.error("Логин не может быть пустым и содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName().length() == 0) {
            log.debug("Имя заменилось логином");
            user.setName(user.getLogin());
        }
        if (user.getEmail().length() == 0 || !user.getEmail().contains("@")) {
            log.error("Электронная почта не может быть пустой и должна содержать символ @");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения не может быть в будущем");
            throw new ValidationException("дата рождения не может быть в будущем");
        }
    }

    @Override
    public void addFriendById(Integer userId, Integer friendId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(USER_REQUEST_SQL, userId);
        if (userRows.next()) {
            userRows = jdbcTemplate.queryForRowSet(USER_REQUEST_SQL, friendId);
            if (userRows.next()) {
                log.debug("Пользователь добавлен в друзья");
                jdbcTemplate.update(INSERT_FRIENDS_SQL, userId, friendId);
            } else {
                throw new UserNotFoundException(String.format("Пользователь %s не найден", friendId));
            }
        } else {
            throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
        }
    }

    @Override
    public void deleteFriendById(Integer userId, Integer friendId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(USER_REQUEST_SQL, userId);
        if (userRows.next()) {
            userRows = jdbcTemplate.queryForRowSet(USER_REQUEST_SQL, friendId);
            if (userRows.next()) {
                log.debug("Пользователь удален из друзей");
                jdbcTemplate.update(DELETE_FRIENDS_SQL, userId, friendId, friendId, userId);
            } else {
                throw new UserNotFoundException(String.format("Пользователь %s не найден", friendId));
            }
        } else {
            throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
        }
    }

    @Override
    public Collection<User> findFriendsById(Integer userId) {
        return jdbcTemplate.query(FRIENDS_BY_ID_SQL, this::makeUser, userId);
    }

    @Override
    public Collection<User> getFriends(Integer id, Integer otherId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(USER_REQUEST_SQL, id);
        if (userRows.next()) {
            userRows = jdbcTemplate.queryForRowSet(USER_REQUEST_SQL, otherId);
            if (userRows.next()) {

                return jdbcTemplate.query(FRIENDS_SQL, this::makeUser, id, otherId);
            } else {
                throw new UserNotFoundException(String.format("Пользователь %s не найден", otherId));
            }
        } else {
            throw new UserNotFoundException(String.format("Пользователь %s не найден", id));
        }
    }
}
