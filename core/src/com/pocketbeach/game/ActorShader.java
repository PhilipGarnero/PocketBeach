package com.pocketbeach.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;


public class ActorShader {
	private final String vertexShader = Gdx.files.internal("shaders/actor.vsh").readString();
	private final String fragmentShader = Gdx.files.internal("shaders/actor.fsh").readString();
	private ShaderProgram shader;
	private Mesh mesh;
	private GameWorld world;

	public ActorShader(GameWorld world) {
		this.world = world;
		this.mesh = new Mesh(true, 4, 0,
				new VertexAttribute(Usage.Position, 2, "a_position"),
				new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
		this.shader.pedantic = false;
		this.shader = new ShaderProgram(this.vertexShader, this.fragmentShader);
		if (!shader.isCompiled()) {
			Gdx.app.log("shader error", shader.getLog());
		}
	}

	public void dispose() {
		this.mesh.dispose();
		this.shader.dispose();
	}

	public void begin() {
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		this.world.screen.textures.get("actor").bind(1);
		this.shader.begin();
		this.shader.setUniformMatrix("u_worldView", this.world.screen.camera.combined);
		this.shader.setUniformi("u_actorTexture", 1);
	}

	public void end() {
		this.shader.end();
	}

	public void draw(Actor a) {
		this.mesh.setVertices(new float[]{a.body.getPosition().x - 1, a.body.getPosition().y - 1, 1, 1,
				a.body.getPosition().x + 1, a.body.getPosition().y - 1, 0, 1,
				a.body.getPosition().x + 1, a.body.getPosition().y + 1, 0, 0,
				a.body.getPosition().x - 1, a.body.getPosition().y + 1, 1, 0});
        Matrix4 m = new Matrix4();
        m.translate(a.body.getPosition().x, a.body.getPosition().y, 0);
        m.rotateRad(0, 0, 1, a.body.getAngle());
        m.translate(-a.body.getPosition().x, -a.body.getPosition().y, 0);
        Color c = a.getHealthColor();
		this.shader.setUniform4fv("u_actorColor", new float[]{c.r, c.g, c.b, c.a}, 0, 4);
		this.shader.setUniformf("u_actorAngle", a.body.getAngle());
        this.shader.setUniformMatrix("u_rotationMatrix", m);
		this.mesh.render(this.shader, GL20.GL_TRIANGLE_FAN);
	}
}
