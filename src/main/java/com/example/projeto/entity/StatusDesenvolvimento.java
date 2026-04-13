package com.example.projeto.entity;

public enum StatusDesenvolvimento {
    ORCAMENTO("ORÇAMENTO"),
    AMOSTRA("AG. AMOSTRA"),
    ALTERACAO("ALTERAÇÃO"),
    APROVADO("APROVADO"),
    CANCELADO("CANCELADO"),
    LIBERADA("AG. APROVAÇÃO");

    private final String label;

    StatusDesenvolvimento(String label) { this.label = label; }

    public String getLabel() { return label; }
}
