package com.smallworld.game.screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.smallworld.game.SmallWorld;

public class MainMenuScreen implements Screen {

    private final SmallWorld game;
    private OrthographicCamera camera;

    public MainMenuScreen(final SmallWorld game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 800, 480);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        this.camera.update();
        this.game.batch.setProjectionMatrix(camera.combined);

        this.game.batch.begin();
        this.game.font.draw(this.game.batch, "Welcome to SmallWorld !", 100, 150);
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