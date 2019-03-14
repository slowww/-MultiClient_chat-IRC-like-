import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server{
	private ArrayList<ServerWorker> workerList;
	private ArrayList<ArrayList<Object>> topicList;
	private ArrayList<String[]> loginList;

	public Server (int serverPort) throws IOException {
        ServerSocket serverSocket = new ServerSocket(serverPort);
        workerList = new ArrayList<>();
        topicList = new ArrayList<>();
        loginList = new ArrayList<>();
        while(true){
            Socket clientSocket = serverSocket.accept();
            ServerWorker worker = new ServerWorker(workerList, topicList, loginList, clientSocket);
            worker.start();
        }
	}
}
