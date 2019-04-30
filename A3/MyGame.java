package A3;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import Network.Client.GhostAvatar;
import Network.Client.ProtocolClient;
import myGameEngine.Controllers.BounceController;
import myGameEngine.Controllers.StretchController;
import myGameEngine.GamepadCommands.*;
import myGameEngine.KeyboardCommands.*;
import myGameEngine.Controllers.Camera3PController;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import ray.input.GenericInputManager;
import ray.input.action.AbstractInputAction;
import ray.rage.*;
import ray.rage.asset.texture.*;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.rendersystem.states.FrontFaceState;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.rendersystem.states.ZBufferState;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.*;
import ray.rage.scene.controllers.*;
import ray.rage.util.BufferUtil;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.input.*;
import ray.networking.IGameConnection.ProtocolType;
import java.util.List;

//Script imports
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import ray.rage.util.*;
import java.awt.geom.*;


public class MyGame extends VariableFrameRateGame {
    // to minimize variable allocation in update()
    private static final String SKYBOX_NAME = "SkyBox";
    private boolean skyBoxVisible = true;
    private String serverAddress;
    private int serverPort;
    private ProtocolType serverProtocol;
    private ProtocolClient protClient;
    private boolean isClientConnected;
    private Vector<UUID> gameObjectsToRemove;
    private int test;

    GL4RenderSystem rs;
    float elapsTime = 0.0f;
    String elapsTimeStr, planetsVisitedString, dispStr, collectedArtifactsString;
    int elapsTimeSec, planetsVisited = 0, collectedArtifacts = 0;
    final int MAXPLANETS = 5;

    private InputManager im;
    private Camera3PController orbitController, orbitController2;
    private Vector<GhostAvatar> ghostList = new Vector<GhostAvatar>();
    boolean ghostListEmpty = true;
    static protected ScriptEngine jsEngine;
    String[] textureNames = {"blue.jpeg", "hexagons.jpeg", "red.jpeg", "moon.jpeg", "chain-fence.jpeg"};
    SceneNode [] planetN, planetsVisitedN;
    SceneNode planetGroupN, playerGroupN, StretchGroupN,BounceGroupN;
    Entity [] planets;
    Entity alienArtifactsE;


    public MyGame(String serverAddr, int sPort) {
        super();
        planetsVisitedN = new SceneNode[MAXPLANETS];
        this.serverAddress = serverAddr;
        this.serverPort = sPort;
        this.serverProtocol = ProtocolType.UDP;
        System.out.println("Left joystick on gamepad controls movement");
        System.out.println("Right joystick controls camera controls");
        System.out.println("Triggers control roll");
        System.out.println("Y button controls camera toggle");
        System.out.println("Start button ends game");

    }

    public static void main(String[] args) {
        Game game = new MyGame("10.117.119.67", Integer.parseInt("59000"));
        ScriptEngineManager factory = new ScriptEngineManager();
        String scriptFileName = "src/Scripts/InitPlanetParams.js";
        List<ScriptEngineFactory> list = factory.getEngineFactories();
        System.out.println("Script Engines found: ");
        for(ScriptEngineFactory f: list){
            System.out.println(" Name = " + f.getEngineName()
                    + " language = " + f.getLanguageName()
                    + " extensions = " + f.getExtensions());
        }
        //get JS engine
        jsEngine = factory.getEngineByName("js");
        //run script
        ((MyGame) game).executeScript(jsEngine, scriptFileName);

        try {
            game.startup();
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            game.shutdown();
            game.exit();
        }
    }

    @Override
    protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
        rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
    }


    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
        //Camera Nodes are set up and attached inside of setupScene
        SceneNode rootNode = sm.getRootSceneNode();
        Camera camera = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
        rw.getViewport(0).setCamera(camera);
        SceneNode cameraN =
                rootNode.createChildSceneNode("MainCameraNode");
        cameraN.attachObject(camera);
        camera.setMode('n');
        camera.getFrustum().setFarClipDistance(1000.0f);
    }

    // now we add setting up viewports in the window
