package com.example.projeto.dto;

import com.example.projeto.entity.StatusDesenvolvimento;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardDTO {

    private Map<String, Long> totalPorColecao;
    private Map<StatusDesenvolvimento, Long> totalPorStatus;
    private long totalAprovados;
    private long totalCancelados;
    private long totalComAlteracao;
    private List<AtrasadoDTO> atrasados;
    private Map<String, Double> leadtimeMediaPorCategoria;
    private Map<String, Double> leadtimeMediaFinalPorCategoria;
    private long totalHistoricoAtrasados;
    private BigDecimal totalCustoAmostras;
    private BigDecimal totalDescontoNegociado;
    private Double percentualDescontoMedio;
    private Map<String, Long> totalPorMarca;
    private Map<String, Long> totalPorSubcategoria;
    private Map<String, Map<String, Long>> aprovadosPorSubcategoriaFornecedor;

    public static class AtrasadoDTO {
        private Long id;
        private String codigo;
        private String descricao;
        private String colecao;
        private String categoria;
        private long diasAtraso;
        private boolean atrasado;

        public AtrasadoDTO(Long id, String codigo, String descricao, String colecao, String categoria, long diasAtraso, boolean atrasado) {
            this.id = id;
            this.codigo = codigo;
            this.descricao = descricao;
            this.colecao = colecao;
            this.categoria = categoria;
            this.diasAtraso = diasAtraso;
            this.atrasado = atrasado;
        }

        public Long getId() { return id; }
        public String getCodigo() { return codigo; }
        public String getDescricao() { return descricao; }
        public String getColecao() { return colecao; }
        public String getCategoria() { return categoria; }
        public long getDiasAtraso() { return diasAtraso; }
        public boolean isAtrasado() { return atrasado; }
    }

    public Map<String, Long> getTotalPorColecao() { return totalPorColecao; }
    public void setTotalPorColecao(Map<String, Long> totalPorColecao) { this.totalPorColecao = totalPorColecao; }
    public Map<StatusDesenvolvimento, Long> getTotalPorStatus() { return totalPorStatus; }
    public void setTotalPorStatus(Map<StatusDesenvolvimento, Long> totalPorStatus) { this.totalPorStatus = totalPorStatus; }
    public long getTotalAprovados() { return totalAprovados; }
    public void setTotalAprovados(long totalAprovados) { this.totalAprovados = totalAprovados; }
    public long getTotalCancelados() { return totalCancelados; }
    public void setTotalCancelados(long totalCancelados) { this.totalCancelados = totalCancelados; }
    public long getTotalComAlteracao() { return totalComAlteracao; }
    public void setTotalComAlteracao(long totalComAlteracao) { this.totalComAlteracao = totalComAlteracao; }
    public List<AtrasadoDTO> getAtrasados() { return atrasados; }
    public void setAtrasados(List<AtrasadoDTO> atrasados) { this.atrasados = atrasados; }
    public Map<String, Double> getLeadtimeMediaPorCategoria() { return leadtimeMediaPorCategoria; }
    public void setLeadtimeMediaPorCategoria(Map<String, Double> leadtimeMediaPorCategoria) { this.leadtimeMediaPorCategoria = leadtimeMediaPorCategoria; }
    public Map<String, Double> getLeadtimeMediaFinalPorCategoria() { return leadtimeMediaFinalPorCategoria; }
    public void setLeadtimeMediaFinalPorCategoria(Map<String, Double> m) { this.leadtimeMediaFinalPorCategoria = m; }
    public long getTotalHistoricoAtrasados() { return totalHistoricoAtrasados; }
    public void setTotalHistoricoAtrasados(long v) { this.totalHistoricoAtrasados = v; }
    public BigDecimal getTotalCustoAmostras() { return totalCustoAmostras; }
    public void setTotalCustoAmostras(BigDecimal totalCustoAmostras) { this.totalCustoAmostras = totalCustoAmostras; }
    public BigDecimal getTotalDescontoNegociado() { return totalDescontoNegociado; }
    public void setTotalDescontoNegociado(BigDecimal totalDescontoNegociado) { this.totalDescontoNegociado = totalDescontoNegociado; }
    public Double getPercentualDescontoMedio() { return percentualDescontoMedio; }
    public void setPercentualDescontoMedio(Double percentualDescontoMedio) { this.percentualDescontoMedio = percentualDescontoMedio; }
    public Map<String, Long> getTotalPorMarca() { return totalPorMarca; }
    public void setTotalPorMarca(Map<String, Long> totalPorMarca) { this.totalPorMarca = totalPorMarca; }
    public Map<String, Long> getTotalPorSubcategoria() { return totalPorSubcategoria; }
    public void setTotalPorSubcategoria(Map<String, Long> totalPorSubcategoria) { this.totalPorSubcategoria = totalPorSubcategoria; }
    public Map<String, Map<String, Long>> getAprovadosPorSubcategoriaFornecedor() { return aprovadosPorSubcategoriaFornecedor; }
    public void setAprovadosPorSubcategoriaFornecedor(Map<String, Map<String, Long>> m) { this.aprovadosPorSubcategoriaFornecedor = m; }
}
