package com.ningf.tank;

import com.almasb.fxgl.app.CursorInfo;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.*;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.dsl.components.EffectComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.tiled.TMXLevelLoader;
import com.almasb.fxgl.time.TimerAction;
import com.ningf.tank.collision.*;
import com.ningf.tank.components.PlayerComponent;
import com.ningf.tank.effects.HelmetEffect;
import com.ningf.tank.network.TCPServer;
import com.ningf.tank.ui.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author LeeWyatt
 */
public class TankApp extends GameApplication {

    public static Entity player;

    public static Entity player2;
    private PlayerComponent playerComponent;
    private PlayerComponent playerComponent2;

    private ExecutorService threadPool; // 线程池用于并行执行在线游戏线程

    private static BufferedReader reader;

    private static String jsonStr;
    private Random random = new Random();
    public LazyValue<FailedScene> failedSceneLazyValue = new LazyValue<>(FailedScene::new);
    private LazyValue<SuccessScene> successSceneLazyValue = new LazyValue<>(SuccessScene::new);

    /**
     * 顶部的三个点,用于产生敌军坦克
     */
    private int[] enemySpawnX = {30, 295 + 30, 589 + 20};

    /**
     * 基地加固定时器动作
     */
    private TimerAction spadeTimerAction;
    /**
     * 敌军冻结计的定时器动作
     */
    private TimerAction freezingTimerAction;
    /**
     * 定时刷新敌军坦克
     */
    private TimerAction spawnEnemyTimerAction;

    @Override
    protected void onPreInit() {
        getSettings().setGlobalSoundVolume(0.5);
        getSettings().setGlobalMusicVolume(0.5);
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(28 * 24 + 6 * 24);
        settings.setHeight(28 * 24);
        settings.setTitle("90 Tank");
        settings.setAppIcon("ui/icon.png");
        settings.setVersion("Version 0.3");
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);
        settings.getCSSList().add("tankApp.css");
        settings.setDefaultCursor(new CursorInfo("ui/cursor.png", 0, 0));
        //FPS,CPU,RAM等信息的显示
        //settings.setProfilingEnabled(true);
        //开发模式.这样可以输出较多的日志异常追踪
        //settings.setApplicationMode(ApplicationMode.DEVELOPER);
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public StartupScene newStartup(int width, int height) {
                //自定义启动场景
                return new GameStartupScene(width, height);
            }

            @Override
            public FXGLMenu newMainMenu() {
                //主菜单场景
                return new GameMainMenu();
            }

