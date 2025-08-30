package dev.olaxomi.backend.service;

import dev.olaxomi.backend.dto.AdminActivityLogDto;
import dev.olaxomi.backend.enums.ActionType;
import dev.olaxomi.backend.enums.Permission;
import dev.olaxomi.backend.enums.TargetType;
import dev.olaxomi.backend.mapper.AdminActivityLogMapper;
import dev.olaxomi.backend.model.AdminActivityLog;
import dev.olaxomi.backend.model.User;
import dev.olaxomi.backend.repository.AdminActivityRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
            ActionType actionType,
            TargetType targetType,
            String targetId,
            String details
    ) {
        AdminActivityLog log = new AdminActivityLog();

        User currentUser = getCurrentUser();
        String ipAddress = getRequestIp();

        log.setUser(currentUser);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(details);
        log.setIpAddress(ipAddress);
        adminActivityRepository.save(log);
    }

    public void logActivity(
            User actor,
            ActionType actionType,
            TargetType targetType,
            String targetId,
            String details
    ) {
        AdminActivityLog log = new AdminActivityLog();
        log.setUser(actor);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(details);
        log.setIpAddress(getRequestIp());
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

    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }

    private String getRequestIp() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getRemoteAddr();
    }

}
