import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private DatagramSocket dgSocket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String username;
    private String state;

    public Client(Socket socket, String username, String state, DatagramSocket dgSocket){
        try{
            this.username = username;
            this.state = state;
            this.socket = socket;
            this.dgSocket = dgSocket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }catch(IOException e){
            closeClient(socket, bufferedReader, bufferedWriter, dgSocket);
        }
    }

    public void sendMessage(){
        try{
            bufferedWriter.write(username+"/"+state);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);

            while(socket.isConnected()){
                String message = scanner.nextLine();
                switch (message) {
                    case "--help" -> helpMenu();
                    case "--state" -> {
                        System.out.println("Ingrese su nuevo estado");
                        state = scanner.nextLine();
                    }
                    case "--exit","--close", "--c","--e" -> {
                        System.out.println("Saliendo del chat!");
                        bufferedWriter.write("--exit");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                        closeClient(socket, bufferedReader, bufferedWriter, dgSocket);
                        System.exit(1);
                    }
                    default -> {
                        bufferedWriter.write(message);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                }
            }
        }catch(IOException e){
            closeClient(socket, bufferedReader, bufferedWriter, dgSocket);
        }
    }
    public void getMessage(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                String messageFromChat;

                while(socket.isConnected()){
                    try{
                        messageFromChat = bufferedReader.readLine();
                        if (messageFromChat == null){
                            closeClient(socket, bufferedReader, bufferedWriter, dgSocket);
                        }
                        System.out.println(messageFromChat);
                    }catch(IOException e){
                        closeClient(socket, bufferedReader, bufferedWriter, dgSocket);
                    }
                }
            }
        }).start();
    }
    public void informState(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                String messageFromChat;
                while(true){
                    try{
                        byte[] buffer = new byte[1024];
                        String dataSend = username+"/"+state;
                        byte[] data = dataSend.getBytes(StandardCharsets.UTF_8);
                        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("localhost"), 1235);
                        dgSocket.send(packet);
                        Thread.sleep(2000);

                    }catch(IOException e){
                        closeClient(socket, bufferedReader, bufferedWriter, dgSocket);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        closeClient(socket, bufferedReader, bufferedWriter, dgSocket);
                    }
                }
            }
        }).start();
    }

    public void closeClient(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter, DatagramSocket dgSocket){
        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if( bufferedWriter != null){
                bufferedWriter.close();
            }
            if (socket != null){
                socket.close();
            }
            if (dgSocket != null){
                dgSocket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void helpMenu(){
        System.out.println("Los comandos existentes son los siguientes: ");
        System.out.println("--state     Cambiar el estado actual");
        System.out.println("--exit, --close, --c, --e    Cerrar el cliente y salir del chat");
        System.out.println("--connected     Mostrar lista de los conectados al chat");
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username for the chat: ");
        String username = scanner.nextLine();
        System.out.println("Enter your state for the chat: ");
        String state = scanner.nextLine();
        Socket socket = new Socket("localhost", 1234);
        DatagramSocket dgSocket = new DatagramSocket();
        Client client = new Client(socket, username, state, dgSocket);
        client.getMessage();
        client.informState();
        client.sendMessage();

    }


}
