package com.module.shipx.quotation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QuotationCostSection implements Serializable {

    private String title;
    private List<QuotationCostLine> lines = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<QuotationCostLine> getLines() {
        return lines;
    }

    public void setLines(List<QuotationCostLine> lines) {
        this.lines = lines;
    }
}
