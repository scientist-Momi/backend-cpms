package dev.olaxomi.backend.mapper;

import dev.olaxomi.backend.dto.AdminActivityLogDto;
import dev.olaxomi.backend.model.AdminActivityLog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdminActivityLogMapper {
    @Autowired
    private ModelMapper modelMapper;

    public AdminActivityLogDto toDto(AdminActivityLog activityLog){
        return modelMapper.map(activityLog, AdminActivityLogDto.class);
    }

    public List<AdminActivityLogDto> toDtoList(List<AdminActivityLog> activityLogs) {
        return activityLogs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public AdminActivityLog fromDto(AdminActivityLogDto activityLogDto) {
        return modelMapper.map(activityLogDto, AdminActivityLog.class);
    }
}
