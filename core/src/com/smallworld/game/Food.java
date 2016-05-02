package com.smallworld.game;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Food {

    public boolean eaten = false;
    public Body body;
    public int nutritionalValue;

    public Food(GameWorld world) {
        Vector2 pos = new Vector2(Rand.rInt(1, (int)world.width - 1),
                                  Rand.rInt(1, (int)world.height - 1));
        if (pos.x < world.tide)
            nutritionalValue = 5;
        else
            nutritionalValue = 3;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(pos);
        this.body = world.physics.createBody(bodyDef);
        CircleShape circle = new CircleShape();
        circle.setRadius(0.2f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.isSensor = true;
        this.body.createFixture(fixtureDef).setUserData(this);
        circle.dispose();
    }

    public void render(ShapeRenderer renderer) {
        renderer.setColor(new Color(0.36f, 0.18f, 0.45f, 1f));
        renderer.circle(this.body.getPosition().x, this.body.getPosition().y, 0.5f);
    }
}
