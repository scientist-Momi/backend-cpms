package dev.olaxomi.backend.mapper;

import dev.olaxomi.backend.dto.UserDto;
import dev.olaxomi.backend.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    @Autowired
    private ModelMapper modelMapper;

    public UserDto toDto(User user){
        return modelMapper.map(user, UserDto.class);
    }

    public List<UserDto> toDtoList(List<User> users) {
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    public User fromDto(UserDto userDto) {
        return modelMapper.map(userDto, User.class);
    }
}
