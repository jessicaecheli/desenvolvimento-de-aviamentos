package com.example.projeto.service;

import com.example.projeto.entity.*;
import com.example.projeto.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DesenvolvimentoService {

    private final DesenvolvimentoRepository desenvolvimentoRepository;
    private final EtapaDesenvolvimentoRepository etapaRepository;
    private final OrcamentoRepository orcamentoRepository;

    public DesenvolvimentoService(DesenvolvimentoRepository desenvolvimentoRepository,
                                   EtapaDesenvolvimentoRepository etapaRepository,
                                   OrcamentoRepository orcamentoRepository) {
        this.desenvolvimentoRepository = desenvolvimentoRepository;
        this.etapaRepository = etapaRepository;
        this.orcamentoRepository = orcamentoRepository;
    }

    public List<Desenvolvimento> listarTodos() {
        return desenvolvimentoRepository.findAll();
    }

    public List<Desenvolvimento> listarComFiltros(Long colecaoId, Long marcaId, Long categoriaMasterId, Long categoriaId, StatusDesenvolvimento status, String codigo, String fornecedor) {
        return desenvolvimentoRepository.findWithFilters(colecaoId, marcaId, categoriaMasterId, categoriaId, status,
            (codigo != null && !codigo.isBlank()) ? codigo.trim() : null,
            (fornecedor != null && !fornecedor.isBlank()) ? fornecedor.trim() : null);
    }

    public Desenvolvimento buscarPorId(Long id) {
        return desenvolvimentoRepository.findByIdFetchingCategoria(id)
            .orElseThrow(() -> new IllegalArgumentException("Desenvolvimento não encontrado: " + id));
    }

    @Transactional
    public Desenvolvimento salvar(Desenvolvimento desenvolvimento) {
        return desenvolvimentoRepository.save(desenvolvimento);
    }

    @Transactional
    public Desenvolvimento criarNovo(Desenvolvimento desenvolvimento) {
        Desenvolvimento salvo = desenvolvimentoRepository.save(desenvolvimento);
        EtapaDesenvolvimento etapaInclusao = new EtapaDesenvolvimento();
        etapaInclusao.setDesenvolvimento(salvo);
        etapaInclusao.setTipo(TipoEtapa.ORCAMENTO);
        etapaInclusao.setDataOcorrencia(LocalDate.now());
        etapaRepository.save(etapaInclusao);
        return salvo;
    }

    @Transactional
    public void excluir(Long id) {
        desenvolvimentoRepository.deleteById(id);
    }

    @Transactional
    public void avancarEtapa(Long id, TipoEtapa tipo, String observacao, LocalDate dataOcorrencia, Orcamento orcamento) {
        Desenvolvimento dev = buscarPorId(id);

        EtapaDesenvolvimento etapa = new EtapaDesenvolvimento();
        etapa.setDesenvolvimento(dev);
        etapa.setTipo(tipo);
        etapa.setObservacao(observacao);
        etapa.setDataOcorrencia(dataOcorrencia != null ? dataOcorrencia : LocalDate.now());
        etapa.setOrcamento(orcamento);

        if (tipo == TipoEtapa.ALTERACAO) {
            long rodadas = dev.getEtapas().stream()
                .filter(e -> e.getTipo() == TipoEtapa.ALTERACAO)
                .count();
            etapa.setNumeroRodada((int) rodadas + 1);
        }

        etapaRepository.save(etapa);

        dev.setStatus(toStatus(tipo));
        desenvolvimentoRepository.save(dev);
    }

    public boolean estaAtrasado(Desenvolvimento dev) {
        if (dev.getStatus() != StatusDesenvolvimento.AMOSTRA &&
            dev.getStatus() != StatusDesenvolvimento.ALTERACAO) {
            return false;
        }
        Optional<EtapaDesenvolvimento> primeiraAmostra = etapaRepository
            .findFirstByDesenvolvimentoIdAndTipoOrderByDataOcorrenciaAsc(dev.getId(), TipoEtapa.AMOSTRA);
        if (primeiraAmostra.isEmpty()) return false;

        if (dev.getCategoria() == null || dev.getCategoria().getPrazoDiasUteis() == null) return false;
        long diasUteis = calcularDiasUteis(primeiraAmostra.get().getDataOcorrencia(), LocalDate.now());
        return diasUteis > dev.getCategoria().getPrazoDiasUteis();
    }

    public long diasAtraso(Desenvolvimento dev) {
        Optional<EtapaDesenvolvimento> primeiraAmostra = etapaRepository
            .findFirstByDesenvolvimentoIdAndTipoOrderByDataOcorrenciaAsc(dev.getId(), TipoEtapa.AMOSTRA);
        if (primeiraAmostra.isEmpty()) return 0;

        if (dev.getCategoria() == null || dev.getCategoria().getPrazoDiasUteis() == null) return 0;
        long diasUteis = calcularDiasUteis(primeiraAmostra.get().getDataOcorrencia(), LocalDate.now());
        return Math.max(0, diasUteis - dev.getCategoria().getPrazoDiasUteis());
    }

    public long diasRestantes(Desenvolvimento dev) {
        Optional<EtapaDesenvolvimento> primeiraAmostra = etapaRepository
            .findFirstByDesenvolvimentoIdAndTipoOrderByDataOcorrenciaAsc(dev.getId(), TipoEtapa.AMOSTRA);
        if (primeiraAmostra.isEmpty()) return Long.MAX_VALUE;
        if (dev.getCategoria() == null || dev.getCategoria().getPrazoDiasUteis() == null) return Long.MAX_VALUE;
        long diasUteis = calcularDiasUteis(primeiraAmostra.get().getDataOcorrencia(), LocalDate.now());
        return dev.getCategoria().getPrazoDiasUteis() - diasUteis;
    }

    public boolean venceEstaSemana(Desenvolvimento dev) {
        if (dev.getStatus() != StatusDesenvolvimento.AMOSTRA &&
            dev.getStatus() != StatusDesenvolvimento.ALTERACAO) {
            return false;
        }
        if (estaAtrasado(dev)) return false;
        long restantes = diasRestantes(dev);
        return restantes >= 0 && restantes <= 5;
    }

    public boolean isTecido(Desenvolvimento dev) {
        if (dev.getCategoria() == null) return false;
        Categoria cat = dev.getCategoria();
        String masterNome = cat.getCategoriaPai() != null
                ? cat.getCategoriaPai().getNome()
                : cat.getNome();
        return "TECIDOS".equalsIgnoreCase(masterNome);
    }

    public long calcularDiasUteis(LocalDate inicio, LocalDate fim) {
        long dias = 0;
        LocalDate data = inicio;
        while (!data.isAfter(fim)) {
            DayOfWeek dow = data.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                dias++;
            }
            data = data.plusDays(1);
        }
        return dias;
    }

    private StatusDesenvolvimento toStatus(TipoEtapa tipo) {
        return switch (tipo) {
            case INCLUSAO -> throw new IllegalArgumentException("Tipo INCLUSAO não avança status");
            case ORCAMENTO -> StatusDesenvolvimento.ORCAMENTO;
            case AMOSTRA -> StatusDesenvolvimento.AMOSTRA;
            case ALTERACAO -> StatusDesenvolvimento.ALTERACAO;
            case APROVADO -> StatusDesenvolvimento.APROVADO;
            case CANCELADO -> StatusDesenvolvimento.CANCELADO;
            case LIBERADA -> StatusDesenvolvimento.LIBERADA;
        };
    }
}
