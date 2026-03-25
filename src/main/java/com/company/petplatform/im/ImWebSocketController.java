package com.company.petplatform.im;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ImWebSocketController {

  private final SimpMessagingTemplate simpMessagingTemplate;

  public ImWebSocketController(SimpMessagingTemplate simpMessagingTemplate) {
    this.simpMessagingTemplate = simpMessagingTemplate;
  }

  @MessageMapping("/im.send")
  public void send(@Payload ImWsMessage message) {
    simpMessagingTemplate.convertAndSend("/topic/im", message);
  }
}
