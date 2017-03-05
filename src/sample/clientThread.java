package sample;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

class clientThread extends Thread {

    private DataInputStream is = null;
    private PrintStream os = null;
    private Socket clientSocket = null;
    private final clientThread[] threads;
    private int maxClientsCount;

    public clientThread(Socket clientSocket, clientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
    }

    public void run() {
        int maxClientsCount = this.maxClientsCount;
        clientThread[] threads = this.threads;

        try {
      /*
       * Create input and output streams for this client.
       */
            is = new DataInputStream(clientSocket.getInputStream());
            os = new PrintStream(clientSocket.getOutputStream());

            //Indtast brugernavn

            String name = "morethan12letters";

            while (name.length()>12 || valid(name) == false) {
                os.println("Enter your name - max 12 letters! (Valid Input: A-Z, 0-9, comma, underscore): ");
                name = is.readLine().trim();
                //Check også i nedenstående if om name er en dublet
                if(name.length()>12 || valid(name) == false) {
                    os.println("J_ERR");
                }

            }

            os.println("Hello [" + name +"]"
                    + " Hit Enter/Return-key to join chatroom");
            os.write(name.getBytes());
            String getJOIN ="JOIN " + name;
            getJOIN = getJOIN.concat(is.readLine());


            System.out.println(getJOIN);

            if(getJOIN.startsWith("JOIN")){
                os.println("J_OK");
            }
            os.println("To leave type \"/quit\" and hit Enter/Return-key");
            os.println("Enter message: ");

            showHeartbeat showHeartbeat = new showHeartbeat();
            showHeartbeat.name = name;

            Timer timer = new Timer();
            timer.schedule(showHeartbeat, 0, 60000);

            Client client = new Client();
            client.setUsername(name);
            client.setPort(clientSocket.getPort());
            client.setClientAddress(clientSocket.getInetAddress());

            TCPChatServer.addToClientList(client);

            System.out.println("New User :" + name + " port: " + clientSocket.getPort() + " IP: "
                    + clientSocket.getInetAddress() +
                    " has connected to the server");
            // Add også bruger til clientList
            // og tjek i while loop at der kun bruges valid input!

            // Print to all clients that a new user has joined
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        threads[i].os.println("*** New User " + name
                                + " entered the chat room! ***");

                    }
                }
            }

            //Send beskeder

            while (true) {
                String line = is.readLine();

                if (line.startsWith("/quit")) {
                    break;
                }
                synchronized (this) {
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null) {
                            threads[i].os.println("DATA [" + name + "]: " + line);
                        }
                    }
                }
                System.out.println("Message received from User: [" + name + "] port: " + clientSocket.getPort() + " IP: "
                        + clientSocket.getInetAddress());
            }


            TCPChatServer.removeFromClientList(client,name);
            System.out.println("User: [" + name + "] left the chatroom.");
            os.println("Server says Bye");

            //Print to all clients
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        threads[i].os.println(" The user [" + name
                                + "] has left the chat room.");
                        threads[i].os.println("Updated Client List: " + TCPChatServer.clientList);
                    }
                }
            }

            TCPChatServer.removeFromClientList(client,name);
            System.out.println("User: [" + name + "] left the chatroom.");
            os.println("Server says Bye");


            timer.cancel();




      /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
       */synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == this) {
                        threads[i] = null;
                    }
                }
            }

      /*
       * Close the output stream, close the input stream, close the socket.
       */
            is.close();
            os.close();
            clientSocket.close();
        } catch (IOException e) {
        }



    }

    //regex checker ved hjælp af en Matcher om parametret kun indeholder de specificerede tegn
    public static boolean valid (String name){
        if (name.matches("[0-9a-zA-Z_-]+")){
            return true;
        }
        else
            return false;
    }

}

class showHeartbeat extends TimerTask {

    String name;
    int count = 0;

    public void run() {

        if (count == 0)
            System.out.println(name + " is now alive.");

        if (count == 1)
            System.out.println(name + " has been alive for " + count + " minute.");

        if (count > 1)
            System.out.println(name + " has been alive for " + count + " minutes.");

        count++;
    }


}
