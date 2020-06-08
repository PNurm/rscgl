package com.rscgl;

import com.badlogic.gdx.graphics.Color;

import java.io.File;

/**
 * @author n0m Configuration using reflection to load/save.
 */
public class Config {

	public static final double VERSION = 0.81;
	public static Color FOG_COLOR = Color.valueOf("28383dff");

	public static final float FIELD_OF_VIEW = 35;
	public static final int RESOLUTION_WIDTH = 640;
	public static final int RESOLUTION_HEIGHT = 480;

	public static final String SERVER_IP = "185.38.149.37";
	public static final int SERVER_PORT = 43594;
	public static final int CLIENT_VERSION = 68;

	public static String CACHE_DIR = "./rscache" + File.separator;

}