package servidor.aplicacion.util;

import servidor.aplicacion.dto.UserDTO;
import servidor.aplicacion.model.User;
import java.util.List;
import java.util.stream.Collectors;

public class UserConverter {

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt());
    }

    public static User fromDTO(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setCreatedAt(dto.getCreatedAt());

        return user;
    }

    public static List<UserDTO> toDTOList(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(UserConverter::toDTO)
                .collect(Collectors.toList());
    }
}