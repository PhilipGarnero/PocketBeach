package com.smallworld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;


public class Sea {
	private final String vertexShader = Gdx.files.internal("shaders/sea.vsh").readString();
	private final String fragmentShader = Gdx.files.internal("shaders/sea.fsh").readString();
	private ShaderProgram shader;
	private Mesh mesh;
	private GameWorld world;

	public Sea(GameWorld world) {
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

	public void render() {
		this.mesh.setVertices(new float[]{this.world.width, 0, 1, 1,
				this.world.tide - 1, 0, 0, 1,
				this.world.tide - 1, this.world.height, 0, 0,
				this.world.width, this.world.height, 1, 0});

		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		this.world.screen.textures.get("tide-noise").bind(1);
		this.world.screen.textures.get("sea").bind(2);
		this.shader.begin();
		this.shader.setUniformMatrix("u_worldView", this.world.screen.camera.combined);
		this.shader.setUniformi("u_noiseTexture", 1);
		this.shader.setUniformi("u_waveTexture", 2);
		this.shader.setUniformf("u_time", this.world.time);
		this.mesh.render(this.shader, GL20.GL_TRIANGLE_FAN);
		this.shader.end();
	}
}
