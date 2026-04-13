package com.example.projeto.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "etapa_desenvolvimento")
public class EtapaDesenvolvimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desenvolvimento_id", nullable = false)
    private Desenvolvimento desenvolvimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private TipoEtapa tipo;

    @Column(name = "data_ocorrencia", nullable = false)
    private LocalDate dataOcorrencia = LocalDate.now();

    private String observacao;

    @Column(name = "numero_rodada")
    private Integer numeroRodada = 1;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "orcamento_id")
    private Orcamento orcamento;

    @Column(name = "custo_amostra", precision = 19, scale = 2)
    private BigDecimal custoAmostra;

    private String nf;

    @Column(name = "valor_frete", precision = 19, scale = 2)
    private BigDecimal valorFrete;

    @Column(name = "valor_amostra", precision = 19, scale = 2)
    private BigDecimal valorAmostra;

    @Column(name = "observacoes_faturamento", columnDefinition = "TEXT")
    private String observacoesFaturamento;

    public EtapaDesenvolvimento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Desenvolvimento getDesenvolvimento() { return desenvolvimento; }
    public void setDesenvolvimento(Desenvolvimento desenvolvimento) { this.desenvolvimento = desenvolvimento; }
    public TipoEtapa getTipo() { return tipo; }
    public void setTipo(TipoEtapa tipo) { this.tipo = tipo; }
    public LocalDate getDataOcorrencia() { return dataOcorrencia; }
    public void setDataOcorrencia(LocalDate dataOcorrencia) { this.dataOcorrencia = dataOcorrencia; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Integer getNumeroRodada() { return numeroRodada; }
    public void setNumeroRodada(Integer numeroRodada) { this.numeroRodada = numeroRodada; }
    public Orcamento getOrcamento() { return orcamento; }
    public void setOrcamento(Orcamento orcamento) { this.orcamento = orcamento; }
    public BigDecimal getCustoAmostra() { return custoAmostra; }
    public void setCustoAmostra(BigDecimal custoAmostra) { this.custoAmostra = custoAmostra; }
    public String getNf() { return nf; }
    public void setNf(String nf) { this.nf = nf; }
    public BigDecimal getValorFrete() { return valorFrete; }
    public void setValorFrete(BigDecimal valorFrete) { this.valorFrete = valorFrete; }
    public BigDecimal getValorAmostra() { return valorAmostra; }
    public void setValorAmostra(BigDecimal valorAmostra) { this.valorAmostra = valorAmostra; }
    public String getObservacoesFaturamento() { return observacoesFaturamento; }
    public void setObservacoesFaturamento(String observacoesFaturamento) { this.observacoesFaturamento = observacoesFaturamento; }
}
