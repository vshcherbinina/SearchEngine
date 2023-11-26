package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "lemma")
@Getter
@Setter
@NoArgsConstructor
public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name = "lemma_site_fk"), nullable = false)
    private SiteEntity site;

    @Column(columnDefinition = "varchar(255) not null, index (lemma(50))")
    private String lemma;

    @Column(columnDefinition = "int", nullable = false)
    private volatile int frequency;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LemmaEntity entity)) return false;
        return Objects.equals(getSite(), entity.getSite()) && Objects.equals(getLemma(), entity.getLemma());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSite(), getLemma());
    }

    @Override
    public String toString() {
        return "lemma = '" + lemma + '\'' +
                ", site = " + site.getUrl() +
                ", frequency = " + frequency +
                " (id = " + id +")"
                ;
    }
}
