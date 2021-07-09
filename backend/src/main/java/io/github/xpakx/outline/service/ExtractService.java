package io.github.xpakx.outline.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.xpakx.outline.entity.dto.Author;
import io.github.xpakx.outline.entity.dto.GraphEntry;
import io.github.xpakx.outline.entity.dto.JsonLdAuthors;
import io.github.xpakx.outline.entity.dto.JsonLdGraph;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

        return getTitleFromH1s(doc, titleContent).orElse(
                getTitleFromMetaTags(doc, titleContent).orElse(
                        splitTitle(titleContent).orElse(
                                titleContent
                        )
                )
        );
    }

    private String getTitleFromUrl(URL url) {
        String path = url.getPath();
        List<String> candidates = Arrays.asList(path.split("/"));
        Collections.reverse(candidates);
        String regex = "\\d+";
        return candidates.stream()
                .filter((a) -> a.length() > MIN_LENGTH && !a.matches(regex))
                .map((a) -> a.replace("-", " "))
                .map((a) -> a.split("\\.")[0])
                .findAny()
                .orElse("");
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

        return metaAttrs.stream()
                .map((a) -> getOneByTagNameAndProperty(doc.head(), "meta", a, "og:title"))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter((a) -> a.hasAttr("content"))
                .map((a) -> a.attr("content").strip())
                .filter((a) -> a.length() > MIN_LENGTH && titleContent.contains(a) && a.length() < titleContent.length())
                .findAny();
    }

    private Optional<String> splitTitle(String titleContent) {
        List<String> splitters = Arrays.asList("|", "_", " » ", "/", " - ");
        return splitters.stream()
                .filter(titleContent::contains)
                .map((a) -> Arrays.asList(titleContent.split(a)))
                .map((a) -> a.stream()
                        .max(Comparator.comparingInt(String::length))
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }


    public Element extractContent(Document doc) {
        doc.getElementsByTag("script")
                        .forEach(Node::remove);

        List<Element> articles = doc.getElementsByTag("article");
        if(articles.size() == 1 && articles.get(0).text().length() > 250) {
            return articles.get(0);
        }

        List<Element> contentElems = doc.select("[class*=\"content\"]");
        if(contentElems.size() == 1) {
            return contentElems.get(0);
        }

        Optional<Element> parentContentElem = contentElems.stream()
                .filter((a) -> a.select("[class*=\"content\"]").size() + 1 == contentElems.size())
                .findAny();
        if(parentContentElem.isPresent()) return parentContentElem.get();

        List<Element> filteredContentElems = contentElems.stream()
                .filter((a) -> a.text().length() > 250)
                .filter((a) -> !a.attr("class").contains("comment"))
                .filter((a) -> a.parents().stream()
                        .noneMatch((b) -> b.attr("class").contains("content")))
                .collect(Collectors.toList());

        Element content = new Element(Tag.valueOf("div"), "");
        if(filteredContentElems.size() > 0) {
            filteredContentElems
                    .forEach(content::appendChild);
            return content;
        }

        return doc.body();
    }

    public String postprocessContent(Element doc) {
        doc.getElementsByTag("style")
                .forEach(Node::remove);
        doc.getElementsByTag("nav")
                .forEach(Node::remove);
        doc.select("[class*=\"comment\"]")
                .forEach(Node::remove);
        doc.select("[aria-label*=\"share\"]")
                .forEach(Node::remove);
        doc.select("[aria-label*=\"Share\"]")
                .forEach(Node::remove);

        final List<String> attributesList = Arrays.asList("class", "id");
        final List<String> valuesList = Arrays.asList("sidebar", "nav", "recomm",
                "menu", "footer", "header", "bottom", "top", "sponsor", "widget");
        for(String attr: attributesList) {
            for(String val : valuesList) {
                String selector = "[" + attr + "*=\"" + val + "\"]";
                List<Element> elems = doc.select(selector);
                elems.stream()
                        .filter((a) -> a.text()
                                .replace(" ", "")
                                .replace("\n", "")
                                .length() < 100)
                        .forEach(Node::remove);
            }
        }
        return doc.html();
    }

    public String extractDate(Document doc, URL url) {
        final List<String> metaPropertyValues = Arrays.asList("article:published_time", "og:published_time",
                "article:published_time", "rnews:datePublished");
        final List<String> metaNameValues = Arrays.asList("article:published_time", "article:publication_date",
                "OriginalPublicationDate", "article_date_original", "sailthru.date", "PublishDate",
                "publish_date");

        Optional<String> metaElemContent1 = getDateFromMeta(doc, metaPropertyValues, "property");
        if (metaElemContent1.isPresent()) return metaElemContent1.get();

        Optional<String> metaElemContent = getDateFromMeta(doc, metaNameValues, "name");
        if (metaElemContent.isPresent()) return metaElemContent.get();

        /* year starting with 20, then sth between 00 and 29, then optional separator
        then number between 00 and 39, then optional and without remembering group (?:):
        back reference to match with separator (\2) and number between 00 and 39
        */
        String urlDateRegex = "(20[0-2][0-9])([-_/]?)([0-3][0-9])\\2([0-3]?[0-9])?";
        Pattern pattern = Pattern.compile(urlDateRegex);
        Matcher matcher = pattern.matcher(url.getPath());

        String year = "";
        String month = "";
        while(matcher.find()) {
            if(matcher.group(2).equals("")) {
                if(matcher.group(0).length() == 6) {
                    if(year.equals("")) {
                        year = matcher.group(1);
                        month = matcher.group(3);
                    }
                } else {
                    return matcher.group(0);
                }
            } else {
                String[] splitDate = matcher.group(0).split(matcher.group(2));
                if(splitDate.length == 3) {
                    return matcher.group(0);
                }
                if(year.equals("")) {
                    year = matcher.group(1);
                    month = matcher.group(3);
                }
            }
        }

        String urlReverseDateRegex = "([0-3]?[0-9])([-_/])([0-3][0-9])\\2(20[0-2][0-9])";
        Pattern patternReverse = Pattern.compile(urlReverseDateRegex);
        Matcher matcherReverse = patternReverse.matcher(url.getPath());

        if(matcherReverse.find()) {
            return matcher.group(0);
        }

        List<String> elementSet = doc.select("[class*=\"date\"]")
                .stream()
                .filter((a) -> a.childrenSize() <= 1)
                .map((a) -> a.childrenSize() == 1 ? a.children().get(0) : a)
                .map((a) -> a.text().strip())
                .filter((a) -> a.length() > MIN_LENGTH)
                .distinct()
                .collect(Collectors.toList());

        if(elementSet.size() == 1) {
            return elementSet.stream()
                    .map((str) -> str
                            .replace("Posted", "")
                            .replace("posted", "")
                            .strip())
                    .findAny()
                    .get();
        }

        String stringDoc = doc.outerHtml();

        String patternHref = "<a.*?>";
        String patternLink = "<link.*?>";
        String patternImg = "<img.*?>";

        stringDoc = stringDoc.replaceAll(patternHref, "")
                .replaceAll(patternLink, "")
                .replaceAll(patternImg, "");

        String htmlDateRegex = "(20[0-2][0-9])([-_/])([0-3][0-9])\\2([0-3]?[0-9])";
        Pattern patternHtml = Pattern.compile(htmlDateRegex);
        Matcher matcherHtml = patternHtml.matcher(stringDoc);

        List<String> datesFromHtml = new ArrayList<>();
        while(matcherHtml.find()) {
            if(year.length() > 0) {
                if(year.equals(matcherHtml.group(1)) &&
                        month.equals(matcherHtml.group(3))) {
                    datesFromHtml.add(matcherHtml.group(1)+"/"+matcherHtml.group(3)+"/"+matcherHtml.group(4));
                }
            } else {
                datesFromHtml.add(matcherHtml.group(1)+"/"+matcherHtml.group(3)+"/"+matcherHtml.group(4));
            }
        }

        Matcher matcherHtmlReversed = patternReverse.matcher(stringDoc);
        while(matcherHtmlReversed.find()) {
            if(year.length() > 0) {
                if(year.substring(0,4).equals(matcherHtml.group(4)) &&
                        month.substring(4,6).equals(matcherHtml.group(3))) {
                    datesFromHtml.add(matcherHtml.group(4)+"/"+matcherHtml.group(3)+"/"+matcherHtml.group(1));
                }
            } else {
                datesFromHtml.add(matcherHtml.group(4)+"/"+matcherHtml.group(3)+"/"+matcherHtml.group(1));
            }
        }

        if(datesFromHtml.size() == 1) {
            return datesFromHtml.get(0);
        }

        datesFromHtml = datesFromHtml.stream().distinct()
                .collect(Collectors.toList());

        if(datesFromHtml.size() == 1) {
            return datesFromHtml.get(0);
        }

        return year.equals("") ? "" : year + "/" + month;
    }

    private Optional<String> getDateFromMeta(Document doc, List<String> metaValues, String property) {
        return metaValues.stream()
                .map((a) -> getOneByTagNameAndProperty(doc.head(), "meta", property, a))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter((a) -> a.hasAttr("content"))
                .map((a) -> a.attr("content").strip())
                .filter((a) -> a.length() > 0)
                .findAny();
    }

    private List<Element> getByTagNameAndProperty(Element element, String tag, String property, String value) {
        return element.getElementsByAttributeValue(property, value).stream()
                .filter(e -> e.tagName().equals(tag))
                .collect(Collectors.toList());
    }

    private Optional<Element> getOneByTagNameAndProperty(Element element, String tag, String property, String value) {
        return getByTagNameAndProperty(element, tag, property, value).stream()
                .findFirst();
    }

    public String extractAuthor(Document doc) {
        final List<String> metaPropertyValues = List.of("article:author");
        final List<String> metaNameValues = Arrays.asList("shareaholic:article_author_name", "byl",
                "sailthru.author", "author");

        return getAnyMetaValue(doc, metaNameValues, "name").orElse(
                getAnyMetaValue(doc, metaPropertyValues, "property").orElse(
                        getAuthorsFromJsonLd(doc).orElse(
                                getAuthorsFromLinkRels(doc).orElse(
                                        getAuthorsFromTagsWithClassContainingAuthor(doc).orElse("")
                                )
                        )
                )
        );
    }

    private Optional<String> getAuthorsFromTagsWithClassContainingAuthor(Document doc) {
        Element authorElem = doc.selectFirst("[class*=\"author\"]");
        if(authorElem != null) {
            List<Element> children = authorElem.children();
            if(children.size() == 0) {
                String authorElemContent = authorElem.text().strip();
                if(authorElemContent.length() > MIN_LENGTH) return Optional.of(authorElemContent);
            } else if(children.size() == 1 && children.get(0).childrenSize() == 0) {
                String authorElemContent = children.get(0).text().strip();
                if(authorElemContent.length() > MIN_LENGTH) return Optional.of(authorElemContent);
            }

            List<Element> nameElements = authorElem.select("[class*=\"name\"]");
            nameElements.addAll(authorElem.select("[id*=\"name\"]"));
            nameElements.addAll(authorElem.select("[itemprop*=\"name\"]"));

            String authorsFromNameElements = nameElements.stream()
                    .filter((a) -> a.childrenSize() == 0 || (a.childrenSize() == 1 && a.children().get(0).childrenSize() == 0))
                    .map((a) -> a.childrenSize() == 0 ? a.text().strip() : a.children().get(0).text().strip())
                    .filter((a) -> a.length() > MIN_LENGTH)
                    .distinct()
                    .collect(Collectors.joining());
            if(authorsFromNameElements.length() > 0) {
                return Optional.of(authorsFromNameElements);
            }
        }
        return Optional.empty();
    }

    private Optional<String> getAuthorsFromLinkRels(Document doc) {
        String authors = getByTagNameAndProperty(doc, "a", "rel", "author").stream()
                .map(Element::text)
                .filter((a) -> !a.equals(""))
                .collect(Collectors.joining(","));
        return authors.length() > 0 ? Optional.of((authors)) : Optional.empty();
    }

    private Optional<String> getAuthorsFromJsonLd(Document doc) {
        Optional<Element> jsonld = getOneByTagNameAndProperty(doc,"script", "type", "application/ld+json");

        if(jsonld.isPresent()) {
            ObjectMapper om = new JsonMapper();
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            try {
                JsonLdAuthors multipleAuthors = om.readValue(jsonld.get().text(), JsonLdAuthors.class);
                if (multipleAuthors.getAuthor() != null) {
                    String authors = multipleAuthors.getAuthor().stream()
                            .map(Author::getName)
                            .collect(Collectors.joining());
                    if (authors.length() > 0) return Optional.of(authors);
                }
            } catch (JsonProcessingException ignored) {
                return Optional.empty();
            }

            try {
                JsonLdGraph graph = om.readValue(jsonld.get().text(), JsonLdGraph.class);
                if (graph.getGraph() != null) {
                    String authors = graph.getGraph().stream()
                            .filter((a) -> a.getType().contains("Person"))
                            .map(GraphEntry::getName)
                            .filter(Objects::nonNull)
                            .filter((a) -> a.length() > 0)
                            .collect(Collectors.joining());
                    if (authors.length() > 0) return Optional.of(authors);
                }
            } catch (JsonProcessingException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private Optional<String> getAnyMetaValue(Document doc, List<String> metaNameValues, String name) {
        return metaNameValues.stream()
                .map((a) -> getAuthorFromMeta(doc, a, name))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }

    private Optional<String> getAuthorFromMeta(Document doc, String value, String name) {
        return getOneByTagNameAndProperty(doc.head(), "meta", name, value).stream()
                .filter((a) -> a.hasAttr("content") || a.hasAttr("value"))
                .map((a) -> a.hasAttr("content") ?
                        a.attr("content").strip() : a.attr("value").strip())
                .filter((a) -> !a.contains("http"))
                .findAny();
    }
}
