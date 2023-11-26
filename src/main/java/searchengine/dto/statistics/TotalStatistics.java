package searchengine.dto.statistics;

import lombok.Data;

@Data
public class TotalStatistics {
    private int sites;
    private int pages;
    private int lemmas;
    private boolean indexing;

    public void addSites(int count) {
        sites += count;
    }

    public void addPages(int count) {
        pages += count;
    }

    public void addLemmas(int count) {
        lemmas += count;
        indexing = lemmas > 0;
    }
}
