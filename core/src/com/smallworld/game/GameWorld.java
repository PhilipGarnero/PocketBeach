package com.smallworld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.World;
import com.smallworld.game.screens.GameScreen;

import java.util.HashMap;


public class GameWorld {
    public World physics;
    public final float width;
    public float tide;
    public final float height;
    public float time = 0f;
    private Experiment experiment;
    private boolean paused = false;
    private SpriteBatch batch = new SpriteBatch();
    private Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    public Vector2 point;
    public GameScreen screen;
    private Sea sea;

    public GameWorld(final int w, final int h, GameScreen screen) {
        this.width = w;
        this.height = h;
        this.tide = 2 * w / 3;
        this.physics = new World(new Vector2(0, 0), true);
        this.createWorldBoundaries();
        this.experiment = new Experiment(this);
        this.experiment.start();
        this.point = new Vector2(w/2f, h/2f);
        this.screen = screen;
        this.sea = new Sea(this);
    }

    private void createWorldBoundaries() {
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(new Vector2(0, 0));
        Body groundBody = this.physics.createBody(groundBodyDef);
        ChainShape shape = new ChainShape();
        Vector2[] boundaries = {new Vector2(0, 0), new Vector2(width, 0), new Vector2(width, height),
                                new Vector2(0, height), new Vector2(0, 0)};
        shape.createChain(boundaries);
        groundBody.createFixture(shape, 0.0f);
        shape.dispose();
    }

    public void pause() {
        this.paused = true;
    }

    public void resume() {
        this.paused = false;
    }

    public void destroy() {
        this.experiment.stop();
        this.physics.dispose();
        this.batch.dispose();
        this.sea.dispose();
    }

    private void updateTide() {
        this.tide = 2 * this.width / 3 + (float)Math.sin(this.time / 2) * 5;
    }

    public void render(HashMap<String, Texture> textures) {
        if (!this.paused) {
            this.time = (this.time + Gdx.graphics.getDeltaTime()) % 101f;
            this.updateTide();
            this.batch.setProjectionMatrix(this.screen.camera.combined);
            this.batch.begin();
            for (int i = 0; i < this.height; i += 5) {
                for (int j = 0; j < this.width; j += 5) {
                    this.batch.draw(textures.get("sand"), j, i, 5, 5);
                }
            }
            this.batch.end();
            //this.debugRenderer.render(this.physics, this.screen.camera.combined);

            this.experiment.update();
            this.sea.render();
            this.physics.step(1 / 60f, 6, 2);
            long currentTime = System.nanoTime() / 1000000000;
            if (this.experiment.TIME_BETWEEN_GEN != 0 && currentTime - this.experiment.nextGen > this.experiment.TIME_BETWEEN_GEN)
                this.experiment.nextGeneration();
        }
    }
}