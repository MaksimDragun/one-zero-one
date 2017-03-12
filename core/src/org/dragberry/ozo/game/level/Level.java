package org.dragberry.ozo.game.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import org.dragberry.ozo.common.audit.LevelAttemptAuditEventRequest;
import org.dragberry.ozo.common.levelresult.LevelResultName;
import org.dragberry.ozo.common.levelresult.NewLevelResultRequest;
import org.dragberry.ozo.common.levelresult.NewLevelResultsRequest;
import org.dragberry.ozo.game.level.generator.Generator;
import org.dragberry.ozo.game.level.generator.RandomGenerator;
import org.dragberry.ozo.game.level.goal.AbstractGoal;
import org.dragberry.ozo.game.level.goal.Goal;
import org.dragberry.ozo.game.level.settings.LevelSettings;
import org.dragberry.ozo.game.objects.Unit;
import org.dragberry.ozo.game.util.CameraHelper;

import java.util.Collections;
import java.util.Map;

/**
 * Created by maksim on 30.01.17.
 */

public abstract class Level<LI extends LevelSettings> {

    private final static String TAG = Level.class.getName();

	protected final static int DEFAULT_WIDTH = 6;
    protected final static int DEFAULT_HEIGHT = 8;

    public final Array<AbstractGoal> goalsToWin = new Array<AbstractGoal>();
    public final Array<AbstractGoal> goalsToLose = new Array<AbstractGoal>();
    
    protected Map<Generator.Id, Generator> generators = Collections.emptyMap();
    
    public final LI settings;
    public final int width;
    public final int height;

    public float time = 0;
    public int steps = 0;
    public int lostNumbers = 0;
	public boolean started = false;
    
    public Level(LI settings) {
        this(settings, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public Level(LI settings, int width, int height) {
        this.width = width;
        this.height = height;
        this.settings = settings;
        createGenerators();
    }
    
    protected void createGenerators() {
    	generators = Collections.emptyMap();
    }

    public Unit generateUnit(int x, int y, Unit selectedUnit, Unit unit) {
    	Generator gen = null;
    	if (!generators.isEmpty()) {
    		gen = generators.get(new Generator.Id(x, y));
    	}
    	if (gen == null) {
    		gen = getDefaultGenerator(x, y);
    	}
        if (unit == null) {
            unit = new Unit();
        }
        return unit.init(gen.next(steps,
                selectedUnit == null ? unit.x : selectedUnit.x,
                selectedUnit == null ? unit.y : selectedUnit.y), x, y);
    }

    protected Generator getDefaultGenerator(int x, int y) {
    	 return RandomGenerator.DEFAUTL;
    }

    protected void addGoalToWin(AbstractGoal goalToWin) {
        this.goalsToWin.add(goalToWin);
    }

    protected void addGoalToLose(AbstractGoal goalToLose) {
        this.goalsToLose.add(goalToLose);
    }

    public boolean isLost(Unit[][] units, Unit selectedUnit, Array<Unit> neighbors) {
        for (AbstractGoal goal : goalsToLose) {
            if (goal.isReached(units, selectedUnit, neighbors)) {
                return true;
            }
        }
        return false;
    }

    public boolean isWon(Unit[][] units, Unit selectedUnit, Array<Unit> neighbors) {
        boolean reached = true;
        for (AbstractGoal goal : goalsToWin) {
            if (!goal.isReached(units, selectedUnit, neighbors)) {
                reached = false;
            }
        }
        return reached;
    }

    public void renderGoals(SpriteBatch batch, float goalPositionX, float goalPositionY ) {
        float goalPosX = goalPositionX;
		for (AbstractGoal goal : goalsToWin) {
			goal.render(batch, goalPosX, goalPositionY);
            goalPosX += (goal.dimension.x);
		}
        goalPosX = CameraHelper.INSTANCE.cameraGui.viewportWidth - goalPositionX;
		for (AbstractGoal goal : goalsToLose) {
            goalPosX -= (goal.dimension.x);
			goal.render(batch, goalPosX, goalPositionY);
		}
    }

    public void update(float deltaTime) {
        for (AbstractGoal goal : goalsToWin) {
            goal.update(deltaTime);
        }
        for (AbstractGoal goal : goalsToLose) {
            goal.update(deltaTime);
        }
    }

    public NewLevelResultsRequest formNewResults() {
        NewLevelResultsRequest results = new NewLevelResultsRequest();
        results.getResults().put(LevelResultName.TIME,
                new NewLevelResultRequest<Integer>((int) time));
        results.getResults().put(LevelResultName.STEPS,
                new NewLevelResultRequest<Integer>(steps));
        results.getResults().put(LevelResultName.LOST_UNITS,
                new NewLevelResultRequest<Integer>(lostNumbers));
        Gdx.app.debug(TAG, "Form new results");
        return results;
    }

    public void reset() {
        time = 0;
        steps = 0;
        lostNumbers = 0;
        started = false;
        for (Goal goal: goalsToWin) {
            goal.reset();
        }
        for (Goal goal: goalsToLose) {
            goal.reset();
        }
        for (Generator generator : generators.values()) {
            generator.reset();
        }
    }

}
