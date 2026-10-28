package com.example.projeto.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Orçamento de um fornecedor. Os preços ficam numa grade dinâmica (OrcamentoPreco):
 * até 3 tamanhos (colunas) × faixas de quantidade (linhas).
 * As colunas antigas tamanho/tamanho2/tamanho3, valor/valor2/valor3 e quantidade
 * permanecem no banco sem uso (migradas por MigracaoPrecosOrcamento).
 */
@Entity
@Table(name = "orcamento")
public class Orcamento {

    public static final int MAX_TAMANHOS = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desenvolvimento_id", nullable = false)
    private Desenvolvimento desenvolvimento;

    private String fornecedor;

    private String observacao;

    /** Exclusão lógica: some da tela, mas continua no banco para o histórico das etapas. */
    @Column(name = "excluido", columnDefinition = "boolean default false")
    private Boolean excluido = false;

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("quantidade ASC, colunaTamanho ASC")
    private List<OrcamentoPreco> precos = new ArrayList<>();

    public Orcamento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Desenvolvimento getDesenvolvimento() { return desenvolvimento; }
    public void setDesenvolvimento(Desenvolvimento desenvolvimento) { this.desenvolvimento = desenvolvimento; }
    public String getFornecedor() { return fornecedor; }
    public void setFornecedor(String fornecedor) { this.fornecedor = fornecedor; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public boolean isExcluido() { return Boolean.TRUE.equals(excluido); }
    public void setExcluido(boolean excluido) { this.excluido = excluido; }
    public List<OrcamentoPreco> getPrecos() { return precos; }
    public void setPrecos(List<OrcamentoPreco> precos) { this.precos = precos; }

    /** Substitui toda a grade de preços. */
    public void substituirPrecos(List<OrcamentoPreco> novos) {
        precos.clear();
        novos.forEach(p -> { p.setOrcamento(this); precos.add(p); });
    }

    // ── Leitura da grade ──────────────────────────────────────────────

    /** Rótulos das colunas de tamanho, na ordem (vazio quando a coluna não tem tamanho). */
    public List<String> getTamanhosGrade() {
        int colunas = precos.stream().mapToInt(OrcamentoPreco::getColunaTamanho).max().orElse(0) + 1;
        List<String> rotulos = new ArrayList<>();
        for (int c = 0; c < colunas; c++) {
            final int col = c;
            rotulos.add(precos.stream()
                .filter(p -> p.getColunaTamanho() == col && p.getTamanho() != null && !p.getTamanho().isBlank())
                .map(OrcamentoPreco::getTamanho).findFirst().orElse(""));
        }
        return rotulos;
    }

    /** Faixas de quantidade, em ordem crescente (null = sem faixa, vem primeiro). */
    public List<Integer> getFaixasGrade() {
        List<Integer> faixas = new ArrayList<>();
        precos.stream().map(OrcamentoPreco::getQuantidade).distinct()
            .sorted(Comparator.nullsFirst(Comparator.naturalOrder()))
            .forEach(faixas::add);
        if (faixas.isEmpty()) faixas.add(null);
        return faixas;
    }

    public BigDecimal getPrecoCelula(Integer quantidade, int coluna) {
        return precos.stream()
            .filter(p -> Objects.equals(p.getQuantidade(), quantidade) && p.getColunaTamanho() == coluna)
            .map(OrcamentoPreco::getValor).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public boolean isTemTamanho() { return getTamanhosGrade().stream().anyMatch(t -> !t.isBlank()); }
    public boolean isTemFaixa() { return precos.stream().anyMatch(p -> p.getQuantidade() != null); }

    /** Menor quantidade informada nas faixas (para a ficha técnica). */
    public Integer getQuantidadeMinima() {
        return precos.stream().map(OrcamentoPreco::getQuantidade).filter(Objects::nonNull)
            .min(Comparator.naturalOrder()).orElse(null);
    }

    /**
     * Preço de referência para comparar orçamentos: o menor preço da menor faixa de quantidade
     * (a primeira faixa que tenha algum preço).
     */
    public BigDecimal getPrecoReferencia() {
        for (Integer faixa : getFaixasGrade()) {
            BigDecimal menor = precos.stream()
                .filter(p -> Objects.equals(p.getQuantidade(), faixa) && p.getValor() != null)
                .map(OrcamentoPreco::getValor).min(Comparator.naturalOrder()).orElse(null);
            if (menor != null) return menor;
        }
        return null;
    }

    /** Resumo em texto para o histórico (ex.: "SS16 / 500: R$ 1,0000 | ..."). */
    public String getResumoPrecos() {
        List<String> tamanhos = getTamanhosGrade();
        List<String> partes = new ArrayList<>();
        for (OrcamentoPreco p : precos) {
            if (p.getValor() == null) continue;
            List<String> rotulo = new ArrayList<>();
            String tam = p.getColunaTamanho() < tamanhos.size() ? tamanhos.get(p.getColunaTamanho()) : "";
            if (!tam.isBlank()) rotulo.add(tam);
            if (p.getQuantidade() != null) rotulo.add(String.valueOf(p.getQuantidade()));
            String preco = "R$ " + String.format("%.4f", p.getValor()).replace('.', ',');
            partes.add(rotulo.isEmpty() ? preco : String.join(" / ", rotulo) + ": " + preco);
        }
        return String.join(" | ", partes);
    }
}