            @Override
            public LoadingScene newLoadingScene() {
                //游戏前的加载场景
                return new GameLoadingScene();
            }
        });
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        if (getFileSystemService().exists(GameConfig.CUSTOM_LEVEL_PATH)) {
            vars.put("level", 0);
        }else {
            vars.put("level", 1);
        }
        vars.put("playerBulletLevel", 1);
        vars.put("freezingEnemy", false);
        vars.put("destroyedEnemy", 0);
        vars.put("spawnedEnemy", 0);
        vars.put("gameOver", false);

        vars.put("spawnPlayer",0); //已召唤的玩家
        vars.put("liveNum",1);//现存玩家数量
    }

    @Override
    protected void initInput() {
        onKey(KeyCode.W, ()->{
            try {
                moveUpAction();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
//        onKey(KeyCode.UP,  ()->{
//            try {
//                moveUpAction();
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });

        onKey(KeyCode.S, ()->{
            try {
                moveDownAction();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
//        onKey(KeyCode.DOWN,()->{
//            try {
//                moveDownAction();
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });

        onKey(KeyCode.A,()->{
            try {
                moveLeftAction();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
//        onKey(KeyCode.LEFT, ()->{
//            try {
//                moveLeftAction();
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });

        onKey(KeyCode.D, ()->{
            try {
                moveRightAction();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
//        onKey(KeyCode.RIGHT, ()->{
//            try {
//                moveRightAction();
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });

        onKey(KeyCode.SPACE, ()->{
            try {
                shootAction();
                System.out.println("射击");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
//        onKey(KeyCode.F, ()->{
//            try {
//                shootAction();
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });

        onKey(KeyCode.O,()->{
            if (ProjectVar.isOnlineGame) {
                Thread networkThread = new Thread(() -> {
                    try {
                        startNetwork();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (JSONException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

                networkThread.start();
            }
        });

        onKey(KeyCode.UP, this::moveUpAction2);
        onKey(KeyCode.DOWN, this::moveDownAction2);
        onKey(KeyCode.LEFT, this::moveLeftAction2);
        onKey(KeyCode.RIGHT, this::moveRightAction2);
        onKey(KeyCode.NUMPAD0, this::shootAction2);

        //player2攻击键选哪一个合适？
        onKey(KeyCode.F, this::shootAction2);
    }

    private boolean tankIsReady() {
        return player != null && playerComponent != null && !getb("gameOver") && player.isActive();
    }

    private boolean tank2IsReady() {
        return player2 != null && playerComponent2 != null && !getb("gameOver") && player2.isActive();
    }

    private void shootAction() throws JSONException, IOException {
        if(ProjectVar.isOnlineGame){
            if(ProjectVar.isServer){
                if (tankIsReady()) {
                    playerComponent.shoot();
                    output(1,false,false,false,false,true);
                }
            }else{
                if (tank2IsReady()) {
                    playerComponent2.shoot();
                    output(2,false,false,false,false,true);
                }
            }
        }else {
            if (tankIsReady()) {
                playerComponent.shoot();
            }
        }
    }

    private void moveRightAction() throws JSONException, IOException {
        if(ProjectVar.isOnlineGame){
            if(ProjectVar.isServer){
                if (tankIsReady()) {
                    playerComponent.right();
                    output(1,false,false,false,true,false);
                }
            }else{
                if (tank2IsReady()) {
                    playerComponent2.right();
                    output(2,false,false,false,true,false);
                }
            }
        }else {
            if (tankIsReady()) {
                playerComponent.right();
            }
        }
    }

    private void moveLeftAction() throws JSONException, IOException {
        if(ProjectVar.isOnlineGame){
            if(ProjectVar.isServer){
                if (tankIsReady()) {
                    playerComponent.left();
                    output(1,false,false,true,false,false);
                }
            }else{
                if (tank2IsReady()) {
                    playerComponent2.left();
                    output(2,false,false,true,false,false);
                }
            }
        }else {
            if (tankIsReady()) {
                playerComponent.left();
            }
        }
    }

    private void moveDownAction() throws JSONException, IOException {
        if(ProjectVar.isOnlineGame){
            if(ProjectVar.isServer){
                if (tankIsReady()) {
                    playerComponent.down();
                    output(1,false,true,false,false,false);
                }
            }else{
                if (tank2IsReady()) {
                    playerComponent2.down();
                    output(2,false,true,false,false,false);
                }
            }
        }else {
            if (tankIsReady()) {
                playerComponent.down();
            }
        }
    }

    private void moveUpAction() throws JSONException, IOException {
        if(ProjectVar.isOnlineGame){
            if(ProjectVar.isServer){
                if (tankIsReady()) {
                    playerComponent.up();
                    output(1,true,false,false,false,false);
                }
            }else{
                if (tank2IsReady()) {
                    playerComponent2.up();
                    output(2,true,false,false,false,false);
                }
            }
        }else {
            if (tankIsReady()) {
                playerComponent.up();
            }
        }
    }

    private void shootAction2() {
        if (tank2IsReady()) {
            playerComponent2.shoot();
        }
    }

    private void moveRightAction2() {
        if (tank2IsReady()) {
            playerComponent2.right();
        }
    }

    private void moveLeftAction2() {
        if (tank2IsReady()) {
            playerComponent2.left();
        }
    }

    private void moveDownAction2() {
        if (tank2IsReady()) {
            playerComponent2.down();
        }
    }

    private void moveUpAction2() {
        if (tank2IsReady()) {
            playerComponent2.up();
        }
    }

    public void startNetwork() throws IOException, JSONException, InterruptedException {
        System.out.println("开启网络");
        if(ProjectVar.isServer){
            while (true){
                System.out.println("成功连接");
                reader=new BufferedReader(new InputStreamReader(ProjectVar.tcpServer.socket.getInputStream()));
                System.out.println("成功加载");
                jsonStr=reader.readLine();
                System.out.println("成功读取");
                JSONObject jsonObject=new JSONObject(jsonStr);



                //对接收到的信息进行处理
                int playerNumber=jsonObject.getInt("playerNumber");
                boolean isUp=jsonObject.getBoolean("isUp");
                boolean isDown=jsonObject.getBoolean("isDown");
                boolean isLeft=jsonObject.getBoolean("isLeft");
                boolean isRight=jsonObject.getBoolean("isRight");
                boolean isShoot=jsonObject.getBoolean("isShoot");
                System.out.println(playerNumber);
                if(isShoot==true){
                    System.out.println("得到玩家2射击信号");
                    Platform.runLater(() -> {
                        // 在这里执行与UI相关的操作
                        playerComponent2.shoot();
                    });

                }
                if(isUp==true){
                    playerComponent2.up();
                } else if (isDown==true) {
                    playerComponent2.down();
                }else if (isLeft==true) {
                    playerComponent2.left();
                }else if (isRight==true) {
                    playerComponent2.right();
                }
                Thread.sleep(5); // 休眠5毫秒，然后继续检查新消息

            }
        }else {
            while (true){
                System.out.println("成功连接");
                reader=new BufferedReader(new InputStreamReader(ProjectVar.client.getInputStream()));
                System.out.println("成功加载");
                jsonStr=reader.readLine();
                System.out.println("成功读取");
                JSONObject jsonObject=new JSONObject(jsonStr);



                //对接收到的信息进行处理
                int playerNumber=jsonObject.getInt("playerNumber");
                boolean isUp=jsonObject.getBoolean("isUp");
                boolean isDown=jsonObject.getBoolean("isDown");
                boolean isLeft=jsonObject.getBoolean("isLeft");
                boolean isRight=jsonObject.getBoolean("isRight");
                boolean isShoot=jsonObject.getBoolean("isShoot");
                System.out.println(playerNumber);
                if(isShoot==true){
                    System.out.println("得到玩家1射击信号");
                    Platform.runLater(() -> {
                        // 在这里执行与UI相关的操作
                        playerComponent.shoot();
                    });


                }
                if(isUp==true){
                    playerComponent.up();
                } else if (isDown==true) {
                    playerComponent.down();
                }else if (isLeft==true) {
                    playerComponent.left();
                }else if (isRight==true) {
                    playerComponent.right();
                }
                Thread.sleep(5); // 休眠5毫秒，然后继续检查新消息
            }
        }

    }

    @Override
    protected void initGame() {
        getGameScene().setBackgroundColor(Color.BLACK);
        getGameWorld().addEntityFactory(new GameEntityFactory());

        buildAndStartLevel();
        getip("destroyedEnemy").addListener((ob, ov, nv) -> {
            if (nv.intValue() == GameConfig.ENEMY_AMOUNT) {
                set("gameOver", true);
                play("Win.wav");
                runOnce(
                        () -> getSceneService().pushSubScene(successSceneLazyValue.get()),
                        Duration.seconds(1.5));
            }
        });

        // 使用线程池并行执行线程
        threadPool = Executors.newFixedThreadPool(2); // 创建一个包含两个线程的线程池

        // 启动线程并提交到线程池
        threadPool.submit(() -> {
            startLevel();
            System.out.println("成功启动关卡数据");
        });

        threadPool.submit(() -> {
            System.out.println("11111111");
            if (ProjectVar.isOnlineGame) {
                System.out.println("22222");
                if (ProjectVar.isServer) {
                    try {

                        //ProjectVar.tcpServer = new TCPServer(Integer.parseInt(ProjectVar.selectedRoomPort));
                        ProjectVar.tcpServer = new TCPServer(9090);
                        ProjectVar.tcpServer.start();
                        System.out.println("server启动");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        System.out.println(ProjectVar.selectedRoomIp);
                        System.out.println(ProjectVar.selectedRoomPort);
                        ProjectVar.client = new Socket(ProjectVar.selectedRoomIp, 9090);
                        System.out.println("client启动");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            System.out.println("成功启动联网模块");
        });

        // 关闭线程池
        threadPool.shutdown();

        // 等待线程池中的任务完成
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void buildAndStartLevel() {
        //1. 清理上一个关卡的残留(这里主要是清理声音残留)
        //清理关卡的残留(这里主要是清理声音残留)
        getGameWorld().getEntitiesByType(
                GameType.BULLET, GameType.ENEMY, GameType.PLAYER
        ).forEach(Entity::removeFromWorld);

        //2. 开场动画
        Rectangle rect1 = new Rectangle(getAppWidth(), getAppHeight() / 2.0, Color.web("#333333"));
        Rectangle rect2 = new Rectangle(getAppWidth(), getAppHeight() / 2.0, Color.web("#333333"));
        rect2.setLayoutY(getAppHeight() / 2.0);
        Text text = new Text("STAGE " + geti("level"));
        text.setFill(Color.WHITE);
        text.setFont(new Font(35));
        text.setLayoutX(getAppWidth() / 2.0 - 80);
        text.setLayoutY(getAppHeight() / 2.0 - 5);
        Pane p1 = new Pane(rect1, rect2, text);

        addUINode(p1);

        Timeline tl = new Timeline(
                new KeyFrame(Duration.seconds(1.2),
                        new KeyValue(rect1.translateYProperty(), -getAppHeight() / 2.0),
                        new KeyValue(rect2.translateYProperty(), getAppHeight() / 2.0)
                ));
        tl.setOnFinished(e -> removeUINode(p1));

        PauseTransition pt = new PauseTransition(Duration.seconds(1.5));
        pt.setOnFinished(e -> {
            text.setVisible(false);
            tl.play();
            //3. 开始新关卡
            if(ProjectVar.isOnlineGame==false){
                startLevel();
            }else {
                startTwoLevel();
            }


        });
        pt.play();
    }

    private void startLevel() {
        if (spawnEnemyTimerAction != null) {
            spawnEnemyTimerAction.expire();
            spawnEnemyTimerAction = null;
        }
        set("gameOver", false);
        //清除上一关残留的道具影响
        set("freezingEnemy", false);
        //恢复消灭敌军数量
        set("destroyedEnemy", 0);
        //恢复生成敌军的数量
        set("spawnedEnemy", 0);

        expireAction(freezingTimerAction);
        expireAction(spadeTimerAction);
        //如果关卡是 0 ,那么从用户自定义地图开始游戏
       if (geti("level")==0){
           Level level;
           try {
               level = new TMXLevelLoader()
                       .load(new File(GameConfig.CUSTOM_LEVEL_PATH).toURI().toURL(), getGameWorld());
               getGameWorld().setLevel(level);
           } catch (MalformedURLException e) {
               throw new RuntimeException(e);
           }
       }else {
           setLevelFromMap("level" + geti("level") + ".tmx");
       }
        play("start.wav");
        player = null;
        player = spawn("player", 9 * 24 + 3, 25 * 24);
        //每局开始玩家坦克都有无敌保护时间
        player.getComponent(EffectComponent.class).startEffect(new HelmetEffect());
        playerComponent = player.getComponent(PlayerComponent.class);

        if(ProjectVar.playerAmount==2) {
            System.out.println("two game start");
            player2 = spawn("player", 17 * 24 + 3, 25 * 24);
            player2.getComponent(EffectComponent.class).startEffect(new HelmetEffect());
            playerComponent2 = player2.getComponent(PlayerComponent.class);
        }else{
            System.out.println("single game start");
        }


        //显示信息的UI
        getGameScene().addGameView(new GameView(new InfoPane(), 100));
        //首先产生几个敌方坦克
        for (int i = 0; i < enemySpawnX.length; i++) {
            spawn("enemy",
                    new SpawnData(enemySpawnX[i], 30).put("assentName", "tank/E" + FXGLMath.random(1, 12) + "U.png"));
            inc("spawnedEnemy", 1);
        }
        spawnEnemy();
    }

    private void startTwoLevel(){
        if (spawnEnemyTimerAction != null) {
            spawnEnemyTimerAction.expire();
            spawnEnemyTimerAction = null;
        }
        set("gameOver", false);
        //清除上一关残留的道具影响
        set("freezingEnemy", false);
        //恢复消灭敌军数量
        set("destroyedEnemy", 0);
        //恢复生成敌军的数量
        set("spawnedEnemy", 0);

        expireAction(freezingTimerAction);
        expireAction(spadeTimerAction);

        setLevelFromMap("levelForTwo.tmx");
        play("start.wav");

        player = null;
        player = spawn("player", 9 * 24 + 3, 25 * 24);
        //每局开始玩家坦克都有无敌保护时间
        player.getComponent(EffectComponent.class).startEffect(new HelmetEffect());
        playerComponent = player.getComponent(PlayerComponent.class);

        player2 = null;
        player2 = spawn("player", 9 * 24 + 3, 8 * 24);
        //每局开始玩家坦克都有无敌保护时间
        player2.getComponent(EffectComponent.class).startEffect(new HelmetEffect());
        playerComponent2 = player2.getComponent(PlayerComponent.class);

        //显示信息的UI
        getGameScene().addGameView(new GameView(new InfoPane(), 100));

    }

    private void spawnEnemy() {
        if (spawnEnemyTimerAction != null) {
            spawnEnemyTimerAction.expire();
            spawnEnemyTimerAction = null;
        }
        //用于检测碰撞的实体(避免坦克一产生就和其他坦克发生碰撞,"纠缠到一起")
        Entity spawnBox = spawn("spawnBox", new SpawnData(-100, -100));

        //用于产生敌人的定时器, 定期尝试产生敌军坦克, 但是如果生成敌军坦克的位置,被其他现有的坦克占据, 那么此次就不生成敌军坦克
        spawnEnemyTimerAction = run(() -> {
            //尝试产生敌军坦克的次数; 2次或者3次
            int testTimes = FXGLMath.random(2, 3);
            for (int i = 0; i < testTimes; i++) {
                if (geti("spawnedEnemy") < GameConfig.ENEMY_AMOUNT) {
                    boolean canGenerate = true;
                    //随机抽取数组的一个x坐标
                    int x = enemySpawnX[random.nextInt(3)];
                    int y = 30;
                    spawnBox.setPosition(x, y);
                    List<Entity> tankList = getGameWorld().getEntitiesByType(GameType.ENEMY, GameType.PLAYER);
                    //如果即将产生的敌军坦克位置和 目前已有的坦克位置冲突, 那么此处就不产生坦克
                    for (Entity tank : tankList) {
                        if (tank.isActive() && spawnBox.isColliding(tank)) {
                            canGenerate = false;
                            break;
                        }
                    }
                    //如果可以产生敌军坦克,那么生成坦克
                    if (canGenerate) {
                        inc("spawnedEnemy", 1);
                        spawn("enemy",
                                new SpawnData(x, y).put("assentName", "tank/E" + FXGLMath.random(1, 12) + "U.png"));
                    }
                    //隐藏这个实体
                    spawnBox.setPosition(-100, -100);

                } else {
                    if (spawnEnemyTimerAction != null) {
                        spawnEnemyTimerAction.expire();
                    }
                }
            }
        }, GameConfig.SPAWN_ENEMY_TIME);
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().addCollisionHandler(new BulletEnemyHandler());
        getPhysicsWorld().addCollisionHandler(new BulletPlayerHandler());
        BulletBrickHandler bulletBrickHandler = new BulletBrickHandler();
        getPhysicsWorld().addCollisionHandler(bulletBrickHandler);
        getPhysicsWorld().addCollisionHandler(bulletBrickHandler.copyFor(GameType.BULLET, GameType.STONE));
        getPhysicsWorld().addCollisionHandler(bulletBrickHandler.copyFor(GameType.BULLET, GameType.GREENS));
        getPhysicsWorld().addCollisionHandler(new BulletFlagHandler());
        getPhysicsWorld().addCollisionHandler(new BulletBorderHandler());
        getPhysicsWorld().addCollisionHandler(new BulletBulletHandler());
        getPhysicsWorld().addCollisionHandler(new PlayerItemHandler());
    }

    public void freezingEnemy() {
        expireAction(freezingTimerAction);
        set("freezingEnemy", true);
        freezingTimerAction = runOnce(() -> {
            set("freezingEnemy", false);
        }, GameConfig.STOP_MOVE_TIME);
    }

    public void spadeBackUpBase() {
        expireAction(spadeTimerAction);
        //升级基地周围为石头墙
        updateWall(true);
        spadeTimerAction = runOnce(() -> {
            //基地周围的墙,还原成砖头墙
            updateWall(false);
        }, GameConfig.SPADE_TIME);
    }
    /**
     * 基地四周的防御
     * 按照游戏规则: 默认是砖头墙, 吃了铁锨后,升级成为石头墙;
     */
    private void updateWall(boolean isStone) {
        //循环找到包围基地周围的墙
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                if (row != 0 && (col == 1 || col == 2)) {
                    continue;
                }
                //删除旧的墙
                List<Entity> entityTempList = getGameWorld().getEntitiesAt(new Point2D(288 + col * 24, 576 + row * 24));
                for (Entity entityTemp : entityTempList) {
                    Serializable type = entityTemp.getType();
                    //如果是玩家自建的地图, 那么需要判断是不是水面草地雪地等
                    if (type == GameType.STONE || type == GameType.BRICK || type == GameType.SNOW || type == GameType.SEA || type == GameType.GREENS) {
                        if (entityTemp.isActive()) {
                            entityTemp.removeFromWorld();
                        }
                    }
                }
                //创建新的墙
                if (isStone) {
                    spawn("itemStone", new SpawnData(288 + col * 24, 576 + row * 24));
                } else {
                    spawn("brick", new SpawnData(288 + col * 24, 576 + row * 24));
                }
            }
        }
    }

    /**
     * 让TimeAction过期
     */
    public void expireAction(TimerAction action) {
        if (action == null) {
            return;
        }
        if (!action.isExpired()) {
            action.expire();
        }
    }

    public void output(int playerNumber, boolean isUp, boolean isDown, boolean isLeft,
                       boolean isRight, boolean isShoot)
            throws JSONException, IOException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("playerNumber", playerNumber);
        jsonObject.put("isUp", isUp);
        jsonObject.put("isDown", isDown);
        jsonObject.put("isLeft", isLeft);
        jsonObject.put("isRight", isRight);
        jsonObject.put("isShoot", isShoot);

        String jsonStr = jsonObject.toString() + "\n"; // 添加换行符

        if(ProjectVar.isServer){
            OutputStream outputStream = ProjectVar.tcpServer.socket.getOutputStream();
            outputStream.write(jsonStr.getBytes(StandardCharsets.UTF_8));
        }else{
            OutputStream outputStream = ProjectVar.client.getOutputStream();
            outputStream.write(jsonStr.getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("成功输出");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
