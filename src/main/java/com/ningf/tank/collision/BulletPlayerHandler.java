package com.ningf.tank.collision;

import com.almasb.fxgl.dsl.components.EffectComponent;
import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.ningf.tank.GameType;
import com.ningf.tank.ProjectVar;
import com.ningf.tank.TankApp;
import com.ningf.tank.effects.HelmetEffect;
import com.ningf.tank.components.OwnerComponent;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author LeeWyatt
 * 子弹和玩家碰撞(为了扩展多个玩家,所以这里忽略了来自同盟的子弹,友军不能误伤;同样忽略的代码在产生子弹实体的方法里)
 * 子弹消失,玩家减少生命值
 */
public class BulletPlayerHandler extends CollisionHandler {

    public BulletPlayerHandler() {
        super(GameType.BULLET, GameType.PLAYER);
    }

    protected void onCollisionBegin(Entity bullet, Entity player) {
        if(bullet.getComponent(OwnerComponent.class).owner.equals(player)){
            return;
        }
        play("normalBomb.wav");
        if (player.getComponent(EffectComponent.class).hasEffect(HelmetEffect.class)) {
            bullet.removeFromWorld();
            return;
        }
        spawn("explode", bullet.getCenter().getX() - 25, bullet.getCenter().getY() - 20);
        bullet.removeFromWorld();
        HealthIntComponent hp = player.getComponent(HealthIntComponent.class);
        hp.damage(1);
        TankApp tankApp = getAppCast();
        if (hp.isZero()) {
            ProjectVar.diedPlayer=player;
            if (!getb("gameOver")) {
                player.removeFromWorld();
                inc("liveNum",-1);
                if (geti("liveNum") == 0) {
                    set("gameOver", true);
                    getSceneService().pushSubScene(tankApp.failedSceneLazyValue.get());
                }
            }
        }
    }
}
