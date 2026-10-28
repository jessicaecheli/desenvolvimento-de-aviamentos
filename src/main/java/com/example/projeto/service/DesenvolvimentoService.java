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
        Desenvolvimento comCategoria = desenvolvimentoRepository
            .findByIdFetchingCategoria(salvo.getId()).orElse(salvo);

        EtapaDesenvolvimento etapaInicial = new EtapaDesenvolvimento();
        etapaInicial.setDesenvolvimento(salvo);
        etapaInicial.setDataOcorrencia(LocalDate.now());

        if (isTecido(comCategoria)) {
            etapaInicial.setTipo(TipoEtapa.AMOSTRA);
            salvo.setStatus(StatusDesenvolvimento.AMOSTRA);
            desenvolvimentoRepository.save(salvo);
        } else {
            etapaInicial.setTipo(TipoEtapa.ORCAMENTO);
        }

        etapaRepository.save(etapaInicial);
        return salvo;
    }

    @Transactional
    public void excluir(Long id) {
        desenvolvimentoRepository.deleteById(id);
    }

    @Transactional
    public void avancarEtapa(Long id, TipoEtapa tipo, String observacao, LocalDate dataOcorrencia,
                              Orcamento orcamento, java.math.BigDecimal custoAmostra,
                              String nf, java.math.BigDecimal valorFrete,
                              java.math.BigDecimal valorAmostra, String observacoesFaturamento,
                              MotivoAlteracao motivoAlteracao) {
        Desenvolvimento dev = buscarPorId(id);

        EtapaDesenvolvimento etapa = new EtapaDesenvolvimento();
        etapa.setDesenvolvimento(dev);
        etapa.setTipo(tipo);
        etapa.setObservacao(observacao);
        etapa.setDataOcorrencia(dataOcorrencia != null ? dataOcorrencia : LocalDate.now());
        etapa.setOrcamento(orcamento);
        if (tipo == TipoEtapa.LIBERADA && custoAmostra != null) {   // custo informado ao receber a amostra
            etapa.setCustoAmostra(custoAmostra);
        }
        etapa.setNf(nf);
        etapa.setValorFrete(valorFrete);
        etapa.setValorAmostra(valorAmostra);
        etapa.setObservacoesFaturamento(observacoesFaturamento);

        if (tipo == TipoEtapa.ALTERACAO) {
            long rodadas = dev.getEtapas().stream()
                .filter(e -> e.getTipo() == TipoEtapa.ALTERACAO)
                .count();
            etapa.setNumeroRodada((int) rodadas + 1);
            etapa.setMotivoAlteracao(motivoAlteracao);
        }
        if (tipo == TipoEtapa.REPETICAO) {
            long repeticoes = dev.getEtapas().stream()
                .filter(e -> e.getTipo() == TipoEtapa.REPETICAO)
                .count();
            etapa.setNumeroRodada((int) repeticoes + 1);
        }

        etapaRepository.save(etapa);

        dev.setStatus(toStatus(tipo));
        desenvolvimentoRepository.save(dev);
    }

    @Transactional
    public void alterarMotivoAlteracao(Long devId, Long etapaId, MotivoAlteracao motivo) {
        EtapaDesenvolvimento etapa = etapaRepository.findById(etapaId)
            .filter(e -> e.getDesenvolvimento().getId().equals(devId)
                && e.getTipo() == TipoEtapa.ALTERACAO)
            .orElseThrow(() -> new IllegalArgumentException("Etapa de alteração não encontrada."));
        etapa.setMotivoAlteracao(motivo);
        etapaRepository.save(etapa);
    }

    /**
     * Corrige o custo da amostra de uma etapa (Ag. Aprovação ou etapa antiga que já tinha custo).
     * A correção fica anotada na observação da própria etapa, sem criar nova etapa,
     * para o card "Custo Amostras" somar só o valor correto.
     */
    @Transactional
    public void corrigirCustoAmostra(Long devId, Long etapaId, java.math.BigDecimal novoCusto) {
        if (novoCusto == null || novoCusto.signum() < 0) {
            throw new IllegalArgumentException("Informe um custo válido (zero ou maior).");
        }
        EtapaDesenvolvimento etapa = etapaRepository.findById(etapaId)
            .filter(e -> e.getDesenvolvimento().getId().equals(devId)
                && (e.getTipo() == TipoEtapa.LIBERADA || e.getCustoAmostra() != null))
            .orElseThrow(() -> new IllegalArgumentException("Etapa não encontrada para correção de custo."));
        java.math.BigDecimal anterior = etapa.getCustoAmostra();
        if (anterior != null && anterior.compareTo(novoCusto) == 0) return;

        String registro = "CUSTO CORRIGIDO DE " + formatarReais(anterior) + " PARA " + formatarReais(novoCusto)
            + " EM " + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String obs = etapa.getObservacao() == null || etapa.getObservacao().isBlank()
            ? registro : etapa.getObservacao() + " | " + registro;
        etapa.setObservacao(obs.length() <= 255 ? obs : "..." + obs.substring(obs.length() - 252));
        etapa.setCustoAmostra(novoCusto);
        etapaRepository.save(etapa);
    }

    private static String formatarReais(java.math.BigDecimal v) {
        return v == null ? "(sem custo)" : "R$ " + String.format("%.4f", v).replace('.', ',');
    }

    /**
     * Data-limite fixa da amostra, quando houver:
     * 1) última alteração de ESTILO → data da alteração + prazo da subcategoria (reinicia o prazo);
     * 2) senão, a Previsão de Amostra informada no cadastro.
     * Alterações por divergência do FORNECEDOR (ou antigas, sem motivo) não mudam o prazo.
     * Retorna null quando o prazo deve ser contado pela 1ª etapa AMOSTRA (fallback).
     */
    public LocalDate previsaoAmostraVigente(Desenvolvimento dev) {
        if (dev.getCategoria() != null && dev.getCategoria().getPrazoDiasUteis() != null) {
            Optional<EtapaDesenvolvimento> ultimaAltEstilo = etapaRepository
                .findFirstByDesenvolvimentoIdAndTipoAndMotivoAlteracaoOrderByDataOcorrenciaDescIdDesc(
                    dev.getId(), TipoEtapa.ALTERACAO, MotivoAlteracao.ESTILO);
            if (ultimaAltEstilo.isPresent()) {
                return adicionarDiasUteis(ultimaAltEstilo.get().getDataOcorrencia(),
                    dev.getCategoria().getPrazoDiasUteis());
            }
        }
        return dev.getPrevisaoAmostra();
    }

    /**
     * Prazo da amostra para exibição (último dia sem atraso), seguindo a mesma regra de estaAtrasado.
     * Antes da 1ª amostra, usa a estimativa pela data de criação.
     */
    public LocalDate prazoAmostraExibicao(Desenvolvimento dev) {
        LocalDate fixa = previsaoAmostraVigente(dev);
        if (fixa != null) return fixa;
        if (dev.getCategoria() == null || dev.getCategoria().getPrazoDiasUteis() == null) return null;
        Optional<EtapaDesenvolvimento> primeiraAmostra = etapaRepository
            .findFirstByDesenvolvimentoIdAndTipoOrderByDataOcorrenciaAsc(dev.getId(), TipoEtapa.AMOSTRA);
        if (primeiraAmostra.isEmpty()) return calcularPrevisaoAmostra(dev);
        // último dia em que calcularDiasUteis(inicio, dia) <= prazo
        int prazo = dev.getCategoria().getPrazoDiasUteis();
        LocalDate data = primeiraAmostra.get().getDataOcorrencia();
        LocalDate ultimoDiaNoPrazo = data;
        int contados = 0;
        while (true) {
            DayOfWeek dow = data.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) contados++;
            if (contados > prazo) return ultimoDiaNoPrazo;
            ultimoDiaNoPrazo = data;
            data = data.plusDays(1);
        }
    }

    public boolean estaAtrasado(Desenvolvimento dev) {
        if (dev.getStatus() != StatusDesenvolvimento.AMOSTRA &&
            dev.getStatus() != StatusDesenvolvimento.ALTERACAO) {
            return false;
        }
        LocalDate previsao = previsaoAmostraVigente(dev);
        if (previsao != null) {
            return LocalDate.now().isAfter(previsao);
        }
        // fallback: cálculo pela data da primeira etapa AMOSTRA + prazo da subcategoria
        Optional<EtapaDesenvolvimento> primeiraAmostra = etapaRepository
            .findFirstByDesenvolvimentoIdAndTipoOrderByDataOcorrenciaAsc(dev.getId(), TipoEtapa.AMOSTRA);
        if (primeiraAmostra.isEmpty()) return false;
        if (dev.getCategoria() == null || dev.getCategoria().getPrazoDiasUteis() == null) return false;
        long diasUteis = calcularDiasUteis(primeiraAmostra.get().getDataOcorrencia(), LocalDate.now());
        return diasUteis > dev.getCategoria().getPrazoDiasUteis();
    }

    public long diasAtraso(Desenvolvimento dev) {
        LocalDate previsao = previsaoAmostraVigente(dev);
        if (previsao != null) {
            return Math.max(0, calcularDiasUteis(previsao, LocalDate.now()) - 1);
        }
        // fallback
        Optional<EtapaDesenvolvimento> primeiraAmostra = etapaRepository
            .findFirstByDesenvolvimentoIdAndTipoOrderByDataOcorrenciaAsc(dev.getId(), TipoEtapa.AMOSTRA);
        if (primeiraAmostra.isEmpty()) return 0;
        if (dev.getCategoria() == null || dev.getCategoria().getPrazoDiasUteis() == null) return 0;
        long diasUteis = calcularDiasUteis(primeiraAmostra.get().getDataOcorrencia(), LocalDate.now());
        return Math.max(0, diasUteis - dev.getCategoria().getPrazoDiasUteis());
    }

    public long diasRestantes(Desenvolvimento dev) {
        LocalDate previsao = previsaoAmostraVigente(dev);
        if (previsao != null) {
            return calcularDiasUteis(LocalDate.now(), previsao) - 1;
        }
        // fallback
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

    public LocalDate calcularPrevisaoAmostra(Desenvolvimento dev) {
        if (dev.getDataCriacao() == null || dev.getCategoria() == null
                || dev.getCategoria().getPrazoDiasUteis() == null) {
            return null;
        }
        return adicionarDiasUteis(dev.getDataCriacao(), dev.getCategoria().getPrazoDiasUteis());
    }

    public LocalDate adicionarDiasUteis(LocalDate inicio, int diasUteis) {
        LocalDate data = inicio;
        int contados = 0;
        while (contados < diasUteis) {
            data = data.plusDays(1);
            DayOfWeek dow = data.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                contados++;
            }
        }
        return data;
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
            case NEGOCIACAO -> throw new IllegalArgumentException("Tipo NEGOCIACAO não avança status");
            case REPETICAO -> StatusDesenvolvimento.AMOSTRA;
        };
    }
}
