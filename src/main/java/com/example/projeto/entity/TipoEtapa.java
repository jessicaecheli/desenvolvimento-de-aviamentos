package com.example.projeto.entity;

public enum TipoEtapa {
    INCLUSAO("INCLUSAO"),
    ORCAMENTO("ORÇAMENTO"),
    AMOSTRA("AG. AMOSTRA"),
    LIBERADA("AG. APROVAÇÃO"),
    ALTERACAO("ALTERAÇÃO"),
    APROVADO("APROVADO"),
    CANCELADO("CANCELADO"),
    NEGOCIACAO("NEGOCIACAO"),
    REPETICAO("REPETIÇÃO");

    private final String label;

    TipoEtapa(String label) { this.label = label; }

    public String getLabel() { return label; }
}
