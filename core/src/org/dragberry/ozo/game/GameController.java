package org.dragberry.ozo.game;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector3;

import org.dragberry.ozo.common.audit.AuditEventType;
import org.dragberry.ozo.common.audit.LevelAttemptAuditEventRequest;
import org.dragberry.ozo.common.audit.LevelAttemptStatus;
import org.dragberry.ozo.common.levelresult.NewLevelResultsRequest;
import org.dragberry.ozo.common.levelresult.NewLevelResultsResponse;
import org.dragberry.ozo.game.level.Level;
import org.dragberry.ozo.game.objects.Unit;
import org.dragberry.ozo.game.util.CameraHelper;
import org.dragberry.ozo.http.HttpClient;
import org.dragberry.ozo.http.PostHttpTask;
import org.dragberry.ozo.screen.popup.DefeatPopup;
import org.dragberry.ozo.screen.popup.PausePopup;
import org.dragberry.ozo.screen.popup.VictoryPopup;

import java.text.MessageFormat;

public class GameController extends InputAdapter {

	private static final String TAG = GameController.class.getName();

	public Level<?> level;

	public LevelAttemptAuditEventRequest attempt;

	public static GameController instance;
	static {
		instance = new GameController();
		instance.attempt = new LevelAttemptAuditEventRequest();
		instance.attempt.setUserId(DirectedGame.game.platform.getUser().getId());
	}

	public GameController init(Level<?> level, boolean restore) {
		this.level = level;
		this.level.reset(restore);
		return this;
	}

	public void update(float deltaTime) {
		level.update(deltaTime);
    	handleDebugInput(deltaTime);
		CameraHelper.INSTANCE.update(deltaTime);
    }

	private void populateLevelAttempt(LevelAttemptStatus status) {
		attempt.setType(AuditEventType.FINISH_LEVEL);
		attempt.setLevelId(level.settings.levelId);
		attempt.setLostUnits(level.lostNumbers);
		attempt.setTime((int) level.time);
		attempt.setSteps(level.steps);
		attempt.setStatus(status);
	}

	public void onGameLost(Level<?> level) {
		level.started = false;
		DirectedGame.game.setPopup(DirectedGame.game.getScreen(DefeatPopup.class));
		populateLevelAttempt(LevelAttemptStatus.FAILED);
		DirectedGame.game.logAuditEvent(attempt);
	}

	public void onGameWon(final Level<?> level) {
		level.started = false;

		NewLevelResultsRequest newResults = level.formNewResults();
		newResults.setLevelId(level.settings.levelId);
		newResults.setUserId(DirectedGame.game.platform.getUser().getId());
		Gdx.app.debug(TAG, "New results have formed:\n" + newResults);

		NewLevelResultsResponse response = level.settings.checkLocalResults(newResults);
		level.settings.completed = true;
		level.settings.updateResults(response);

		DirectedGame.game.platform.getHttpClient().executeTask(
				new PostHttpTask<NewLevelResultsRequest, NewLevelResultsResponse>(
						newResults, NewLevelResultsResponse.class, HttpClient.URL.NEW_RESULT) {

					@Override
					public void onComplete(NewLevelResultsResponse result) {
						level.settings.updateResults(result);
					}
				});

		DirectedGame.game.setPopup(DirectedGame.game.getScreen(VictoryPopup.class).init(response));

		populateLevelAttempt(LevelAttemptStatus.SUCCESS);
		DirectedGame.game.logAuditEvent(attempt);
	}

    private void onScreenTouch(float xCoord, float yCoord) {
		if (level.inStepMotion()) {
			// level is in motion
			return;
		}
		Unit currentSelectedUnit = level.selectUnit(xCoord, yCoord);
		if (currentSelectedUnit == null) {
			// unit is border unit
			level.deselectAllUnits();
			return;
		}
		if (level.isUnitSelectedAgain(currentSelectedUnit)) {
			level.startStepMotion();
			return;
		}
		if (level.selectedUnit != null) {
			level.deselectAllUnits();
		} else {
			level.processFirstSection(currentSelectedUnit);
		}
    }

	private boolean isBorderUnit(Unit selectedUnit) {
		return selectedUnit.x == 0 || selectedUnit.x == level.width - 1
				|| selectedUnit.y == 0 || selectedUnit.y == level.height - 1;
	}
	
	private void handleDebugInput(float deltaTime) {
		if (Gdx.app.getType() != ApplicationType.Desktop) {
			return;
		}

		float camMoveSpeed = 100 * deltaTime;
		float camMoveSpeedAccelerationFactor = 100;

		if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
			camMoveSpeed *= camMoveSpeedAccelerationFactor;
		}
		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
			moveCamera(-camMoveSpeed, 0);
		}
		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			moveCamera(camMoveSpeed, 0);
		}
		if (Gdx.input.isKeyPressed(Keys.UP)) {
			moveCamera(0, camMoveSpeed);
		}
		if (Gdx.input.isKeyPressed(Keys.DOWN)) {
			moveCamera(0, -camMoveSpeed);
		}
		if (Gdx.input.isKeyPressed(Keys.BACKSPACE)) {
			CameraHelper.INSTANCE.setPosition(0, 0);
		}

		float camZoomSpeed = 1 * deltaTime;
		float camZoomSpeedAccelerationfactor = 5;

		if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
			camZoomSpeed *= camZoomSpeedAccelerationfactor;
		}
		if (Gdx.input.isKeyPressed(Keys.COMMA)) {
			CameraHelper.INSTANCE.addZoom(camZoomSpeed);
			Gdx.app.log(TAG, MessageFormat.format("Zoom={0}", CameraHelper.INSTANCE.getZoom()));
		}
		if (Gdx.input.isKeyPressed(Keys.PERIOD)) {
			CameraHelper.INSTANCE.addZoom(-camZoomSpeed);
			Gdx.app.log(TAG, MessageFormat.format("Zoom={0}", CameraHelper.INSTANCE.getZoom()));
		}
		if (Gdx.input.isKeyPressed(Keys.SLASH)) {
			CameraHelper.INSTANCE.setZoom(1);
		}

	}

    private void moveCamera(float x, float y) {
    	x += CameraHelper.INSTANCE.getPosition().x;
    	y += CameraHelper.INSTANCE.getPosition().y;
		CameraHelper.INSTANCE.setPosition(x, y);
    }

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		Vector3 touchCoord = CameraHelper.INSTANCE.camera.unproject(new Vector3(screenX, screenY, 0));
		onScreenTouch(touchCoord.x, touchCoord.y);
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
			case Input.Keys.BACK:
			case Input.Keys.ESCAPE:
				populateLevelAttempt(LevelAttemptStatus.INTERRUPTED);
				DirectedGame.game.setPopup(DirectedGame.game.getScreen(PausePopup.class).init(level, attempt));
				break;
		}
		return false;
	}
}
