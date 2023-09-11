package com.ningf.tank.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPServer {
    private ServerSocket serverSocket;
    public Socket socket;

    public TCPServer(int port) throws IOException {
        serverSocket=new ServerSocket(port);
        System.out.println("服务器开始监听:"+port);
    }

    public void start() throws IOException {
        socket=serverSocket.accept();
    }

    public void output(int playerNumber,boolean isUp,boolean isDown,boolean isLeft,
                       boolean isRight,boolean isShoot)
            throws JSONException, IOException {

        JSONObject jsonObject=new JSONObject();
        jsonObject.put("playerNumber",playerNumber);
        jsonObject.put("isUp",isUp);
        jsonObject.put("isDown",isDown);
        jsonObject.put("isLeft",isLeft);
        jsonObject.put("isRight",isRight);
        jsonObject.put("isShoot",isShoot);

        String jsonStr=jsonObject.toString();
        OutputStream outputStream=socket.getOutputStream();
        outputStream.write(jsonStr.getBytes(StandardCharsets.UTF_8));
    }
}
