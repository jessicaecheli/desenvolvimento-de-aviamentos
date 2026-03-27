package com.example.projeto.controller;

import com.example.projeto.entity.*;
import com.example.projeto.repository.DadosTecidoRepository;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/desenvolvimentos")
public class DesenvolvimentoController {

    private final DesenvolvimentoService service;
    private final MarcaService marcaService;
    private final ColecaoService colecaoService;
    private final CategoriaService categoriaService;
    private final FornecedorService fornecedorService;
    private final EtapaDesenvolvimentoRepository etapaRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final DadosTecidoRepository dadosTecidoRepository;

    public DesenvolvimentoController(DesenvolvimentoService service,
                                      MarcaService marcaService,
                                      ColecaoService colecaoService,
                                      CategoriaService categoriaService,
                                      FornecedorService fornecedorService,
                                      EtapaDesenvolvimentoRepository etapaRepository,
                                      OrcamentoRepository orcamentoRepository,
                                      DadosTecidoRepository dadosTecidoRepository) {
        this.service = service;
        this.marcaService = marcaService;
        this.colecaoService = colecaoService;
        this.categoriaService = categoriaService;
        this.fornecedorService = fornecedorService;
        this.etapaRepository = etapaRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.dadosTecidoRepository = dadosTecidoRepository;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) Long colecaoId,
                        @RequestParam(required = false) Long marcaId,
                        @RequestParam(required = false) Long categoriaMasterId,
                        @RequestParam(required = false) Long categoriaId,
                        @RequestParam(required = false) StatusDesenvolvimento status,
                        @RequestParam(required = false) String codigo,
                        @RequestParam(required = false) String fornecedor,
                        Model model) {
        List<Desenvolvimento> lista = service.listarComFiltros(colecaoId, marcaId, categoriaMasterId, categoriaId, status, codigo, fornecedor);

        Set<StatusDesenvolvimento> statusComFornecedor = Set.of(
            StatusDesenvolvimento.AMOSTRA, StatusDesenvolvimento.LIBERADA, StatusDesenvolvimento.APROVADO);
        List<Long> idsComStatus = lista.stream()
            .filter(d -> statusComFornecedor.contains(d.getStatus()))
            .map(Desenvolvimento::getId)
            .toList();
        Map<Long, String> fornecedoresPorDev = new HashMap<>();
        if (!idsComStatus.isEmpty()) {
            List<EtapaDesenvolvimento> etapas = etapaRepository.findEtapasComOrcamentoPorDesenvolvimentos(
                idsComStatus, List.of(TipoEtapa.AMOSTRA, TipoEtapa.LIBERADA, TipoEtapa.APROVADO));
            for (EtapaDesenvolvimento e : etapas) {
                Long devId = e.getDesenvolvimento().getId();
                if (!fornecedoresPorDev.containsKey(devId) && e.getOrcamento() != null) {
                    fornecedoresPorDev.put(devId, e.getOrcamento().getFornecedor());
                }
            }
        }

        model.addAttribute("desenvolvimentos", lista);
        model.addAttribute("fornecedoresPorDev", fornecedoresPorDev);
        model.addAttribute("marcas", marcaService.listarTodas());
        model.addAttribute("colecoes", colecaoService.listarTodas());
        model.addAttribute("categoriasMaster", categoriaService.listarMasters());
        model.addAttribute("categorias", categoriaService.listarSubcategorias());
        model.addAttribute("statusValues", StatusDesenvolvimento.values());
        model.addAttribute("filtroColecaoId", colecaoId);
        model.addAttribute("filtroMarcaId", marcaId);
        model.addAttribute("filtroCategoriaMasterId", categoriaMasterId);
        model.addAttribute("filtroCategoriaId", categoriaId);
        model.addAttribute("filtroStatus", status);
        model.addAttribute("filtroCodigo", codigo);
        model.addAttribute("filtroFornecedor", fornecedor);
        return "desenvolvimentos/lista";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("desenvolvimento", new Desenvolvimento());
        model.addAttribute("marcas", marcaService.listarTodas());
        model.addAttribute("colecoes", colecaoService.listarTodas());
        model.addAttribute("categoriasMaster", categoriaService.listarMasters());
        model.addAttribute("subcategorias", categoriaService.listarSubcategorias());
        model.addAttribute("statusValues", StatusDesenvolvimento.values());
        return "desenvolvimentos/form";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        Desenvolvimento dev = service.buscarPorId(id);
        model.addAttribute("desenvolvimento", dev);
        model.addAttribute("marcas", marcaService.listarTodas());
        model.addAttribute("colecoes", colecaoService.listarTodas());
        model.addAttribute("categoriasMaster", categoriaService.listarMasters());
        model.addAttribute("subcategorias", categoriaService.listarSubcategorias());
        model.addAttribute("statusValues", StatusDesenvolvimento.values());
        return "desenvolvimentos/form";
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam String codigo,
                         @RequestParam(required = false) String descricao,
                         @RequestParam(required = false) Long marcaId,
                         @RequestParam(required = false) Long colecaoId,
                         @RequestParam(required = false) Long categoriaId,
                         @RequestParam(required = false) StatusDesenvolvimento status,
                         @RequestParam(required = false) Long id,
                         @RequestParam(required = false) String codigoSystextil1,
                         @RequestParam(required = false) String codigoSystextil2,
                         @RequestParam(required = false) String codigoSystextil3,
                         RedirectAttributes ra) {
        Desenvolvimento dev;
        if (id != null) {
            dev = service.buscarPorId(id);
        } else {
            dev = new Desenvolvimento();
        }
        dev.setCodigo(codigo);
        dev.setDescricao(descricao);
        if (marcaId != null) dev.setMarca(marcaService.buscarPorId(marcaId));
        if (colecaoId != null) dev.setColecao(colecaoService.buscarPorId(colecaoId));
        if (categoriaId != null) dev.setCategoria(categoriaService.buscarPorId(categoriaId));
        if (status != null) dev.setStatus(status);
        dev.setCodigoSystextil1(codigoSystextil1);
        dev.setCodigoSystextil2(codigoSystextil2);
        dev.setCodigoSystextil3(codigoSystextil3);
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

        if (service.isTecido(dev)) {
            DadosTecido dadosTecido = dadosTecidoRepository.findByDesenvolvimentoId(id)
                    .orElse(new DadosTecido());
            model.addAttribute("dadosTecido", dadosTecido);
            model.addAttribute("fornecedores", fornecedorService.listarTodos());
            categoriaService.listarMasters().stream()
                .filter(m -> "TECIDOS".equalsIgnoreCase(m.getNome()))
                .findFirst()
                .ifPresent(master -> model.addAttribute("subcategoriasTecidos",
                    categoriaService.listarSubcategoriasPorMaster(master.getId())));
            if (!model.containsAttribute("subcategoriasTecidos")) {
                model.addAttribute("subcategoriasTecidos", List.of());
            }
            return "desenvolvimentos/detalhe-tecido";
        }

        List<Orcamento> orcamentos = orcamentoRepository.findByDesenvolvimentoIdOrderByIdAsc(id);
        Long menorOrcamentoId = orcamentos.stream()
            .filter(o -> o.getValorMinimo() != null)
            .min(Comparator.comparing(Orcamento::getValorMinimo))
            .map(Orcamento::getId)
            .orElse(null);
        model.addAttribute("orcamentos", orcamentos);
        model.addAttribute("menorOrcamentoId", menorOrcamentoId);
        model.addAttribute("totalOrcamentos", orcamentoRepository.countByDesenvolvimentoId(id));
        model.addAttribute("fornecedores", fornecedorService.listarTodos());
        return "desenvolvimentos/detalhe";
    }

    @PostMapping("/{id}/tecido")
    public String salvarDadosTecido(@PathVariable Long id,
                                     @RequestParam(required = false) String corEstampa,
                                     @RequestParam(required = false) String unidadeMedida,
                                     @RequestParam(required = false) Long fornecedorId,
                                     @RequestParam(required = false) BigDecimal minimoCompraQtd,
                                     @RequestParam(required = false) Long tipoEstampaId,
                                     @RequestParam(required = false) String infoCompraAmostra,
                                     @RequestParam(required = false) BigDecimal preco,
                                     @RequestParam(required = false) BigDecimal precoNegociado,
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
        dados.setTipoEstampa(tipoEstampaId != null ? categoriaService.buscarPorId(tipoEstampaId) : null);
        dados.setInfoCompraAmostra(infoCompraAmostra);
        dados.setPreco(preco);
        dados.setPrecoNegociado(precoNegociado);
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
                                RedirectAttributes ra) {
        if (tipo == null) {
            return "redirect:/desenvolvimentos/" + id;
        }
        Orcamento orcamento = (orcamentoId != null) ? orcamentoRepository.findById(orcamentoId).orElse(null) : null;
        service.avancarEtapa(id, tipo, observacao, dataOcorrencia, orcamento, custoAmostra);
        ra.addFlashAttribute("sucesso", "Etapa registrada com sucesso.");
        return "redirect:/desenvolvimentos/" + id;
    }

    @PostMapping("/{id}/orcamento")
    public String adicionarOrcamento(@PathVariable Long id,
                                      @RequestParam(required = false) String fornecedor,
                                      @RequestParam(required = false) String tamanho,
                                      @RequestParam(required = false) String tamanho2,
                                      @RequestParam(required = false) String tamanho3,
                                      @RequestParam(required = false) BigDecimal valor,
                                      @RequestParam(required = false) BigDecimal valor2,
                                      @RequestParam(required = false) BigDecimal valor3,
                                      @RequestParam(required = false) Integer quantidade,
                                      @RequestParam(required = false) String observacao,
                                      RedirectAttributes ra) {
        if (orcamentoRepository.countByDesenvolvimentoId(id) >= 4) {
            ra.addFlashAttribute("erro", "Limite de 4 orçamentos atingido.");
            return "redirect:/desenvolvimentos/" + id;
        }
        Desenvolvimento dev = service.buscarPorId(id);
        Orcamento orc = new Orcamento();
        orc.setDesenvolvimento(dev);
        orc.setFornecedor(fornecedor);
        orc.setTamanho(tamanho);
        orc.setTamanho2(tamanho2);
        orc.setTamanho3(tamanho3);
        orc.setValor(valor);
        orc.setValor2(valor2);
        orc.setValor3(valor3);
        orc.setQuantidade(quantidade);
        orc.setObservacao(observacao);
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
                                   @RequestParam(required = false) String tamanho,
                                   @RequestParam(required = false) String tamanho2,
                                   @RequestParam(required = false) String tamanho3,
                                   @RequestParam(required = false) BigDecimal valor,
                                   @RequestParam(required = false) BigDecimal valor2,
                                   @RequestParam(required = false) BigDecimal valor3,
                                   @RequestParam(required = false) Integer quantidade,
                                   @RequestParam(required = false) String observacao,
                                   RedirectAttributes ra) {
        Orcamento orc = orcamentoRepository.findById(orcId)
            .orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado: " + orcId));
        orc.setFornecedor(fornecedor);
        orc.setTamanho(tamanho);
        orc.setTamanho2(tamanho2);
        orc.setTamanho3(tamanho3);
        orc.setValor(valor);
        orc.setValor2(valor2);
        orc.setValor3(valor3);
        orc.setQuantidade(quantidade);
        orc.setObservacao(observacao);
        orcamentoRepository.save(orc);
        Desenvolvimento devOrc = service.buscarPorId(devId);
        EtapaDesenvolvimento etapaAlteracao = new EtapaDesenvolvimento();
        etapaAlteracao.setDesenvolvimento(devOrc);
        etapaAlteracao.setTipo(TipoEtapa.ORCAMENTO);
        etapaAlteracao.setDataOcorrencia(LocalDate.now());
        StringBuilder obsAlteracao = new StringBuilder("ALTERAÇÃO - ");
        obsAlteracao.append(fornecedor != null && !fornecedor.isBlank() ? fornecedor : "(sem fornecedor)");
        if (tamanho != null && !tamanho.isBlank()) {
            obsAlteracao.append(" | ").append(tamanho);
            if (valor != null) obsAlteracao.append(": R$").append(String.format("%.2f", valor).replace('.', ','));
        }
        if (tamanho2 != null && !tamanho2.isBlank()) {
            obsAlteracao.append(" | ").append(tamanho2);
            if (valor2 != null) obsAlteracao.append(": R$").append(String.format("%.2f", valor2).replace('.', ','));
        }
        if (tamanho3 != null && !tamanho3.isBlank()) {
            obsAlteracao.append(" | ").append(tamanho3);
            if (valor3 != null) obsAlteracao.append(": R$").append(String.format("%.2f", valor3).replace('.', ','));
        }
        if (quantidade != null) obsAlteracao.append(" | Qtd: ").append(quantidade);
        if (observacao != null && !observacao.isBlank()) obsAlteracao.append(" | ").append(observacao);
        etapaAlteracao.setObservacao(obsAlteracao.toString());
        etapaRepository.save(etapaAlteracao);
        ra.addFlashAttribute("sucesso", "Orçamento atualizado.");
        return "redirect:/desenvolvimentos/" + devId;
    }

    @GetMapping("/{id}/ficha")
    public String ficha(@PathVariable Long id, Model model) {
        Desenvolvimento dev = service.buscarPorId(id);
        List<Orcamento> orcamentos = orcamentoRepository.findByDesenvolvimentoIdOrderByIdAsc(id);
        List<EtapaDesenvolvimento> etapas = etapaRepository.findByDesenvolvimentoIdOrderByDataOcorrenciaAscIdAsc(id);
        LocalDate dataAprovacao = etapas.stream()
            .filter(e -> e.getTipo() == TipoEtapa.APROVADO)
            .map(EtapaDesenvolvimento::getDataOcorrencia)
            .findFirst().orElse(null);
        model.addAttribute("dev", dev);
        model.addAttribute("orcamentos", orcamentos);
        model.addAttribute("dataAprovacao", dataAprovacao);
        return "desenvolvimentos/ficha";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Desenvolvimento excluído.");
        return "redirect:/desenvolvimentos";
    }
}
