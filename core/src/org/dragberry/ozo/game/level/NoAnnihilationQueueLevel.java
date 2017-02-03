package org.dragberry.ozo.game.level;

import org.dragberry.ozo.game.level.generator.Generator;
import org.dragberry.ozo.game.level.generator.QueueGeneratorHelper;

public class NoAnnihilationQueueLevel extends NoAnnihilationLevel {

	public NoAnnihilationQueueLevel(String levelName, Integer goalToLose, Integer goalToWin) {
		super(levelName, goalToLose, goalToWin);
	}
	
	@Override
	protected void createGenerators() {
		generators = QueueGeneratorHelper.createGenerators(width, height);
	}
	
	@Override
	protected Generator getDefaultGenerator(int x, int y) {
		return QueueGeneratorHelper.getDefaultGenerator(x, y);
	}
}
