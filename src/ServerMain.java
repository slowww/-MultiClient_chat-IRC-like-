import java.io.IOException;

public class ServerMain {
	public static void main(String[] args) {
        int port;

		if (args.length == 1){ //Supporto i parametri da riga di comando
            port = Integer.parseInt(args[0]);
        }else{
            port = 8818;
        }
        try {
            Server server = new Server(port);
        } catch (IOException e) {
            System.out.println("Errore: impossibile avviare il server sulla porta " + port);
        }
    }
}
