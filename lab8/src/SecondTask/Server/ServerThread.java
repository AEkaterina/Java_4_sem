package SecondTask.Server;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {
    private Socket socket;
    private BufferedReader reader;   //прием
    private BufferedWriter writer;

    public ServerThread(Socket socket) throws IOException {
        this.socket = socket;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        Server.messageStory.printStory(writer);
        start();
    }

    @Override
    public void run() {
        try {
            String message = reader.readLine();
            try {
                writer.write(message + "\n");
                writer.flush();
            } catch (IOException ignored) { }
            try {
                while (true) {
                    message = reader.readLine();
                    if(message.equals("end")) {
                        this.downService();
                        break;
                    }
                    System.out.println(message);
                    Server.messageStory.addElement(message);
                    for(ServerThread vr : Server.serverThreads) {
                        vr.send(message);
                    }
                }
            } catch (NullPointerException ignored) { }
        } catch (IOException e) {
            this.downService();
        }
    }

    private void downService() {
        try {
            if(!socket.isClosed()) {
                socket.close();
                reader.close();
                writer.close();
                for(ServerThread vr : Server.serverThreads) {
                    if(vr.equals(this))
                        vr.interrupt();
                    Server.serverThreads.remove(this);
                }
            }
        }catch (IOException ignored) { }
    }

    private void send(String message) {
        try {
            writer.write(message + "\n");
            writer.flush();
        } catch (IOException ignored) { }
    }
}
