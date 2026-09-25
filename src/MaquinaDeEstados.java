// Máquina de estados genérica: guarda o estado atual de UM agente
// e é o único lugar do código que faz a troca de estados.
public class MaquinaDeEstados<T> {
    // O agente dono desta máquina
    private T dono;
    // O estado em que o agente está agora
    private Estado<T> estadoAtual;

    public MaquinaDeEstados(T dono, Estado<T> estadoInicial) {
        this.dono = dono;
        this.estadoAtual = estadoInicial;
        // O estado inicial também precisa da sua ação de entrada
        estadoAtual.entrar(dono);
    }

    // Chamado a cada tick: executa o estado atual
    public void atualizar() {
        estadoAtual.executar(dono);
    }

    // Troca de estado sempre na mesma ordem: sair -> trocar -> entrar
    public void mudarEstado(Estado<T> novoEstado) {
        // getSimpleName() devolve o nome da classe (ex.: "Cozinhando")
        System.out.println("    >>> TRANSIÇÃO: " + estadoAtual.getClass().getSimpleName()
                + " -> " + novoEstado.getClass().getSimpleName());

        estadoAtual.sair(dono);    // 1. ação de saída do estado antigo
        estadoAtual = novoEstado;  // 2. troca o estado
        estadoAtual.entrar(dono);  // 3. ação de entrada do estado novo
    }

    public Estado<T> getEstadoAtual() {
        return estadoAtual;
    }
}
