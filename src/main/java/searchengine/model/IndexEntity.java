package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "`index`")
@Getter
@Setter
@NoArgsConstructor
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", foreignKey = @ForeignKey(name = "index_page_fk"), nullable = false)
    private PageEntity page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", foreignKey = @ForeignKey(name = "index_lemma_fk"), nullable = false)
    private LemmaEntity lemma;

    @Column(name = "`rank`", nullable = false, columnDefinition = "float")
    private float rank;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexEntity entity)) return false;
        return Objects.equals(getPage(), entity.getPage()) && Objects.equals(getLemma(), entity.getLemma());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPage(), getLemma());
    }

    @Override
    public String toString() {
        return "page = " + page.getAbsUrl() +
                ", lemmaId = " + lemma.getLemma() +
                ", rank = " + rank +
                " (id = " + id +")"
                ;
    }

}
