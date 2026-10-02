// AGENTE A: o Cozinheiro.
// Assim como o Juca, ele só guarda seus atributos e sabe fazer ações simples.
// QUEM DECIDE quando trocar de estado são as classes de estado
// (EsperandoPedido, Cozinhando, Descansando).
public class Cozinheiro {
    // Limiares (valores fixos que controlam as transições)
    public static final int PROGRESSO_PRATO_PRONTO = 100; // prato fica pronto em 100%
    public static final int LIMITE_CANSACO = 60;          // a partir daqui precisa descansar

    // Quanto cada ação altera os atributos
    private static final int PROGRESSO_POR_TICK = 25;
    private static final int CANSACO_COZINHANDO = 10;
    private static final int DESCANSO_POR_TICK = 20;

    // Atributos (características) do Cozinheiro
    private String nome;
    private int cansaco = 0;        // de 0 a ~100
    private int progressoPrato = 0; // de 0 a 100 (%)
    private Balcao balcao;          // por onde ele conversa com o garçom

    // Cada agente tem a SUA máquina de estados
    private MaquinaDeEstados<Cozinheiro> maquina;

    public Cozinheiro(String nome, Balcao balcao) {
        this.nome = nome;
        this.balcao = balcao;
        // Estado inicial: esperando o primeiro pedido
        this.maquina = new MaquinaDeEstados<>(this, new EsperandoPedido());
    }

    // Chamado pela Simulacao a cada tick
    public void atualizar() {
        maquina.atualizar();
    }

    // Os estados chamam este método para pedir uma troca de estado
    public void mudarEstado(Estado<Cozinheiro> novoEstado) {
        maquina.mudarEstado(novoEstado);
    }

    // Imprime uma mensagem com o nome do agente na frente
    public void falar(String mensagem) {
        System.out.println("[Cozinheiro " + nome + "] " + mensagem);
    }

    // ----- Ações (comportamentos) do Cozinheiro -----
    public void cozinhar() {
        progressoPrato += PROGRESSO_POR_TICK;
        cansaco += CANSACO_COZINHANDO;
    }

    public void descansar() {
        cansaco -= DESCANSO_POR_TICK;
        // Garante que o cansaço não fique negativo
        if (cansaco < 0) {
            cansaco = 0;
        }
    }

    // ----- Getters e Setters -----
    public int getCansaco() {
        return cansaco;
    }

    public int getProgressoPrato() {
        return progressoPrato;
    }

    public void setProgressoPrato(int progressoPrato) {
        this.progressoPrato = progressoPrato;
    }

    public Balcao getBalcao() {
        return balcao;
    }

    public String getNome() {
        return nome;
    }

    // Nome do estado atual (ex.: "Cozinhando"), usado pela tela visual
    public String getNomeEstado() {
        return maquina.getNomeEstadoAtual();
    }
}
