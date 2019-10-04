package com.fullstackdevelopers.inclass03.dto;

public class Options {
    boolean submitForSettlement;

    public Options(boolean submitForSettlement) {
        this.submitForSettlement = submitForSettlement;
    }

    public boolean isSubmitForSettlement() {
        return submitForSettlement;
    }

    public void setSubmitForSettlement(boolean submitForSettlement) {
        this.submitForSettlement = submitForSettlement;
    }

    @Override
    public String toString() {
        return "Options{" +
                "submitForSettlement=" + submitForSettlement +
                '}';
    }
}
