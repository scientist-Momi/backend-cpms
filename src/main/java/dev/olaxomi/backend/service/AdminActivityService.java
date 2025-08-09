package dev.olaxomi.backend.service;

import dev.olaxomi.backend.dto.AdminActivityLogDto;
import dev.olaxomi.backend.enums.ActionType;
import dev.olaxomi.backend.enums.Permission;
import dev.olaxomi.backend.enums.TargetType;
import dev.olaxomi.backend.mapper.AdminActivityLogMapper;
import dev.olaxomi.backend.model.AdminActivityLog;
import dev.olaxomi.backend.model.User;
import dev.olaxomi.backend.repository.AdminActivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminActivityService {
    private final AdminActivityRepository adminActivityRepository;
    private final AdminActivityLogMapper adminActivityLogMapper;

    public AdminActivityService(AdminActivityRepository adminActivityRepository, AdminActivityLogMapper adminActivityLogMapper) {
        this.adminActivityRepository = adminActivityRepository;
        this.adminActivityLogMapper = adminActivityLogMapper;
    }

    public void logActivity(
            User user,
            ActionType actionType,
            TargetType targetType,
            String targetId,
            String details,
            String ip
    ) {
        AdminActivityLog log = new AdminActivityLog();
        log.setUser(user);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(details);
        log.setIpAddress(ip);
        adminActivityRepository.save(log);
    }

    public List<AdminActivityLogDto> allLogs(){
        List<AdminActivityLog> initLogs = adminActivityRepository.findAllOrderByCreatedAtDesc();
        return adminActivityLogMapper.toDtoList(initLogs);
    }

    public List<AdminActivityLogDto> getById(Long userId){
        List<AdminActivityLog> initLogs = adminActivityRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return adminActivityLogMapper.toDtoList(initLogs);
    }

}
