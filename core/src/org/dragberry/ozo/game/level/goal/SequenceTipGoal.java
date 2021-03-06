package org.dragberry.ozo.game.level.goal;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import org.dragberry.ozo.game.Assets;
import org.dragberry.ozo.game.objects.Unit;

/**
 * Created by maksim on 02.04.17.
 */

public class SequenceTipGoal extends AbstractGoal {

    private GlyphLayout title;
    private GlyphLayout sequence;

    private boolean rendered = false;

    public SequenceTipGoal() {
        title = new GlyphLayout(Assets.instance.fonts.gui_s, Assets.instance.translation.get("ozo.sequence"));
        sequence = new GlyphLayout();
    }

    @Override
    public boolean isReached(Unit[][] units, Unit selectedUnit, Array<Unit> neighbors) {
        return true;
    }

    @Override
    public void render(SpriteBatch batch, float x, float y) {
        if (rendered) {
            Assets.instance.fonts.gui_s.draw(batch, title,
                    x + 15, y + 25);
            Assets.instance.fonts.gui_s.draw(batch, sequence,
                    x + 15, y + 25 + sequence.height * 1.5f);
        }
    }

    public void updateSequence(String seq) {
        if (seq != null) {
            sequence.setText(Assets.instance.fonts.gui_s, seq);
            rendered = true;
        } else {
            rendered = false;
        }
    }

}
