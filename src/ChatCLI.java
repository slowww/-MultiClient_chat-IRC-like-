import java.io.*;

public class ChatCLI {
    private void lettoreLoopMsg(BufferedReader bufferedReader) {
        try{
            String line;
            while (true){
                line = bufferedReader.readLine();
                if(line != null){
                    System.out.println(line);
				}
			}
		}catch (IOException e) {
			System.out.println("Errore: Impossibile mettersi in ascolto al server.");
		}
	}

    public void startLettoreMsg(BufferedReader bufferedReader) {
        Thread t = new Thread()
        {
            public void run()
            {
                lettoreLoopMsg(bufferedReader);
            }
        };
        t.start();
    }

    private void lettoreLoopTastiera(BufferedReader bufferedReader, OutputStream outputStream) throws IOException {
        String temp;
        do{
            temp=bufferedReader.readLine() + "\n";
            outputStream.write(temp.getBytes());
        }while (!temp.equalsIgnoreCase("logoff"));
    }

    public void startLettoreTastiera(BufferedReader bufferedReader, OutputStream outputStream) {
        Thread t = new Thread()
        {
            public void run()
            {
            try {
                lettoreLoopTastiera(bufferedReader, outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            }


        };
        t.start();
    }

    public static void main(String args[]){
        String ip;
        int port;

        if (args.length == 2){ //Supporto i parametri da riga di comando
            ip = args[0];
            port = Integer.parseInt(args[1]);
        }else{
            ip = "localhost";
            port = 8818;
        }

        ChatClient client = new ChatClient(ip, port);
        BufferedReader bufferTastiera = new BufferedReader(new InputStreamReader(System.in));

        try {
            client.connect(); //connessione
        } catch (IOException e) {
            System.out.println("Errore: Impossibile connettersi al server.");
        }

        OutputStream outputStream = client.getOutputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream())); //cio che arriva al client
        ChatCLI chatCLI = new ChatCLI();//creazione oggetto di tipo ChatCLI per poter invocare i metodi di loopmsg (multithread)
        String reply = "";


        chatCLI.startLettoreMsg(bufferedReader);
        chatCLI.startLettoreTastiera(bufferTastiera,outputStream);
    }
}
