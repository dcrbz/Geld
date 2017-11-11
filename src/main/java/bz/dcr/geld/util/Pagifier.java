package bz.dcr.geld.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Pagifier<T> {

    private int pageSize;
    private List<List<T>> pages;

    public Pagifier(int pageSize) {
        this.pageSize = pageSize;
        this.pages = new ArrayList();
        this.pages.add(new ArrayList());
    }

    public void addItem(T item) {
        int pageNum = this.pages.size() - 1;
        List<T> currentPage = this.pages.get(pageNum);

        if(currentPage.size() >= this.pageSize) {
            currentPage = new ArrayList();
            this.pages.add(currentPage);
        }

        currentPage.add(item);
    }

    public Optional<List<T>> getPage(int pageNum) {
        return this.pages.size() == 0 ? Optional.empty() : Optional.of(this.pages.get(pageNum));
    }

    public List<List<T>> getPages() {
        return this.pages;
    }

    public int getPageSize() {
        return this.pageSize;
    }

}
