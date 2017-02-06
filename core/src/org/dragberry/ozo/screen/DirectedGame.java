package org.dragberry.ozo.screen;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

import java.lang.reflect.Constructor;

import org.dragberry.ozo.game.Assets;
import org.dragberry.ozo.game.level.ChessboardLevel;
import org.dragberry.ozo.game.level.Level;
import org.dragberry.ozo.game.level.MashroomRainLevel;
import org.dragberry.ozo.game.level.NoAnnihilationQueueLevel;
import org.dragberry.ozo.game.level.QueueLevel;
import org.dragberry.ozo.game.level.goal.JustReachGoal;
import org.dragberry.ozo.game.level.settings.LevelSettings;
import org.dragberry.ozo.game.level.settings.NoAnnihilationLevelSettings;
import org.dragberry.ozo.game.level.settings.ReachMultiGoalLevelSettings;
import org.dragberry.ozo.game.level.settings.ReachTheGoalLevelSettings;
import org.dragberry.ozo.screen.transitions.ScreenTransition;
import org.dragberry.ozo.screen.transitions.ScreenTransitionFade;

/**
 * Created by maksim on 29.01.17.
 */

public abstract class DirectedGame implements ApplicationListener {

	public final Array<LevelSettings> levels = new Array<LevelSettings>();
	
    private LevelSettings currentLevelSettings;

    private boolean init;
    private AbstractGameScreen currScreen;
    private AbstractGameScreen nextScreen;
    private FrameBuffer currFbo;
    private FrameBuffer nextFbo;
    private FrameBuffer popupFbo;;
    private SpriteBatch batch;
    private float time;
    private ScreenTransition screenTransition;
    
    private ShaderProgram blackoutShader;
    
    private Class<? extends AbstractGameScreen> callerScreen;
    
    @Override
    public void create() {
    	Assets.instance.init(new AssetManager());
    	levels.add(new ReachTheGoalLevelSettings("ozo.lvl.letsStart", -10, 2, JustReachGoal.Operator.MORE));
		levels.add(new ReachTheGoalLevelSettings("ozo.lvl.littleBitHarder", -5, 10));
		levels.add(new ReachTheGoalLevelSettings("ozo.lvl.needMore", -10, 33));
		levels.add(new ReachMultiGoalLevelSettings("ozo.lvl.double5", -10, 5, 5));
		levels.add(new NoAnnihilationLevelSettings("ozo.lvl.saveUs", 5, 10));
		levels.add(new ReachMultiGoalLevelSettings("ozo.lvl.roulette", -10, 7, 7, 7));
		levels.add(new ReachTheGoalLevelSettings(MashroomRainLevel.class, "ozo.lvl.mushroomRain", -10, 25));
		levels.add(new ReachTheGoalLevelSettings(QueueLevel.class, "ozo.lvl.queues", -10, 25));
		levels.add(new ReachTheGoalLevelSettings(ChessboardLevel.class, "ozo.lvl.chessboard", -10, 25));
		levels.add(new ReachTheGoalLevelSettings(MashroomRainLevel.class, "ozo.lvl.mushroomShower", -25, 75));
		levels.add(new ReachMultiGoalLevelSettings("ozo.lvl.casinoRoyale", -99, 99, 99, 99));
		levels.add(new ReachTheGoalLevelSettings(QueueLevel.class, "ozo.lvl.regularity", -33, 99));
		levels.add(new NoAnnihilationLevelSettings("ozo.lvl.unsafePlace", 49, 99));
		levels.add(new NoAnnihilationLevelSettings(NoAnnihilationQueueLevel.class, "ozo.lvl.unsafeRegularity", 30, 50));
    }
    
    public void setScreen(AbstractGameScreen screen) {
        setScreen(screen, null, null);
    }
    
    public void setScreen(AbstractGameScreen screen, ScreenTransition screenTransition) {
        setScreen(screen, screenTransition, null);
    }
    
    public void setScreen(AbstractGameScreen screen, Class<? extends AbstractGameScreen> callerScreen) {
        setScreen(screen, null, callerScreen);
    }

    public void setScreen(AbstractGameScreen screen, ScreenTransition screenTransition, Class<? extends AbstractGameScreen> callerScreen) {
        if (callerScreen != null) {
        	this.callerScreen = callerScreen;
        }
    	int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        if (!init) {
            currFbo = new FrameBuffer(Pixmap.Format.RGB888, width, height, false);
            nextFbo = new FrameBuffer(Pixmap.Format.RGB888, width, height, false);
            popupFbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
            batch = new SpriteBatch();
            blackoutShader = new ShaderProgram(
            		Gdx.files.internal("shaders/blackout.vert"), 
            		Gdx.files.internal("shaders/blackout.frag"));
            init = true;
        }
        // start new transition
        nextScreen = screen;
        Gdx.app.debug(getClass().getName(), "setScreen()");
        nextScreen.show(); // activate next screen
        nextScreen.resize(width, height);
        nextScreen.render(0); // let screen update() once
        if (currScreen != null) {
            currScreen.pause();
        }
        nextScreen.pause();
        Gdx.input.setInputProcessor(null); // disable input
        this.screenTransition = screenTransition;
        time = 0;
    }
    
