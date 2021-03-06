package org.dragberry.ozo.game;

import org.dragberry.ozo.game.util.Constants;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectMap;

public class Assets implements Disposable, AssetErrorListener {

	private static final String TAG = Assets.class.getName();
	
	private static final String FONT_CHARS;
	static {
		StringBuilder sb = new StringBuilder();
		for( int i = 32; i < 127; i++ ) {
			sb.append((char)i);
		}
		for( int i = 1024; i < 1104; i++ ) {
			sb.append((char)i); 
		}
		FONT_CHARS = sb.toString();
	}
	
	public static final Assets instance = new Assets();
	
	private AssetManager assetManager;

	public AssetLevel level;
	
	public AssetFonts fonts;
	public I18NBundle translation;
	public AssetSkin skin;
	
	private Assets() {}
	
	public void init(AssetManager assetManager) {
		this.assetManager = assetManager;
		assetManager.setErrorListener(this);
		assetManager.load(Constants.TEXTURE_ATLAS_OBJECTS, TextureAtlas.class);
		assetManager.load(Constants.TRANSLATION, I18NBundle.class);
		assetManager.load(Constants.TEXTURE_ATLAS_SKIN, TextureAtlas.class);
		assetManager.finishLoading();
		if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
			Gdx.app.debug(TAG, "# of assets loaded: " + assetManager.getAssetNames().size);
		}
		for (String asset : assetManager.getAssetNames()) {
			if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
				Gdx.app.debug(TAG, "asset: " + asset);
			}
		}
		
		TextureAtlas atlas = getTextureAtlas(Constants.TEXTURE_ATLAS_OBJECTS);
		level = new AssetLevel(atlas);
		fonts = AssetFonts.create(Gdx.graphics.getWidth());
		skin = new AssetSkin(assetManager, getTextureAtlas(Constants.TEXTURE_ATLAS_SKIN));
		translation = assetManager.get(Constants.TRANSLATION, I18NBundle.class);
	}
	
	private TextureAtlas getTextureAtlas(String id) {
		TextureAtlas atlas = assetManager.get(id);
		for (Texture txt : atlas.getTextures()) {
			txt.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		}
		return atlas;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void error(AssetDescriptor asset, Throwable throwable) {
		Gdx.app.error(TAG, "Couldn't load asset '" + asset.fileName + "'", throwable);
	}

	@Override
	public void dispose() {
		assetManager.dispose();
		fonts.dispose();
	}

	public class AssetLevel {

		public final AtlasRegion background;
		public final AtlasRegion uiTop;
		public final AtlasRegion uiBottom;
		public final AssetUnit unit;
		public final AssetDigits digits;

		public AssetLevel(TextureAtlas atlas) {
			background = atlas.findRegion("background");
			uiTop = atlas.findRegion("ui_top");
			uiBottom = atlas.findRegion("ui_bottom");
			unit = new AssetUnit(atlas);
			digits = new AssetDigits(atlas);
		}

		public class AssetUnit {
			public final AtlasRegion positive;
			public final AtlasRegion neutral;
			public final AtlasRegion negative;
			public final AtlasRegion frame;
			public final AtlasRegion body;

			public AssetUnit(TextureAtlas atlas) {
				neutral = atlas.findRegion("unit_neutral");
				negative = atlas.findRegion("unit_neg");
				positive = atlas.findRegion("unit_pos");
				frame = atlas.findRegion("unit_frame");
				body = atlas.findRegion("unit_body");
			}
		}

		public class AssetDigits {
			public final AtlasRegion[] digits = new AtlasRegion[10];
			public final AtlasRegion plus;
			public final AtlasRegion minus;

			public AssetDigits(TextureAtlas atlas) {
				for (int i = 0; i < 10; i++) {
					digits[i] = atlas.findRegion("n" + i);
				}
				plus = atlas.findRegion("n+");
				minus = atlas.findRegion("n-");
			}
		}
	}

	public static class AssetFonts implements Disposable {

		public final BitmapFont gui_xs;
		public final BitmapFont gui_s;
		public final BitmapFont gui_m;
		public final BitmapFont gui_l;
		public final BitmapFont menu_m;
		public final BitmapFont menu_l;

		private AssetFonts(int guiXS, int guiS, int guiM, int guiL, int menuM, int menuL) {
			gui_xs = createFont(guiXS, true, Constants.FONT_BLOCK_1);
			gui_s = createFont(guiS, true, Constants.FONT_BLOCK_1);
			gui_m = createFont(guiM, true, Constants.FONT_BLOCK_1);
			gui_l = createFont(guiL, true, Constants.FONT_BLOCK_1);
			menu_m = createFont(menuM, false, Constants.FONT_BLOCK_1);
			menu_l = createFont(menuL, false, Constants.FONT_BLOCK_1);
		}

		public static AssetFonts create(int screenWidth) {
			switch (screenWidth) {
				case 240:
					return new AssetFonts(18, 23, 26, 28, 26, 32);
				case 540:
				case 720:
				case 768:
					return new AssetFonts(24, 28, 36, 40, 30, 36);
				case 1080:
				case 1200:
				case 1440:
				case 1600:
					return new AssetFonts(17, 18, 25, 27, 34, 38);
				default:
					float factorGui = screenWidth /  Constants.VIEWPORT_GUI_WIDTH;
					return new AssetFonts(
							(int)(24 * factorGui),
							(int)(28 * factorGui),
							(int)(36 * factorGui),
							(int)(40 * factorGui),
							(int)(40 * factorGui),
							(int)(46 * factorGui));
			}
		}

		@Override
		public void dispose() {
			gui_xs.dispose();
			gui_s.dispose();
			gui_m.dispose();
			gui_l.dispose();
			menu_m.dispose();
			menu_l.dispose();
		}
	}

	private static BitmapFont createFont(int size, boolean flip, Color color) {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(Constants.FONTS));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.characters = FONT_CHARS;
		parameter.color = color;
		parameter.size = size;
		parameter.flip = flip;
		parameter.magFilter = TextureFilter.Linear;
		parameter.minFilter = TextureFilter.Linear;
		BitmapFont font = generator.generateFont(parameter);
		generator.dispose();
		return font;
	}
	
	public class AssetSkin {
		public final Skin skin;
		public final TextureRegion logo;
		
		public AssetSkin(AssetManager manager, TextureAtlas atlas) {
			logo = atlas.findRegion("logo");
			ObjectMap<String, Object> resources = new ObjectMap<String, Object>();
			resources.put("menu-font-medium", fonts.menu_m);
			resources.put("menu-font-large", fonts.menu_l);
			manager.load(Constants.SKIN, Skin.class, new SkinLoader.SkinParameter(resources));
			manager.finishLoading();
			skin = manager.get(Constants.SKIN);
		}
	}
}
