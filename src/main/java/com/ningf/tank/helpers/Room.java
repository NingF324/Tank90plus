package com.ningf.tank.helpers;

public class Room {
    private int room_id;
    private String room_description;
    private String room_status;
    private String room_ip;
    private String room_port;
    private String room_master;


    public Room() {
    }

    public Room(int room_id, String room_description, String room_status, String room_ip, String room_port, String room_master) {
        this.room_id = room_id;
        this.room_description = room_description;
        this.room_status = room_status;
        this.room_ip = room_ip;
        this.room_port = room_port;
        this.room_master = room_master;
    }

    public int getRoom_id() {
        return room_id;
    }

    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }

    public String getRoom_description() {
        return room_description;
    }

    public void setRoom_description(String room_description) {
        this.room_description = room_description;
    }

    public String getRoom_status() {
        return room_status;
    }

    public void setRoom_status(String room_status) {
        this.room_status = room_status;
    }

    public String getRoom_ip() {
        return room_ip;
    }

    public void setRoom_ip(String room_ip) {
        this.room_ip = room_ip;
    }

    public String getRoom_port() {
        return room_port;
    }

    public void setRoom_port(String room_port) {
        this.room_port = room_port;
    }

    public String getRoom_master() {
        return room_master;
    }

    public void setRoom_master(String room_master) {
        this.room_master = room_master;
    }
}
