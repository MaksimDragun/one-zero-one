package org.dragberry.ozo.game.objects;

import org.dragberry.ozo.game.Assets;
import org.dragberry.ozo.game.util.Constants;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;

public class Unit extends AbstractUnit {

	public enum Direction {
		NORTH, SOUTH, EAST, WEST
	}
	
	private static final float UNIT_INITIAL_SCALE = 0.01f;
	private static final float UNIT_UNSELECTED_SCALE = 0.8f;
	private static final float UNIT_SELECTED_SCALE = 1.0f;

	public int x;
	public int y;
	public int previousValue;
	
	private boolean selected;
	private boolean selectedNeighbor;
	
	public enum State {
		FIXED, GROW_UP, GROW_DOWN, INITIAL
	}
	private State state;
	private float time;
	private static final float GROWING_TIME = 0.2f;
	
	public Unit() {
		this(0, 0, 0);
	}
	
	@Override
	public void update(float deltaTime) {
		switch (state) {
			case INITIAL:
				time += deltaTime;
				scale.x += deltaTime * 2;
				scale.y += deltaTime * 2;
				if (scale.x >= UNIT_UNSELECTED_SCALE) {
					time = 0;
					state = State.FIXED;
					scale.x = UNIT_UNSELECTED_SCALE;
					scale.y = UNIT_UNSELECTED_SCALE;
					renderValue = true;
				}
				break;
			case GROW_UP:
				time += deltaTime;
				scale.x += deltaTime;
				scale.y += deltaTime;
				if (time >= GROWING_TIME || scale.x >= UNIT_SELECTED_SCALE) {
					time = 0;
					state = State.FIXED;
					scale.x = UNIT_SELECTED_SCALE;
					scale.y = UNIT_SELECTED_SCALE;
				}
				break;
			case GROW_DOWN:
				time += deltaTime;
				scale.x -= deltaTime;
				scale.y -= deltaTime;
				if (time >= GROWING_TIME || scale.x <= UNIT_UNSELECTED_SCALE) {
					time = 0;
					state = State.FIXED;
					scale.x = UNIT_UNSELECTED_SCALE;
					scale.y = UNIT_UNSELECTED_SCALE;
				}
				break;
			default:
				break;
		}
	}
	
	public Unit(int value, int x, int y) {
		super(value);
		this.previousValue = value;
		this.dimension = new Vector2(Constants.UNIT_SIZE, Constants.UNIT_SIZE);
		this.x = x;
		this.y = y;
		origin.x = dimension.x / 2;
		origin.y = dimension.y / 2;
		scale.x = UNIT_INITIAL_SCALE;
		scale.y = UNIT_INITIAL_SCALE;
		time = 0;
		state = State.INITIAL;
		renderValue = false;
		init();
	}
	
	@Override
	protected void init() {
		position = new Vector2(x * Constants.UNIT_SIZE, y * Constants.UNIT_SIZE);
		bounds.set(position.x, position.y, dimension.x, dimension.y);
	}
	
	public void moveTo(Direction direction, float step) {
		float border;
		switch (direction) {
		case SOUTH:
			border = (y - 1) * Constants.UNIT_SIZE;
			position.y -= step;
			if (position.y < border) {
				position.y = border;
			}
			break;
		case NORTH:
			border = (y + 1) * Constants.UNIT_SIZE;
			position.y += step;
			if (position.y > border) {
				position.y = border;
			}
			break;
		case WEST:
			border = (x - 1) * Constants.UNIT_SIZE;
			position.x -= step;
			if (position.x < border) {
				position.x = border;
			}
			break;
		case EAST:
			border = (x + 1) * Constants.UNIT_SIZE;
			position.x += step;
			if (position.x > border) {
				position.x = border;
			}
			break;
		default:
			throw new IllegalArgumentException();
		}
	}
	
	public void moveTo(int gameX, int gameY) {
		this.x = gameX;
		this.y = gameY;
		init();
	}
	
	@Override
	protected BitmapFont getFont() {
		return selected || selectedNeighbor ? Assets.instance.fonts.game_l : Assets.instance.fonts.game_m;
	}
	
	@Override
	protected float getFontX(GlyphLayout layout) {
		return position.x + (dimension.x - layout.width) * 0.4f;
	}

	@Override
	protected float getFontY(GlyphLayout layout) {
		return position.y + dimension.y / 2 + layout.height / 2;
	}
	
	@Override
	public String toString() {
		return "Unit[" + x + "][" + y + "]=" + value;
	}

	public boolean isSelected() {
		return selected;
	}

	public void select() {
		selected = true;
		state = State.GROW_UP;
		time = 0;
	}
	
	public void unselect() {
		selected = false;
		state = State.GROW_DOWN;
		time = 0;
	}

	public boolean isSelectedNeighbor() {
		return selectedNeighbor;
	}

	public void selectedNeighbor() {
		selectedNeighbor = true;
		state = State.GROW_UP;
		time = 0;
		
	}
	
	public void unselectedNeighbor() {
		selectedNeighbor = false;
		state = State.GROW_DOWN;
		time = 0;
	}
	
}
