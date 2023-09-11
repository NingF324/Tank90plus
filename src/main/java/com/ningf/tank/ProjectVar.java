package com.ningf.tank;

import com.almasb.fxgl.entity.Entity;
import com.ningf.tank.network.TCPServer;

import java.net.Socket;

public class ProjectVar {
    public static TCPServer tcpServer;

    public static Socket client;

    public static boolean isServer;

    public static int playerAmount;

    public static boolean isOnlineGame;
    public static Entity diedPlayer;

    public static String selectedRoomIp;
    public static String selectedRoomPort;

    public static String createRoomIp;
    public static String createRoomPort="9090";

}
