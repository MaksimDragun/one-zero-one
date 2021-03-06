package org.dragberry.ozo.game.level.settings;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import org.dragberry.ozo.common.CommonConstants;
import org.dragberry.ozo.common.levelresult.LevelResultName;
import org.dragberry.ozo.common.levelresult.LevelResults;
import org.dragberry.ozo.common.levelresult.LevelSingleResult;
import org.dragberry.ozo.common.levelresult.NewLevelResultRequest;
import org.dragberry.ozo.common.levelresult.NewLevelResultResponse;
import org.dragberry.ozo.common.levelresult.NewLevelResultsRequest;
import org.dragberry.ozo.common.levelresult.NewLevelResultsResponse;
import org.dragberry.ozo.game.Assets;
import org.dragberry.ozo.game.DirectedGame;
import org.dragberry.ozo.game.level.Level;
import org.dragberry.ozo.game.util.StringConstants;

import java.text.MessageFormat;
import java.util.Map;

/**
 * Created by maksim on 01.02.17.
 */
public class LevelSettings {

	private static final String TAG = LevelSettings.class.getName();

	private static final Json JSON = new Json();

	private static final String COMPLETED = "completed";
	private static final String STATE = "state";

	public final Array<String> rules;

	public final Class<? extends Level<? extends LevelSettings>> clazz;
    public final String levelId;
    public final String name;
    
    public boolean completed;

	public final LevelResults results = new LevelResults();

    public LevelSettings(Class<? extends Level<? extends LevelSettings>> clazz, String levelId, String... ruleKeys) {
        this.clazz = clazz;
        this.levelId = levelId;
        this.name = Assets.instance.translation.get(levelId);
		this.rules = new Array<String>();
		for (String key : ruleKeys) {
			rules.add(Assets.instance.translation.get(key));
		}
        load();
    }

	public LevelSettings(Class<? extends Level<? extends LevelSettings>> clazz, String levelId) {
		this(clazz, levelId, new String[0]);
	}
    
    public void load() {
    	load(loadPreferences());
    }

