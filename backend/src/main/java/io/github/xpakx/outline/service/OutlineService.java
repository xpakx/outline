package io.github.xpakx.outline.service;

import io.github.xpakx.outline.entity.Link;
import io.github.xpakx.outline.entity.dto.LinkDto;
import io.github.xpakx.outline.entity.dto.OutlineRequest;
import io.github.xpakx.outline.entity.dto.OutlineResponse;
import io.github.xpakx.outline.error.NotFoundException;
import io.github.xpakx.outline.error.UrlLoadingException;
import io.github.xpakx.outline.repo.LinkRepository;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class OutlineService {
    private final LinkService linkService;
    private final LinkRepository linkRepository;
    private final UrlReaderService urlReaderService;
    private final ExtractService extractService;

    @Autowired
    public OutlineService(LinkService linkService, LinkRepository linkRepository, UrlReaderService urlReaderService, ExtractService extractService) {
        this.linkService = linkService;
        this.linkRepository = linkRepository;
        this.urlReaderService = urlReaderService;
        this.extractService = extractService;
    }

    public OutlineResponse addLink(OutlineRequest request) {
        Link newLink = new Link();
        newLink.setLongUrl(request.getUrl());

        String pageContent = "";

        try {
            pageContent = urlReaderService.read(new URL(request.getUrl()));
        } catch(MalformedURLException ex) {
            throw new UrlLoadingException("Malformed url!");
        } catch(IOException ex) {
            throw new UrlLoadingException("Error while loading data from url!");
        }

        try {
            URL url = new URL(request.getUrl());
            Document pageDocument = extractService.parse(pageContent);
            newLink.setTitle(extractService.extractTitle(pageDocument, url));
            newLink.setContent(
                    extractService.postprocessContent(
                            extractService.extractContent(pageDocument),
                            url
                    )
            );
            newLink.setDate(extractService.extractDate(pageDocument, url));
            newLink.setAuthor(extractService.extractAuthor(pageDocument));
        } catch(IOException ex) {
            throw new UrlLoadingException("Malformed url!");
        }

        String shortUrl = linkService.encode(
                linkRepository
                        .save(newLink)
                        .getId()
        );
        OutlineResponse response = new OutlineResponse();
        response.setShortUrl(shortUrl);
        return response;
    }

    public LinkDto getLink(String shortUrl) {
        return linkRepository.getProjectedById(
                linkService.decode(shortUrl)
        ).orElseThrow(
                () -> new NotFoundException("No link for short url " + shortUrl + " found")
        );
    }
}
