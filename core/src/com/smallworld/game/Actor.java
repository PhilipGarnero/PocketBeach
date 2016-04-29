package com.smallworld.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.smallworld.game.phenotypes.Brain;

public class Actor {
    public GameWorld world;
    public int id;
    public Genotype genotype;
    public Body body;
    boolean alive;
    private Brain brain;
    private Experiment.FitnessEvaluation fitnessEvaluation;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    public MouseJoint mouseJoint = null;

    public Actor(GameWorld world, int id, Experiment.FitnessEvaluation fitnessEvaluation, Genotype genotype, Vector2 position) {
        this.world = world;
        this.id = id;
        this.fitnessEvaluation = fitnessEvaluation;
        this.alive = true;
        if (genotype == null)
            this.genotype = new Genotype(null);
        else
            this.genotype = genotype;

        this.createBody(position);
        this.buildPhenotype();
    }

    private void createBody(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        this.body = this.world.physics.createBody(bodyDef);
        this.body.setUserData(this);
    }

    public void dispose() {
        if (this.mouseJoint != null) {
            this.world.physics.destroyJoint(this.mouseJoint);
            this.world.screen.inputs.mouseJoint = null;
        }
        this.world.physics.destroyBody(this.body);
        this.shapeRenderer.dispose();

    }

    public float getFitness() {
        return this.fitnessEvaluation.evaluate(this);
    }

    public boolean isDead() {
        return !this.alive;
    }

    public void render() {
        this.shapeRenderer.setProjectionMatrix(this.world.screen.camera.combined);
        this.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (this.isDead())
            this.shapeRenderer.setColor(1, 0, 0, 1);
        else
            this.shapeRenderer.setColor(1, 1, 0, 1);
        this.shapeRenderer.circle(this.body.getPosition().x, this.body.getPosition().y, 1);
        this.shapeRenderer.end();
    }

    public void buildPhenotype() {
        this.brain = (Brain)this.genotype.getPhenotype("brain");
        if (!this.brain.isViable())
            this.alive = false;

        CircleShape circle = new CircleShape();
        circle.setRadius(1f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.5f;
        fixtureDef.filter.groupIndex = -1;
        this.body.createFixture(fixtureDef);
        circle.dispose();
        //this.properties = this.genotype.getPhenotype("properties");
        //this.body.createFixture(this.genotype.getPhenotype("body").fixture);
    }

    public Vector2 directionToWorldPoint() {
        return this.world.point.cpy().sub(this.body.getPosition()).nor();
    }

    public float distanceToWorldPoint() {
        return this.world.point.cpy().sub(this.body.getPosition()).len() / (float)Math.sqrt(Math.pow(this.world.width, 2) + Math.pow(this.world.height, 2));
    }

    public boolean inWater() {
        return this.body.getPosition().x > this.world.tide;
    }

    public void update() {
        if (!this.isDead())
            this.act();
        this.render();
    }

    public void act() {
        Vector2 dir = this.directionToWorldPoint();
        float dis = this.distanceToWorldPoint();
        float water = this.inWater() ? 1 : 0;
        float[] inputs = {dir.x, dir.y, dis, water};
        float[] outputs = this.brain.think(inputs, 2);
        this.move(dir.set(outputs[0], outputs[1]));
    }

    public void move(Vector2 dir) {
        if (this.inWater())
            this.body.applyForceToCenter(dir.x, dir.y, true);
        else {
            this.body.setLinearVelocity(dir);
        }
    }
}
