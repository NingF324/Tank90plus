package com.ningf.tank.ui;

import com.almasb.fxgl.scene.Scene;
import com.almasb.fxgl.scene.SceneService;
import com.almasb.fxgl.scene.SubScene;
import com.almasb.fxgl.ui.DialogService;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.sql.*;
import java.util.Locale;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * 登录页面
 */
public class LoginInPane extends Pane {
    public LoginInPane(){
        setBackground(new Background(new BackgroundFill(Color.BLACK,CornerRadii.EMPTY, Insets.EMPTY)));
        getStyleClass().add("LoginIn-pane");
        setPrefSize(getAppWidth(), getAppHeight());
        //设置总排列
        VBox pane=new VBox();
        //设置用户名和密码标签
        Label userLabel=new Label("Username");
        userLabel.setFont(Font.font("微软雅黑",20));
        userLabel.setStyle("-fx-text-fill: #B9340D");
        Label passwordLabel=new Label("Password");
        passwordLabel.setFont(Font.font("微软雅黑",20));
        passwordLabel.setStyle("-fx-text-fill: #B9340D");
        //设置用户名和密码输入框
        TextField usernameText=new TextField();
        PasswordField passwordText=new PasswordField();
        usernameText.setMinSize(350,45);
        passwordText.setMinSize(350,45);
        //设置输入框透明
        usernameText.setOpacity(0.4);
        passwordText.setOpacity(0.4);
        //设置两个水平排列
        HBox hb1=new HBox();
        HBox hb2=new HBox();
        //放入排列
        hb1.getChildren().addAll(userLabel,usernameText);
        hb1.setSpacing(200);
        hb2.getChildren().addAll(passwordLabel,passwordText);
        hb2.setSpacing(205);
        //设置注册按钮
        Button registerBtn=getUIFactoryService().newButton("Register");
        //设置提示信息
        Tooltip tooltip=new Tooltip("Click here to enter the registration page");
        registerBtn.setTooltip(tooltip);
        //设置登录按钮
        Button loginBtn=getUIFactoryService().newButton("Login");
        //设置退出按钮
        Button exitBtn=getUIFactoryService().newButton("Exit");
        //设置按钮排列
        HBox hb3=new HBox();
        hb3.getChildren().addAll(loginBtn,registerBtn,exitBtn);
        hb3.setSpacing(40);
        //监听Login按钮事件
        loginBtn.addEventFilter(ActionEvent.ACTION, event->{
            //验证登录逻辑
            try {
                //提取输入框内容
                String username=usernameText.getText();
                String password=passwordText.getText();
                //加载驱动
                //把jar包放入项目里
                Class.forName("com.mysql.cj.jdbc.Driver");
                //通过DriverManager得到连接对象
                //url:连接数据库的地址  user：连接数据库的用户名  password：连接数据库的密码
                String url="jdbc:mysql://localhost:3306/tank?serverTimezone=GMT";
                String user="root";
                String JDBCpassword="1234";
                Connection connection= DriverManager.getConnection(url,user,JDBCpassword);
                //通过链接对象得到语句对象
                Statement statement=connection.createStatement();
                //查询数据库表内容
                String sql="select * from usermember";
                ResultSet rs=statement.executeQuery(sql);
                boolean loggedIn=rs.next();
                //比对rs中的数据
                //循环结构
                while(loggedIn){

                    String UN=rs.getString("username");
                    String PW=rs.getString("password");
                    if(UN.equals(username)&&PW.equals(password)){
                        SceneService sceneService = getSceneService();
                        Scene currentScene = sceneService.getCurrentScene();
                        currentScene.getContentRoot().getChildren().setAll(new GreatHallPane(UN));
                        break;
                    }else {
                        loggedIn = rs.next();
                        if (!loggedIn) {
                            //登录失败，显示错误提示信息
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error!");
                            alert.setHeaderText(null);
                            alert.setContentText("Invalid username or password.");
                            alert.showAndWait();
                            //阻止对话框关闭
                            event.consume();
                        } else {
                            continue;
                        }
                    }
                }




                //关闭
                rs.close();
                statement.close();
                connection.close();

            } catch (ClassNotFoundException | SQLException e) {
                throw new RuntimeException(e);
            }
        });
        //监听Register按钮事件
        registerBtn.addEventFilter(ActionEvent.ACTION,event->{
            try {
                registerWindow();
            } catch (ClassNotFoundException | SQLException e) {
                throw new RuntimeException(e);
            }
        });
        //监听Exit按钮事件
        exitBtn.addEventFilter(ActionEvent.ACTION,event -> {
                getGameController().gotoMainMenu();
        });

        Label label=new Label();
        Image image = new Image("assets/textures/ui/logo.png");
        ImageView imageView = new ImageView(image);
        label.setGraphic(imageView);
        label.setLayoutX(150);
        label.setLayoutY(180);
        label.setAlignment(Pos.BASELINE_CENTER);
        //放入排列
        pane.getChildren().addAll(hb1,hb2,hb3);
        pane.setSpacing(40);
        pane.setLayoutX(80);
        pane.setLayoutY(400);
        this.getChildren().addAll(label,pane);
    }
    private void registerWindow() throws ClassNotFoundException, SQLException {
        DialogService registerDialogService=getDialogService();
        //添加控件
        //设置总排列
        GridPane pane1=new GridPane();
        pane1.setHgap(20);
        pane1.setVgap(15);
        //设置用户名和密码标签
        Label userLabel1=new Label("Username");
        userLabel1.setFont(Font.font("微软雅黑",20));
        userLabel1.setStyle("-fx-text-fill: #B9340D");
        Label passwordLabel1=new Label("Password");
        passwordLabel1.setFont(Font.font("微软雅黑",20));
        passwordLabel1.setStyle("-fx-text-fill: #B9340D");
        //设置用户名和密码输入框
        TextField usernameText1=new TextField();
        TextField passwordText1=new TextField();
        usernameText1.setMinWidth(280);
        passwordText1.setMinWidth(280);
        //设置输入框透明
        usernameText1.setOpacity(0.4);
        passwordText1.setOpacity(0.4);
        //设置两个水平排列
        HBox hb11=new HBox();
        HBox hb22=new HBox();
        //放入排列
        hb11.getChildren().addAll(userLabel1,usernameText1);
        hb11.setSpacing(60);
        hb22.getChildren().addAll(passwordLabel1,passwordText1);
        hb22.setSpacing(66);
        //设置确认注册按钮
        Button ConfirmRegisterBtn=getUIFactoryService().newButton("Registration");
        //设置退出按钮
        Button exit1=getUIFactoryService().newButton("Exit");
        //放入排列
        pane1.addRow(0,hb11);
        pane1.addRow(2,hb22);
        //监听ConfirmRegisterBtn按钮事件
        ConfirmRegisterBtn.addEventFilter(ActionEvent.ACTION,event1->{
            try {
                //提取输入框内容
                String UM=usernameText1.getText();
                String PW=passwordText1.getText();
                //加载驱动
                //把jar包放入项目里
                Class.forName("com.mysql.cj.jdbc.Driver");
                //通过DriverManager得到连接对象
                //url:连接数据库的地址  user：连接数据库的用户名  password：连接数据库的密码
                String url="jdbc:mysql://localhost:3306/tank?serverTimezone=GMT";
                String user="root";
                String JDBCpassword="1234";
                Connection connection= DriverManager.getConnection(url,user,JDBCpassword);
                //通过链接对象得到语句对象
                Statement statement=connection.createStatement();
                //加入数据库表内容
                String sql="insert into usermember(username,password) values(?,?)";

                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1,UM);
                preparedStatement.setString(2,PW);
                preparedStatement.executeUpdate();
                //关闭
                statement.close();
                connection.close();

            } catch (ClassNotFoundException | SQLException e) {
                throw new RuntimeException(e);
            }
        });
        registerDialogService.showBox("Register",pane1,ConfirmRegisterBtn,exit1);
    }
}
