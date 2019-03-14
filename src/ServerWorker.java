import java.util.*;
import java.io.*;
import java.net.*;

public class ServerWorker extends Thread {
    private List<ServerWorker> workerList;
    private ArrayList<ArrayList<Object>> topicList;
    private ArrayList<String[]> loginList;
	private final Socket clientSocket;
	private String login;
	private OutputStream outputstream;
	private boolean loggato;

	public ServerWorker(ArrayList<ServerWorker> workerList, ArrayList<ArrayList<Object>> topicList, ArrayList<String[]> loginList, Socket clientSocket){

        try {
            this.outputstream = clientSocket.getOutputStream();//quello che il server manda al client
        } catch (IOException e) {
            System.out.println("Errore: Impossibile instaurare una connessione col client.");
        }
        //NOTA: ho dovuto associare l'output stream all'oggetto ServerWorker (mettendo il this) per rendere il flusso
        //di dati verso il client condiviso tra i vari metodi della classe: in questo modo posso utilizzarlo nel
        //metodo Send


		this.clientSocket = clientSocket;
		this.topicList = topicList;
		this.workerList = workerList;
		this.loginList = loginList;
		loggato = false;

        try {
            send("Benvenuto sul server pseudo-IRC di Preda e Iosca.");
            send("Registrati con: register username password");
            send("O effettua il login se sei gia registrato: login username password");
        } catch (IOException e) {
            System.out.println("Errore: Impossibile inviare un messaggio al client client.");
        }
	}
	
////----------------METODI GESTIONE DEI TOPIC------------------------------------------------/////
	ArrayList<Object> trovaTopic(String topic){
		if(topicList == null) return null;
		for (ArrayList<Object> cursore : topicList){
			if(((String)cursore.get(0)).equals(topic)){
				return cursore;
			}
		}
		return null;
	}
	
	public void gestoreJoin(String[] tokens) {
			String topic = tokens[0];
			ArrayList<Object> temp = trovaTopic(topic);
			if(temp == null){
				temp = new ArrayList<>();
				temp.add(topic);
				temp.add(this);
				topicList.add(temp);
			}else{
				temp.add(this);
			}
			System.out.println(temp.toString()); //Per scopi di debug
	}

	public void gestoreLeave(String[] tokens) throws IOException {
			String topic = tokens[0];
			ArrayList<Object> temp = trovaTopic(topic);
			if(temp == null){
                send("Errore: Non e' possibile lasciare il topic " + topic + " in quanto non e' esistente.");
			}else{
				temp.remove(this);
			}
	}
//////---------------------FINE METODI GESTIONE DEI TOPIC-------------------------------/////
	
