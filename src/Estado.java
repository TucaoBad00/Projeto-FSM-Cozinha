// Interface do padrão State.
// Todo estado de qualquer agente precisa ter estes 3 métodos.
// O "T" é o tipo do agente dono do estado (Cozinheiro ou Garcom).
public interface Estado<T> {

    // Chamado UMA vez, quando o agente ENTRA neste estado
    void entrar(T agente);

    // Chamado a CADA tick (volta do loop) enquanto o agente está neste estado.
    // É aqui que o agente age e que verificamos as regras de transição.
    void executar(T agente);

    // Chamado UMA vez, quando o agente SAI deste estado
    void sair(T agente);
}
