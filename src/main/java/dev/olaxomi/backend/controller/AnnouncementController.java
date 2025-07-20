package dev.olaxomi.backend.controller;

import dev.olaxomi.backend.dto.AnnouncementDto;
import dev.olaxomi.backend.service.AnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/announcements")
public class AnnouncementController {
    private final AnnouncementService service;

    public AnnouncementController(AnnouncementService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AnnouncementDto> createAnnouncement(@RequestBody AnnouncementDto dto) {
        return ResponseEntity.ok(service.createAnnouncement(dto));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<AnnouncementDto>> getAnnouncementsByAuthor(@PathVariable Integer authorId) {
        return ResponseEntity.ok(service.getAnnouncementsByAuthor(authorId));
    }
}
