import java.io.*;
import java.net.Socket;

public class ChatClient {
	private final String serverName;
	private final int serverPort;
	private Socket socket;
	private OutputStream versoServer;
	private InputStream dalServer;

	public ChatClient(String serverName, int serverPort){
		this.serverName = serverName;
		this.serverPort = serverPort;
	}

	public OutputStream getOutputStream(){
	    return versoServer;
    }

    public InputStream getInputStream(){
	    return dalServer;
    }
	
	public void connect() throws IOException {
		//per ogni client viene istanziato un socket verso il server (dunque una connessione)
		//ed i relativi "mezzi" per la comunicazione con esso
        this.socket = new Socket(serverName,serverPort);
		this.versoServer = socket.getOutputStream();
		this.dalServer = socket.getInputStream();
	}
}
