import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


class Familia {
    private String id;
    private double rendaPerCapita;
    private int numDependentes;
    private boolean possuiDeficiencia;
    private int tempoDesempregoMeses;
    private String bairroRisco; // alto, medio, baixo
    
    // Construtor, getters e setters omitidos pra encurtar
    public Familia(String id, double renda, int deps, boolean def, int desemp, String bairro) {
        this.id = id;
        this.rendaPerCapita = renda;
        this.numDependentes = deps;
        this.possuiDeficiencia = def;
        this.tempoDesempregoMeses = desemp;
        this.bairroRisco = bairro;
    }
    
    // Getters...
    public String getId() { return id; }
    public double getRendaPerCapita() { return rendaPerCapita; }
    public int getNumDependentes() { return numDependentes; }
    public boolean isPossuiDeficiencia() { return possuiDeficiencia; }
    public int getTempoDesempregoMeses() { return tempoDesempregoMeses; }
    public String getBairroRisco() { return bairroRisco; }
}

class ResultadoPrioridade {
    private String idFamilia;
    private int pontuacaoFinal;
    private List<String> logCalculo;

    public ResultadoPrioridade(String id, int pontuacao, List<String> log) {
        this.idFamilia = id;
        this.pontuacaoFinal = pontuacao;
        this.logCalculo = log;
    }
    
    public int getPontuacaoFinal() { return pontuacaoFinal; }
    public String getIdFamilia() { return idFamilia; }

    public void imprimirExplicacao() {
        System.out.println("--- Extrato de Prioridade - Família " + idFamilia + " ---");
        System.out.println("Pontuação Final: " + pontuacaoFinal + " pontos");
        System.out.println("Como chegamos nesse número:");
        for (String linha : logCalculo) {
            System.out.println("- " + linha);
        }
        System.out.println("--------------------------------------------\n");
    }
}

public class SistemaPrioridadeSocial {

    // CRITÉRIOS PÚBLICOS E FIXOS. Isso vai pro Diário Oficial.
    private static final double PESO_RENDA = 0.4;
    private static final int TETO_RENDA = 1000;
    private static final int PONTO_POR_DEPENDENTE = 15;
    private static final int MAX_DEPENDENTES = 5;
    private static final int BONUS_DEFICIENCIA = 50;
    private static final int BONUS_DESEMPREGO_LONGO = 40;
    private static final int BONUS_BAIRRO_ALTO_RISCO = 30;

    // SUBROTINA 1: Validação = SRP. Para o código se o dado for lixo.
    public static void validarDados(Familia f) {
        if (f.getRendaPerCapita() < 0) {
            throw new IllegalArgumentException("Família " + f.getId() + ": Renda não pode ser negativa.");
        }
        if (f.getNumDependentes() < 0) {
            throw new IllegalArgumentException("Família " + f.getId() + ": Nº de dependentes inválido.");
        }
        if (f.getTempoDesempregoMeses() < 0) {
            throw new IllegalArgumentException("Família " + f.getId() + ": Tempo de desemprego inválido.");
        }
    }

    // SUBROTINA 2: Cálculo isolado. Responsabilidade única.
    public static ResultadoPrioridade calcularPrioridade(Familia f) {
        validarDados(f); // Salvaguarda 1: Impede dado inválido
        
        int pontuacao = 0;
        List<String> log = new ArrayList<>();

        // Regra 1: Renda. Menor renda = maior score.
        int scoreRenda = (int) ((TETO_RENDA - Math.min(f.getRendaPerCapita(), TETO_RENDA)) * PESO_RENDA);
        pontuacao += scoreRenda;
        log.add("Renda per capita R$" + f.getRendaPerCapita() + " = " + scoreRenda + " pts");

        // Regra 2: Dependentes. Com teto pra evitar fraude.
        int depsValidos = Math.min(f.getNumDependentes(), MAX_DEPENDENTES);
        int scoreDeps = depsValidos * PONTO_POR_DEPENDENTE;
        pontuacao += scoreDeps;
        log.add(depsValidos + " dependente(s) = " + scoreDeps + " pts");

        // Regra 3: Deficiência. Dado sensível LGPD.
        if (f.isPossuiDeficiencia()) {
            pontuacao += BONUS_DEFICIENCIA;
            log.add("Possui deficiência = " + BONUS_DEFICIENCIA + " pts");
        }

        // Regra 4: Desemprego longo prazo.
        if (f.getTempoDesempregoMeses() > 12) {
            pontuacao += BONUS_DESEMPREGO_LONGO;
            log.add("Desemprego > 12 meses = " + BONUS_DESEMPREGO_LONGO + " pts");
        }

        // Regra 5: Bairro de risco. RISCO DE VIÉS. Documentar justificativa.
        if ("alto".equalsIgnoreCase(f.getBairroRisco())) {
            pontuacao += BONUS_BAIRRO_ALTO_RISCO;
            log.add("Bairro de alto risco = " + BONUS_BAIRRO_ALTO_RISCO + " pts");
        }
        
        return new ResultadoPrioridade(f.getId(), pontuacao, log);
    }

    public static void main(String[] args) {
        // Lista pré-cadastrada da tabela que a Anhembi deu
        List<Familia> familias = new ArrayList<>();
        familias.add(new Familia("F001", 320.00, 3, true, 8, "alto"));
        familias.add(new Familia("F002", 540.00, 1, false, 2, "baixo"));
        familias.add(new Familia("F003", 290.00, 2, false, 14, "medio"));
        familias.add(new Familia("F004", 410.00, 4, true, 1, "alto"));
        familias.add(new Familia("F005", 380.00, 0, false, 10, "medio"));
        
        List<ResultadoPrioridade> resultados = new ArrayList<>();
        
        // Estrutura repetitiva pra processar todas as famílias
        for (Familia f : familias) {
            try {
                ResultadoPrioridade res = calcularPrioridade(f);
                resultados.add(res);
            } catch (IllegalArgumentException e) {
                System.out.println("ERRO AO PROCESSAR: " + e.getMessage());
            }
        }
        
        // Ordena por prioridade: maior score primeiro
        resultados.sort(Comparator.comparingInt(ResultadoPrioridade::getPontuacaoFinal).reversed());
        
        // Saída final com ordem de prioridade
        System.out.println("======= RANKING DE PRIORIDADE SOCIAL =======");
        int pos = 1;
        for (ResultadoPrioridade r : resultados) {
            System.out.println(pos + "º lugar: Família " + r.getIdFamilia() + " com " + r.getPontuacaoFinal() + " pontos");
            pos++;
        }
        System.out.println("============================================\n");

        // Imprime a explicação detalhada de cada uma = TRANSPARÊNCIA
        for (ResultadoPrioridade r : resultados) {
            r.imprimirExplicacao();
        }
        
        // TESTES SIMPLES: Prova que o cálculo é coerente
        System.out.println("======= TESTES DE COERÊNCIA =======");
        Familia teste1 = new Familia("TESTE1", 100, 1, false, 0, "baixo"); // Baixa renda
        Familia teste2 = new Familia("TESTE2", 900, 1, false, 0, "baixo"); // Renda alta
        System.out.println("Teste 1: Família pobre deve ter score maior que família rica.");
        System.out.println("Score TESTE1: " + calcularPrioridade(teste1).getPontuacaoFinal());
        System.out.println("Score TESTE2: " + calcularPrioridade(teste2).getPontuacaoFinal());
        System.out.println("Resultado: " + (calcularPrioridade(teste1).getPontuacaoFinal() > calcularPrioridade(teste2).getPontuacaoFinal() ? "PASSOU" : "FALHOU"));
    }
}
