package com.geekbrains.cloud.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class CloudFileHandler implements Runnable {

    private static final int BUFFER_SIZE = 8192;
    private final DataInputStream is;
    private final DataOutputStream os;
    private Socket socket;
    private final byte[] buf;
    private final byte[] bufWr;
    private File serverDirectory;

    public CloudFileHandler(Socket socket) throws IOException {
        System.out.println("Client connected!");
        this.socket=socket;
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        buf = new byte[BUFFER_SIZE];
        bufWr = new byte[BUFFER_SIZE];
        serverDirectory = new File("server");
    }

    @Override
    public void run() {
        try {
            while (socket!=null) {
                String command= null;
                command = is.readUTF();
                //приём файла
                if ("#file_message#".equals(command)) {
                    fileUpload();
                    continue;
                }
                //отправка файла
                if ("#give_file_message#".equals(command)) {
                    fileDownload();
                    continue;
                }
                if("#dir_info#".equals(command)){
                    sendFileList();
                    continue;
                }
                if("#close_connection#".equals(command)){
                    connectionClose();
                    break;
                }
                else {
                    System.err.println("Unknown command: " + command);
                }
            }
            System.out.println("Client disconnected");
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                if (socket!=null){
                    socket.close();
                }
                if(is != null){
                    is.close();
                }
                if (os!=null){
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void connectionClose() throws IOException{
        os.writeUTF("#server_close_connection#");
        if (socket!= null){socket.close();}
        if (os!=null){os.close();}
        if (is!=null){is.close();}
    }

    private void sendFileList() throws IOException{
        StringBuffer sb = new StringBuffer();
        sb.append(serverDirectory.getAbsolutePath());
        sb.append("##");
        for (int i =0 ; i<serverDirectory.list().length;i++){
            sb.append(serverDirectory.list()[i]);
            sb.append("##");
        }
            os.writeUTF(sb.toString());
            os.flush();
    }

    private void fileUpload() throws IOException{
        String name = is.readUTF();
        long size = is.readLong();
        File newFile = serverDirectory.toPath()
                .resolve(name)
                .toFile();
        try (OutputStream fos = new FileOutputStream(newFile)) {
            for (int i = 0; i < (size + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                int readCount = is.read(buf);
                fos.write(buf, 0, readCount);
            }
        }
        System.out.println("File: " + name + " is uploaded");
        os.writeUTF("#server_update#");
    }

    private void fileDownload() throws IOException{
        String name = is.readUTF();
        File selected = serverDirectory.toPath().resolve(name).toFile();
        os.writeUTF("#upload_file#");
        os.writeUTF(selected.getName());
        os.writeLong(selected.length());
        try (InputStream fis = new FileInputStream(selected)) {
            while (fis.available() > 0) {
                int readBytes = fis.read(bufWr);
                os.write(bufWr, 0, readBytes);
            }
        }
        os.flush();
    }
}
