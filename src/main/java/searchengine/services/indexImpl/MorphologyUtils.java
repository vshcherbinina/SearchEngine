package searchengine.services.indexImpl;

import lombok.Getter;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;

@Getter
public class MorphologyUtils {
    private final LuceneMorphology luceneMorphology;
    public static final String WORD_REGEX_FOR_CHECK_WORD = "\\W\\w&&[^а-яА-Я\\s]";
    public static final String WORD_REGEX_FOR_REPLACE = "[^ЁёА-Яа-я\\s]";
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ"};

    public static MorphologyUtils getInstance() throws IOException {
        LuceneMorphology morphology = new RussianLuceneMorphology();
        return new MorphologyUtils(morphology);
    }

    private MorphologyUtils(LuceneMorphology luceneMorphology) {
        this.luceneMorphology = luceneMorphology;
    }

    public HashMap<String, Integer> collectLemmas(String text) {
        String[] words = getArrayRussianWords(text);
        HashMap<String, Integer> lemmas = new HashMap<>();
        for (String word : words) {
            String normalWord = getNormalForm(word);
            if (normalWord.isEmpty()) {
                continue;
            }
            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }
        return lemmas;
    }

    private String getNormalForm(String word) {
        if (word.isBlank()) {
            return "";
        }
        String prepareWord = prepareTextForFindNormalForm(word);
        if (!isCorrectWordForm(prepareWord) || wordBaseBelongToParticle(prepareWord)) {
            return "";
        }
        List<String> normalForms = luceneMorphology.getNormalForms(prepareWord);
        if (normalForms.isEmpty()) {
            return "";
        }
        for (String normalForm : normalForms) {
            if (normalForm.equals(prepareWord)) {
                return normalForm;
            }
        };
        return normalForms.get(0);
    }

    public Set<String> getLemmaSet(String text) {
        String[] textArray = getArrayRussianWords(text);
        Set<String> lemmaSet = new HashSet<>();
        for (String word : textArray) {
            String normalWord = getNormalForm(word);
            if (normalWord.isEmpty() || lemmaSet.contains(normalWord)) {
                continue;
            }
            lemmaSet.add(normalWord);
        }
        return lemmaSet;
    }

    public Set<String> getLemmaAllForms(String text, Set<String> lemmas) {
        String[] textArray = getArrayRussianWordsNotReplaces(text);
        Set<String> lemmaSet = new HashSet<>();
        for (String word : textArray) {
            if (word.isEmpty() && lemmaSet.contains(word)) {
                continue;
            }
            String normalWord = getNormalForm(word);
            if (normalWord.isEmpty() || !lemmas.contains(normalWord)) {
                continue;
            }
            lemmaSet.add(word);
        }
        return lemmaSet;
    }

    private boolean wordBaseBelongToParticle(String word) {
        List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
        return anyWordBaseBelongToParticle(wordBaseForms);
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    private boolean hasParticleProperty(String wordBase) {
        String[] wordProperties = wordBase.toUpperCase().trim().split(" ");
        for (String property : particlesNames) {
            for (String wordProperty : wordProperties) {
                if (wordProperty.equals(property)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String[] getArrayRussianWordsNotReplaces(String text) {
        return text
                .replaceAll(WORD_REGEX_FOR_REPLACE, " ")
                .trim()
                .split("\\s+");
    }

    public String prepareTextForFindNormalForm(String text) {
        return text.toLowerCase(Locale.ROOT).replaceAll("ё", "е");
    }

    public String[] getArrayRussianWords(String text) {
        return getArrayRussianWordsNotReplaces(prepareTextForFindNormalForm(text));
    }

    private boolean isCorrectWordForm(String word) {
        List<String> wordInfo = luceneMorphology.getMorphInfo(word);
        for (String morphInfo : wordInfo) {
            if (morphInfo.matches(WORD_REGEX_FOR_CHECK_WORD)) {
                return false;
            }
        }
        return true;
    }
}
