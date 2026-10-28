package com.example.projeto.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Uma célula da grade de preços do orçamento: tamanho (coluna) × faixa de quantidade (linha).
 * Tamanho vazio = preço sem tamanho; quantidade vazia = preço sem escalonamento.
 */
@Entity
@Table(name = "orcamento_preco")
public class OrcamentoPreco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orcamento_id", nullable = false)
    private Orcamento orcamento;

    /** Posição da coluna de tamanho na grade (0 a 2). */
    @Column(name = "coluna_tamanho", nullable = false)
    private Integer colunaTamanho = 0;

    @Column(length = 60)
    private String tamanho;

    /** Quantidade a partir da qual o preço vale (faixa). */
    private Integer quantidade;

    @Column(precision = 19, scale = 4)
    private BigDecimal valor;

    public OrcamentoPreco() {}

    public OrcamentoPreco(Orcamento orcamento, int colunaTamanho, String tamanho, Integer quantidade, BigDecimal valor) {
        this.orcamento = orcamento;
        this.colunaTamanho = colunaTamanho;
        this.tamanho = tamanho;
        this.quantidade = quantidade;
        this.valor = valor;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Orcamento getOrcamento() { return orcamento; }
    public void setOrcamento(Orcamento orcamento) { this.orcamento = orcamento; }
    public Integer getColunaTamanho() { return colunaTamanho; }
    public void setColunaTamanho(Integer colunaTamanho) { this.colunaTamanho = colunaTamanho; }
    public String getTamanho() { return tamanho; }
    public void setTamanho(String tamanho) { this.tamanho = tamanho; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
}
