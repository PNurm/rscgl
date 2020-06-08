package com.rscgl.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.rscgl.Game;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.foregroundFPS = 9999;
		config.backgroundFPS = 9999;
		config.vSyncEnabled = false;
		config.title = "rscopengl";
		new LwjglApplication(new Game(), config);
	}
}
