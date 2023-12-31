package com.ningf.tank.ui;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.input.view.KeyView;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.ui.DialogService;
import com.ningf.tank.ProjectVar;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Optional;

import static com.almasb.fxgl.dsl.FXGL.*;
import static javafx.scene.input.KeyCode.*;

/**
 * 游戏的主菜单场景
 */
public class GameMainMenu extends FXGLMenu {

    private final TranslateTransition tt;
    private final Pane defaultPane;

    public GameMainMenu() {
        super(MenuType.MAIN_MENU);
        Texture texture = texture("ui/logo.png");
        texture.setLayoutX(144);
        texture.setLayoutY(160);

//        MainMenuButton loginBtn = new MainMenuButton("Login", () -> {
//            getContentRoot().getChildren().setAll(new LoginInPane());
//        });


        //单人游戏
        MainMenuButton standAloneGameBtn = new MainMenuButton("Local Game", ()->{



            DialogService dialogService=getDialogService();
            //设置按钮
            Button singlePlayer=getUIFactoryService().newButton("1Player");
            Button doublePlayer=getUIFactoryService().newButton("2Player");
            Button cancelBtn=getUIFactoryService().newButton("Cancel");
            //设置按钮事件
            singlePlayer.addEventFilter(ActionEvent.ACTION, event -> {
                ProjectVar.playerAmount = 1;
                ProjectVar.isOnlineGame = false;
                FXGL.getGameController().startNewGame();
            });
            doublePlayer.addEventFilter(ActionEvent.ACTION,event -> {
                ProjectVar.playerAmount = 2;
                ProjectVar.isOnlineGame = false;
                FXGL.getGameController().startNewGame();
            });
            cancelBtn.addEventFilter(ActionEvent.ACTION,event -> {
                getGameController().gotoMainMenu();
            });
            //设置布局放入三个按钮
            HBox hb=new HBox();
            hb.getChildren().addAll(singlePlayer,doublePlayer,cancelBtn);
            //展示布局
            dialogService.showBox("选择游戏模式",hb);

        });

//在线游戏
        MainMenuButton networkPlayBtn = new MainMenuButton("Online Game", () -> {
            getContentRoot().getChildren().setAll(new LoginInPane());
        });



//        MainMenuButton newOnlineGameBtn = new MainMenuButton("START Online GAME", ()->{
//            ProjectVar.playerAmount=2;
//            ProjectVar.isOnlineGame=true;
//            Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
//            alert.setHeaderText("是否选择作为服务器");
//            alert.setContentText("请选择");
//            Optional<ButtonType> result=alert.showAndWait();
//            if(result.get()==ButtonType.OK){
//                ProjectVar.isServer=true;
//                FXGL.getGameController().startNewGame();
//            }else {
//                ProjectVar.isServer=false;
//                FXGL.getGameController().startNewGame();
//            }
//        });

        MainMenuButton constructBtn = new MainMenuButton("CONSTRUCT", () -> {
            getContentRoot().getChildren().setAll(new ConstructPane());
        });
        MainMenuButton helpBtn = new MainMenuButton("HELP", this::instructions);
        MainMenuButton exitBtn = new MainMenuButton("EXIT", () -> getGameController().exit());
        ToggleGroup tg = new ToggleGroup();
        tg.getToggles().addAll(standAloneGameBtn, networkPlayBtn,constructBtn, helpBtn, exitBtn);
        standAloneGameBtn.setSelected(true);
        VBox menuBox = new VBox(
                5,
                standAloneGameBtn,
                networkPlayBtn,
                constructBtn,
                helpBtn,
                exitBtn
        );
        menuBox.setAlignment(Pos.CENTER_LEFT);
        menuBox.setLayoutX(240);
        menuBox.setLayoutY(360);
        menuBox.setVisible(false);

        Texture tankTexture = FXGL.texture("ui/tankLoading.png");

        tt = new TranslateTransition(Duration.seconds(2), tankTexture);
        tt.setInterpolator(Interpolators.ELASTIC.EASE_OUT());
        tt.setFromX(172);
        tt.setFromY(252);
        tt.setToX(374);
        tt.setToY(252);
        tt.setOnFinished(e -> menuBox.setVisible(true));

        Rectangle bgRect = new Rectangle(getAppWidth(), getAppHeight());
        Line line = new Line(30, 580, 770, 580);
        line.setStroke(Color.web("#B9340D"));
        line.setStrokeWidth(2);
        Texture textureWall = texture("ui/fxgl.png");
        textureWall.setLayoutX(310);
        textureWall.setLayoutY(600);

        defaultPane = new Pane(bgRect, texture, tankTexture, menuBox, line, textureWall);
        getContentRoot().getChildren().setAll(defaultPane);
    }

    @Override
    public void onCreate() {
        getContentRoot().getChildren().setAll(defaultPane);
        FXGL.play("mainMenuLoad.wav");
        tt.play();
    }

    /**
     * 显示玩家使用帮助.比如如何移动坦克,如何发射子弹
     */
    private void instructions() {
        GridPane pane = new GridPane();
        pane.setHgap(20);
        pane.setVgap(15);
        KeyView kvW = new KeyView(W);
        kvW.setPrefWidth(38);
        TilePane tp1 = new TilePane(kvW, new KeyView(S), new KeyView(A), new KeyView(D));
        tp1.setPrefWidth(200);
        tp1.setHgap(2);
        tp1.setAlignment(Pos.CENTER_LEFT);

        pane.addRow(0, getUIFactoryService().newText("Movement"), tp1);
        pane.addRow(1, getUIFactoryService().newText("Shoot"), new KeyView(SPACE));
        KeyView kvL = new KeyView(LEFT);
        kvL.setPrefWidth(38);
        TilePane tp2 = new TilePane(new KeyView(UP), new KeyView(DOWN), kvL, new KeyView(RIGHT));
        tp2.setPrefWidth(200);
        tp2.setHgap(2);
        tp2.setAlignment(Pos.CENTER_LEFT);
        pane.addRow(2, getUIFactoryService().newText("Movement"), tp2);
        pane.addRow(3, getUIFactoryService().newText("Shoot"), new KeyView(ENTER));
        DialogService dialogService = getDialogService();
        dialogService.showBox("Help", pane, getUIFactoryService().newButton("OK"));
    }

}
