package com.ningf.tank.ui;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.scene.SubScene;
import com.almasb.fxgl.texture.Texture;
import com.ningf.tank.GameType;
import com.ningf.tank.ProjectVar;
import com.ningf.tank.TankApp;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author LeeWyatt
 * 失败后的场景
 */
public class FailedScene extends SubScene {

    private final TranslateTransition tt;
    private Texture gameOverTexture;

    public FailedScene() {
        if(ProjectVar.isOnlineGame){
            if(ProjectVar.isServer){
                if(ProjectVar.diedPlayer.equals(TankApp.player)){
                    gameOverTexture = texture("ui/GameOver.png");
                }else {
                    gameOverTexture = texture("ui/YouWin.png");
                }
            } else {
                if(ProjectVar.diedPlayer.equals(TankApp.player)){
                    gameOverTexture = texture("ui/YouWin.png");
                }else {
                    gameOverTexture = texture("ui/GameOver.png");
                }
            }
        }else {
            gameOverTexture = texture("ui/GameOver.png");
        }
        gameOverTexture.setScaleX(2);
        gameOverTexture.setScaleY(2);
        gameOverTexture.setTranslateY(getAppHeight() - gameOverTexture.getHeight() + 24);
        gameOverTexture.setTranslateX(28*24 / 2.0
                - gameOverTexture.getWidth() / 2.0);
        tt = new TranslateTransition(Duration.seconds(3.8), gameOverTexture);
        tt.setInterpolator(Interpolators.ELASTIC.EASE_OUT());
        tt.setToY(getAppHeight() / 2.0 - gameOverTexture.getHeight() / 2);
        tt.setOnFinished(e -> {
            FXGL.getSceneService().popSubScene();
            gameOverTexture.setTranslateY(getAppHeight() - gameOverTexture.getHeight() + 24);
            //清理关卡的残留(这里主要是清理声音残留)
            getGameWorld().getEntitiesByType(
                    GameType.BULLET, GameType.ENEMY, GameType.PLAYER
            ).forEach(Entity::removeFromWorld);
            getGameController().gotoMainMenu();
        });
        getContentRoot().getChildren().add(gameOverTexture);

    }

    @Override
    public void onCreate() {
        play("GameOver.wav");
        tt.play();
    }


}
