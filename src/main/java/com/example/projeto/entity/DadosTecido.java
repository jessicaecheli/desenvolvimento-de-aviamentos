package com.example.projeto.entity;

import com.example.projeto.entity.Categoria;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_estampa_id")
    private Categoria tipoEstampa;

    @Column(columnDefinition = "TEXT")
    private String infoCompraAmostra;

    private BigDecimal preco;
    private BigDecimal precoNegociado;
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
    public Categoria getTipoEstampa() { return tipoEstampa; }
    public void setTipoEstampa(Categoria tipoEstampa) { this.tipoEstampa = tipoEstampa; }
    public String getInfoCompraAmostra() { return infoCompraAmostra; }
    public void setInfoCompraAmostra(String infoCompraAmostra) { this.infoCompraAmostra = infoCompraAmostra; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public BigDecimal getPrecoNegociado() { return precoNegociado; }
    public void setPrecoNegociado(BigDecimal precoNegociado) { this.precoNegociado = precoNegociado; }
    public BigDecimal getValorDolar() { return valorDolar; }
    public void setValorDolar(BigDecimal valorDolar) { this.valorDolar = valorDolar; }
    public BigDecimal getRendimento() { return rendimento; }
    public void setRendimento(BigDecimal rendimento) { this.rendimento = rendimento; }
}
