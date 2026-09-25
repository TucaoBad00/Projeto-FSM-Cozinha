// Estado do COZINHEIRO: esperando um pedido chegar no balcão.
public class EsperandoPedido implements Estado<Cozinheiro> {

    @Override
    public void entrar(Cozinheiro cozinheiro) {
        cozinheiro.falar("Vou esperar um pedido no balcão.");
    }

    @Override
    public void executar(Cozinheiro cozinheiro) {
        // Condição de saída: existe pedido no balcão? (comunicação com o garçom)
        if (cozinheiro.getBalcao().temPedido()) {
            cozinheiro.getBalcao().pegarPedido();
            cozinheiro.falar("Chegou um pedido!");
            cozinheiro.mudarEstado(new Cozinhando());
        } else {
            cozinheiro.falar("Nenhum pedido ainda... | Cansaço: " + cozinheiro.getCansaco());
        }
    }

    @Override
    public void sair(Cozinheiro cozinheiro) {
        cozinheiro.falar("Pegando o pedido do balcão.");
    }
}
