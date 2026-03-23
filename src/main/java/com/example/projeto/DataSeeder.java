package com.example.projeto;

import com.example.projeto.entity.*;
import com.example.projeto.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final MarcaRepository marcaRepo;
    private final ColecaoRepository colecaoRepo;
    private final CategoriaRepository categoriaRepo;
    private final DesenvolvimentoRepository devRepo;
    private final EtapaDesenvolvimentoRepository etapaRepo;
    private final OrcamentoRepository orcRepo;

    public DataSeeder(MarcaRepository marcaRepo, ColecaoRepository colecaoRepo,
                      CategoriaRepository categoriaRepo, DesenvolvimentoRepository devRepo,
                      EtapaDesenvolvimentoRepository etapaRepo, OrcamentoRepository orcRepo) {
        this.marcaRepo = marcaRepo;
        this.colecaoRepo = colecaoRepo;
        this.categoriaRepo = categoriaRepo;
        this.devRepo = devRepo;
        this.etapaRepo = etapaRepo;
        this.orcRepo = orcRepo;
    }

    @Override
    public void run(String... args) {
        // Garante categorias master SEMPRE, independente de outros dados existentes
        if (!categoriaRepo.existsByNomeIgnoreCase("AVIAMENTOS"))
            categoriaRepo.save(categoriaMaster("AVIAMENTOS"));
        if (!categoriaRepo.existsByNomeIgnoreCase("TECIDOS"))
            categoriaRepo.save(categoriaMaster("TECIDOS"));

        if (devRepo.count() > 0) return; // já tem dados de demo, não faz nada

        // --- Marcas ---
        Marca zara     = marcaRepo.save(new Marca("ZARA"));
        Marca renner   = marcaRepo.save(new Marca("RENNER"));
        Marca forever  = marcaRepo.save(new Marca("FOREVER 21"));

        // --- Coleções ---
        Colecao inv26 = colecao("INVERNO", 2026);
        Colecao ver27 = colecao("VERÃO",   2027);
        Colecao inv27 = colecao("INVERNO", 2027);
        inv26 = colecaoRepo.save(inv26);
        ver27 = colecaoRepo.save(ver27);
        inv27 = colecaoRepo.save(inv27);

        // =====================================================================
        // INVERNO 2026 — coleção encerrada, itens finalizados
        // =====================================================================

        // AV001 — APROVADO
        Desenvolvimento av001 = dev("AV001", "BOLSA ESTRUTURADA COURO", zara, inv26, null,
                StatusDesenvolvimento.APROVADO, LocalDate.of(2025, 7, 1));
        av001 = devRepo.save(av001);
        etapa(av001, TipoEtapa.ORCAMENTO,  LocalDate.of(2025, 7, 1),  null, 1);
        etapa(av001, TipoEtapa.AMOSTRA,    LocalDate.of(2025, 7, 15), "Amostra aprovada na 1ª rodada", 1);
        etapa(av001, TipoEtapa.APROVADO,   LocalDate.of(2025, 8, 5),  "Aprovado para produção", 1);
        orcamento(av001, "COUROS SA",    "P", "M", "G",   new BigDecimal("189.90"), 200, "Entrega 30 dias");
        orcamento(av001, "ARTIGOS LTDA", "P", "M", null,  new BigDecimal("175.00"), 200, null);

        // AV002 — APROVADO
        Desenvolvimento av002 = dev("AV002", "MULE COURO NATURAL", renner, inv26, null,
                StatusDesenvolvimento.APROVADO, LocalDate.of(2025, 7, 5));
        av002 = devRepo.save(av002);
        etapa(av002, TipoEtapa.ORCAMENTO,  LocalDate.of(2025, 7, 5),  null, 1);
        etapa(av002, TipoEtapa.AMOSTRA,    LocalDate.of(2025, 7, 20), null, 1);
        etapa(av002, TipoEtapa.ALTERACAO,  LocalDate.of(2025, 7, 28), "Ajuste no bico", 1);
        etapa(av002, TipoEtapa.APROVADO,   LocalDate.of(2025, 8, 12), "Aprovado após 1 alteração", 1);
        orcamento(av002, "CALÇADOS BELEM", "36", "37", "38", new BigDecimal("98.50"), 300, null);

        // AV003 — CANCELADO
        Desenvolvimento av003 = dev("AV003", "CINTO TRANÇADO VEGANO", zara, inv26, null,
                StatusDesenvolvimento.CANCELADO, LocalDate.of(2025, 7, 10));
        av003 = devRepo.save(av003);
        etapa(av003, TipoEtapa.ORCAMENTO,  LocalDate.of(2025, 7, 10), null, 1);
        etapa(av003, TipoEtapa.CANCELADO,  LocalDate.of(2025, 7, 25), "Cancelado por restrição de custo", 1);

        // =====================================================================
        // VERÃO 2027 — coleção em andamento, mistura de status
        // =====================================================================

        // AV004 — AMOSTRA ATRASADA (38+ d.u. desde amostra, bolsa prazo 15)
        Desenvolvimento av004 = dev("AV004", "BOLSA PALHA COLORIDA", forever, ver27, null,
                StatusDesenvolvimento.AMOSTRA, LocalDate.of(2026, 1, 5));
        av004 = devRepo.save(av004);
        etapa(av004, TipoEtapa.ORCAMENTO, LocalDate.of(2026, 1, 5),  null, 1);
        etapa(av004, TipoEtapa.AMOSTRA,   LocalDate.of(2026, 1, 20), "Aguardando retorno do fornecedor", 1);
        orcamento(av004, "ARTESANATO MG", "U", null, null, new BigDecimal("65.00"), 150, "Palha natural");
        orcamento(av004, "CESTAS & CIA",  "U", null, null, new BigDecimal("72.00"), 150, "Palha sintética");

        // AV005 — AMOSTRA ATRASADA (calçado prazo 10)
        Desenvolvimento av005 = dev("AV005", "SANDÁLIA PLATAFORMA CORTIÇA", zara, ver27, null,
                StatusDesenvolvimento.AMOSTRA, LocalDate.of(2026, 1, 10));
        av005 = devRepo.save(av005);
        etapa(av005, TipoEtapa.ORCAMENTO, LocalDate.of(2026, 1, 10), null, 1);
        etapa(av005, TipoEtapa.AMOSTRA,   LocalDate.of(2026, 2, 3),  null, 1);
        orcamento(av005, "SOLADOS SP",    "35", "36", "37", new BigDecimal("55.80"), 250, null);

        // AV006 — ALTERAÇÃO ATRASADA (acessório prazo 8)
        Desenvolvimento av006 = dev("AV006", "ÓCULOS TARTARUGA DEGRADÊ", renner, ver27, null,
                StatusDesenvolvimento.ALTERACAO, LocalDate.of(2026, 1, 15));
        av006 = devRepo.save(av006);
        etapa(av006, TipoEtapa.ORCAMENTO,  LocalDate.of(2026, 1, 15), null, 1);
        etapa(av006, TipoEtapa.AMOSTRA,    LocalDate.of(2026, 2, 5),  null, 1);
        etapa(av006, TipoEtapa.ALTERACAO,  LocalDate.of(2026, 2, 20), "Ajuste na armação e degradê", 2);
        orcamento(av006, "ÓTICAS PRIME",   "U", null, null, new BigDecimal("32.00"), 400, "Lente UV400");

        // AV007 — AMOSTRA VENCENDO ESTA SEMANA (acessório prazo 8, amostra ~5 d.u. atrás)
        Desenvolvimento av007 = dev("AV007", "COLAR DOURADO GEOMÉTRICO", zara, ver27, null,
                StatusDesenvolvimento.AMOSTRA, LocalDate.of(2026, 2, 20));
        av007 = devRepo.save(av007);
        etapa(av007, TipoEtapa.ORCAMENTO, LocalDate.of(2026, 2, 20), null, 1);
        etapa(av007, TipoEtapa.AMOSTRA,   LocalDate.of(2026, 3, 12), "Amostra recebida", 1);
        orcamento(av007, "BIJOUX ATELIÊ",  "U", null, null, new BigDecimal("18.50"), 500, null);

        // AV008 — ORÇAMENTO (recém iniciado)
        Desenvolvimento av008 = dev("AV008", "BOLSA MINIMALISTA NYLON", forever, ver27, null,
                StatusDesenvolvimento.ORCAMENTO, LocalDate.of(2026, 3, 1));
        av008 = devRepo.save(av008);
        etapa(av008, TipoEtapa.ORCAMENTO, LocalDate.of(2026, 3, 1), null, 1);
        orcamento(av008, "NYLON TECH",     "P", "M", "G", new BigDecimal("88.00"), 200, "Zíper YKK");
        orcamento(av008, "MALAS & BOLSAS", "P", "M", "G", new BigDecimal("92.50"), 200, null);

        // AV009 — AMOSTRA VENCENDO ESTA SEMANA (calçado prazo 10, amostra ~7 d.u. atrás)
        Desenvolvimento av009 = dev("AV009", "RASTEIRINHA COURO TRESSÊ", renner, ver27, null,
                StatusDesenvolvimento.AMOSTRA, LocalDate.of(2026, 2, 25));
        av009 = devRepo.save(av009);
        etapa(av009, TipoEtapa.ORCAMENTO, LocalDate.of(2026, 2, 25), null, 1);
        etapa(av009, TipoEtapa.AMOSTRA,   LocalDate.of(2026, 3, 10), "Amostra em avaliação", 1);
        orcamento(av009, "CALÇADOS BELEM", "35", "36", "37", new BigDecimal("49.90"), 300, null);

        // =====================================================================
        // INVERNO 2027 — coleção nova, em fase inicial
        // =====================================================================

        // AV010 — NOVO (sem etapas ainda)
        Desenvolvimento av010 = dev("AV010", "BOTA CANO LONGO CAMURÇA", zara, inv27, null,
                StatusDesenvolvimento.NOVO, LocalDate.of(2026, 3, 10));
        devRepo.save(av010);

        // AV011 — ORÇAMENTO
        Desenvolvimento av011 = dev("AV011", "BOLSA ENVELOPE VERNIZ", renner, inv27, null,
                StatusDesenvolvimento.ORCAMENTO, LocalDate.of(2026, 3, 12));
        av011 = devRepo.save(av011);
        etapa(av011, TipoEtapa.ORCAMENTO, LocalDate.of(2026, 3, 12), null, 1);
        orcamento(av011, "VERNIZ BRASIL",  "P", "M", null, new BigDecimal("145.00"), 180, "Acabamento espelhado");

        // AV012 — AMOSTRA VENCENDO ESTA SEMANA (acessório prazo 8)
        Desenvolvimento av012 = dev("AV012", "ECHARPE WOOL LISTRADA", forever, inv27, null,
                StatusDesenvolvimento.AMOSTRA, LocalDate.of(2026, 2, 25));
        av012 = devRepo.save(av012);
        etapa(av012, TipoEtapa.ORCAMENTO, LocalDate.of(2026, 2, 25), null, 1);
        etapa(av012, TipoEtapa.AMOSTRA,   LocalDate.of(2026, 3, 11), "Amostra em aprovação", 1);
        orcamento(av012, "TEXTIL FRIO",    "U", null, null, new BigDecimal("28.00"), 350, "100% lã");

        // AV013 — ALTERAÇÃO ATRASADA (acessório prazo 8)
        Desenvolvimento av013 = dev("AV013", "LUVA COURO FORRADA", zara, inv27, null,
                StatusDesenvolvimento.ALTERACAO, LocalDate.of(2026, 2, 1));
        av013 = devRepo.save(av013);
        etapa(av013, TipoEtapa.ORCAMENTO,  LocalDate.of(2026, 2, 1),  null, 1);
        etapa(av013, TipoEtapa.AMOSTRA,    LocalDate.of(2026, 2, 15), null, 1);
        etapa(av013, TipoEtapa.ALTERACAO,  LocalDate.of(2026, 2, 28), "Ajuste no forro e costura", 2);
        orcamento(av013, "COUROS SA",      "P/M", "G/GG", null, new BigDecimal("42.00"), 250, null);

        // AV014 — APROVADO
        Desenvolvimento av014 = dev("AV014", "SAPATILHA VELUDO BORDADA", renner, inv27, null,
                StatusDesenvolvimento.APROVADO, LocalDate.of(2026, 1, 20));
        av014 = devRepo.save(av014);
        etapa(av014, TipoEtapa.ORCAMENTO, LocalDate.of(2026, 1, 20), null, 1);
        etapa(av014, TipoEtapa.AMOSTRA,   LocalDate.of(2026, 2, 5),  null, 1);
        etapa(av014, TipoEtapa.APROVADO,  LocalDate.of(2026, 2, 25), "Aprovado sem alterações", 1);
        orcamento(av014, "SOLADOS SP",     "35", "36", "37", new BigDecimal("62.00"), 220, null);
        orcamento(av014, "CALÇADOS BELEM", "35", "36", "38", new BigDecimal("58.50"), 220, "Veludo importado");

        // AV015 — CANCELADO
        Desenvolvimento av015 = dev("AV015", "PULSEIRA CRISTAL TCHECO", forever, inv27, null,
                StatusDesenvolvimento.CANCELADO, LocalDate.of(2026, 2, 10));
        av015 = devRepo.save(av015);
        etapa(av015, TipoEtapa.ORCAMENTO, LocalDate.of(2026, 2, 10), null, 1);
        etapa(av015, TipoEtapa.CANCELADO, LocalDate.of(2026, 3, 1),  "Custo acima do budget", 1);
    }

    // --- helpers ---

    private Colecao colecao(String nome, int ano) {
        Colecao c = new Colecao();
        c.setNome(nome);
        c.setAno(ano);
        return c;
    }

    private Categoria categoriaMaster(String nome) {
        Categoria c = new Categoria();
        c.setNome(nome);
        return c;
    }

    private Desenvolvimento dev(String codigo, String descricao, Marca marca, Colecao colecao,
                                 Categoria categoria, StatusDesenvolvimento status, LocalDate criacao) {
        Desenvolvimento d = new Desenvolvimento();
        d.setCodigo(codigo);
        d.setDescricao(descricao);
        d.setMarca(marca);
        d.setColecao(colecao);
        d.setCategoria(categoria);
        d.setStatus(status);
        d.setDataCriacao(criacao);
        return d;
    }

    private void etapa(Desenvolvimento dev, TipoEtapa tipo, LocalDate data, String obs, int rodada) {
        EtapaDesenvolvimento e = new EtapaDesenvolvimento();
        e.setDesenvolvimento(dev);
        e.setTipo(tipo);
        e.setDataOcorrencia(data);
        e.setObservacao(obs);
        e.setNumeroRodada(rodada);
        etapaRepo.save(e);
    }

    private void orcamento(Desenvolvimento dev, String fornecedor,
                            String t1, String t2, String t3,
                            BigDecimal valor, Integer qtd, String obs) {
        Orcamento o = new Orcamento();
        o.setDesenvolvimento(dev);
        o.setFornecedor(fornecedor);
        o.setTamanho(t1);
        o.setTamanho2(t2);
        o.setTamanho3(t3);
        o.setValor(valor);
        o.setQuantidade(qtd);
        o.setObservacao(obs);
        orcRepo.save(o);
    }
}
