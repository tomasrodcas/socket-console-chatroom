import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private DatagramSocket dgSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUser;
    private String clientState;
    private boolean running = true;

    public ClientHandler(Socket socket, DatagramSocket dgSocket){

        try {
            this.socket = socket;
            this.dgSocket = dgSocket;
            this.dgSocket.setSoTimeout(1000);
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader( new InputStreamReader(socket.getInputStream()));
            String[] userInfo = bufferedReader.readLine().split("/");
            this.clientUser =  userInfo[0];
            this.clientState = userInfo[1];
            clientHandlers.add(this);
            broadcastMessage("ha entrado al chat!");
        } catch (IOException e) {
            e.printStackTrace();
            closeHandler(socket, bufferedReader, bufferedWriter, dgSocket);
        }
    }

    @Override
    public void run(){
        String message;

        while(running){
            receiveState();
            try{
                message = bufferedReader.readLine();
                if (message == null || message.equals("--exit")){
                    closeHandler(socket, bufferedReader, bufferedWriter, dgSocket);
                    break;
                }
                if (message.equals("--connected")){
                    StringBuilder connectedClients  = new StringBuilder();
                    connectedClients.append("Conectados:  ");
                    for(ClientHandler connectedClient : clientHandlers){
                        connectedClients.append("[").append(connectedClient.clientUser).append(" - ").append(connectedClient.clientState).append("] ");
                    }
                    this.bufferedWriter.write(String.valueOf(connectedClients));
                    this.bufferedWriter.newLine();
                    this.bufferedWriter.flush();
                }
                broadcastMessage(message);
            }catch(IOException e){
                e.printStackTrace();
                closeHandler(socket, bufferedReader, bufferedWriter, dgSocket);
                break;
            }
        }
    }
    public void receiveState(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                String state;

                while(running){
                    try{
                        byte[] buffer = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        dgSocket.receive(packet);
                        state = new String(packet.getData(), packet.getOffset(), packet.getLength());
                        String userToUpdate = state.split("/")[0];
                        String stateToUpdate = state.split("/")[1];
                        for(ClientHandler client : clientHandlers){
                            if (client.clientUser.equals(userToUpdate)){
                                client.clientState = stateToUpdate;
                            }

                        }

                    } catch(SocketTimeoutException ignored){

                    } catch(IOException e){
                        e.printStackTrace();
                        closeHandler(socket, bufferedReader, bufferedWriter, dgSocket);
                        break;
                    }
                }
            }
        }).start();
    }

    public void broadcastMessage(String message){

        for(ClientHandler clientHandler: clientHandlers){
            try{
                if(!clientHandler.clientUser.equals(clientUser)){
                    clientHandler.bufferedWriter.write(clientUser+" ("+clientState+"): "+message);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }catch(IOException e){
                e.printStackTrace();
                closeHandler(socket, bufferedReader, bufferedWriter, dgSocket);
                break;
            }
        }
    }
    public void removeHandler(){
        clientHandlers.remove(this);
        broadcastMessage(clientUser+" se ha desconectado!");
    }

    public void closeHandler(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter, DatagramSocket dgSocket){

        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if( bufferedWriter != null){
                bufferedWriter.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        running = false;
        clientHandlers.removeIf(clientHandler -> clientHandler.clientUser.equals(clientUser));
        Thread.currentThread().interrupt();
    }
}
