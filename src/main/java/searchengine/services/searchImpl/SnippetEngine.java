package searchengine.services.searchImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.services.indexImpl.MorphologyUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Slf4j
public class SnippetEngine {
    private static final String SNIPPET_ENDED_WITH = " ... ";
    private static final String DEV_SYMBOLS = "</>=\"";
    private static final String PHRASE_PATTERN = "([" + DEV_SYMBOLS + "\\W\\w][^" + DEV_SYMBOLS + "]+)[" + DEV_SYMBOLS + "]";

    private String content;
    private MorphologyUtils morphologyUtils;
    private Set<String> lemmas;
    private final int snippetMaxLength;

    public boolean initialize(SearchPage searchPage) {
        log.info("Инициализация поиска сниппетов для страницы: " + searchPage.getPage() + " ...");
        if (!searchPage.checkPage() || !searchPage.checkLemmas()) {
            return false;
        }
        try {
            morphologyUtils = MorphologyUtils.getInstance();
        } catch (IOException e) {
            log.error("\tне удалось подключить библиотеку русских слов для формирования сниппета: " + e);
            return false;
        }
        content = searchPage.getPage().getContent();
        lemmas = searchPage.getLemmaRanks().keySet();
        return true;
    }

    private String findPhrasesAndPut() {
        Pattern pattern = Pattern.compile(PHRASE_PATTERN);
        Matcher matcher = pattern.matcher(content);
        StringBuilder result = new StringBuilder();
        List<String> snippets = new ArrayList<>();
        while (matcher.find()) {
            String phrase = preparePhrase(matcher.group(1));
            if (phrase.isBlank() || snippets.contains(phrase)) {
                continue;
            }
            snippets.add(phrase);
            result.append(substringPhrase(phrase, snippetMaxLength - result.length())).append(SNIPPET_ENDED_WITH);
            log.info("\t- " + phrase);
            if (result.length() >= snippetMaxLength - 10) {
                break;
            }
        }
        return result.toString();
    }

    private String substringPhrase(String phrase, int length) {
        if (phrase.length() >= length - SNIPPET_ENDED_WITH.length()) {
            return phrase.substring(0, length - SNIPPET_ENDED_WITH.length());
        }
        return phrase;
    }

    private String preparePhrase(String text) {
        String phrase = text
                .replaceAll("[" + DEV_SYMBOLS + "]+","")
                .replaceAll("\\s+", " ")
                .trim();
        if (phrase.isBlank()) {
            return "";
        }
        Set<String> lemmaFormsInPhrase = morphologyUtils.getLemmaAllForms(phrase, lemmas);
        if (lemmaFormsInPhrase.isEmpty()) {
            return "";
        }
        for (String form : lemmaFormsInPhrase) {
            phrase = phrase.replaceAll(form, "<b>" + form + "</b>");
        }
        return phrase;
    }

    public String getTextSnippets() {
        return findPhrasesAndPut();
    }
}
