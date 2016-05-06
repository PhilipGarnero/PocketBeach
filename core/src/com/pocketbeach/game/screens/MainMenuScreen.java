package com.pocketbeach.game.screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.pocketbeach.game.PocketBeach;

import java.util.HashMap;

public class MainMenuScreen implements Screen {

    private final com.pocketbeach.game.PocketBeach game;
    private OrthographicCamera camera;
    private HashMap<String, Texture> textures = new HashMap<String, Texture>();

    public MainMenuScreen(final PocketBeach game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 1280, 800);
        this.loadTextures();
    }

    private void loadTextures() {
        this.textures.put("background", new Texture(Gdx.files.internal("homebackground.png")));
    }

    @Override
    public void render(float delta) {

        this.camera.update();
        this.game.batch.setProjectionMatrix(camera.combined);

        this.game.batch.begin();
        this.game.batch.draw(this.textures.get("background"), 1, 1);
        this.game.font.draw(this.game.batch, "Welcome to PocketBeach !", 100, 150);
        this.game.font.draw(this.game.batch, "Tap anywhere to begin !", 100, 100);
        this.game.batch.end();

        if (Gdx.input.isTouched()) {
            this.game.setScreen(new GameScreen(this.game));
            this.dispose();
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void resize(int width, int height) {
    }
}