package io.github.xpakx.outline.controller;

import io.github.xpakx.outline.entity.dto.OutlineRequest;
import io.github.xpakx.outline.service.LinkService;
import io.github.xpakx.outline.service.OutlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {
    private final OutlineService outlineService;

    @Autowired
    public MainController(OutlineService outlineService) {
        this.outlineService = outlineService;
    }

    @PostMapping("/outline")
    @ResponseBody
    public String addLink(@RequestBody OutlineRequest request) {
        return "";
    }
}
