package org.dragberry.ozo.game.level.settings;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;

import org.dragberry.ozo.common.levelresult.LevelResultName;
import org.dragberry.ozo.common.levelresult.NewLevelResultsRequest;
import org.dragberry.ozo.common.levelresult.NewLevelResultsResponse;
import org.dragberry.ozo.game.Assets;
import org.dragberry.ozo.game.level.Level;

/**
 * Created by maksim on 03.02.17.
 */
public class FreeplayReachTheGoalLevelSettings extends LevelSettings {

    private static final String TAG = FreeplayReachTheGoalLevelSettings.class.getName();

	public final float ratio;
    public final int posGoalValue;

    public FreeplayReachTheGoalLevelSettings(Class<? extends Level<? extends LevelSettings>> clazz, String name, float ratio, int posGoal) {
        super(clazz, name, "ozo.rule.getTheLargestValue" ,"ozo.rule.reachTheLoseGoal", "ozo.rule.dynamicGrowth");
        this.ratio = ratio;
        this.posGoalValue = posGoal;
    }

    @Override
    protected void load(Preferences prefs) {
        if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
            Gdx.app.debug(TAG, "load results for " + levelId);
        }
        loadSingleResult(LevelResultName.MAX_VALUE, prefs);
        loadSingleResult(LevelResultName.MAX_AND_LOST, prefs);
    }

    @Override
    protected void update(Preferences prefs) {
        if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
            Gdx.app.debug(TAG, "update results for " + levelId);
        }
        updateSingleResult(LevelResultName.MAX_VALUE, prefs);
        updateSingleResult(LevelResultName.MAX_AND_LOST, prefs);
    }

    @Override
    public NewLevelResultsResponse checkLocalResults(NewLevelResultsRequest newResults) {
        NewLevelResultsResponse response = new NewLevelResultsResponse();
        checkSingleLocalResult(newResults, response, LevelResultName.MAX_VALUE);
        checkSingleLocalResult(newResults, response, LevelResultName.MAX_AND_LOST);
        return response;
    }
}