//    protected void setupWindowViewports(RenderWindow rw) {
//        rw.addKeyListener(this);
//        Viewport topViewport = rw.getViewport(0);
//        topViewport.setDimensions(.51f, .01f, .99f, .49f); // B,L,W,H
//        topViewport.setClearColor(new Color(1.0f, .7f, .7f));
//        Viewport botViewport = rw.createViewport(.01f, .01f, .99f, .49f);
//        botViewport.setClearColor(new Color(.5f, 1.0f, .5f));
//    }

    protected void checkDistancePlanetToPlayer(Engine engine){
        float dolphinDistX, dolphinDistY, dolphinDistZ;
        float cubeDistX, cubeDistY, cubeDistZ;
        float maxDistance = 4;
        boolean flag = false;
        SceneManager sm = engine.getSceneManager();
        SceneNode dolphinN = sm.getSceneNode("myDolphinNode");
        SceneNode cubeN = sm.getSceneNode("myCubeNode");

    }


    public boolean checkDistanceDolphinCamera(){
        float DistX, DistY, DistZ;
        DistX = DistY = DistZ = 0;
        float maxDistance = 8; // Farthest distance you can get away from the dolphin before it spawns you back on top
        Camera mainCamN = getEngine().getSceneManager().getCamera("MainCamera");
        SceneNode dolphinN = getEngine().getSceneManager().getSceneNode("myDolphinNode");


        if(mainCamN.getMode() == 'c'){
            DistX = Math.abs(mainCamN.getPo().x() - dolphinN.getLocalPosition().x());
            DistY = Math.abs(mainCamN.getPo().y() - dolphinN.getLocalPosition().y());
            DistZ = Math.abs(mainCamN.getPo().z() - dolphinN.getLocalPosition().z());

            if(DistX > maxDistance || DistY > maxDistance || DistZ > maxDistance)
                return false;
        }
        return true;
    }



    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException {
        TextureManager tm = eng.getTextureManager();
        RenderSystem rs = sm.getRenderSystem();
        Texture texture = tm.getAssetByPath("red.jpeg");
        TextureState state = (TextureState)rs.createRenderState(RenderState.Type.TEXTURE);
        Angle rotAmt = Degreef.createFrom(180.0f);
        ManualObject manObjGroundPlane;
        ZBufferState zstate = (ZBufferState) rs.createRenderState(RenderState.Type.ZBUFFER);
        zstate.setTestEnabled(true);

        ScriptEngineManager factory = new ScriptEngineManager();
        java.util.List<ScriptEngineFactory> list = factory.getEngineFactories();

        jsEngine = factory.getEngineByName("js");
        // use spin speed setting from the first script to initialize dolphin rotation
        File scriptFile1 = new File("src/Scripts/InitPlanetParams.js");
        this.runScript(scriptFile1);

        // set up sky box
        Configuration conf = eng.getConfiguration();
        tm.setBaseDirectoryPath(conf.valueOf("assets.skyboxes.path"));
        Texture front = tm.getAssetByPath("front.png");
        Texture back = tm.getAssetByPath("back.png");
        Texture left = tm.getAssetByPath("left.png");
        Texture right = tm.getAssetByPath("right.png");
        Texture top = tm.getAssetByPath("top.png");
        Texture bottom = tm.getAssetByPath("bottom.png");
        tm.setBaseDirectoryPath(conf.valueOf("assets.textures.path"));
// cubemap textures are flipped upside-down.
// All textures must have the same dimensions, so any image’s
// heights will work since they are all the same height
        AffineTransform xform = new AffineTransform();
        xform.translate(0, front.getImage().getHeight());
        xform.scale(1d, -1d);
        front.transform(xform);
        back.transform(xform);
        left.transform(xform);
        right.transform(xform);
        top.transform(xform);
        bottom.transform(xform);
        SkyBox sb = sm.createSkyBox(SKYBOX_NAME);
        sb.setTexture(front, SkyBox.Face.FRONT);
        sb.setTexture(back, SkyBox.Face.BACK);
        sb.setTexture(left, SkyBox.Face.LEFT);
        sb.setTexture(right, SkyBox.Face.RIGHT);
        sb.setTexture(top, SkyBox.Face.TOP);
        sb.setTexture(bottom, SkyBox.Face.BOTTOM);
        sm.setActiveSkyBox(sb);
//End skybox

//        manObjGroundPlane = makePlane(eng, sm);
//        manObjGroundPlane.setPrimitive(Primitive.TRIANGLES);
//        SceneNode groundPlaneN = sm.getRootSceneNode().createChildSceneNode("groundPlaneN");
//        groundPlaneN.attachObject(manObjGroundPlane);
//        groundPlaneN.scale((float)20.0, (float)1.0, (float)20.0);

        playerGroupN = sm.getRootSceneNode().createChildSceneNode("playerGroupNode");

        //Cube code
        Entity cubeE = sm.createEntity("myCube", "cube.obj");
        cubeE.setPrimitive(Primitive.TRIANGLES);

        SceneNode cubeN = playerGroupN.createChildSceneNode("myCubeNode");
        cubeN.moveBackward(5.0f);
        cubeN.moveUp(0.1f);
        cubeN.attachObject(cubeE);
        cubeN.scale(.3f,.3f,.3f);

        SceneNode CubeNode =  cubeN.createChildSceneNode("CubeCamNode");
//        CubeNode.attachObject(camera2);
        //CubeNode.setLocalPosition(Vector3f.createFrom(0.0f, 0.5f, -0.5f));

        //Blender Tree =============================================================
        Entity treeOne = sm.createEntity("Tree1","Tree1.obj");
        Texture tex = sm.getTextureManager().getAssetByPath("treeTexture.png");
        TextureState tstate = (TextureState) sm.getRenderSystem()
                .createRenderState(RenderState.Type.TEXTURE);
        tstate.setTexture(tex);
        treeOne.setRenderState(tstate);
        treeOne.setRenderState(zstate);

        SceneNode manN =
                sm.getRootSceneNode().createChildSceneNode("treeNode");
        manN.attachObject(treeOne);
        manN.scale(0.5f, 0.5f, 0.5f);
        manN.translate(0f, 0.0f, .5f);

        //=====Rock Border===========================================================
        Entity borderOne = sm.createEntity("Border","Border.obj");
        Texture borderTex = sm.getTextureManager().getAssetByPath("Border.png");
        TextureState Bstate = (TextureState) sm.getRenderSystem()
                .createRenderState(RenderState.Type.TEXTURE);
        Bstate.setTexture(borderTex);
        borderOne.setRenderState(Bstate);

        SceneNode borderNode =
                sm.getRootSceneNode().createChildSceneNode("borderNode");
        borderNode.attachObject(borderOne);
        borderNode.scale(2.f, 2.0f, 2.0f);
        borderNode.translate(15f, 0.0f, 10f);
        borderNode.yaw(Degreef.createFrom(90));

        //===========Cystral Rocks============================================================

        Entity rock = sm.createEntity("rock1","Rock2.obj");
        Texture rocktex = sm.getTextureManager().getAssetByPath("rock.jpg");
        TextureState Rstate = (TextureState) sm.getRenderSystem()
                .createRenderState(RenderState.Type.TEXTURE);
        Rstate.setTexture(rocktex);
        rock.setRenderState(Rstate);

        SceneNode rockNode =
                sm.getRootSceneNode().createChildSceneNode("rockNode");
        rockNode.attachObject(rock);

        //========= Rock Float =========================================================
        Entity floatIsland = sm.createEntity("floatIsland","FloatIsland3.obj");
        Texture floatText = sm.getTextureManager().getAssetByPath("FloatIsland.png");
        TextureState floatState = (TextureState) sm.getRenderSystem()
                .createRenderState(RenderState.Type.TEXTURE);
        floatState.setTexture(floatText);
        floatIsland.setRenderState(floatState);

        SceneNode floatIslandNode =
                sm.getRootSceneNode().createChildSceneNode("floatIslandNode");
        floatIslandNode.attachObject(floatIsland);
        floatIslandNode.setLocalScale(7,7,7);
        floatIslandNode.setLocalPosition(0,-2.5f,0);
        //========= Rock Float End =========================================================


        //Light code
        sm.getAmbientLight().setIntensity(new Color(.0f, .0f, .0f));

        //===========Light Sorce Modle================================

        Light plight2 = sm.createLight("testLamp2", Light.Type.DIRECTIONAL);
        plight2.setAmbient(new Color(.2f, .2f, .2f));
        plight2.setDiffuse(new Color(1.0f, 1.0f, 1.0f));
        plight2.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight2.setRange(100f);

        SceneNode plightNode2 = sm.getRootSceneNode().createChildSceneNode("plightNode2");
        plightNode2.attachObject(plight2);
        plightNode2.setLocalPosition(10,10,-10);


        Entity testLight = sm.createEntity("lightCube", "cube.obj");
        testLight.setPrimitive(Primitive.TRIANGLES);
        plightNode2.scale(.3f,.3f,.3f);

        plightNode2.attachObject(testLight);


        //Rotation code
        RotationController rc = new RotationController(Vector3f.createUnitVectorY(), ((Double)(jsEngine.get("spinSpeed"))).floatValue());

        //Stretch controller
        StretchController sc = new StretchController();

        //Bounce Controller
        BounceController bc = new BounceController();

        //================ Terrian ==================================
        Tessellation tessE = sm.createTessellation("tessE", 5);
        tessE.setSubdivisions(16f);
        SceneNode tessN =
                sm.getRootSceneNode().
                        createChildSceneNode("TessN");
        tessN.attachObject(tessE);
        tessN.scale(70, 100, 70);
        tessE.setHeightMap(this.getEngine(), "floor3.png");
        tessE.setTexture(this.getEngine(), "grass.jpg");

        Tessellation tessWaterE = sm.createTessellation("tessWaterE", 5);
        tessWaterE.setSubdivisions(16f);
        SceneNode tessWaterN =
                sm.getRootSceneNode().
                        createChildSceneNode("TessWaterN");
        tessWaterN.attachObject(tessWaterE);
        tessWaterN.scale(70, 100, 70);
        tessWaterE.setHeightMap(this.getEngine(), "FloorFlat.png");
        tessWaterE.setTexture(this.getEngine(), "blue.jpeg");
        //=============== Terrian End ================================

        sm.addController(rc);
        sm.addController(sc);
        sm.addController(bc);


        setupInputs();
        //orbitController
        setupOrbitCamera(eng, sm);
        //dolphinN.yaw(Degreef.createFrom(45.0f));
        cubeN.yaw(Degreef.createFrom(45.0f));

        setupNetworking();


    }

    public void updateVerticalPosition(){
        SceneNode avatarN = this.getEngine().getSceneManager().
                getSceneNode("myCubeNode");
        SceneNode tessN =
                this.getEngine().getSceneManager().
                        getSceneNode("TessN");
        Tessellation tessE = ((Tessellation) tessN.getAttachedObject("tessE"));

        Vector3 worldAvatarPosition = avatarN.getWorldPosition();
        Vector3 localAvatarPosition = avatarN.getLocalPosition();

        Vector3 newAvatarPosition = Vector3f.createFrom(
                // Keep the X coordinate
                localAvatarPosition.x(),
                // The Y coordinate is the varying height
                tessE.getWorldHeight(
                        worldAvatarPosition.x() + .3f,
                        worldAvatarPosition.z()) + .3f,
                //Keep the Z coordinate
                localAvatarPosition.z()
        );

        avatarN.setLocalPosition(newAvatarPosition);
    }

    //============= Networking ==========================================

    private void setupNetworking()
    { gameObjectsToRemove = new Vector<UUID>();
        isClientConnected = false;
        try
        { protClient = new ProtocolClient(InetAddress.
                getByName(serverAddress), serverPort, serverProtocol, this);
        } catch (IOException e) { e.printStackTrace();
        }
        if (protClient == null)
        { System.out.println("missing protocol host"); }
        else
        { // ask client protocol to send initial join message
            //to server, with a unique identifier for this client
            protClient.sendJoinMessage();
        } }

    protected void processNetworking(float elapsTime)
    { // Process packets received by the client from the server
        if (protClient != null)
            protClient.processPackets();
        // remove ghost avatars for players who have left the game
        Iterator<UUID> it = gameObjectsToRemove.iterator();
        while(it.hasNext())
        { getEngine().getSceneManager().destroySceneNode(it.next().toString());
        }
        gameObjectsToRemove.clear();
    }

    public Vector3 getPlayerPosition()
    { SceneNode dolphinN = getEngine().getSceneManager().getSceneNode("myCubeNode");
        return dolphinN.getWorldPosition();
    }

    public void addGhostAvatarToGameWorld(GhostAvatar avatar)
            throws IOException
    { if (avatar != null) {
        ghostList.add(avatar);
        ghostListEmpty = false;
        Entity ghostE = getEngine().getSceneManager().createEntity("ghost", "dolphinHighPoly.obj");
        ghostE.setPrimitive(Primitive.TRIANGLES);
        SceneNode ghostN = getEngine().getSceneManager().getRootSceneNode().
                createChildSceneNode(avatar.getId().toString());
        System.out.println(avatar.getId());
        ghostN.attachObject(ghostE);
        ghostN.setLocalPosition(avatar.getPos().x(), avatar.getPos().y() ,avatar.getPos().z());
        avatar.setNode(ghostN);
        avatar.setEntity(ghostE);
        //avatar.setPosition(0,0,0);

    } }

    public void removeGhostAvatarFromGameWorld(GhostAvatar avatar)
    { if(avatar != null) gameObjectsToRemove.add(avatar.getId());
    }

    public void setIsConnected(boolean b) {
        isClientConnected = b;
    }

    public ProtocolClient getProtClient() {
        return protClient;
    }

    public boolean getisClientConnected() {
        return isClientConnected;
    }

    public void updateGhostPosition(){

        if(!ghostListEmpty){
           Iterator<GhostAvatar> iterate = ghostList.iterator();
           while (iterate.hasNext()){
               GhostAvatar temp = iterate.next();
               temp.getNode().setLocalPosition(temp.getPos());
           }
        }
    }

    //============ END Networking =====================================


    protected void setupOrbitCamera(Engine eng, SceneManager sm) {
        String gpName;
        SceneNode cubeN = sm.getSceneNode("myCubeNode");
        SceneNode cameraN = sm.getSceneNode("MainCameraNode");
        Camera camera = sm.getCamera("MainCamera");
        if(im.getFirstGamepadName() == null){
            gpName = im.getMouseName();
        }else{
            gpName = im.getFirstGamepadName();
        }

        orbitController = new Camera3PController(camera, cameraN, cubeN, gpName, im);

    }


    @Override
    protected void update(Engine engine) {
        // build and set HUD
        rs = (GL4RenderSystem) engine.getRenderSystem();
//        checkDistancePlanetToPlayer(engine);
        elapsTime += engine.getElapsedTimeMillis();
        elapsTimeSec = Math.round(elapsTime/1000.0f);
        elapsTimeStr = Integer.toString(elapsTimeSec);
        planetsVisitedString = Integer.toString(planetsVisited);
        collectedArtifactsString= Integer.toString(collectedArtifacts);
        dispStr = "Cube position " + elapsTimeStr;
        rs.setHUD(dispStr, 15, 15);
        dispStr = "Dolphin Time = " + elapsTimeStr;
        rs.setHUD2(dispStr, 15, (rs.getRenderWindow().getViewport(0).getActualHeight() + 20));
        im.update(elapsTime);
        orbitController.updateCameraPosition();
        updateGhostPosition();
        processNetworking(elapsTime);
        updateVerticalPosition();
    }

    protected void setupInputs(){
        im = new GenericInputManager();
        //Creating action objects
        MoveForwardBackwardAction moveForwardBackwardCmd = new MoveForwardBackwardAction(this, protClient);
        MoveLeftRightAction moveLeftRightCmd = new MoveLeftRightAction(this,protClient);
        QuitGameAction quitGameCmd = new QuitGameAction(this);
        CameraYawAction CameraYawCmd = new CameraYawAction(this);
        CameraPitchAction CameraPitchCmd = new CameraPitchAction(this);
        CameraTiltAction CameraTiltCmd = new CameraTiltAction(this);


        ArrayList controllers = im.getControllers();
        for(int i = 0; i < controllers.size(); i++){
            Controller c = (Controller)controllers.get(i);

            if (c.getType() == Controller.Type.KEYBOARD) {
                //Quit game action using q key on keyboard
                im.associateAction(c, Component.Identifier.Key.Q, quitGameCmd, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);


                //move forward action using key W on keyboard
                im.associateAction(c, Component.Identifier.Key.W, new KBMoveForwardAction(this), InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                //move forward action using key S on keyboard
                im.associateAction(c, Component.Identifier.Key.S, new KBMoveBackwardAction(this), InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                //move forward action using key D on keyboard
                im.associateAction(c, Component.Identifier.Key.D, new KBMoveRightAction(this), InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                //move forward action using key A on keyboard
                im.associateAction(c, Component.Identifier.Key.A, new KBMoveLeftAction(this), InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                //yaw camera using left arrow  key
                im.associateAction(c, Component.Identifier.Key.LEFT, new KBCameraYawLeftAction(this), InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                //yaw camera using right arrow  key
                im.associateAction(c, Component.Identifier.Key.RIGHT, new KBCameraYawAction(this), InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                //Pitch camera down using down arrow key
                im.associateAction(c, Component.Identifier.Key.DOWN, new KBCameraPitchDownAction(this), InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                //Pitch camera down using up arrow key
                im.associateAction(c, Component.Identifier.Key.UP, new KBCameraPitchUpAction(this), InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            }
            else if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
                //Code for gamepad usage

                //move forward and backward using y axis joystick on gamepad(Xbox controller)
                im.associateAction(c, Component.Identifier.Axis.Y, moveForwardBackwardCmd, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                //move left and right using x axis joystick on gamepad(Xbox controller)
                im.associateAction(c, Component.Identifier.Axis.X, moveLeftRightCmd, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

//                //yaw camera using right joystick on gamepad(Xbox controller)
//                im.associateAction(c, Component.Identifier.Axis.RX, CameraYawCmd, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
//
//                //pitch camera using right joystick on gamepad(Xbox controller)
//                im.associateAction(c, Component.Identifier.Axis.RY, CameraPitchCmd, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
//
//                //Tilt camera using left and right triggers on gamepad(Xbox controller)
//                im.associateAction(c, Component.Identifier.Axis.Z, CameraTiltCmd, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                //Quit game action using button 7(Start button) on gamepad
                im.associateAction(c, net.java.games.input.Component.Identifier.Button._7, quitGameCmd, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
            }
        }
    }


    private void executeScript(ScriptEngine engine, String scriptFileName)
    {
        try {
            FileReader fileReader = new FileReader(scriptFileName);
            engine.eval(fileReader); //execute the script statements in the file
            fileReader.close();
        }
        catch (FileNotFoundException e1) {
            System.out.println(scriptFileName + " not found " + e1);
        }
        catch (IOException e2) {
            System.out.println("IO problem with " + scriptFileName + e2);
        }
        catch (ScriptException e3) {
            System.out.println("ScriptException in " + scriptFileName + e3);
        }
        catch (NullPointerException e4) {
            System.out.println ("Null ptr exception in " + scriptFileName + e4);
        }
    }

    private void runScript(File scriptFile)
    { try
    { FileReader fileReader = new FileReader(scriptFile);
        jsEngine.eval(fileReader);
        fileReader.close();
    }
    catch (FileNotFoundException e1)
    { System.out.println(scriptFile + " not found " + e1); }
    catch (IOException e2)
    { System.out.println("IO problem with " + scriptFile + e2); }
    catch (ScriptException e3)
    { System.out.println("Script Exception in " + scriptFile + e3); }
    catch (NullPointerException e4)
    { System.out.println ("Null ptr exception reading " + scriptFile + e4); }
    }

}