	private void gestoreClient() throws IOException{
	//gestisce lo scambio di stringhe con il client E BASTA
	//per gestire il login c'� il metodo gestoreLogin() piu in basso
        InputStream inputstream = null;//quello che il client manda al server
        try {
            inputstream = clientSocket.getInputStream();
        } catch (IOException e) {
            System.out.println("Errore: Impossibile instaurare una connessione col client.");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
		
		String stringadalclient = reader.readLine();

		boolean continua = true;

		while(stringadalclient!=null && continua){
            System.out.println("Stringa dal client: " + stringadalclient); // Per scopi di debug
            String[] tokens = stringadalclient.split(" ");
            if (tokens.length > 0) {
                String cmd = tokens[0];//controllo la prima parola inserita
                String[] tokensmsg = new String[tokens.length - 1];

                for (int i = 1; i < tokens.length; i++) {
                    tokensmsg[i - 1] = tokens[i]; //Shifto l'array di una posizione in modo da ottenere un array senza il token, interpredabile da gestoreMessaggi
                }

                switch(cmd.toLowerCase()){
                    case "exit":
                        continua = false;
                        gestoreLogoff();
                        clientSocket.close();
                        break;
                    case "login":
                        if(!loggato) gestisciLogin(tokensmsg); else send("Errore: Risulti gia loggato.");
                        break;
                    case "msg":
                        if(loggato) gestoreMessaggi(tokensmsg); else send("Errore: Devi loggarti per inviare un messaggio.");
                        break;
                    case "join":
                        if(loggato) gestoreJoin(tokensmsg); else send("Errore: Devi loggarti per unirti ad un topic.");
                        break;
                    case "leave":
                        if(loggato) gestoreLeave(tokensmsg); else send("Errore: Devi loggarti per lasciare ad un topic.");
                        break;
                    case "register":
                        if(!loggato) gestoreRegistration(tokensmsg); else send("Errore: Impossibile registrarsi da loggato.");
                        break;
                    default:
                        send("Errore: Comando non riconosciuto e/o sintassi non corretta.");
                }
            }
            stringadalclient = reader.readLine(); //Se non gli fai leggere un'altra riga cicla all'infinito
		}
	}

	private void gestoreRegistration(String[] tokens) throws IOException{
		if(tokens.length==2){
		    boolean trovato = false;
		    for(String[] cursore : loginList){
		        trovato = cursore[0].equals(tokens[0]); //Controllo che non ci sia un altro utente registrato con lo stesso nickname
            }
            if(trovato) send("Errore: E' gia presente un utente registrato col nickname " + tokens[0]);
            else {
                loginList.add(tokens);
                send("Registrazione effettuata con successo. Effettua il login.");
                System.out.println(tokens[0] + " si e' appena registrato." );
            }
        }else send("Errore: Si e' verificato un errore durante la registrazione.");

	}

	public String getLogin() {
		return login;
	}
	
	private void gestoreLogoff() throws IOException {
		workerList.remove(this);

		//comunica a tutti gli user che l'user corrente e' offline
		for (ServerWorker worker : workerList) {
			if(!login.equals(worker.getLogin())) {
				worker.send("L'utente " + login + " si e' appena scollegato.");
			}
		}
		
		clientSocket.close();
		
	}

    private void gestisciLogin(String[] tokens) throws IOException
    //controlla l'inserimento di user e password da parte del client
	//e gli comunica se � andato a buon fine oppure no
	{
		//ovvero la lista di tutti i client connessi al server

		if(tokens.length == 2){//se la lunghezza dell'array e' 2 significa
			//che l'utente ha inserito correttamente la sequenza user e password 

            boolean trovato = false;
            for(ServerWorker serverWorker: workerList){
                trovato = serverWorker.getLogin().equals(tokens[0]); //Controllo che non ci sia un altro utente registrato con lo stesso nickname
            }

            if(trovato) send("Errore: E' gia presente un utente online col nickname " + tokens[0]);
            else{
                for(String cursore[] : loginList){
                    if (tokens[0].equals(cursore[0]) && tokens[1].equals(cursore[1])) {
                        login = tokens[0];
                        send("Login effettuato con successo.");
                        System.out.println(login + " loggato con successo!"); //Debug

                        workerList.add(this); // Bisogna aggiungere il worker attuale alla lista dei worker!!!


                        //Comunica all'utente che si e'collegato la lista degli utenti presenti nel server
                        String temp = "Lista utenti online: ";
                        int count = 0;
                        for (ServerWorker worker : workerList) {

                            if (worker.getLogin() != null && worker.getLogin() != login) {
                                temp += (worker.getLogin() + ", ");
                                count++;
                            }
                        }

                        if(count == 0)
                        {send("Non ci sono utenti collegati al server");}
                        else
                        {send(temp);}

                        //comunica a tutti gli user che l'utente attuale si e' collegato
                        for (ServerWorker worker : workerList)
                        {
                            if(!login.equals(worker.getLogin()))
                            {
                                worker.send("L'utente " + login + " si e' appena collegato al server.");
                            }
                        }
                        loggato = true;
                    }
                }
                if(!loggato){
                    send("Errore: Utente non esistente e/o password non corretta. Ritenta");
                }
            }
		}
	}
		
	public void send(String msg) throws IOException {
		outputstream.write((msg + "\n").getBytes());
	}
	
			
	//FORMATO: "msg *nome utente destinatario [0]* *contenuto del messaggio[>=1]*"
	//FORMATO TOPIC "msg #topic contenuto del messaggio"
	
	public void gestoreMessaggi(String[] tokens) throws IOException {
        String dest = tokens[0];
        String contenuto = "";
        for (int i=1; i<tokens.length;i++){ //Devo unire di nuovo i token in modo da poter inviare un messaggio il cui contenuto ha dei spazi al suo interno
            contenuto += (tokens[i] + " ");
        }

		//quindi eventualmente dest diventa la variabile che contiene il nome del topic

		if (dest.charAt(0) == '#'){
			ArrayList<Object> topic = trovaTopic(dest);
			if(topic == null){
			 send("Topic non esistente!");
			}else{
				for (Object cursore : topic){
					if(cursore instanceof ServerWorker && !((ServerWorker)cursore).equals(this)){
						((ServerWorker)cursore).send("Msg da " + login + " (" + dest + ") : " + contenuto);
					}
				}
			}
		}else{
			for (ServerWorker worker : workerList) {
				if(dest.equalsIgnoreCase(worker.getLogin())) {
					worker.send("Msg da " + login + ": " + contenuto);
				}
			}
		}
	}

	public void run() {
		try {
			gestoreClient();
		} catch (IOException e) {
			if(login==null){
			    System.out.println("La connessione con un client non loggato si e' interrotta."); //Interrotta non significa per forza che sia andato qualcosa storto, magari l'utente ha semplicemente chiuso il client
            }else{
			    System.out.println("La connessione con il client dell'utente " + login + " si e' interrotta");
            }
		}
		workerList.remove(this); //Lo rimuovo dalla lista dei worker attivi
		this.interrupt(); //Interrompo l'esecuzione del thread corrente
        //Ora tocca al Garbage collector invocare il destroyer e buttar via questo oggetto.
	}
}
