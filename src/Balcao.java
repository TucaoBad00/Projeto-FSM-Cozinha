// Balcão da cozinha: é por aqui que os dois agentes se comunicam (bônus).
// - O Garçom coloca PEDIDOS e o Cozinheiro pega.
// - O Cozinheiro coloca PRATOS prontos e o Garçom pega.
// Nenhum agente mexe diretamente no outro: tudo passa pelo balcão.
public class Balcao {
    private int pedidos = 0; // pedidos esperando o cozinheiro
    private int pratos = 0;  // pratos prontos esperando o garçom

    // ----- Pedidos (Garçom -> Cozinheiro) -----
    public void colocarPedido() {
        pedidos++;
        System.out.println("    [Balcão] Novo pedido! Pedidos na fila: " + pedidos);
    }

    public boolean temPedido() {
        return pedidos > 0;
    }

    public void pegarPedido() {
        pedidos--;
    }

    // ----- Pratos (Cozinheiro -> Garçom) -----
    public void colocarPrato() {
        pratos++;
        System.out.println("    [Balcão] Prato pronto! Pratos no balcão: " + pratos);
    }

    public boolean temPrato() {
        return pratos > 0;
    }

    public void pegarPrato() {
        pratos--;
    }
}
