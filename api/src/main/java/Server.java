import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket svSocket;
    private DatagramSocket dgSocket;

    public Server(ServerSocket svSocket, DatagramSocket dgSocket){
        this.svSocket =  svSocket;
        this.dgSocket = dgSocket;
    }

    public void start(){
        try {

            while(!svSocket.isClosed()){
                Socket socket = svSocket.accept();
                System.out.println("Nueva conexion creada");
                ClientHandler clientHandler = new ClientHandler(socket, dgSocket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void closeServer(){
        try{
            if(svSocket != null){
                svSocket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        ServerSocket svSocket = new ServerSocket(1234);
        DatagramSocket dgSocket = new DatagramSocket(1235);
        Server server = new Server(svSocket, dgSocket);
        server.start();
    }
}
