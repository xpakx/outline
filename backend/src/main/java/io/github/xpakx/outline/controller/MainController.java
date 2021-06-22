package io.github.xpakx.outline.controller;

import io.github.xpakx.outline.entity.dto.LinkDto;
import io.github.xpakx.outline.entity.dto.OutlineRequest;
import io.github.xpakx.outline.service.OutlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
        return outlineService.addLink(request);
    }

    @GetMapping("/{shortUrl}")
    @ResponseBody
    public LinkDto getLink(@PathVariable String shortUrl) {
        return outlineService.getLink(shortUrl);
    }
}
