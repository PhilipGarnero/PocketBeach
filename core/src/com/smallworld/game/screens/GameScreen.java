package com.smallworld.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.smallworld.game.GameInputs;
import com.smallworld.game.GameWorld;
import com.smallworld.game.SmallWorld;

import java.util.HashMap;

public class GameScreen implements Screen {
    final SmallWorld game;
    public final static float CAMERA_VIEWPOINT = 100f;
    public final static int WORLD_WIDTH = 100;
    public final static int WORLD_HEIGHT = 55;
    public OrthographicCamera camera;
    public GameWorld gameWorld;
    public HashMap<String, Texture> textures = new HashMap<String, Texture>();
    public GameInputs inputs;

    public GameScreen(final SmallWorld game) {
        this.game = game;
        this.loadTextures();
        this.setCamera();
        this.gameWorld = new GameWorld(WORLD_WIDTH, WORLD_HEIGHT, this);
        this.enableInputs();
    }

    private void loadTextures() {
        this.textures.put("sand", new Texture(Gdx.files.internal("beach-sand.png")));
        this.textures.put("water-normals", new Texture(Gdx.files.internal("water-normals.png")));
        this.textures.get("water-normals").setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        this.textures.put("actor", new Texture(Gdx.files.internal("actor.png")));
        this.textures.put("food", new Texture(Gdx.files.internal("food.png")));
        this.textures.put("cloud", new Texture(Gdx.files.internal("cloud.png")));
    }

    private void setCamera() {
        float aspectRatio = (float)Gdx.graphics.getHeight() / (float)Gdx.graphics.getWidth();
        this.camera = new OrthographicCamera(CAMERA_VIEWPOINT, CAMERA_VIEWPOINT  * aspectRatio);
        this.camera.position.set(this.camera.viewportWidth / 2f, this.camera.viewportHeight / 2f, 0);
        this.camera.setToOrtho(true);
        this.camera.update();
    }

    private void enableInputs() {
        this.inputs = new GameInputs(this);
        InputMultiplexer im = new InputMultiplexer();
        GestureDetector gd = new GestureDetector(this.inputs);
        im.addProcessor(this.inputs);
        im.addProcessor(gd);
        Gdx.input.setInputProcessor(im);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        this.gameWorld.update();
    }

    @Override
    public void dispose() {
        for (Texture t : this.textures.values())
            t.dispose();
        this.gameWorld.destroy();
    }

    @Override
    public void hide() {

    }

    @Override
    public void pause() {
        this.gameWorld.pause();
    }

    @Override
    public void resume() {
        this.gameWorld.resume();
    }

    @Override
    public void resize(int width, int height) {
        float aspectRatio = (float)height / (float)width;
        this.camera.viewportWidth = CAMERA_VIEWPOINT;
        this.camera.viewportHeight = CAMERA_VIEWPOINT * aspectRatio;
        this.clampCamera();
        this.camera.update();
    }

    public void clampCamera() {
        this.camera.zoom = MathUtils.clamp(this.camera.zoom, 0.1f, WORLD_WIDTH / this.camera.viewportWidth);
        this.camera.zoom = MathUtils.clamp(this.camera.zoom, 0.1f, WORLD_HEIGHT / this.camera.viewportHeight);
        float effectiveViewportWidth = this.camera.viewportWidth * this.camera.zoom;
        float effectiveViewportHeight = this.camera.viewportHeight * this.camera.zoom;
        this.camera.position.x = MathUtils.clamp(this.camera.position.x, effectiveViewportWidth / 2f, WORLD_WIDTH - effectiveViewportWidth / 2f);
        this.camera.position.y = MathUtils.clamp(this.camera.position.y, effectiveViewportHeight / 2f, WORLD_HEIGHT - effectiveViewportHeight / 2f);
    }
}