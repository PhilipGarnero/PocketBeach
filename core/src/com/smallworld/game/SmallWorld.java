package com.smallworld.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.smallworld.game.screens.MainMenuScreen;

public class SmallWorld extends Game {
	public SpriteBatch batch;
	public BitmapFont font;

	public void create() {
		this.batch = new SpriteBatch();
		this.font = new BitmapFont();
		this.setScreen(new MainMenuScreen(this));
	}

	public void render() {
		super.render();
	}

	public void dispose() {
		this.batch.dispose();
		this.font.dispose();
	}
}