    public void back() {
        Gdx.input.setCatchBackKey(false);
    	try {
	    	if (callerScreen != null) {
	    		Constructor<? extends AbstractGameScreen> constructor = callerScreen.getConstructor(DirectedGame.class);
	    		constructor.newInstance(this);
	    		setScreen(constructor.newInstance(this), ScreenTransitionFade.init(), null);
	    		Gdx.app.debug(getClass().getName(), "Navigate to " + callerScreen);
	    	} else {
	    		setScreen(new MainMenuScreen(this), ScreenTransitionFade.init(), null);
	    		Gdx.app.debug(getClass().getName(), "Navigate to " + MainMenuScreen.class);
	    	}
	    	callerScreen = null;
    	} catch (Exception exc) {
    		Gdx.app.error(getClass().getName(), "An error has occured during navigation! Application is terminated!", exc);
    		Gdx.app.exit();
    	}
    }

   private void renderPopup(float deltaTime) {
	   currFbo.begin();
       if (currScreen != null) {
           currScreen.render(deltaTime);
       }
       currFbo.end();
       popupFbo.begin();
       currScreen.getPopup().render(deltaTime);
       popupFbo.end();
       
       
       float width = currFbo.getColorBufferTexture().getWidth();
       float height = currFbo.getColorBufferTexture().getHeight();
       Gdx.gl.glClearColor(0, 0, 0, 0);
       Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

       batch.begin();
       batch.setShader(blackoutShader);
       batch.setColor(1, 1, 1, 1);
       batch.draw(currFbo.getColorBufferTexture(),
    		   0, 0, 
    		   0, 0, 
    		   width, height,
    		   1, 1, 0, 0, 0,
    		   currFbo.getColorBufferTexture().getWidth(), currFbo.getColorBufferTexture().getHeight(),
    		   false, true);
       batch.setShader(null);
       batch.setColor(1, 1, 1, 1);
       batch.draw(popupFbo.getColorBufferTexture(), 
    		   0, 0, 
    		   0, 0, width, height,
    		   1, 1, 0, 0, 0,
    		   popupFbo.getColorBufferTexture().getWidth(), popupFbo.getColorBufferTexture().getHeight(),
               false, true);
       batch.end();
   }
    
    @Override
    public void render() {
        float deltaTime = Math.min(Gdx.graphics.getDeltaTime(), 1.0f / 60.0f);
        if (nextScreen == null) {
            // no ongoing transition
            if (currScreen != null) {
            	if (currScreen.hasPopup()) {
            		// render popup
            		renderPopup(deltaTime);
            	} else {
            		currScreen.render(deltaTime);
            	}
            }
        } else {
            //ongoing transition
            float duration = 0;
            if (screenTransition != null) {
                duration = screenTransition.getDuration();
            }
            // update progress of ongoing transition
            time = Math.min(time + deltaTime, duration);
            if (screenTransition == null || time >= duration) {
                // no transition effect set or transition has just finished
                if (currScreen != null) {
                    currScreen.hide();
                }
                nextScreen.resume();
                // enable input  for next screen
                Gdx.input.setInputProcessor(nextScreen.getInputProcessor());
                // switch screens
                currScreen = nextScreen;
                nextScreen = null;
                screenTransition = null;
            } else {
                // render screens to FBOs
                currFbo.begin();
                if (currScreen != null) {
                    currScreen.render(deltaTime);
                }
                currFbo.end();
                nextFbo.begin();
                nextScreen.render(deltaTime);
                nextFbo.end();
                // render transition effect to screen
                float alpha = time / duration;
                screenTransition.render(batch, currFbo.getColorBufferTexture(), nextFbo.getColorBufferTexture(), alpha);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        if (currScreen != null) {
            currScreen.resize(width, height);
        }
        if (nextScreen != null) {
            nextScreen.resize(width, height);
        }
    }

    @Override
    public void pause() {
        if (currScreen != null) {
            currScreen.pause();
        }
    }

    @Override
    public void resume() {
        if (currScreen != null) {
            currScreen.resume();
        }
    }

    @Override
    public void dispose() {
        if (currScreen != null) {
            currScreen.hide();
        }
        if (nextScreen != null) {
            nextScreen.hide();
        }
        if (init) {
        	popupFbo.dispose();
            currFbo.dispose();
            currScreen = null;
            nextFbo.dispose();
            nextScreen = null;
            batch.dispose();
            blackoutShader.dispose();
            init = false;
        }
    }

    public void setCurrentLevelSettings(LevelSettings currentLevelSettings) {
    	this.currentLevelSettings = currentLevelSettings;
    }
    
    public void playNextLevel() {
    	int currLevelIndex = levels.indexOf(currentLevelSettings, true);
    	if (currLevelIndex < levels.size - 1) {
    		setCurrentLevelSettings(levels.get(currLevelIndex + 1));
    		playLevel();
    	} else {
    		back();
    	}
    }
    
    public void playLevel() {
    	if (currentLevelSettings != null) {
    		playLevel(currentLevelSettings, null);
    	}
    }
    
    public void playLevel(LevelSettings currentLevelSettings, Class<? extends AbstractGameScreen> callerClass) {
        Gdx.input.setCatchBackKey(true);
        this.currentLevelSettings = currentLevelSettings;
        try {
            Constructor<? extends Level<? extends LevelSettings>> constructor = currentLevelSettings.clazz.getConstructor(currentLevelSettings.getClass());
            Level<? extends LevelSettings> level = constructor.newInstance(currentLevelSettings);
            setScreen(new GameScreen(this, level), ScreenTransitionFade.init(), callerClass);
        } catch (Exception exc) {
            Gdx.app.error(getClass().getName(), "An exception has occured during level creation", exc);
        }
    }

}