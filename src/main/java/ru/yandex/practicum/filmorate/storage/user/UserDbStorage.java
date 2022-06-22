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

        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE id = ?", userId);
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
            log.info("Пользователь с идентификатором {} не найден.", userId);
            throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
        }
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT id, login, name, email, birthday FROM USERS";
        return jdbcTemplate.query(sql, this::makeUser);
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
        user.setId(getIdCounter());
        log.debug("Пользователь сохранен");
        String sqlQuery = "INSERT INTO  USERS (login,name,email,birthday)" + "VALUES(?,?,?,?)";
        jdbcTemplate.update(sqlQuery,
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                user.getBirthday());
        return user;
    }

    @Override
    public User update(User user) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE id = ?", user.getId());
        if (userRows.next()) {
            if (user.getLogin().length() == 0 || user.getLogin().contains(" ")) {
                log.error("Логин не может быть пустым и содержать пробелы");
                throw new ValidationException("Логин не может быть пустым и содержать пробелы");
            }
            if (user.getName().length() == 0) {
                log.debug("Имя заменилось логином");
            }
            if (user.getEmail().length() == 0 || !user.getEmail().contains("@")) {
                log.error("Электронная почта не может быть пустой и должна содержать символ @");
                throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
            }
            if (user.getBirthday().isAfter(LocalDate.now())) {
                log.error("Дата рождения не может быть в будущем");
                throw new ValidationException("дата рождения не может быть в будущем");
            }
            log.debug("Пользователь изменен");
            String sqlQuery = "UPDATE USERS SET " +
                    "login = ?, name = ?, email = ? , birthday= ? " +
                    "WHERE id = ?";
            jdbcTemplate.update(sqlQuery,
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

    @Override
    public void addFriendById(Integer userId, Integer friendId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE id = ?", userId);
        if (userRows.next()) {
            userRows = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE id = ?", friendId);
            if (userRows.next()) {
                log.debug("Пользователь добавлен в друзья");
                String sqlQuery = "INSERT INTO FRIENDS(user_id,friend_id,status)" + "VALUES(?,?,0)";
                jdbcTemplate.update(sqlQuery, userId, friendId);
            } else {
                throw new UserNotFoundException(String.format("Пользователь %s не найден", friendId));
            }
        } else {
            throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
        }
    }

    @Override
    public void deleteFriendById(Integer userId, Integer friendId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE id = ?", userId);
        if (userRows.next()) {
            userRows = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE id = ?", friendId);
            if (userRows.next()) {
                log.debug("Пользователь удален из друзей");
                String sqlQuery = "DELETE FROM FRIENDS WHERE (user_id = ? AND friend_id = ?) OR " +
                        "(user_id = ? AND friend_id = ?)";
                jdbcTemplate.update(sqlQuery, userId, friendId, friendId, userId);
            } else {
                throw new UserNotFoundException(String.format("Пользователь %s не найден", friendId));
            }
        } else {
            throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
        }
    }

    @Override
    public Collection<User> findFriendsById(Integer userId) {
        String sql = "SELECT * FROM FRIENDS  fr " +
                "LEFT JOIN USERS  u ON fr.friend_id=u.id WHERE fr.user_id =?";
        return jdbcTemplate.query(sql, this::makeUser, userId);
    }

    @Override
    public Collection<User> getFriends(Integer id, Integer otherId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE id = ?", id);
        if (userRows.next()) {
            userRows = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE id = ?", otherId);
            if (userRows.next()) {
                String sql = "SELECT u.id, u.login, u.name, u.email, u.birthday FROM FRIENDS fr " +
                        "LEFT JOIN FRIENDS f ON fr.friend_id = f.friend_id " +
                        "LEFT JOIN USERS u ON fr.friend_id = u.id " +
                        "WHERE fr.user_id=? AND f.user_id=? AND fr.friend_id=f.friend_id";
                return jdbcTemplate.query(sql, this::makeUser, id, otherId);
            } else {
                throw new UserNotFoundException(String.format("Пользователь %s не найден", otherId));
            }
        } else {
            throw new UserNotFoundException(String.format("Пользователь %s не найден", id));
        }
    }
}
