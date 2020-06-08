package com.rscgl.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.rscgl.GameCamera;
import com.rscgl.Config;
import com.rscgl.Game;
import com.rscgl.assets.model.Sector;
import com.rscgl.assets.model.Tile;
import com.rscgl.model.entity.Entity;
import com.rscgl.model.entity.EntityType;
import com.rscgl.GameWorld;
import com.rscgl.net.Connection;
import com.rscgl.ui.util.Backgrounds;
import com.rscgl.ui.util.Fonts;
import com.rscgl.ui.util.Style;
import com.rscgl.model.SectorLocation;
import com.rscgl.render.SectorRenderContext;
import com.rscgl.model.entity.ObjectEntity;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Map;

public class LoginScreen {

    private String username;
    private String password;

    private int autoLoginTimeout;
    private int connectTimeout;

    private Label status;
    private Dialog dialog;
    private TextButton.TextButtonStyle textButtonStyle;
    private GameWorld world;

    private final void login(String pass, String user, boolean reconnecting) {
        if (this.connectTimeout > 0) {
            this.showLoginScreenStatus("Please wait...", "Connecting to server");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            while (this.autoLoginTimeout > 0) {
                try {
                    username = user;
                    password = pass;
                    trim(20, pass);
                    if (username.trim().length() == 0) {
                        showLoginScreenStatus("You must enter both a username",
                                "and a password - Please try again");
                        return;
                    }
                    Game.inst.connection = new Connection(Config.SERVER_IP, Config.SERVER_PORT);
                    Game.outBuffer().newPacket(0);
                    Game.outBuffer().putInt(Config.CLIENT_VERSION);
                    Game.outBuffer().putByte(0);
                    Game.outBuffer().putByte(0);
                    Game.outBuffer().putString("RS1HD");
                    Game.outBuffer().putLong(0);
                    Game.outBuffer().putString("failed");

                    Game.outBuffer().putString(username);

                    byte[] encryptedPass = encryptPassword(password);
                    Game.outBuffer().putByte(encryptedPass.length);
                    Game.outBuffer().writeBytes(encryptedPass, 0, encryptedPass.length);
                    Game.outBuffer().finishPacket();
                    Game.inst.flushConnection();

                    int response = Game.net().read();
                    LoginResponse loginResponse = LoginResponse.get(response);
                    System.out.println("login response:" + loginResponse);

                    if (loginResponse == LoginResponse.LOGIN_SUCCESSFUL || loginResponse == LoginResponse.RECONNECT_SUCCESFUL) {
                        this.autoLoginTimeout = 0;
                        Game.inst.setState(Game.GameState.GAME);
                    } else {
                        showLoginScreenStatus(loginResponse.message, "");
                    }
                    return;
                } catch (Exception var15) {
                    var15.printStackTrace();
                    if (autoLoginTimeout <= 0) {
                        if (reconnecting) {
                            password = "";
                            username = "";
                            //jumpToLogin();
                        } else {
                            // GenUtil.reportErrorToJagex(var15, "Error
                            // while connecting");
                            showLoginScreenStatus("Sorry! Unable to connect.",
                                    "Check internet settings or try another world");
                        }
                    } else {
                        try {
                            //Thread.sleep(5000L);
                            //GenUtil.sleepShadow(5000L);
                        } catch (Exception var12) {
                        }
                        --autoLoginTimeout;
                    }
                }
            }
        }
    }

    private static PublicKey publicKey;

