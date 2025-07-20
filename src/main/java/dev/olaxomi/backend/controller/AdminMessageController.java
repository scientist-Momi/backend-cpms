package dev.olaxomi.backend.controller;

import dev.olaxomi.backend.dto.AdminMessageDto;
import dev.olaxomi.backend.service.AdminMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/admin-messages")
public class AdminMessageController {
    private final AdminMessageService service;

    public AdminMessageController(AdminMessageService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AdminMessageDto> sendMessage(@RequestBody AdminMessageDto dto) {
        return ResponseEntity.ok(service.sendMessage(dto));
    }

    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<List<AdminMessageDto>> getMessagesForRecipient(@PathVariable Integer recipientId) {
        return ResponseEntity.ok(service.getMessagesForRecipient(recipientId));
    }
}
