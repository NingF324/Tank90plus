package com.ningf.tank.components;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.EffectComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityGroup;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.almasb.fxgl.time.LocalTimer;
import com.ningf.tank.GameConfig;
import com.ningf.tank.ProjectVar;
import com.ningf.tank.effects.ShipEffect;
import com.ningf.tank.GameType;

import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.spawn;

/**
 * @author LeeWyatt
 * 玩家的行为,移动和射击
 */
public class PlayerComponent extends Component {
    /**
     * 为了防止出现斜向上,斜向下等角度的移动,
     */
    private boolean movedThisFrame = false;
    private double speed = 0;
    private Vec2 velocity = new Vec2();
    private BoundingBoxComponent bbox;

    private LazyValue<EntityGroup> blocksAll = new LazyValue<>(() -> entity.getWorld().getGroup(GameType.BRICK, GameType.FLAG, GameType.SEA, GameType.STONE, GameType.ENEMY, GameType.BORDER_WALL));
    private LazyValue<EntityGroup> blocks = new LazyValue<>(() -> entity.getWorld().getGroup(GameType.BRICK, GameType.FLAG, GameType.STONE, GameType.ENEMY, GameType.BORDER_WALL));
    private LocalTimer shootTimer = FXGL.newLocalTimer();
    private LocalTimer moveTimer  = FXGL.newLocalTimer();
    private Dir moveDir = Dir.UP;

    @Override
    public void onUpdate(double tpf) {
        speed = tpf * GameConfig.PLAYER_SPEED;
        movedThisFrame = false;
    }

    public void right() {
        if (!moveTimer.elapsed(GameConfig.PLAYER_MOVE_DELAY)) {
            return;
        }
        if (movedThisFrame) {
            return;
        }
        movedThisFrame = true;
        getEntity().setRotation(90);
        moveDir = Dir.RIGHT;
        move();
    }

    public void left() {
        if (!moveTimer.elapsed(GameConfig.PLAYER_MOVE_DELAY)) {
            return;
        }
        if (movedThisFrame) {
            return;
        }
        movedThisFrame = true;
        getEntity().setRotation(270);
        moveDir = Dir.LEFT;
        move();

    }

    public void down() {
        if (!moveTimer.elapsed(GameConfig.PLAYER_MOVE_DELAY)) {
            return;
        }
        if (movedThisFrame) {
            return;
        }
        movedThisFrame = true;
        getEntity().setRotation(180);
        moveDir = Dir.DOWN;
        move();
    }

    public void up() {
        if (!moveTimer.elapsed(GameConfig.PLAYER_MOVE_DELAY)) {
            return;
        }
        if (movedThisFrame) {
            return;
        }
        movedThisFrame = true;
        getEntity().setRotation(0);
        moveDir = Dir.UP;
        move();
    }

    private void move() {
        if (!getEntity().isActive()) {
            return;
        }
        velocity.set((float) (moveDir.getVector().getX()*speed), (float) (moveDir.getVector().getY()*speed));
        int length = Math.round(velocity.length());
        velocity.normalizeLocal();
        List<Entity> blockList;
        if (entity.getComponent(EffectComponent.class).hasEffect(ShipEffect.class)) {
            blockList = blocks.get().getEntitiesCopy();
        } else {
            blockList = blocksAll.get().getEntitiesCopy();
        }
        for (int i = 0; i < length; i+=1) {
            entity.translate(velocity.x, velocity.y);
            boolean collision = false;
            for (int j = 0; j < blockList.size(); j++) {
                if (blockList.get(j).getBoundingBoxComponent().isCollidingWith(bbox)) {
                    collision = true;
                    break;
                }
            }
            //运动, 遇到障碍物回退
            if (collision) {
                entity.translate(-velocity.x, -velocity.y);
                break;
            }
        }
    }

    public void shoot() {
        if (!shootTimer.elapsed(GameConfig.PLAYER_SHOOT_DELAY)) {
            return;
        }
        spawn("bullet", new SpawnData(getEntity().getCenter().add(-4, -4.5))
                .put("direction", moveDir.getVector())
                .put("owner", entity));
        shootTimer.capture();
    }
}
