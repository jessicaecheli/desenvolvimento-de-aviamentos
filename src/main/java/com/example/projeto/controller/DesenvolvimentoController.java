package com.example.projeto.controller;

import com.example.projeto.entity.*;
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
import java.util.List;

@Controller
@RequestMapping("/desenvolvimentos")
public class DesenvolvimentoController {

    private final DesenvolvimentoService service;
    private final MarcaService marcaService;
    private final ColecaoService colecaoService;
    private final CategoriaService categoriaService;
    private final EtapaDesenvolvimentoRepository etapaRepository;
    private final OrcamentoRepository orcamentoRepository;

    public DesenvolvimentoController(DesenvolvimentoService service,
                                      MarcaService marcaService,
                                      ColecaoService colecaoService,
                                      CategoriaService categoriaService,
                                      EtapaDesenvolvimentoRepository etapaRepository,
                                      OrcamentoRepository orcamentoRepository) {
        this.service = service;
        this.marcaService = marcaService;
        this.colecaoService = colecaoService;
        this.categoriaService = categoriaService;
        this.etapaRepository = etapaRepository;
        this.orcamentoRepository = orcamentoRepository;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) Long colecaoId,
                        @RequestParam(required = false) Long marcaId,
                        @RequestParam(required = false) Long categoriaId,
                        @RequestParam(required = false) StatusDesenvolvimento status,
                        @RequestParam(required = false) String codigo,
                        Model model) {
        List<Desenvolvimento> lista = service.listarComFiltros(colecaoId, marcaId, categoriaId, status, codigo);
        model.addAttribute("desenvolvimentos", lista);
        model.addAttribute("marcas", marcaService.listarTodas());
        model.addAttribute("colecoes", colecaoService.listarTodas());
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("statusValues", StatusDesenvolvimento.values());
        model.addAttribute("filtroColecaoId", colecaoId);
        model.addAttribute("filtroMarcaId", marcaId);
        model.addAttribute("filtroCategoriaId", categoriaId);
        model.addAttribute("filtroStatus", status);
        model.addAttribute("filtroCodigo", codigo);
        return "desenvolvimentos/lista";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("desenvolvimento", new Desenvolvimento());
        model.addAttribute("marcas", marcaService.listarTodas());
        model.addAttribute("colecoes", colecaoService.listarTodas());
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("statusValues", StatusDesenvolvimento.values());
        return "desenvolvimentos/form";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        Desenvolvimento dev = service.buscarPorId(id);
        model.addAttribute("desenvolvimento", dev);
        model.addAttribute("marcas", marcaService.listarTodas());
        model.addAttribute("colecoes", colecaoService.listarTodas());
        model.addAttribute("categorias", categoriaService.listarTodas());
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
        List<Orcamento> orcamentos = orcamentoRepository.findByDesenvolvimentoIdOrderByIdAsc(id);
        Long menorOrcamentoId = orcamentos.stream()
            .filter(o -> o.getValorMinimo() != null)
            .min(Comparator.comparing(Orcamento::getValorMinimo))
            .map(Orcamento::getId)
            .orElse(null);
        model.addAttribute("dev", dev);
        model.addAttribute("etapas", etapas);
        model.addAttribute("orcamentos", orcamentos);
        model.addAttribute("menorOrcamentoId", menorOrcamentoId);
        model.addAttribute("tiposEtapa", TipoEtapa.values());
        model.addAttribute("atrasado", service.estaAtrasado(dev));
        model.addAttribute("diasAtraso", service.diasAtraso(dev));
        model.addAttribute("totalOrcamentos", orcamentoRepository.countByDesenvolvimentoId(id));
        return "desenvolvimentos/detalhe";
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
                                @RequestParam TipoEtapa tipo,
                                @RequestParam(required = false) String observacao,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataOcorrencia,
                                RedirectAttributes ra) {
        service.avancarEtapa(id, tipo, observacao, dataOcorrencia);
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
        ra.addFlashAttribute("sucesso", "Orçamento atualizado.");
        return "redirect:/desenvolvimentos/" + devId;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Desenvolvimento excluído.");
        return "redirect:/desenvolvimentos";
    }
}
