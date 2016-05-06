package com.pocketbeach.game;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Food {
    public boolean eaten = false;
    public Body body;
    private GameWorld world;

    public Food(GameWorld world) {
        this.world = world;
        Vector2 pos = new Vector2(com.pocketbeach.game.Rand.rInt(1, (int) world.width - 1),
                                  com.pocketbeach.game.Rand.rInt(1, (int) world.height - 1));
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(pos);
        this.body = world.physics.createBody(bodyDef);
        CircleShape circle = new CircleShape();
        circle.setRadius(0.4f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.isSensor = true;
        this.body.createFixture(fixtureDef).setUserData(this);
        circle.dispose();
    }

    public void render(SpriteBatch renderer, Texture t) {
        renderer.draw(t, this.body.getPosition().x - 0.4f, this.body.getPosition().y - 0.4f, 0.8f, 0.8f);
    }

    public int getNutritionalValue() {
        return (this.body.getPosition().x < world.tide) ? 10 : 6;
    }
}
