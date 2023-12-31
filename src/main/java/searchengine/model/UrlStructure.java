package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@NoArgsConstructor
public class UrlStructure {
    private String urlInput;
    private String protocol;
    private String subDomain;
    private String domainName;
    @Setter
    private String path;

    public final static String DELIMITER = "/";
    private final static String MASK_URL =
            "(https?://)?" +                        // protocol
            "(.*w{3}\\.)?" +                        // authorisation + subDomain
            "([^?#,\\s/]+\\.[^?#,.\\s/]+)?" +       // domain name (II-level + zone + port)
            "/?([^#,\\s]+)?";                      // path
    private final static Pattern PATTERN_URL = Pattern.compile(MASK_URL);
    private static final String[] IGNORE_EXTENSIONS = new String[]{"xml", "pdf", "rss"};

    private String getGroupFromMatcher(Matcher matcher, int num) {
        String group = matcher.group(num);
        return group == null ? "" : group;
    }

    public UrlStructure(String urlInput) {
        this.urlInput = urlInput;
        Matcher matcher = PATTERN_URL.matcher(urlInput);
        if (!matcher.find()) {
            return;
        }
        this.protocol = getGroupFromMatcher(matcher, 1);
        this.subDomain =  getGroupFromMatcher(matcher, 2);
        this.domainName =  getGroupFromMatcher(matcher, 3);
        this.path = DELIMITER + getGroupFromMatcher(matcher, 4);
    }

    public boolean isIgnoreExtensions() {
        String lowerPath = path.replaceAll(DELIMITER,"").toLowerCase();
        for (String ext: IGNORE_EXTENSIONS) {
            if (lowerPath.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    public String getUrl() {
        if (domainName.isBlank()) {
            return "";
        }
        return protocol.concat(subDomain).concat(domainName.replaceAll("^/$", ""));
    }

    public boolean equalsDomainName(UrlStructure urlStructure) {
        return domainName.isBlank() || domainName.equals(urlStructure.getDomainName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UrlStructure that)) return false;
        return Objects.equals(getUrlInput(), that.getUrlInput());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUrlInput());
    }

    @Override
    public String toString() {
        return "UrlStructure:\n" +
                "\tprotocol = " + protocol + '\n' +
                "\tsubDomain = " + subDomain + '\n' +
                "\trootDomain = " + domainName + '\n' +
                "\tpath = " + path
                ;
    }
}
