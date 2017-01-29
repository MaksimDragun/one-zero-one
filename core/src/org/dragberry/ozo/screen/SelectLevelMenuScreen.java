package org.dragberry.ozo.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;

import org.dragberry.ozo.game.level.AbstractLevel;
import org.dragberry.ozo.game.level.DefaultLevel;
import org.dragberry.ozo.game.level.DoubleFiveLevel;
import org.dragberry.ozo.game.level.LetsStartLevel;
import org.dragberry.ozo.game.level.TripleFiveLevel;
import org.dragberry.ozo.screen.transitions.ScreenTransition;
import org.dragberry.ozo.screen.transitions.ScreenTransitionFade;

import java.lang.reflect.Constructor;

/**
 * Created by maksim on 28.01.17.
 */
public class SelectLevelMenuScreen extends AbstractGameScreen {

    private static final String TAG = SelectLevelMenuScreen.class.getName();

    private static class LevelInfo {
        private Class<? extends AbstractLevel> clazz;
        private Object[] params;

        public LevelInfo(Class<? extends AbstractLevel> clazz, Object... params) {
            this.clazz = clazz;
            this.params = params;
        }
    }

    private static final ArrayMap<String, LevelInfo> levels = new ArrayMap<String, LevelInfo>(true, 1);
    static {
        levels.put("Let's start!", new LevelInfo(LetsStartLevel.class));
        levels.put("Double 5", new LevelInfo(DoubleFiveLevel.class));
        levels.put("Triple 5", new LevelInfo(TripleFiveLevel.class));
        levels.put("Default level", new LevelInfo(DefaultLevel.class));
    }

    private Stage stage;

    private float buttonWidth;
    private float buttonHeight;

    public SelectLevelMenuScreen(DirectedGame game) {
        super(game);
    }

    @Override
    public InputProcessor getInputProcessor() {
        return stage;
    }

    @Override
    public void render(float deltaTime) {
        Gdx.gl.glClearColor(MenuSkin.BACKGROUND_COLOR.r, MenuSkin.BACKGROUND_COLOR.g, MenuSkin.BACKGROUND_COLOR.b, MenuSkin.BACKGROUND_COLOR.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void show() {
        stage = new Stage();

        buttonWidth = Gdx.graphics.getWidth() * 0.75f;
        buttonHeight = Gdx.graphics.getHeight() / 10.0f;
        float topButtonPositionY = Gdx.graphics.getHeight() - 1.2f * buttonHeight;
        int position = 0;
        for (ObjectMap.Entry<String, LevelInfo> entry : levels.entries()) {
            stage.addActor(createLevelBtn(entry.key, entry.value, position++, topButtonPositionY));
        }
    }

    @Override
    public void hide() {
        stage.dispose();
    }

    @Override
    public void pause() {

    }

    private TextButton createLevelBtn(String btnLabel, final LevelInfo levelInfo, int position, float topButtonPosition) {
        TextButton btn = new TextButton(btnLabel, MenuSkin.getSkin());
        btn.setWidth(buttonWidth);
        btn.setPosition(Gdx.graphics.getWidth() / 2 - buttonWidth / 2, topButtonPosition - (buttonHeight * position * 1.2f)) ;
        btn.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    Class<?>[] paramClasses = new Class<?>[levelInfo.params.length];
                    for (int i = 0; i < levelInfo.params.length; i++) {
                        paramClasses[i] = levelInfo.params[i].getClass();
                    }
                    Constructor<? extends AbstractLevel> constructor = levelInfo.clazz.getConstructor(paramClasses);
                    AbstractLevel level = levelInfo.params.length == 0 ? constructor.newInstance() : constructor.newInstance(levelInfo.params);
                    game.setScreen(new GameScreen(game, level), ScreenTransitionFade.init(0.25f));
                } catch (Exception exc) {
                    Gdx.app.debug(TAG, "An exception has occured during level creation", exc);
                }
            }
        });
        return btn;
    }

}