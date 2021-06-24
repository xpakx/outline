package io.github.xpakx.outline.service;

import io.github.xpakx.outline.entity.Link;
import io.github.xpakx.outline.entity.dto.LinkDto;
import io.github.xpakx.outline.entity.dto.OutlineRequest;
import io.github.xpakx.outline.error.NotFoundException;
import io.github.xpakx.outline.error.UrlLoadingException;
import io.github.xpakx.outline.repo.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.jsoup.nodes.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
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

    public String addLink(OutlineRequest request) {
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
            Document pageDocument = extractService.parse(pageContent);
            newLink.setTitle(extractService.extractTitle(pageDocument, new URL(request.getUrl())));
            newLink.setContent(extractService.extractContent(pageDocument));
            newLink.setDate(extractService.extractDate(pageDocument));
        } catch(IOException ex) {
            throw new UrlLoadingException("Parsing error" + ex.getMessage());
        }

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
