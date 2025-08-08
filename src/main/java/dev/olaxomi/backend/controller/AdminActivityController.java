package dev.olaxomi.backend.controller;

import dev.olaxomi.backend.dto.AdminActivityLogDto;
import dev.olaxomi.backend.response.MessageResponse;
import dev.olaxomi.backend.service.AdminActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/v1/admin/activities")
@RestController
public class AdminActivityController {

    private final AdminActivityService activityService;

    public AdminActivityController(AdminActivityService activityService) {
        this.activityService = activityService;
    }

    @PreAuthorize("hasAuthority('VIEW_USER')")
    @GetMapping("/all")
    public ResponseEntity<MessageResponse> all(){
        List<AdminActivityLogDto> logs = activityService.allLogs();
        return ResponseEntity.ok(new MessageResponse("success", logs));
    }

    @PreAuthorize("hasAuthority('VIEW_USER')")
    @GetMapping("/{userId}")
    public ResponseEntity<MessageResponse> getById(@PathVariable Long userId){
        List<AdminActivityLogDto> logs = activityService.getById(userId);
        return ResponseEntity.ok(new MessageResponse("success", logs));
    }
}
