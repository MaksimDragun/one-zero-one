package org.dragberry.ozo.screen;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import org.dragberry.ozo.game.level.settings.LevelSettings;

/**
 * Created by maksim on 28.01.17.
 */
public class SelectLevelMenuScreen extends AbstractSelectLevelMenuScreen {

    public SelectLevelMenuScreen(DirectedGame game) {
        super(game, "ozo.selectLevel");
    }

    @Override
    protected ClickListener getActionListener(final LevelSettings levelSettings) {
    	return new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.playLevel(levelSettings, SelectLevelMenuScreen.this.getClass());
            }
        };
    }

}