	protected void load(Preferences prefs) {
		completed = prefs.getBoolean(COMPLETED, false);

		if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
			Gdx.app.debug(TAG, "Load results for " + levelId);
		}
		loadSingleResult(LevelResultName.TIME, prefs);
		loadSingleResult(LevelResultName.STEPS, prefs);
		loadSingleResult(LevelResultName.LOST_UNITS, prefs);
	}

	protected void loadSingleResult(LevelResultName name, Preferences prefs) {
		LevelSingleResult<Integer> result = new LevelSingleResult<Integer>();
		String personal = prefs.getString(name.personal());
		result.setPersonal(personal.isEmpty() ? null : Integer.valueOf(personal));
		String worlds = prefs.getString(name.worlds());
		result.setWorlds(worlds.isEmpty() ? null : Integer.valueOf(worlds));
		result.setOwner(prefs.getString(name.owner()));
		if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
			Gdx.app.debug(TAG, MessageFormat.format("loadSingleResult: {0}={1}", name, result));
		}
		results.getResults().put(name, result);
	}
	
	public void save() {
		Preferences prefs = loadPreferences();
		update(prefs);
		prefs.flush();
	}
    
	protected void update(Preferences prefs) {
		prefs.putBoolean(COMPLETED, completed);

		if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
			Gdx.app.debug(TAG, "update results for " + levelId);
		}
		updateSingleResult(LevelResultName.TIME, prefs);
		updateSingleResult(LevelResultName.STEPS, prefs);
		updateSingleResult(LevelResultName.LOST_UNITS, prefs);
	}

	protected void updateSingleResult(LevelResultName name, Preferences prefs) {
		LevelSingleResult<Integer> result = results.getResults().get(name);
		prefs.putString(name.personal(), result.getPersonal() == null ? StringConstants.EMPTY : result.getPersonal().toString());
		prefs.putString(name.worlds(), result.getWorlds() == null ? StringConstants.EMPTY : result.getWorlds().toString());
		prefs.putString(name.owner(), result.getOwner() == null ? StringConstants.EMPTY : result.getOwner());
		if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
			Gdx.app.debug(TAG, MessageFormat.format("updateSingleResult: {0}={1}", name, result));
		}
	}

	protected Preferences loadPreferences() {
		return Gdx.app.getPreferences(levelId);
	}
	
	/**
	 * Updates results after sending a request to the server after completing the level
	 * @param newResults
     */
	public void updateResults(NewLevelResultsResponse newResults) {
		for (Map.Entry<LevelResultName, NewLevelResultResponse<Integer>> entry : newResults.getResults().entrySet()) {
			if (entry.getValue().isPersonal()) {
				results.getResults().get(entry.getKey()).setPersonal(entry.getValue().getValue());
				if (entry.getValue().isWorlds()) {
					results.getResults().get(entry.getKey()).setWorlds(entry.getValue().getValue());
					results.getResults().get(entry.getKey()).setOwner(newResults.getUserName());
				}
			}
		}
		save();
	}

	public NewLevelResultsResponse checkLocalResults(NewLevelResultsRequest newResults) {
		NewLevelResultsResponse response = new NewLevelResultsResponse();
		response.setUserName(DirectedGame.game.platform.getUser().getName());
		response.setUserId(DirectedGame.game.platform.getUser().getId());
		response.setLevelId(newResults.getLevelId());
		checkSingleLocalResult(newResults, response, LevelResultName.TIME);
		checkSingleLocalResult(newResults, response, LevelResultName.STEPS);
		checkSingleLocalResult(newResults, response, LevelResultName.LOST_UNITS);
		return response;
	}

	protected void checkSingleLocalResult(
			NewLevelResultsRequest newResults, NewLevelResultsResponse response, LevelResultName name) {
		LevelSingleResult<Integer> result = results.getResults().get(name);
		NewLevelResultRequest<Integer> newResult = newResults.getResults().get(name);
		NewLevelResultResponse<Integer> resultResponse = new NewLevelResultResponse<Integer>();

		if (result.getWorlds() == null && result.getPersonal() == null) {
			resultResponse.setWorlds(true);
			resultResponse.setPersonal(true);
			resultResponse.setValue(newResult.getValue());
			response.getResults().put(name, resultResponse);
		} else if (result.getWorlds() != null && name.isRecordBeaten(result.getWorlds(), newResult.getValue())) {
			resultResponse.setWorlds(true);
			resultResponse.setPersonal(true);
			resultResponse.setValue(newResult.getValue());
			response.getResults().put(name, resultResponse);
		} else if (result.getPersonal() == null || name.isRecordBeaten(result.getPersonal(), newResult.getValue())) {
			resultResponse.setWorlds(false);
			resultResponse.setPersonal(true);
			resultResponse.setValue(newResult.getValue());
			response.getResults().put(name, resultResponse);
		}
	}

	public synchronized void refreshResultsWithOwner() {
		for (LevelSingleResult<Integer> result : results.getResults().values()) {
			if (CommonConstants.DEFAULT_USER_NAME.equals(result.getOwner())) {
				result.setOwner(DirectedGame.game.platform.getUser().getName());
			}
		}
		save();
	}

	protected interface ResultComparator {

		boolean isRecordBeaten(Integer newValue, Integer value);

	}

	protected static final ResultComparator LESS_RESULT_COMPORATOR = new ResultComparator() {

		@Override
		public boolean isRecordBeaten(Integer newValue, Integer value) {
			return newValue < value;
		}
	};

	protected static final ResultComparator GREATER_RESULT_COMPORATOR = new ResultComparator() {

		@Override
		public boolean isRecordBeaten(Integer newValue, Integer value) {
			return newValue > value;
		}
	};

	/**
	 * Updates level results by new one. Called when levels are loaded from the server first time
	 *
	 * @param newResults
     */
	public NewLevelResultsRequest updateResults(LevelResults newResults) {
		NewLevelResultsRequest newResultsRequest = new NewLevelResultsRequest();
		newResultsRequest.setLevelId(levelId);

		for (Map.Entry<LevelResultName, LevelSingleResult<Integer>> entry : newResults.getResults().entrySet()) {
			LevelResultName resultName = entry.getKey();
			LevelSingleResult<Integer> newResult = entry.getValue();
			LevelSingleResult<Integer> result = results.getResults().get(resultName);
			Gdx.app.log(TAG, MessageFormat.format("Level [{0}]: result from server: {1}", levelId, newResult));

			if (result.getWorlds() == null || (newResult.getPersonal() != null && result.getWorlds() > newResult.getWorlds())) {
				result.setWorlds(newResult.getWorlds());
				result.setOwner(newResult.getOwner());
				Gdx.app.log(TAG, MessageFormat.format("Level [{0}]: new worlds record has been received: {1} = {2}",
						levelId, resultName, result.getWorlds()));
			}

			if (result.getPersonal() == null || (newResult.getPersonal() != null && result.getPersonal() >= newResult.getPersonal())) {
				Gdx.app.log(TAG, MessageFormat.format("Level [{0}]: personal record is not beaten", levelId));
				result.setPersonal(newResult.getPersonal());
			} else {
				Gdx.app.log(TAG, MessageFormat.format("Level [{0}]: personal record is beaten: {1}: old = {2}, new = {3}",
						levelId, resultName, newResult.getPersonal(), result.getPersonal()));
				newResultsRequest.getResults().put(resultName, new NewLevelResultRequest<Integer>(result.getPersonal()));

				if (result.getPersonal() != null && result.getWorlds() != null && result.getPersonal() < result.getWorlds()) {
					Gdx.app.log(TAG, MessageFormat.format("Level [{0}]: world record is beaten: {1}: old = {2}, new = {3}",
							levelId, resultName, result.getWorlds(), result.getPersonal()));
					result.setWorlds(result.getPersonal());
					result.setOwner(newResult.getOwner());
				}
			}
		}
		return newResultsRequest;
	}

	public Level<? extends LevelSettings> loadLevelState() {
		if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
			Gdx.app.debug(TAG, "Trying load incomplete level: " + levelId);
		}
		Preferences prefs = loadLevelStatePrefs();
		String stateStr = prefs.getString(STATE);
		if (stateStr.isEmpty()) {
			return null;
		}
		try {
			return JSON.fromJson(clazz, stateStr);
		} catch (Exception exc) {
			Gdx.app.error(TAG, "Unable to load level state!", exc);
		}
		return null;
	}

	private Preferences loadLevelStatePrefs() {
		return Gdx.app.getPreferences(levelId + ".state");
	}

	public void saveState(Level<? extends LevelSettings> level, boolean shouldSave) {
		Preferences prefs = loadLevelStatePrefs();
		prefs.putString(STATE, shouldSave ? JSON.toJson(level) : StringConstants.EMPTY);
		prefs.flush();
	}

	@Override
	public String toString() {
		return "Level Settings: " + levelId;
	}
}
