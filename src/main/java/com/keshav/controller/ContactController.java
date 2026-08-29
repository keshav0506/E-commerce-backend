package com.keshav.controller;

import com.keshav.dto.ContactRequestDTO;
import com.keshav.entity.ContactMessage;
import com.keshav.repository.ContactMessageRepository;
import com.keshav.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ContactController {

    private final ContactMessageRepository contactMessageRepository;
    private final EmailService emailService;

    public ContactController(ContactMessageRepository contactMessageRepository, EmailService emailService) {
        this.contactMessageRepository = contactMessageRepository;
        this.emailService = emailService;
    }

    @PostMapping("/contact")
    public ResponseEntity<Map<String, Object>> submitContactMessage(
            @Valid @RequestBody ContactRequestDTO request) {

        ContactMessage msg = new ContactMessage();
        msg.setName(request.getName());
        msg.setEmail(request.getEmail());
        msg.setSubject(request.getSubject() != null ? request.getSubject() : "General Inquiry");
        msg.setMessage(request.getMessage());
        msg.setStatus("PENDING");

        ContactMessage saved = contactMessageRepository.save(msg);
        String ticketId = "TKT-" + saved.getId();

        // Asynchronously dispatch email to admin
        emailService.sendContactInquiryNotification(
                ticketId,
                saved.getName(),
                saved.getEmail(),
                saved.getSubject(),
                saved.getMessage()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Thank you for reaching out! Our support team will get back to you within 24 hours.");
        response.put("ticketId", ticketId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/contact-messages")
    public ResponseEntity<List<ContactMessage>> getAllContactMessages() {
        return ResponseEntity.ok(contactMessageRepository.findAllByOrderByCreatedAtDesc());
    }
}
