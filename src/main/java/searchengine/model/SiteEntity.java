package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "site")
@Getter
@Setter
@NoArgsConstructor
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('INDEXING', 'INDEXED', 'FAILED')", nullable = false)
    private SiteStatus status;

    @Column(columnDefinition = "datetime", nullable = false)
    private LocalDateTime statusTime;

    @Column(columnDefinition = "text")
    private String lastError;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String name;

    @Transient
    private UrlStructure urlStructure;

    public SiteEntity(String name, String url) {
        this.name = (name == null || name.isBlank()) ? url : name;
        this.url = url;
        this.urlStructure = new UrlStructure(url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SiteEntity entity)) return false;
        return Objects.equals(getUrl(), entity.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUrl());
    }

    @Override
    public String toString() {
        return url + " status = " + status +
                ", statusTime = " + statusTime +
                (lastError.isBlank() ? "" : ", error = \"" + lastError + '\"') +
                " (id = " + id + ")"
                ;
    }
}
