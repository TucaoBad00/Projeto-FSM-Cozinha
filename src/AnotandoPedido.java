// Estado do GARÇOM: na mesa, anotando o pedido do cliente.
public class AnotandoPedido implements Estado<Garcom> {

    @Override
    public void entrar(Garcom garcom) {
        // Zera o timer para o novo pedido
        garcom.setTempoNaMesa(0);
        garcom.falar("Indo até a mesa anotar um pedido.");
    }

    @Override
    public void executar(Garcom garcom) {
        // Ação: anotar (timer +1)
        garcom.anotarPedido();
        garcom.falar("Anotando pedido... | Tempo na mesa: " + garcom.getTempoNaMesa()
                + "/" + Garcom.TEMPO_ANOTAR_PEDIDO);

        // Condição de saída: terminou de anotar? (timer chegou no limite)
        if (garcom.getTempoNaMesa() >= Garcom.TEMPO_ANOTAR_PEDIDO) {
            garcom.mudarEstado(new AguardandoPrato());
        }
    }

    @Override
    public void sair(Garcom garcom) {
        // Entrega o pedido ao cozinheiro pelo balcão (comunicação)
        garcom.falar("Pedido anotado! Deixando no balcão.");
        garcom.getBalcao().colocarPedido();
    }
}
