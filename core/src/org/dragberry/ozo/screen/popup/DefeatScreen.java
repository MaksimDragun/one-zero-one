package org.dragberry.ozo.screen.popup;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import org.dragberry.ozo.game.Assets;
import org.dragberry.ozo.screen.ActionExecutor;
import org.dragberry.ozo.screen.DirectedGame;
import org.dragberry.ozo.screen.MenuSkin;

/**
 * Created by maksim on 01.02.17.
 */

public class DefeatScreen extends AbstractPopup {

    public DefeatScreen(DirectedGame game) {
        super(game);
    }

    @Override
    protected void rebuildStage(float viewportWidth, float viewportHeight) {
        popupWindow.setWidth(viewportWidth * 0.75f);
        popupWindow.setHeight(viewportHeight / 2);
        Label lostLbl = new Label(Assets.instance.translation.get("ozo.youLost"), MenuSkin.getSkin());
        lostLbl.setAlignment(Align.center);
        popupWindow.add(lostLbl).fill().expand();
        popupWindow.row();
        popupWindow.add(createRetryBtn()).fill().expand().pad(10f);
        popupWindow.row();
        popupWindow.add(createMainMenuBtn()).fill().expand().pad(10f);
    }

    private TextButton createMainMenuBtn() {
        TextButton btn = new TextButton(Assets.instance.translation.get("ozo.goToMenu"), MenuSkin.getSkin());
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            	game.hidePopup(new ActionExecutor() {
					
					@Override
					public void execute() {
						game.back();
					}
				});
            }
        });
        return btn;
    }

    private TextButton createRetryBtn() {
        TextButton btn = new TextButton(Assets.instance.translation.get("ozo.retry"), MenuSkin.getSkin());
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            	game.hidePopup(new ActionExecutor() {
					
					@Override
					public void execute() {
						game.playLevel();
					}
				});
            }
        });
        return btn;
    }

}
