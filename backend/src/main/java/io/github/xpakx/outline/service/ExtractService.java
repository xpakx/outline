package io.github.xpakx.outline.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExtractService {
    final int MIN_LENGTH = 2;

    public Document parse(String input) {
        return Jsoup.parse(input);
    }

    public String extractTitle(Document doc, URL url) {
        String titleContent = doc.title();

        if(titleContent.length() == 0) {
            return getTitleFromUrl(url);
        }

        return getTitleFromH1s(doc, titleContent)
                .orElse(
                        getTitleFromMetaTags(doc, titleContent)
                        .orElse(
                                splitTitle(titleContent)
                                .orElse(titleContent)
                        )
                );
    }

    private String getTitleFromUrl(URL url) {
        String path = url.getPath();
        List<String> candidates = Arrays.asList(path.split("/"));
        Collections.reverse(candidates);
        String regex = "\\d+";
        for(String candidate : candidates) {
            if(candidate.length() > MIN_LENGTH && !candidate.matches(regex)) {
                candidate = candidate.replace("-", " ");
                return candidate.split("\\.")[0];
            }
        }
        return "";
    }

    private Optional<String> getTitleFromH1s(Document doc, String titleContent) {
        List<Element> h1Elems = doc.getElementsByTag("h1");
        List<String> h1Candidates = new ArrayList<>();

        for(Element elem : h1Elems) {
            String h1Title = elem.text().strip();
            if(h1Title.equalsIgnoreCase(titleContent)) {
                return Optional.of(titleContent);
            }
            if(h1Title.length() > MIN_LENGTH && titleContent.contains(h1Title)) {
                h1Candidates.add(h1Title);
            }
        }

        if(h1Candidates.size() > 0) {
            h1Candidates.sort(Comparator.comparingInt(String::length).reversed());
            return Optional.of(h1Candidates.get(0));
        }

        return Optional.empty();
    }

    private Optional<String> getTitleFromMetaTags(Document doc, String titleContent) {
        List<String> metaAttrs = Arrays.asList("property", "name");

        for(String attr : metaAttrs) {
            Optional<Element> metaElem =
                    getOneByTagNameAndProperty(doc.head(), "meta", attr, "og:title");
            if (metaElem.isPresent()) {
                String metaElemContent = metaElem.get().attr("content").strip();
                if (metaElemContent.length() > MIN_LENGTH && titleContent.contains(metaElemContent) && metaElemContent.length() < titleContent.length()) {
                    return Optional.of(metaElemContent);
                }
            }
        }

        return Optional.empty();
    }

    private Optional<String> splitTitle(String titleContent) {
        List<String> splitters = Arrays.asList("|", "_", " » ", "/", " - ");
        for(String splitter : splitters) {
            if(titleContent.contains(splitter)) {
                List<String> splitTitles = Arrays.asList(titleContent.split(splitter));
                splitTitles.sort(Comparator.comparingInt(String::length).reversed());
                return Optional.of(splitTitles.get(0));
            }
        }
        return Optional.empty();
    }


    public String extractContent(Document doc) {
        return doc.body().text();
    }

    public String extractDate(Document doc) {
        List<String> metaPropertyValues = Arrays.asList("article:published_time", "og:published_time",
                "article:published_time", "rnews:datePublished");
        List<String> metaNameValues = Arrays.asList("article:published_time", "article:publication_date",
                "OriginalPublicationDate", "article_date_original", "sailthru.date", "PublishDate",
                "publish_date");

        Optional<String> metaElemContent1 = getDateFromMeta(doc, metaPropertyValues, "property");
        if (metaElemContent1.isPresent()) return metaElemContent1.get();

        Optional<String> metaElemContent = getDateFromMeta(doc, metaNameValues, "name");
        if (metaElemContent.isPresent()) return metaElemContent.get();

        return "";
    }

    private Optional<String> getDateFromMeta(Document doc, List<String> metaValues, String property) {
        for (String value : metaValues) {
            Optional<Element> metaElem =
                    getOneByTagNameAndProperty(doc.head(), "meta", property, value);
            if (metaElem.isPresent()) {
                String metaElemContent = metaElem.get().attr("content").strip();
                if (metaElemContent.length() > 0) {
                    return Optional.of(metaElemContent);
                }
            }
        }
        return Optional.empty();
    }

    private List<Element> getByTagNameAndProperty(Element element, String tag, String property, String value) {
        return element.getElementsByAttributeValue(property, value).stream()
                .filter(e -> e.tagName().equals(tag))
                .collect(Collectors.toList());
    }

    private Optional<Element> getOneByTagNameAndProperty(Element element, String tag, String property, String value) {
        List<Element> elemList = getByTagNameAndProperty(element, tag, property, value);
        if(elemList.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(elemList.get(0));
    }
}
