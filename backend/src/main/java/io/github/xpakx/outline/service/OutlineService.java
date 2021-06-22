package io.github.xpakx.outline.service;

import io.github.xpakx.outline.entity.Link;
import io.github.xpakx.outline.entity.dto.LinkDto;
import io.github.xpakx.outline.entity.dto.OutlineRequest;
import io.github.xpakx.outline.error.NotFoundException;
import io.github.xpakx.outline.repo.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OutlineService {
    private final LinkService linkService;
    private final LinkRepository linkRepository;

    @Autowired
    public OutlineService(LinkService linkService, LinkRepository linkRepository) {
        this.linkService = linkService;
        this.linkRepository = linkRepository;
    }

    public String addLink(OutlineRequest request) {
        Link newLink = new Link();
        newLink.setLongUrl(request.getUrl());

        return linkService.encode(
                linkRepository
                        .save(newLink)
                        .getId()
        );
    }

    public LinkDto getLink(String shortUrl) {
        return linkRepository.getProjectedById(
                linkService.decode(shortUrl)
        ).orElseThrow(
                () -> new NotFoundException("No link for short url " + shortUrl + " found")
        );
    }
}
