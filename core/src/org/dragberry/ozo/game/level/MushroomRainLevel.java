package org.dragberry.ozo.game.level;

import org.dragberry.ozo.game.level.generator.ConstGenerator;
import org.dragberry.ozo.game.level.generator.Generator;
import org.dragberry.ozo.game.level.settings.ReachTheGoalLevelSettings;

import java.util.HashMap;

/**
 * Created by maksim on 31.01.17.
 */

public class MushroomRainLevel extends ReachTheGoalLevel {

    public MushroomRainLevel(ReachTheGoalLevelSettings levelInfo) {
        super(levelInfo);
    }

    @Override
    protected Generator getDefaultGenerator(int x, int y) {
    	if (x == 0 || x == width - 1) {
            return ConstGenerator.ZERO;
        }
    	if (y == 0) {
            return ConstGenerator.POS_ONE;
        }
        if (y == height - 1) {
            return ConstGenerator.NEG_ONE;
        }
        return super.getDefaultGenerator(x, y);
    }

    @Override
    protected void createGenerators() {
        generators = new HashMap<Generator.Id, Generator>();
        generators.put(new Generator.Id(0, height - 1), ConstGenerator.NEG_ONE);
        generators.put(new Generator.Id(0, height - 2), ConstGenerator.NEG_ONE);
        generators.put(new Generator.Id(width - 1, height - 1), ConstGenerator.NEG_ONE);
        generators.put(new Generator.Id(width - 1, height - 2), ConstGenerator.NEG_ONE);
    }
}
