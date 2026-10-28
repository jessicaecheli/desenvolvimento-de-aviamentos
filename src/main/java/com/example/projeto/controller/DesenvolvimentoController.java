package com.example.projeto.controller;

import com.example.projeto.entity.*;
import com.example.projeto.repository.DadosTecidoRepository;
import com.example.projeto.repository.DesenvolvimentoRepository;
import com.example.projeto.repository.EtapaDesenvolvimentoRepository;
import com.example.projeto.repository.OrcamentoRepository;
import com.example.projeto.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/desenvolvimentos")
public class DesenvolvimentoController {

    private final DesenvolvimentoService service;
    private final MarcaService marcaService;
    private final ColecaoService colecaoService;
    private final CategoriaService categoriaService;
    private final FornecedorService fornecedorService;
    private final TipoEstampaService tipoEstampaService;
    private final EtapaDesenvolvimentoRepository etapaRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final DadosTecidoRepository dadosTecidoRepository;
    private final DesenvolvimentoRepository desenvolvimentoRepository;

    public DesenvolvimentoController(DesenvolvimentoService service,
                                      MarcaService marcaService,
                                      ColecaoService colecaoService,
                                      CategoriaService categoriaService,
                                      FornecedorService fornecedorService,
                                      TipoEstampaService tipoEstampaService,
                                      EtapaDesenvolvimentoRepository etapaRepository,
                                      OrcamentoRepository orcamentoRepository,
                                      DadosTecidoRepository dadosTecidoRepository,
                                      DesenvolvimentoRepository desenvolvimentoRepository) {
        this.service = service;
        this.marcaService = marcaService;
        this.colecaoService = colecaoService;
        this.categoriaService = categoriaService;
        this.fornecedorService = fornecedorService;
        this.tipoEstampaService = tipoEstampaService;
        this.etapaRepository = etapaRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.dadosTecidoRepository = dadosTecidoRepository;
        this.desenvolvimentoRepository = desenvolvimentoRepository;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) Long colecaoId,
                        @RequestParam(required = false) Long marcaIds,
                        @RequestParam(required = false) Long categoriaMasterId,
                        @RequestParam(required = false) Long categoriaId,
                        @RequestParam(required = false) StatusDesenvolvimento status,
                        @RequestParam(required = false) String codigo,
                        @RequestParam(required = false) String fornecedor,
                        Model model) {
        List<Desenvolvimento> lista = new ArrayList<>(
            service.listarComFiltros(colecaoId, marcaIds, categoriaMasterId, categoriaId, status, codigo, fornecedor));
        // Sempre do mais recente incluído para o mais antigo (data de criação; desempate pelo id)
        lista.sort(Comparator.comparing(Desenvolvimento::getDataCriacao,
                Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(Desenvolvimento::getId, Comparator.nullsLast(Comparator.reverseOrder())));

        model.addAttribute("desenvolvimentos", lista);
        model.addAttribute("marcas", marcaService.listarTodas());
        model.addAttribute("colecoes", colecaoService.listarTodas());
        model.addAttribute("categoriasMaster", categoriaService.listarMasters());
        model.addAttribute("categorias", categoriaService.listarSubcategorias());
        model.addAttribute("statusValues", StatusDesenvolvimento.values());
        model.addAttribute("filtroColecaoId", colecaoId);
        model.addAttribute("filtroMarcaId", marcaIds);
        model.addAttribute("filtroCategoriaMasterId", categoriaMasterId);
        model.addAttribute("filtroCategoriaId", categoriaId);
        model.addAttribute("filtroStatus", status);
        model.addAttribute("filtroCodigo", codigo);
        model.addAttribute("filtroFornecedor", fornecedor);
        return "desenvolvimentos/lista";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        return popularFormModel(new Desenvolvimento(), model);
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        return popularFormModel(service.buscarPorId(id), model);
    }

    private String popularFormModel(Desenvolvimento dev, Model model) {
        model.addAttribute("desenvolvimento", dev);
        model.addAttribute("marcas", marcaService.listarTodas());
        model.addAttribute("colecoes", colecaoService.listarTodas());
        model.addAttribute("categoriasMaster", categoriaService.listarMasters());
        List<Categoria> subcategorias = categoriaService.listarSubcategorias();
        model.addAttribute("subcategorias", subcategorias);
        model.addAttribute("statusValues", StatusDesenvolvimento.values());
        Map<Long, Integer> prazoPorSubcategoria = subcategorias.stream()
            .filter(c -> c.getPrazoDiasUteis() != null)
            .collect(Collectors.toMap(Categoria::getId, Categoria::getPrazoDiasUteis));
        model.addAttribute("prazoPorSubcategoria", prazoPorSubcategoria);
        return "desenvolvimentos/form";
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam String codigo,
                         @RequestParam(required = false) String descricao,
                         @RequestParam(required = false) List<Long> marcaIds,
                         @RequestParam(required = false) Long colecaoId,
                         @RequestParam(required = false) Long categoriaId,
                         @RequestParam(required = false) StatusDesenvolvimento status,
                         @RequestParam(required = false) Long id,
                         @RequestParam(required = false) String codigoSystextil1,
                         @RequestParam(required = false) String codigoSystextil2,
                         @RequestParam(required = false) String codigoSystextil3,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate previsaoAmostra,
                         @RequestParam(required = false) String familia,
                         @RequestParam(required = false) BigDecimal precoTarget,
                         RedirectAttributes ra,
                         Model model) {
        Desenvolvimento dev;
        if (id != null) {
            dev = service.buscarPorId(id);
        } else {
            dev = new Desenvolvimento();
        }
        dev.setCodigo(codigo == null ? null : codigo.trim());
        dev.setDescricao(descricao);
        List<Marca> marcasList = new ArrayList<>();
        if (marcaIds != null) marcaIds.forEach(mid -> marcasList.add(marcaService.buscarPorId(mid)));
        dev.setMarcas(marcasList);
        if (colecaoId != null) dev.setColecao(colecaoService.buscarPorId(colecaoId));
        if (categoriaId != null) dev.setCategoria(categoriaService.buscarPorId(categoriaId));
        if (status != null) dev.setStatus(status);
        // O formulário de cadastro não tem os códigos Systêxtil (são editados no card do detalhe,
        // via /{id}/systextil). Só atualiza se vierem na requisição, para não apagá-los ao editar.
        if (codigoSystextil1 != null) dev.setCodigoSystextil1(codigoSystextil1);
        if (codigoSystextil2 != null) dev.setCodigoSystextil2(codigoSystextil2);
        if (codigoSystextil3 != null) dev.setCodigoSystextil3(codigoSystextil3);
        dev.setPrevisaoAmostra(previsaoAmostra);
        // Família só vale para a subcategoria Strass; em outras subcategorias fica vazia
        boolean isStrass = dev.getCategoria() != null && dev.getCategoria().getNome() != null
            && dev.getCategoria().getNome().toUpperCase().contains("STRASS");
        dev.setFamilia(isStrass && familia != null && !familia.isBlank()
            ? familia.trim().toUpperCase() : null);
        dev.setPrecoTarget(precoTarget);   // opcional: vazio grava sem target
        List<Desenvolvimento> conflitosCodigo =
            desenvolvimentoRepository.findByCodigoDuplicado(dev.getCodigo(), id);
        if (!conflitosCodigo.isEmpty()) {
            model.addAttribute("erro", "O código \"" + dev.getCodigo()
                + "\" já está cadastrado em outro desenvolvimento. Informe um código diferente.");
            return popularFormModel(dev, model);
        }
        if (id == null) {
            service.criarNovo(dev);
        } else {
            service.salvar(dev);
        }
        ra.addFlashAttribute("sucesso", "Desenvolvimento salvo com sucesso.");
        return "redirect:/desenvolvimentos";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        Desenvolvimento dev = service.buscarPorId(id);
        List<EtapaDesenvolvimento> etapas = etapaRepository
            .findByDesenvolvimentoIdOrderByDataOcorrenciaAscIdAsc(id);
        model.addAttribute("dev", dev);
        model.addAttribute("etapas", etapas);
        model.addAttribute("tiposEtapa", TipoEtapa.values());
        model.addAttribute("atrasado", service.estaAtrasado(dev));
        model.addAttribute("diasAtraso", service.diasAtraso(dev));
        model.addAttribute("prazoAmostra", service.prazoAmostraExibicao(dev));
        model.addAttribute("motivosAlteracao", MotivoAlteracao.values());

        if (service.isTecido(dev)) {
            DadosTecido dadosTecido = dadosTecidoRepository.findByDesenvolvimentoId(id)
                    .orElse(new DadosTecido());
            model.addAttribute("dadosTecido", dadosTecido);
            model.addAttribute("fornecedores", fornecedorService.listarTodos());
            model.addAttribute("tiposEstampa", tipoEstampaService.listarTodos());
            return "desenvolvimentos/detalhe-tecido";
        }

        List<Orcamento> orcamentos = orcamentoRepository.findAtivosByDesenvolvimentoId(id);
        Orcamento melhorOrcamento = orcamentos.stream()
            .filter(o -> o.getPrecoReferencia() != null)
            .min(Comparator.comparing(Orcamento::getPrecoReferencia))
            .orElse(null);
        Long menorOrcamentoId = melhorOrcamento != null ? melhorOrcamento.getId() : null;
        model.addAttribute("orcamentos", orcamentos);
        model.addAttribute("menorOrcamentoId", menorOrcamentoId);
        model.addAttribute("totalOrcamentos", orcamentoRepository.countAtivosByDesenvolvimentoId(id));
        model.addAttribute("fornecedores", fornecedorService.listarTodos());
        return "desenvolvimentos/detalhe";
    }

    @PostMapping("/{id}/tecido")
    public String salvarDadosTecido(@PathVariable Long id,
                                     @RequestParam(required = false) String corEstampa,
                                     @RequestParam(required = false) String unidadeMedida,
                                     @RequestParam(required = false) Long fornecedorId,
                                     @RequestParam(required = false) BigDecimal minimoCompraQtd,
                                     @RequestParam(required = false) String qtdAmostra,
                                     @RequestParam(required = false) Long tipoEstampaId,
                                     @RequestParam(required = false) String infoCompraAmostra,
                                     @RequestParam(required = false) String estoqueFornecedor,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataConsulta,
                                     @RequestParam(required = false) String reserva,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataTermino,
                                     @RequestParam(required = false) BigDecimal preco,
                                     @RequestParam(required = false) BigDecimal precoNegociado,
                                     @RequestParam(required = false) BigDecimal precoEmDolar,
                                     @RequestParam(required = false) BigDecimal precoNegociadoEmDolar,
                                     @RequestParam(required = false) BigDecimal valorDolar,
                                     @RequestParam(required = false) BigDecimal rendimento,
                                     @RequestParam(required = false) String origem,
                                     RedirectAttributes ra) {
        Desenvolvimento dev = service.buscarPorId(id);
        DadosTecido dados = dadosTecidoRepository.findByDesenvolvimentoId(id)
                .orElse(new DadosTecido());
        dados.setDesenvolvimento(dev);
        dados.setCorEstampa(corEstampa);
        dados.setUnidadeMedida(unidadeMedida);
        dados.setFornecedor(fornecedorId != null ? fornecedorService.buscarPorId(fornecedorId) : null);
        dados.setMinimoCompraQtd(minimoCompraQtd);
        dados.setQtdAmostra(qtdAmostra);
        dados.setTipoEstampa(tipoEstampaId != null ? tipoEstampaService.buscarPorId(tipoEstampaId) : null);
        dados.setInfoCompraAmostra(infoCompraAmostra);
        dados.setEstoqueFornecedor(estoqueFornecedor);
        dados.setDataConsulta(dataConsulta);
        dados.setReserva(reserva);
        dados.setDataTermino(dataTermino);
        dados.setPreco(preco);
        dados.setPrecoNegociado(precoNegociado);
        dados.setPrecoEmDolar(precoEmDolar);
        dados.setPrecoNegociadoEmDolar(precoNegociadoEmDolar);
        dados.setValorDolar(valorDolar);
        dados.setRendimento(rendimento);
        dadosTecidoRepository.save(dados);
        if ("preco".equals(origem)) {
            EtapaDesenvolvimento etapaPreco = new EtapaDesenvolvimento();
            etapaPreco.setDesenvolvimento(dev);
            etapaPreco.setTipo(TipoEtapa.ORCAMENTO);
            etapaPreco.setDataOcorrencia(LocalDate.now());
            String nomeFornecedor = dados.getFornecedor() != null ? dados.getFornecedor().getNome() : "(sem fornecedor)";
            etapaPreco.setObservacao("PREÇO - " + nomeFornecedor);
            etapaRepository.save(etapaPreco);
        }
        ra.addFlashAttribute("sucesso", "Dados do tecido salvos.");
        return "redirect:/desenvolvimentos/" + id;
    }

    @PostMapping("/{id}/systextil")
    public String salvarSystextil(@PathVariable Long id,
                                   @RequestParam(required = false) String codigoSystextil1,
                                   @RequestParam(required = false) String codigoSystextil2,
                                   @RequestParam(required = false) String codigoSystextil3,
                                   RedirectAttributes ra) {
        // Verifica duplicatas para cada código informado
        for (String codigo : new String[]{codigoSystextil1, codigoSystextil2, codigoSystextil3}) {
            if (codigo != null && !codigo.isBlank()) {
                List<Desenvolvimento> conflitos = desenvolvimentoRepository.findBySystextilDuplicado(codigo.trim(), id);
                if (!conflitos.isEmpty()) {
                    String conflito = conflitos.get(0).getCodigo();
                    ra.addFlashAttribute("erro", "Código Systêxtil \"" + codigo.trim().toUpperCase()
                        + "\" já está cadastrado no desenvolvimento " + conflito + ".");
                    return "redirect:/desenvolvimentos/" + id;
                }
            }
        }
        Desenvolvimento dev = service.buscarPorId(id);
        dev.setCodigoSystextil1(codigoSystextil1);
        dev.setCodigoSystextil2(codigoSystextil2);
        dev.setCodigoSystextil3(codigoSystextil3);
        service.salvar(dev);
        ra.addFlashAttribute("sucesso", "Código Systêxtil salvo.");
        return "redirect:/desenvolvimentos/" + id;
    }

    @PostMapping("/{id}/avancar")
    public String avancarEtapa(@PathVariable Long id,
                                @RequestParam(required = false) TipoEtapa tipo,
                                @RequestParam(required = false) String observacao,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataOcorrencia,
                                @RequestParam(required = false) Long orcamentoId,
                                @RequestParam(required = false) BigDecimal custoAmostra,
                                @RequestParam(required = false) String nf,
                                @RequestParam(required = false) BigDecimal valorFrete,
                                @RequestParam(required = false) BigDecimal valorAmostra,
                                @RequestParam(required = false) String observacoesFaturamento,
                                @RequestParam(required = false) MotivoAlteracao motivoAlteracao,
                                RedirectAttributes ra) {
        if (tipo == null) {
            return "redirect:/desenvolvimentos/" + id;
        }
        if (tipo == TipoEtapa.ALTERACAO && motivoAlteracao == null) {
            ra.addFlashAttribute("erro", "Informe o motivo da alteração (estilo ou divergência do fornecedor).");
            return "redirect:/desenvolvimentos/" + id;
        }
        // Aviamento: o custo da amostra é informado quando a amostra é recebida (etapa Ag. Aprovação)
        if (tipo == TipoEtapa.LIBERADA && custoAmostra == null && !service.isTecido(service.buscarPorId(id))) {
            ra.addFlashAttribute("erro", "Informe o custo da amostra recebida.");
            return "redirect:/desenvolvimentos/" + id;
        }
        Orcamento orcamento = (orcamentoId != null) ? orcamentoRepository.findById(orcamentoId).orElse(null) : null;
        service.avancarEtapa(id, tipo, observacao, dataOcorrencia, orcamento, custoAmostra,
                nf, valorFrete, valorAmostra, observacoesFaturamento, motivoAlteracao);
        ra.addFlashAttribute("sucesso", "Etapa registrada com sucesso.");
        return "redirect:/desenvolvimentos/" + id;
    }

    @PostMapping("/{id}/etapa/{etapaId}/motivo")
    public String alterarMotivoAlteracao(@PathVariable Long id,
                                          @PathVariable Long etapaId,
                                          @RequestParam MotivoAlteracao motivoAlteracao,
                                          RedirectAttributes ra) {
        service.alterarMotivoAlteracao(id, etapaId, motivoAlteracao);
        ra.addFlashAttribute("sucesso", "Motivo da alteração atualizado: " + motivoAlteracao.getLabel() + ".");
        return "redirect:/desenvolvimentos/" + id;
    }

    @PostMapping("/{id}/etapa/{etapaId}/custo")
    public String corrigirCustoAmostra(@PathVariable Long id,
                                        @PathVariable Long etapaId,
                                        @RequestParam(required = false) BigDecimal custoAmostra,
                                        RedirectAttributes ra) {
        try {
            service.corrigirCustoAmostra(id, etapaId, custoAmostra);
            ra.addFlashAttribute("sucesso", "Custo da amostra corrigido.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/desenvolvimentos/" + id;
    }

    /**
     * Lê a grade de preços enviada pelo formulário do orçamento:
     * colunas (1–3) com rótulos tam{c}; linhas com faixa{r} (quantidade, opcional); células p_{r}_{c}.
     */
    private List<OrcamentoPreco> lerGradePrecos(Map<String, String> p) {
        int colunas = Math.min(Orcamento.MAX_TAMANHOS, Math.max(1, parseIntOuZero(p.get("colunas"))));
        int linhas = Math.max(1, parseIntOuZero(p.get("linhas")));
        List<String> tamanhos = new ArrayList<>();
        for (int c = 0; c < colunas; c++) {
            String t = p.getOrDefault("tam" + c, "").trim().toUpperCase();
            if (!t.isEmpty() && tamanhos.contains(t)) throw new IllegalArgumentException("Tamanho \"" + t + "\" repetido na grade.");
            tamanhos.add(t);
        }
        List<Integer> faixas = new ArrayList<>();
        List<OrcamentoPreco> precos = new ArrayList<>();
        for (int r = 0; r < linhas; r++) {
            String fx = p.getOrDefault("faixa" + r, "").replace(".", "").trim();
            Integer qtd = fx.isEmpty() ? null : Integer.valueOf(fx);
            boolean linhaTemPreco = false;
            for (int c = 0; c < colunas; c++) {
                String v = p.getOrDefault("p_" + r + "_" + c, "").trim().replace(',', '.');
                if (v.isEmpty()) continue;
                precos.add(new OrcamentoPreco(null, c, tamanhos.get(c).isEmpty() ? null : tamanhos.get(c), qtd, new BigDecimal(v)));
                linhaTemPreco = true;
            }
            if (linhaTemPreco) {
                if (faixas.contains(qtd)) throw new IllegalArgumentException("Faixa de quantidade repetida na grade.");
                faixas.add(qtd);
            }
        }
        if (precos.isEmpty()) throw new IllegalArgumentException("Informe pelo menos um preço no orçamento.");
        return precos;
    }

    /** Observação da etapa cabe em 255 caracteres no banco. */
    private static String limitarObs(String s) {
        return s.length() <= 255 ? s : s.substring(0, 252) + "...";
    }

    private static int parseIntOuZero(String s) {
        try { return s == null ? 0 : Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return 0; }
    }

    @PostMapping("/{id}/orcamento")
    public String adicionarOrcamento(@PathVariable Long id,
                                      @RequestParam(required = false) String fornecedor,
                                      @RequestParam(required = false) String observacao,
                                      @RequestParam Map<String, String> params,
                                      RedirectAttributes ra) {
        if (orcamentoRepository.countAtivosByDesenvolvimentoId(id) >= 4) {
            ra.addFlashAttribute("erro", "Limite de 4 orçamentos atingido.");
            return "redirect:/desenvolvimentos/" + id;
        }
        List<OrcamentoPreco> precos;
        try {
            precos = lerGradePrecos(params);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/desenvolvimentos/" + id;
        }
        Desenvolvimento dev = service.buscarPorId(id);
        Orcamento orc = new Orcamento();
        orc.setDesenvolvimento(dev);
        orc.setFornecedor(fornecedor);
        orc.setObservacao(observacao);
        orc.substituirPrecos(precos);
        orcamentoRepository.save(orc);
        EtapaDesenvolvimento etapaOrc = new EtapaDesenvolvimento();
        etapaOrc.setDesenvolvimento(dev);
        etapaOrc.setTipo(TipoEtapa.ORCAMENTO);
        etapaOrc.setDataOcorrencia(LocalDate.now());
        etapaOrc.setObservacao(fornecedor);
        etapaRepository.save(etapaOrc);
        ra.addFlashAttribute("sucesso", "Orçamento salvo.");
        return "redirect:/desenvolvimentos/" + id;
    }

    @PostMapping("/{devId}/orcamento/{orcId}/editar")
    public String editarOrcamento(@PathVariable Long devId,
                                   @PathVariable Long orcId,
                                   @RequestParam(required = false) String fornecedor,
                                   @RequestParam(required = false) String observacao,
                                   @RequestParam Map<String, String> params,
                                   RedirectAttributes ra) {
        Orcamento orc = orcamentoRepository.findById(orcId)
            .orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado: " + orcId));
        List<OrcamentoPreco> precos;
        try {
            precos = lerGradePrecos(params);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/desenvolvimentos/" + devId;
        }
        orc.setFornecedor(fornecedor);
        orc.setObservacao(observacao);
        orc.substituirPrecos(precos);
        orcamentoRepository.save(orc);
        Desenvolvimento devOrc = service.buscarPorId(devId);
        EtapaDesenvolvimento etapaAlteracao = new EtapaDesenvolvimento();
        etapaAlteracao.setDesenvolvimento(devOrc);
        etapaAlteracao.setTipo(TipoEtapa.ORCAMENTO);
        etapaAlteracao.setDataOcorrencia(LocalDate.now());
        StringBuilder obsAlteracao = new StringBuilder("ALTERAÇÃO - ");
        obsAlteracao.append(fornecedor != null && !fornecedor.isBlank() ? fornecedor : "(sem fornecedor)");
        String resumo = orc.getResumoPrecos();
        if (!resumo.isBlank()) obsAlteracao.append(" | ").append(resumo);
        if (observacao != null && !observacao.isBlank()) obsAlteracao.append(" | ").append(observacao);
        etapaAlteracao.setObservacao(limitarObs(obsAlteracao.toString()));
        etapaRepository.save(etapaAlteracao);
        ra.addFlashAttribute("sucesso", "Orçamento atualizado.");
        return "redirect:/desenvolvimentos/" + devId;
    }

    /**
     * Exclusão lógica do orçamento: sai da tela e da contagem, mas continua no banco para que as
     * etapas do histórico que apontam para ele mantenham o fornecedor. A exclusão é registrada no histórico.
     */
    @PostMapping("/{devId}/orcamento/{orcId}/excluir")
    public String excluirOrcamento(@PathVariable Long devId, @PathVariable Long orcId, RedirectAttributes ra) {
        Orcamento orc = orcamentoRepository.findById(orcId)
            .filter(o -> o.getDesenvolvimento().getId().equals(devId) && !o.isExcluido())
            .orElse(null);
        if (orc == null) {
            ra.addFlashAttribute("erro", "Orçamento não encontrado.");
            return "redirect:/desenvolvimentos/" + devId;
        }
        orc.setExcluido(true);
        orcamentoRepository.save(orc);

        EtapaDesenvolvimento etapaExclusao = new EtapaDesenvolvimento();
        etapaExclusao.setDesenvolvimento(orc.getDesenvolvimento());
        etapaExclusao.setTipo(TipoEtapa.ORCAMENTO);
        etapaExclusao.setDataOcorrencia(LocalDate.now());
        StringBuilder obs = new StringBuilder("EXCLUSÃO - ");
        obs.append(orc.getFornecedor() != null && !orc.getFornecedor().isBlank() ? orc.getFornecedor() : "(sem fornecedor)");
        String resumo = orc.getResumoPrecos();
        if (!resumo.isBlank()) obs.append(" | ").append(resumo);
        etapaExclusao.setObservacao(limitarObs(obs.toString()));
        etapaRepository.save(etapaExclusao);

        ra.addFlashAttribute("sucesso", "Orçamento excluído. O registro foi mantido no histórico.");
        return "redirect:/desenvolvimentos/" + devId;
    }

    @GetMapping("/{id}/ficha")
    public String ficha(@PathVariable Long id, Model model) {
        Desenvolvimento dev = service.buscarPorId(id);
        List<Orcamento> orcamentos = orcamentoRepository.findAtivosByDesenvolvimentoId(id);
        List<EtapaDesenvolvimento> etapas = etapaRepository.findByDesenvolvimentoIdOrderByDataOcorrenciaAscIdAsc(id);
        EtapaDesenvolvimento etapaAprovado = etapas.stream()
            .filter(e -> e.getTipo() == TipoEtapa.APROVADO)
            .findFirst().orElse(null);
        Orcamento orcamentoAprovado = (etapaAprovado != null) ? etapaAprovado.getOrcamento() : null;
        model.addAttribute("dev", dev);
        model.addAttribute("orcamentos", orcamentos);
        model.addAttribute("orcamentoAprovado", orcamentoAprovado);
        return "desenvolvimentos/ficha";
    }

    @PostMapping("/{id}/negociacao")
    public String salvarNegociacao(@PathVariable Long id,
                                    @RequestParam(required = false) BigDecimal precoInicial,
                                    @RequestParam(required = false) BigDecimal precoFinal,
                                    @RequestParam(required = false) Integer qtdCompraMostruario,
                                    RedirectAttributes ra) {
        Desenvolvimento dev = service.buscarPorId(id);
        dev.setPrecoInicial(precoInicial);
        dev.setPrecoFinal(precoFinal);
        dev.setQtdCompraMostruario(qtdCompraMostruario);
        service.salvar(dev);

        StringBuilder obs = new StringBuilder("NEGOCIAÇÃO");
        if (precoInicial != null) obs.append(" | Inicial: R$ ").append(String.format("%.4f", precoInicial).replace('.', ','));
        if (precoFinal != null)   obs.append(" | Final: R$ ").append(String.format("%.4f", precoFinal).replace('.', ','));
        if (precoInicial != null && precoFinal != null && precoInicial.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pct = precoInicial.subtract(precoFinal)
                .divide(precoInicial, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
            obs.append(" | Desconto: ").append(String.format("%.2f", pct).replace('.', ',')).append("%");
        }
        if (qtdCompraMostruario != null) obs.append(" | Qtd: ").append(qtdCompraMostruario);

        EtapaDesenvolvimento etapa = new EtapaDesenvolvimento();
        etapa.setDesenvolvimento(dev);
        etapa.setTipo(TipoEtapa.NEGOCIACAO);
        etapa.setDataOcorrencia(LocalDate.now());
        etapa.setObservacao(obs.toString());
        etapaRepository.save(etapa);

        ra.addFlashAttribute("sucesso", "Negociação salva.");
        return "redirect:/desenvolvimentos/" + id;
    }

    @PostMapping("/{devId}/etapa/{etapaId}/editar")
    public String editarEtapa(@PathVariable Long devId,
                               @PathVariable Long etapaId,
                               @RequestParam(required = false) String nf,
                               @RequestParam(required = false) BigDecimal valorFrete,
                               @RequestParam(required = false) BigDecimal valorAmostra,
                               @RequestParam(required = false) String observacoesFaturamento,
                               @RequestParam(required = false) String observacao,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataOcorrencia,
                               RedirectAttributes ra) {
        EtapaDesenvolvimento etapa = etapaRepository.findById(etapaId)
            .orElseThrow(() -> new IllegalArgumentException("Etapa não encontrada: " + etapaId));
        etapa.setNf(nf);
        etapa.setValorFrete(valorFrete);
        etapa.setValorAmostra(valorAmostra);
        etapa.setObservacoesFaturamento(observacoesFaturamento);
        etapa.setObservacao(observacao);
        if (dataOcorrencia != null) etapa.setDataOcorrencia(dataOcorrencia);
        etapaRepository.save(etapa);
        ra.addFlashAttribute("sucesso", "Etapa atualizada.");
        return "redirect:/desenvolvimentos/" + devId;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Desenvolvimento excluído.");
        return "redirect:/desenvolvimentos";
    }
}
