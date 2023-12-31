package com.ningf.tank.ui;

import com.almasb.fxgl.app.services.FXGLDialogService;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.ui.FXGLDialogFactoryServiceProvider;
import com.ningf.tank.ProjectVar;
import com.ningf.tank.TankApp;
import com.ningf.tank.helpers.Room;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.Consumer;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getAppHeight;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getAppWidth;

public class GreatHallPane extends SplitPane {

    //数据集
    //private final ObservableList<Room> rooms;

    //private TextField room_name_field=new TextField();

    private Alert alert = new Alert(Alert.AlertType.INFORMATION);

    //获取当前ip
    private InetAddress addr ;

    private String hostAddress;

    //房主名字
    private String roomMasterName;

    private int localRoomNum;


    private Scene creatRoomScene;
    public GreatHallPane(String UN) {
        setPrefSize(getAppWidth(), getAppHeight());//设置界面大小

        /**
         * 获取本机ip
         */
        try {
            addr=InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        hostAddress = addr.getHostAddress();
        localRoomNum=0;

        roomMasterName=UN;//房主名字，同时也是登陆的账号的名字
        initGreatHall();//初始化界面


    }

    private void initGreatHall() {
        //产生表视图对象
        TableView<Room> tableView=new TableView<>();
        tableView.setMinHeight(2*FXGL.getAppHeight());
        //tableView.setStyle("-fx-font-style:");

        //设置表视图是否编辑
        tableView.setEditable(true);


        //设计表中的列字段
        TableColumn room_id=new TableColumn("房间号");
        TableColumn room_description=new TableColumn("描述");
        TableColumn room_status=new TableColumn("状态");
        TableColumn room_ip=new TableColumn("房间ip");
        TableColumn room_port=new TableColumn("端口号");
        TableColumn room_master=new TableColumn("房主");
        
        //设置格子宽度
        room_id.setMinWidth(100);
        room_description.setMinWidth(100);
        room_status.setMinWidth(100);
        room_ip.setMinWidth(100);
        room_port.setMinWidth(100);
        room_master.setMinWidth(100);

        //setCellFactory
        room_id.setCellValueFactory(new PropertyValueFactory<>("room_id"));
        room_description.setCellValueFactory(new PropertyValueFactory<>("room_description"));
        room_status.setCellValueFactory(new PropertyValueFactory<>("room_status"));
        room_ip.setCellValueFactory(new PropertyValueFactory<>("room_ip"));
        room_port.setCellValueFactory(new PropertyValueFactory<>("room_port"));
        room_master.setCellValueFactory(new PropertyValueFactory<>("room_master"));
        
//以下<-------------------------------------------------------------------------------------------------->
        getDataFromDB(tableView,room_id,room_description,room_status,room_ip,room_port,room_master);
        tableView.getColumns().addAll(room_id,room_description,room_status,room_ip,room_port,room_master);
//以上<-------------------------------------------------------------------------------------------------->


        //创建一个ScrollPane,把tableview放进去
        ScrollPane scrollPane = new ScrollPane(tableView);
        scrollPane.setPrefHeight(FXGL.getAppHeight());
        scrollPane.setStyle("-fx-background-color: black");

        //创建左侧AnchorPane
        AnchorPane leftAnchorPane = new AnchorPane(scrollPane);

//<------------------------------------------------------------------------------------------------------>        
        //分割线，上面是tableview部分

        //退出按钮，返回主菜单
        Button btnBack = getUIFactoryService().newButton("返回主菜单");
        btnBack.setGraphic(new Region());
        btnBack.setId("btn-back");
        btnBack.setOnAction(event -> {
            getGameController().gotoMainMenu();
        });

        /**
         * 进入房间按钮，
         * 如果房间空闲，可以读取到所选房间的ip和端口，
         * 调起开始联机游戏的函数
         * 在数据库更新房间状态(状态改为gaming)
         */
        
        Button btnEnterRoom = getUIFactoryService().newButton("进入房间");
        btnEnterRoom.setGraphic(new Region());
        btnEnterRoom.setLayoutX(650);
        btnEnterRoom.setId("btn-EnterRoom");

        //获取所选行
        //int moveIndex = tableView.getSelectionModel().getFocusedIndex();
        int focusedIndex = tableView.getSelectionModel().getFocusedIndex();



        btnEnterRoom.setOnAction(event -> {
            TableView.TableViewSelectionModel<Room> selectionModel = tableView.getSelectionModel();
            Room selectedRoom = selectionModel.getSelectedItem();
            //System.out.println(selectedRoom.getRoom_status());

            if(selectedRoom==null){
                Alert alert2=new Alert(Alert.AlertType.ERROR);
                alert2.setHeaderText("错误");
                alert2.setContentText("您未选择房间");
                alert2.show();
                return;
            }
            else {
                if(selectedRoom.getRoom_status().equals("gaming")){
                    Alert alert1 = new Alert(Alert.AlertType.ERROR);
                    alert1.setHeaderText("进入失败");
                    alert1.setContentText("该房间正在游戏中，请选择其他房间");
                    alert1.show();
                }
                else if(selectedRoom.getRoom_ip().equals(hostAddress)){
//                    ProjectVar.selectedRoomIp=selectedRoom.getRoom_ip();
//                    ProjectVar.selectedRoomPort=selectedRoom.getRoom_port();

                    if(localRoomNum==0){
                        ProjectVar.selectedRoomIp=selectedRoom.getRoom_ip();
                        ProjectVar.selectedRoomPort=selectedRoom.getRoom_port();

                        ProjectVar.playerAmount=2;
                        ProjectVar.isOnlineGame=true;
                        ProjectVar.isServer=false;
                        FXGL.getGameController().startNewGame();
                        localRoomNum = 2;
                    }
                    else {
                        Alert alert3 = new Alert(Alert.AlertType.ERROR);
                        alert3.setHeaderText("进入成功");
                        alert3.setContentText("请等待其他玩家进入");
                        alert3.show();
                        System.out.println("请等待其他玩家进入");


                        ProjectVar.playerAmount=2;
                        ProjectVar.isOnlineGame=true;
                        ProjectVar.isServer=true;
                        FXGL.getGameController().startNewGame();
                    }



                    //createRoom(selectedRoom.getRoom_description(),ProjectVar.selectedRoomIp,ProjectVar.selectedRoomPort);

                    //输入房间的IP和端口，开始游戏



                    selectedRoom.setRoom_status("gaming");
                } else if (!selectedRoom.getRoom_ip().equals(hostAddress)) {
                    ProjectVar.selectedRoomIp=selectedRoom.getRoom_ip();
                    ProjectVar.selectedRoomPort=selectedRoom.getRoom_port();

                    ProjectVar.playerAmount=2;
                    ProjectVar.isOnlineGame=true;
                    ProjectVar.isServer=false;
                    FXGL.getGameController().startNewGame();
                }
            }

            System.out.println(focusedIndex);

        });

        /**
         * 创建房间按钮
         * 判断当前ip是否已经在数据库存在，
         * 如果已经存在，弹窗已创建房间，请等待其他玩家进入
         * 否则
         * 调起一个界面，让用户输入房间描述，
         * 设置房间状态为waiting
         * 自动获取本机的ip
         * 强制规定每个房间的端口都是9090
         * 自动在数据库添加房主的id
         */
        Button btnCreateRoom = getUIFactoryService().newButton("创建房间");
        btnCreateRoom.setGraphic(new Region());
        btnCreateRoom.setId("btn-CreateRoom");
        btnCreateRoom.setOnAction(event -> {

            //获取当前玩家的id
            String roomMaster=roomMasterName;

            ProjectVar.createRoomIp=hostAddress;

            TextInputDialog textInputDialog=new TextInputDialog();
            textInputDialog.setHeaderText("请输入房间的描述");

            textInputDialog.showAndWait();
            String roomDescription = textInputDialog.getResult();


            if (isRoomExist(hostAddress)) {
                localRoomNum=1;
                Alert alert1 = new Alert(Alert.AlertType.ERROR);
                alert1.setHeaderText("创建失败");
                alert1.setContentText("您已创建过房间，请等待其他玩家进入");
                alert1.show();
                System.out.println("您已创建过房间，请等待其他玩家进入");
            }
            else {
                createRoom(roomDescription,hostAddress,roomMaster);
                localRoomNum=1;

                getDataFromDB(tableView,room_id,room_description,room_status,room_ip,room_port,room_master);

                Alert alert2 = new Alert(Alert.AlertType.ERROR);
                alert2.setHeaderText("创建成功");
                alert2.setContentText("您已创建过房间，请等待其他玩家进入");
                alert2.show();
                System.out.println("创建房间成功");
            }

            ProjectVar.playerAmount=2;
            ProjectVar.isOnlineGame=true;
            ProjectVar.isServer=true;
            FXGL.getGameController().startNewGame();



        });




        /**
         * 垂直布局，管理右侧区域
         */
        VBox vBox=new VBox(btnBack,btnEnterRoom,btnCreateRoom);
        vBox.setSpacing(30);
        vBox.setAlignment(Pos.BASELINE_CENTER);


        Label label=new Label();
        Image image = new Image("assets/textures/ui/fxgl.png");
        ImageView imageView = new ImageView(image);
        label.setGraphic(imageView);
        label.setRotate(90);
        label.setLayoutY(400);
        AnchorPane rightAnchorPane = new AnchorPane(vBox,label);





        //设置分割线，放入左右块
        this.setDividerPosition(0,scrollPane.getWidth());
        this.getItems().setAll(leftAnchorPane,rightAnchorPane);
        this.setStyle("-fx-background-color: black");


    }

    /**
     * 创建房间，并更新到数据库
     * @param desc
     * @param addr
     * @param master
     */
    private void createRoom(String desc,String addr,String master) {
        String url = "jdbc:mysql://127.0.0.1:3306/tank?serverTimezone=GMT";
        String user = "root";
        String pwd = "1234";
        String jdbc = "com.mysql.cj.jdbc.Driver";
        int rst ;
        Connection cont = null;
        PreparedStatement ppst = null;

        String room_description=desc;
        String room_ip=addr;


        try {
            Class.forName(jdbc);
        }catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
        String creatSql = "INSERT INTO room1(room_description,room_status,room_ip,room_port,room_master) VALUES (?,?,?,?,?)";
        ObservableList<Room> data = FXCollections.observableArrayList();
        try {
            Connection connection = DriverManager.getConnection(url, user, pwd);
            ppst = connection.prepareStatement(creatSql);
            ppst.setString(1,room_description);
            ppst.setString(2,"waiting");
            ppst.setString(3,room_ip);
            ppst.setString(4,"9090");
            ppst.setString(5,master);
            rst = ppst.executeUpdate();

        }catch(Exception e) {
            e.printStackTrace();
        }finally {
            if(cont != null && ppst != null ) {
                try {
                    cont.close();
                    ppst.close();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 导入数据库中的房间数据
     * @param table
     * @param room_id
     * @param room_description
     * @param room_status
     * @param room_ip
     * @param room_port
     * @param room_master
     */
    public void getDataFromDB(TableView table,  TableColumn room_id, TableColumn room_description,
                     TableColumn room_status, TableColumn room_ip,TableColumn room_port,TableColumn room_master) {

        String url = "jdbc:mysql://127.0.0.1:3306/tank?serverTimezone=GMT";
        String user = "root";
        String pwd = "1234";
        String jdbc = "com.mysql.cj.jdbc.Driver";
        ResultSet rst = null;
        Connection cont = null;
        PreparedStatement ppst = null;

        try {
            Class.forName(jdbc);
        }catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
        String querySql = "SELECT All(room_id), room_description, room_status, room_ip,room_port,room_master FROM room1";
        ObservableList<Room> data = FXCollections.observableArrayList();
        try {
            Connection connection = DriverManager.getConnection(url, user, pwd);
            ppst = connection.prepareStatement(querySql);
            rst = ppst.executeQuery(querySql);
            //System.out.print("连接成功");
            while(rst.next()) {
                data.add(new
                        Room(rst.getInt(1),rst.getString(2),rst.getString(3),
                        rst.getString(4),rst.getString(5),rst.getString(6)));

                table.setItems(data);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }finally {
            if(cont != null && ppst != null && rst != null) {
                try {
                    cont.close();
                    ppst.close();
                    rst.close();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 查找该ip是否已经创建过房间
     * @param text
     */
    public boolean isRoomExist(String text) {
        String url = "jdbc:mysql://127.0.0.1:3306/tank?serverTimezone=GMT";
        String user = "root";
        String pwd = "1234";
        String jdbc = "com.mysql.cj.jdbc.Driver";
        ResultSet rst = null;
        Connection cont = null;
        PreparedStatement ppst = null;

        //String room_ip="110110110";
        String room_ip_par=text;


        try {
            Class.forName(jdbc);
        }catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
        String querySql = "SELECT room_ip FROM room1 where room_ip = ?";

        ObservableList<Room> data = FXCollections.observableArrayList();
        try {
            System.out.println("111111111");
            Connection connection = DriverManager.getConnection(url, user, pwd);
            System.out.println("222222222");
            ppst = connection.prepareStatement(querySql);
            System.out.println("333333333");
            ppst.setString(1,room_ip_par);

            System.out.println(querySql);


            rst = ppst.executeQuery();


            System.out.println("连接成功");

            if(rst.next()){
                System.out.println("rst不为空");
                System.out.println(rst.getString("room_ip"));
                return true;
            }
            else {
                System.out.println("rst为空");
            }
            //System.out.println(rst.getString("room_ip"));


            rst.close();
            ppst.close();
            //cont.close();

        }catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}




