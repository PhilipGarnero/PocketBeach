package com.smallworld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.smallworld.game.phenotypes.Features;
import com.smallworld.game.screens.GameScreen;

import java.util.ArrayList;
import java.util.Iterator;


public class GameWorld implements ContactListener {
    public GameScreen screen;
    public World physics;
    public final float width;
    private final float TIDE_ORIGINAL_POS = 0.6f;
    public final float height;
    public float tide;
    public float time = 0f;
    private boolean paused = false;
    private Experiment experiment;
    public SpriteBatch batch = new SpriteBatch();
    private Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    public ShapeRenderer shapeRenderer = new ShapeRenderer();
    private SeaShader seaShader;
    public ActorShader actorShader;
    private ArrayList<Food> food = new ArrayList<Food>();

    public GameWorld(final int w, final int h, GameScreen screen) {
        this.width = w;
        this.height = h;
        this.tide = this.TIDE_ORIGINAL_POS * w;
        this.physics = new World(new Vector2(0, 0), true);
        this.physics.setContactListener(this);
        this.createWorldBoundaries();
        this.experiment = new Experiment(this);
        this.experiment.start();
        this.screen = screen;
        this.seaShader = new SeaShader(this);
        this.actorShader = new ActorShader(this);
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
        this.shapeRenderer.dispose();
        this.seaShader.dispose();
        this.actorShader.dispose();
    }

    private void updateTide() {
        this.tide = this.TIDE_ORIGINAL_POS * this.width + (float)Math.sin(this.time / 2) * 5;
    }

    private void regulateFood() {
        Iterator<Food> iterator = this.food.iterator();
        while (iterator.hasNext()) {
            Food f = iterator.next();
            if (f.eaten) {
                this.physics.destroyBody(f.body);
                iterator.remove();
            }
        }
        int i = 0;
        if (this.time % 10 != 0)
            i = Rand.rInt(0, 10);
        while (i-- > 0 && this.food.size() < 50)
            this.food.add(new Food(this));
    }

    public void render() {
        if (!this.paused) {
            this.time = this.time + Gdx.graphics.getDeltaTime();
            this.updateTide();
            this.batch.setProjectionMatrix(this.screen.camera.combined);
            this.screen.textures.get("sand").bind();
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0); // needed because binding the texture later for the seaShader messes up the indexes
            this.batch.begin();
            for (int i = 0; i < this.height; i += 5) {
                for (int j = 0; j < this.width; j += 5) {
                    this.batch.draw(this.screen.textures.get("sand"), j, i, 5, 5);
                }
            }
            this.screen.textures.get("actor").bind();
            for (final Food food : this.food)
                food.render(this.batch, this.screen.textures.get("food"));
            this.batch.end();
            this.actorShader.begin();
            this.experiment.update();
            this.actorShader.end();
            this.seaShader.render();
            this.debugRenderer.render(this.physics, this.screen.camera.combined);
            this.physics.step(1 / 60f, 6, 2);
            this.regulateFood();
            long currentTime = System.nanoTime() / 1000000000;
            if (this.experiment.TIME_BETWEEN_GEN != 0 && currentTime - this.experiment.nextGen > this.experiment.TIME_BETWEEN_GEN)
                this.experiment.nextGeneration();
        }
    }

    @Override
    public void beginContact (Contact contact) {
        // make sure only one of the fixtures is a sensor
        if (contact.getFixtureA().getUserData() instanceof Features.Sensor ^
                contact.getFixtureB().getUserData() instanceof Features.Sensor) {
            this.updateActorSensor(contact, 1);
        } else if (contact.getFixtureA().getUserData() instanceof Food &&
                contact.getFixtureB().getBody().getUserData() instanceof Actor) {
            ((Actor)contact.getFixtureB().getBody().getUserData()).eat((Food) contact.getFixtureA().getUserData());
        } else if (contact.getFixtureA().getBody().getUserData() instanceof Actor &&
                contact.getFixtureB().getUserData() instanceof Food) {
            ((Actor)contact.getFixtureA().getBody().getUserData()).eat((Food)contact.getFixtureB().getUserData());
        }
    }

    private void updateActorSensor(Contact contact, float sensorValue) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        if (fixtureA.getUserData() instanceof Food || fixtureB.getUserData() instanceof Food)
            sensorValue = -1f;
        if (fixtureA.isSensor())
            ((Features.Sensor)fixtureA.getUserData()).setValue(sensorValue);
        else
            ((Features.Sensor)fixtureB.getUserData()).setValue(sensorValue);
    }

    @Override
    public void endContact(Contact contact) {
         // make sure only one of the fixtures is a sensor
        if (contact.getFixtureA().getUserData() instanceof Features.Sensor ^
                contact.getFixtureB().getUserData() instanceof Features.Sensor) {
            this.updateActorSensor(contact, 0);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}