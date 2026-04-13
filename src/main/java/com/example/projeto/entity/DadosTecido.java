package com.example.projeto.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "dados_tecido")
public class DadosTecido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desenvolvimento_id", unique = true)
    private Desenvolvimento desenvolvimento;

    private String corEstampa;
    private String unidadeMedida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    private BigDecimal minimoCompraQtd;
    private String qtdAmostra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_estampa_id")
    private TipoEstampa tipoEstampa;

    @Column(columnDefinition = "TEXT")
    private String infoCompraAmostra;

    private String estoqueFornecedor;
    private LocalDate dataConsulta;
    private String reserva;
    private LocalDate dataTermino;

    private BigDecimal preco;
    private BigDecimal precoNegociado;
    private BigDecimal precoEmDolar;
    private BigDecimal precoNegociadoEmDolar;
    private BigDecimal valorDolar;
    private BigDecimal rendimento;

    public BigDecimal getPercentualReducao() {
        if (preco == null || precoNegociado == null || preco.compareTo(BigDecimal.ZERO) == 0) return null;
        return preco.subtract(precoNegociado)
                .divide(preco, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(1, RoundingMode.HALF_UP);
    }

    public BigDecimal getConversaoKgMetro() {
        if (precoNegociado == null || rendimento == null || rendimento.compareTo(BigDecimal.ZERO) == 0) return null;
        return precoNegociado.divide(rendimento, 4, RoundingMode.HALF_UP);
    }

    public DadosTecido() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Desenvolvimento getDesenvolvimento() { return desenvolvimento; }
    public void setDesenvolvimento(Desenvolvimento desenvolvimento) { this.desenvolvimento = desenvolvimento; }
    public String getCorEstampa() { return corEstampa; }
    public void setCorEstampa(String corEstampa) { this.corEstampa = corEstampa; }
    public String getUnidadeMedida() { return unidadeMedida; }
    public void setUnidadeMedida(String unidadeMedida) { this.unidadeMedida = unidadeMedida; }
    public Fornecedor getFornecedor() { return fornecedor; }
    public void setFornecedor(Fornecedor fornecedor) { this.fornecedor = fornecedor; }
    public BigDecimal getMinimoCompraQtd() { return minimoCompraQtd; }
    public void setMinimoCompraQtd(BigDecimal minimoCompraQtd) { this.minimoCompraQtd = minimoCompraQtd; }
    public String getQtdAmostra() { return qtdAmostra; }
    public void setQtdAmostra(String qtdAmostra) { this.qtdAmostra = qtdAmostra; }
    public TipoEstampa getTipoEstampa() { return tipoEstampa; }
    public void setTipoEstampa(TipoEstampa tipoEstampa) { this.tipoEstampa = tipoEstampa; }
    public String getInfoCompraAmostra() { return infoCompraAmostra; }
    public void setInfoCompraAmostra(String infoCompraAmostra) { this.infoCompraAmostra = infoCompraAmostra; }
    public String getEstoqueFornecedor() { return estoqueFornecedor; }
    public void setEstoqueFornecedor(String estoqueFornecedor) { this.estoqueFornecedor = estoqueFornecedor; }
    public LocalDate getDataConsulta() { return dataConsulta; }
    public void setDataConsulta(LocalDate dataConsulta) { this.dataConsulta = dataConsulta; }
    public String getReserva() { return reserva; }
    public void setReserva(String reserva) { this.reserva = reserva; }
    public LocalDate getDataTermino() { return dataTermino; }
    public void setDataTermino(LocalDate dataTermino) { this.dataTermino = dataTermino; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public BigDecimal getPrecoNegociado() { return precoNegociado; }
    public void setPrecoNegociado(BigDecimal precoNegociado) { this.precoNegociado = precoNegociado; }
    public BigDecimal getPrecoEmDolar() { return precoEmDolar; }
    public void setPrecoEmDolar(BigDecimal precoEmDolar) { this.precoEmDolar = precoEmDolar; }
    public BigDecimal getPrecoNegociadoEmDolar() { return precoNegociadoEmDolar; }
    public void setPrecoNegociadoEmDolar(BigDecimal precoNegociadoEmDolar) { this.precoNegociadoEmDolar = precoNegociadoEmDolar; }
    public BigDecimal getValorDolar() { return valorDolar; }
    public void setValorDolar(BigDecimal valorDolar) { this.valorDolar = valorDolar; }
    public BigDecimal getRendimento() { return rendimento; }
    public void setRendimento(BigDecimal rendimento) { this.rendimento = rendimento; }
}
