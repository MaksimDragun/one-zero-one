package org.dragberry.ozo.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import org.dragberry.ozo.game.Assets;
import org.dragberry.ozo.game.level.settings.LevelSettings;

/**
 * Created by maksim on 28.01.17.
 */
public class SelectLevelMenuScreen extends AbstractGameScreen {

    private Stage stage;

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
        rebuildStage();
    }

    private void rebuildStage() {
        Table table = new Table();
        table.setFillParent(true);

        Label label = new Label(Assets.instance.translation.format("ozo.selectLevel"), MenuSkin.getSkin());
        label.setAlignment(Align.center);
        table.add(label).fill().expand();
        table.row();

        Table scrollTable = new Table();
        boolean previousCompleted = true;
        for (LevelSettings levelSettings : game.levels) {
        	if (previousCompleted) {
	        	scrollTable.add(createLevelBtn(levelSettings)).fillX().expand(true, false);
	            scrollTable.row();
	            previousCompleted = levelSettings.completed;
        	} else {
        		break;
        	}
        }
        ScrollPane scroller = new ScrollPane(scrollTable);
        table.add(scroller).fill().expand();
        table.row().fill().expand();

        TextButton backBtn = new TextButton(Assets.instance.translation.format("ozo.back"), MenuSkin.getSkin());
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.back();
            }
        });
        table.add(backBtn);
        this.stage.addActor(table);
    }


    @Override
    public void hide() {
        stage.dispose();
    }

    @Override
    public void pause() {
    }

    private TextButton createLevelBtn(final LevelSettings levelSettings) {
        TextButton btn = new TextButton(levelSettings.name, MenuSkin.getSkin());
        btn.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.playLevel(levelSettings, SelectLevelMenuScreen.this.getClass());
            }
        });
        return btn;
    }

}
