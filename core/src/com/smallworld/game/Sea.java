package com.smallworld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;


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
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        this.mesh.setVertices(new float[]{this.world.tide - 2, 0, 3, 3,
                this.world.width, 0, 0, 3,
                this.world.width, this.world.height, 0, 0,
                this.world.tide - 2, this.world.height, 3, 0});
		this.world.screen.textures.get("tide-noise").bind(1);
        this.shader.begin();
        this.shader.setUniformMatrix("u_worldView", this.world.screen.camera.combined);
        this.shader.setUniformi("u_noiseTexture", 1);
        this.shader.setUniform4fv("u_seaColor", new float[]{0f, 0.7f, 1f, 0.3f}, 0, 4);
        this.shader.setUniformf("u_time", this.world.time);
        this.shader.setUniformf("u_cyclingTime", (float)(this.world.time % (Math.PI * 2)));
        this.mesh.render(this.shader, GL20.GL_TRIANGLE_FAN);
        this.shader.end();
	}
}
