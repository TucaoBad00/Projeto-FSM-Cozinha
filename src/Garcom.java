// AGENTE B: o Garçom.
// Ele só guarda seus atributos e sabe fazer ações simples.
// QUEM DECIDE quando trocar de estado são as classes de estado
// (AnotandoPedido, AguardandoPrato, ServindoPrato).
public class Garcom {
    // Limiar: quantos ticks ele leva para anotar um pedido na mesa
    public static final int TEMPO_ANOTAR_PEDIDO = 2;

    // Atributos (características) do Garçom
    private String nome;
    private int tempoNaMesa = 0;     // timer: ticks gastos anotando o pedido atual
    private int pratosEntregues = 0; // contador de pratos servidos
    private Balcao balcao;           // por onde ele conversa com o cozinheiro

    // Cada agente tem a SUA máquina de estados
    private MaquinaDeEstados<Garcom> maquina;

    public Garcom(String nome, Balcao balcao) {
        this.nome = nome;
        this.balcao = balcao;
        // Estado inicial: indo anotar o pedido de uma mesa
        this.maquina = new MaquinaDeEstados<>(this, new AnotandoPedido());
    }

    // Chamado pela Simulacao a cada tick
    public void atualizar() {
        maquina.atualizar();
    }

    // Os estados chamam este método para pedir uma troca de estado
    public void mudarEstado(Estado<Garcom> novoEstado) {
        maquina.mudarEstado(novoEstado);
    }

    // Imprime uma mensagem com o nome do agente na frente
    public void falar(String mensagem) {
        System.out.println("[Garçom " + nome + "] " + mensagem);
    }

    // ----- Ações (comportamentos) do Garçom -----
    public void anotarPedido() {
        tempoNaMesa++;
    }

    public void servirPrato() {
        pratosEntregues++;
    }

    // ----- Getters e Setters -----
    public int getTempoNaMesa() {
        return tempoNaMesa;
    }

    public void setTempoNaMesa(int tempoNaMesa) {
        this.tempoNaMesa = tempoNaMesa;
    }

    public int getPratosEntregues() {
        return pratosEntregues;
    }

    public Balcao getBalcao() {
        return balcao;
    }

    public String getNome() {
        return nome;
    }

    // Nome do estado atual (ex.: "AnotandoPedido"), usado pela tela visual
    public String getNomeEstado() {
        return maquina.getNomeEstadoAtual();
    }
}
