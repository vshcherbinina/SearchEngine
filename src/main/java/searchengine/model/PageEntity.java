package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "page")
@Getter
@Setter
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name = "page_site_fk"), nullable = false)
    private SiteEntity site;

    @Column(columnDefinition = "text not null, index (path(50))")
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(columnDefinition = "mediumtext")
    private String content;

    @Transient
    private UrlStructure urlStructure;

    public PageEntity() {
        this.urlStructure = new UrlStructure();
    }

    public PageEntity(SiteEntity site, UrlStructure urlStructure) {
        this.site = site;
        this.path = urlStructure.getPath();
        this.urlStructure = urlStructure;
    }

    public void setPath(String path) {
        this.path = path;
        this.urlStructure.setPath(path);
    }

    public boolean isSuccessLoad() {
        return code == HttpStatus.OK.value();
    }

    public String getAbsUrl() {
        String absUrl = urlStructure.getUrl();
        if (absUrl.isEmpty()) {
            absUrl = site.getUrlStructure().getUrl();
        }
        return absUrl.concat(urlStructure.getPath());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageEntity page)) return false;
        return Objects.equals(getSite(), page.getSite()) && Objects.equals(getPath(), page.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSite(), getPath());
    }

    @Override
    public String toString() {
        return "site = " + site.getUrl() +
                ", path = \"" + path + "\"" +
                ", code = " + code +
                " (id = " + id +")"
                ;
    }
}
