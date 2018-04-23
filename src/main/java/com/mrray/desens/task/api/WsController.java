package com.mrray.desens.task.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class WsController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping(value = {"/", "/index", "/index.html"})
    public String index() {
        return "index";
    }

    @GetMapping("/test")
    @ResponseBody
    public void send() {
        Map<String, Object> notice = new HashMap<>();
        notice.put("progress", "已完成");
        notice.put("loads", "fdsafrew");
        notice.put("time", "00:05:01");
        messagingTemplate.convertAndSend("/topic/progress", notice);
    }
}