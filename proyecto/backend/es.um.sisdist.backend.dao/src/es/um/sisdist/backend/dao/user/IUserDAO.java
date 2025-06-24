package es.um.sisdist.backend.dao.user;

import java.util.List;
import java.util.Optional;

import es.um.sisdist.backend.dao.models.User;

public interface IUserDAO
{
    public Optional<User> getUserById(String id);

    public Optional<User> getUserByEmail(String id);

    public Optional<User> registerUser(String id, String name, String email, String hash_password);

    public Optional<List<String>> getConversationsIds(String user_id);

    public Optional<User> updateUser(User user);

    public void deleteUser(String id);
}
