package com.company.petplatform.im;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/im")
public class ImController {

  private final ImService imService;

  public ImController(ImService imService) {
    this.imService = imService;
  }

  @GetMapping("/ws-ticket")
  public ImDtos.WsTicketResponse wsTicket() {
    return imService.wsTicket();
  }

  @PostMapping("/sessions")
  public ImDtos.SessionResponse createSession(@Valid @RequestBody ImDtos.CreateSessionRequest request) {
    return imService.createSession(request);
  }

  @PostMapping("/sessions/{sessionId}/messages")
  public ImDtos.MessageResponse sendMessage(@PathVariable Long sessionId, @Valid @RequestBody ImDtos.SendMessageRequest request) {
    return imService.sendMessage(sessionId, request);
  }

  @PostMapping("/messages/{messageId}/recall")
  public ImDtos.MessageResponse recall(@PathVariable Long messageId, @RequestBody(required = false) ImDtos.RecallRequest request) {
    return imService.recallMessage(messageId);
  }

  @PostMapping("/sessions/{sessionId}/read")
  public ImDtos.UnreadResponse markRead(@PathVariable Long sessionId, @Valid @RequestBody ImDtos.ReadRequest request) {
    return imService.markRead(sessionId, request);
  }

  @GetMapping("/sessions/{sessionId}/unread")
  public ImDtos.UnreadResponse unread(@PathVariable Long sessionId) {
    return imService.unread(sessionId);
  }
}