    static {
        try {
            KeyFactory fact = KeyFactory.getInstance("RSA");
            publicKey = fact.generatePublic(
                    new RSAPublicKeySpec(
                            new BigInteger("157052809420546740450727883040177872335298520609265078369774157947802898550229265348272890472114564163668620406482032227343980533173362658279117877770736410815745053760004288324434381873728860059567309954792418321220683028091805987766347863346808067341128470030594228173601330984259732403587647956145901029983"),
                            new BigInteger("65537")
                    ));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public byte[] encryptPassword(String string) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(string.getBytes());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static final String trim(int length, String input) {
        String result = "";
        for (int currentChar = 0; currentChar < length; ++currentChar) {
            if (input.length() > currentChar) {
                char charCode = input.charAt(currentChar);
                if (charCode >= 97 && charCode <= 122) {
                    result = result + charCode;
                } else if (charCode >= 65 && charCode <= 90) {
                    result = result + charCode;
                } else if (charCode >= 48 && charCode <= 57) {
                    result = result + charCode;
                } else {
                    result = result + '_';
                }
            } else {
                result = result + " ";
            }
        }
        return result;
    }

    private void showLoginScreenStatus(String s, String connecting_to_server) {

        Window.WindowStyle style = new Window.WindowStyle();
        style.titleFont = Fonts.Font14B_NOSHADOW;
        style.background = new Backgrounds().create(Color.BLACK, 64, 64);
        style.titleFontColor = Color.WHITE;

        dialog = new Dialog("Login error", style) {
            protected void result(Object object) {
                dialog.hide();
            }

            ;
        };
        dialog.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ENTER)
                    dialog.hide();
                return true;
            }
        });

        dialog.text(s + " " + connecting_to_server, Style.labelBoldRegularW);
        dialog.padTop(30);
        dialog.button("Retry", null, textButtonStyle);
        dialog.pack();
        dialog.background(new Backgrounds().create(Color.BLACK, Color.WHITE, (int) dialog.getWidth(), (int) dialog.getHeight()));
        dialog.show(stage);
        status.setText(s + " " + connecting_to_server);
    }

    public Stage stage;
    public GameCamera camera;



    public void show() {

        stage = new Stage();

        world = Game.world();
        world.reset();
        world.manageLoadedSectors(120, 645);

        camera = Game.cam();

        camera.near = 0.01f;
        camera.far = 102f;
        camera.position.set(-113 * GameWorld.TSIZE, 25, 640 * GameWorld.TSIZE);
        camera.lookAt(-130 * GameWorld.TSIZE, 3, 660 * GameWorld.TSIZE);
        camera.update();

        addLoginScreenObjects();

        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.focusedFontColor = Color.BLACK;
        style.font = Fonts.Font12B_NOSHADOW;
        style.fontColor = Color.BLACK;
        style.cursor = Backgrounds.create(Color.WHITE, 1, 1);

        textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = Fonts.Font12B_NOSHADOW;
        textButtonStyle.fontColor = Color.BLACK;
        textButtonStyle.downFontColor = Color.RED;
        textButtonStyle.checkedOverFontColor = Color.RED;
        textButtonStyle.over = Backgrounds.drawVerticalGradient(100, 25, new Color(0.52f, 0.57f, 0.7f, 1f), new Color(0.32f, 0.36f, 0.47f, 1));
        textButtonStyle.up = textButtonStyle.over;

        status = new Label("Please enter your username and password", Style.labelBoldRegularW);

        Label user = new Label("Username:", Style.labelBigB);
        Label pass = new Label("Password:", Style.labelBigB);

        final TextField username = new TextField("", style);
        username.setAlignment(Align.center);
        final TextField password = new TextField("", style);
        password.setAlignment(Align.center);
        password.setPasswordMode(true);
        password.setPasswordCharacter((char) 42);

        username.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ENTER) {
                    stage.setKeyboardFocus(password);
                    return true;
                }
                return false;
            }
        });
        password.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ENTER) {
                    autoLoginTimeout = 2;
                    login(password.getText(), username.getText(), false);
                    return true;
                }
                return false;
            }
        });

        TextButton button = new TextButton("Login", this.textButtonStyle);
        button.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                autoLoginTimeout = 2;
                login(password.getText(), username.getText(), false);
                return true;
            }
        });

        Label.LabelStyle style1 = new Label.LabelStyle();
        style1.font = Fonts.Font16B;
        style1.fontColor = Color.GOLDENROD;
        Table credentials = new Table();
        credentials.add(new Label("rscopengl", style1)).row();
        credentials.add(new Label("version " + Config.VERSION, Style.labelBoldRegularW)).row();
        credentials.add(new Label("Developed by P.Nurm", Style.labelSmallW)).row();
        credentials.pad(25);

        Table login = new Table();
        Table userT = new Table();
        userT.background(Backgrounds.drawVerticalGradient(200, 40, new Color(0.52f, 0.57f, 0.7f, 1f), new Color(0.32f, 0.36f, 0.47f, 1)));
        userT.add(user).padBottom(3);
        userT.row();
        userT.add(username).padBottom(3);
        login.add(userT).pad(10).colspan(2).row();


        Table passT = new Table();
        passT.background(Backgrounds.drawVerticalGradient(200, 40, new Color(0.52f, 0.57f, 0.7f, 1f), new Color(0.32f, 0.36f, 0.47f, 1)));
        passT.add(pass).padBottom(3);
        passT.row();
        passT.add(password).padBottom(3);

        login.add(passT).padLeft(30).colspan(2);
        login.add(button).padLeft(30).right().row();
        login.align(Align.bottom).padTop(120);
        login.pack();
        credentials.add(login);

        credentials.pack();
        credentials.setFillParent(true);
        stage.addActor(credentials);

        Gdx.input.setInputProcessor(stage);
        stage.setKeyboardFocus(username);

    }

    public void addLoginScreenObjects() {
        for (Map.Entry<SectorLocation, SectorRenderContext> entry : world.loadedSectors.entrySet()) {
            SectorLocation location = entry.getKey();
            SectorRenderContext sector = entry.getValue();

            for (int x = 0; x < Sector.SIZE; ++x) {
                for (int y = 0; y < Sector.SIZE; ++y) {
                    int worldX = location.worldX() + x;
                    int worldY = location.worldY() + y;

                    Tile tile = sector.lookupTile(worldX, worldY);

                    if (tile.getWallDiagonal() > 48000 && tile.getWallDiagonal() < 60000) {
                        int objectID = tile.getWallDiagonal() - 48001;
                        ObjectEntity object = world.registerObject(objectID, worldX - GameWorld.WORLD_WIDTH, worldY - GameWorld.WORLD_HEIGHT, 0);

                        if (object.getWidth() > 1 || object.getHeight() > 1) {
                            for (int xx = x; x + object.getWidth() > xx; ++xx) {
                                for (int yy = y; y + object.getHeight() > yy; ++yy) {
                                    Tile t = entry.getValue().lookupTile(location.worldX() + xx, location.worldY() + yy);
                                    if ((x < xx || y < yy) && objectID == t.wallDiagonal - 48001) {
                                        t.wallDiagonal = 0;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void render(float delta, GameCamera camera, ModelBatch modelBatch, DecalBatch decalBatch) {
        if (this.connectTimeout > 0) {
            connectTimeout--;
        }
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        camera.rotate(Vector3.Y, Gdx.graphics.getDeltaTime() * 3);
        camera.update();

        modelBatch.begin(camera);
        for(SectorRenderContext sector : world.getLoadedSectors().values()) {
            modelBatch.render(sector);
        }

        for (Map.Entry<EntityType, ArrayList<Entity>> entry : world.getEntities().entrySet()) {
            for (Entity entity : entry.getValue()) {
                entity.setPicked(false);

                if (entity.getRenderContext() != null) {
                    entity.getRenderContext().render(camera, modelBatch, decalBatch);
                    entity.setPicked(true);
                }
            }
        }
        modelBatch.end();


        stage.getBatch().enableBlending();
        stage.act();
        stage.draw();
        stage.getBatch().disableBlending();
    }

    public enum LoginResponse {
        SERVER_TIMEDOUT(-1, "Error unable to login., Server timed out"),
        LOGIN_SUCCESSFUL(0, "Login Succesful!"),
        RECONNECT_SUCCESFUL(1, "Reconnect succesful!"),
        UNREGONIZED_LOGIN(2, "Error unable to login. Unrecognised response code"),
        INVALID_CREDENTIALS(3, "Invalid username or password. Try again, or create a new account"),
        ACCOUNT_LOGGEDIN(4, "That username is already logged in. Wait 60 seconds then retry"),
        CLIENT_UPDATED(5, "The client has been updated. Please reload this page"),
        IP_IN_USE(6, "You may only use 1 character at once. Your ip-address is already in use"),
        LOGIN_ATTEMPTS_EXCEEDED(7, "Login attempts exceeded! Please try again in 5 minutes"),
        SERVER_REJECT(8, "Error unable to login. Server rejected session"),
        UNDER_13_YEARS_OLD(9, "Error unable to login. Under 13 accounts cannot access RuneScape Classic"),
        USERNAME_ALREADY_IN_USE(10, "That username is already logged in. Wait 60 seconds then retry"),
        ACCOUNT_TEMP_DISABLED(11,"Account temporarily disabled. Check your message inbox for details"),
        ACCOUNT_PERM_DISABLED(12, "Account permanently disabled. Check your message inbox for details"),
        WORLD_IS_FULL(14, "Sorry! This world is currently full. Please try a different world"),
        NEED_MEMBERS_ACCOUNT(15, "You need a members account to login to this world"),
        LOGINSERVER_OFFLINE(16, "Error - no reply from loginserver. Please try again"),
        FAILED_TO_DECODE_PROFILE(17, "Error - failed to decode profile. Contact customer support"),
        ACCOUNT_SUSPECTED_STOLEN(18, "Account suspected stolen. Press \'recover a locked account\' on front page."),
        LOGINSERVER_MISMATCH(20,"Error - loginserver mismatch. Please try a different world"),
        NOT_VETERAN_ACCOUNT(21, "That is not a veteran RS-Classic account. Please try a non-veterans world."),
        PASSWORD_STOLEN(22, "Login from new location detected! Check your e-mail(spam too) to validate login"),
        NEED_TO_SET_DISPLAY_NAME(23, "You need to set your display name. Please go to the Account Management page to do this."),
        WORLD_DOES_NOT_ACCEPT_NEW_PLAYERS(24, "This world does not accept new players. Please see the launch page for help"),
        NONE_OF_YOUR_CHARACTERS_CAN_LOGIN(25, "None of your characters can log in. Contact customer support"),
        MULTILOG_IN_WILD(26, "You can't login in wilderness while you have account in wilderness or have recently left wilderness"),
        MULTILOG_TIMEOUT(27, "One of your accounts recently logged out in wilderness, wait 10 minutes and try again");

        public int id;
        public String message;

        LoginResponse(int id, String message) {
            this.id = id;
            this.message = message;
        }

        public static LoginResponse get(int response) {
            for(LoginResponse lr : LoginResponse.values())
                if(lr.id == response)
                    return lr;
            return null;
        }

    }
}
