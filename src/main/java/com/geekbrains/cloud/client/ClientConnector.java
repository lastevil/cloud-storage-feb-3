package com.geekbrains.cloud.client;

import java.io.*;
import java.net.Socket;

public class ClientConnector {
    private MainController mainController;
    private DataInputStream is;
    private DataOutputStream os;
    private Socket socket;
    private File curDir;
    private byte[] buf;
    private byte[] bufWr;
    private static final int BUFFER_SIZE = 8192;


    public ClientConnector(MainController controller){
        this.mainController = controller;
        initialiseConnect();
        getServerDirInfo();
       new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    createConnection();
                }catch (IOException exception){
                    exception.printStackTrace();
                }
            }}).start();
   }

    private void createConnection() throws IOException {
            while(socket != null){
                String command = null;
                command = is.readUTF();
                if ("#server_update#".equals(command)){
                getServerDirInfo();
                continue;
                }
                if ("#upload_file#".equals(command)){
                    String name = is.readUTF();
                    long size = is.readLong();
                    File newFile = curDir.toPath()
                            .resolve(name)
                            .toFile();
                    try (OutputStream fos = new FileOutputStream(newFile)) {
                        for (int i = 0; i < (size + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                            int readCount = is.read(bufWr);
                            fos.write(bufWr, 0, readCount);
                        }
                    }
                    System.out.println("File: " + name + " is uploaded");
                }
                if ("#server_close_connection#".equals(command)){
                    if (socket!=null){socket.close();}
                    if (is!=null){is.close();}
                    if (os!=null){os.close();}
                    break;
                }
            }
        }

    private void initialiseConnect(){
        try {
            buf = new byte[BUFFER_SIZE];
            bufWr = new byte[BUFFER_SIZE];
            socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getServerDirInfo(){
        try{
        os.writeUTF("#dir_info#");
        String sb;
        sb = is.readUTF();
        String[] arr = sb.split("##");
        mainController.updateServerView(arr);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void sendSelectedFile(File selected){
        try{
            os.writeUTF("#file_message#");
            os.writeUTF(selected.getName());
            os.writeLong(selected.length());
                try (InputStream fis = new FileInputStream(selected)) {
                    while (fis.available() > 0) {
                        int readBytes = fis.read(buf);
                        os.write(buf, 0, readBytes);
                    }
                }
                os.flush();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getSelectedFile(String item, File currentDirectory) {
        curDir=currentDirectory;
        try{
            os.writeUTF("#give_file_message#");
            os.writeUTF(item);
        } catch (Exception e){
        e.printStackTrace();
        }
    }
    public void sendClose(){
        try {
            os.writeUTF("#close_connection#");
        }catch (IOException i){
            i.printStackTrace();
        }
    }
}
