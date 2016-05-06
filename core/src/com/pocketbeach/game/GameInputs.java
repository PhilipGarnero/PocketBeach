package com.pocketbeach.game;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

public class GameInputs implements GestureDetector.GestureListener, InputProcessor {
    private OrthographicCamera cam;
    private com.pocketbeach.game.screens.GameScreen screen;
    private GameWorld gameWorld;
    private Vector3 dragMem = new Vector3();
    private Vector3 originalCam = new Vector3();
    private Vector3 newPos = new Vector3();
    private Vector3 testPoint = new Vector3();
    private Body hitBody = null;
    public MouseJoint mouseJoint = null;
    private Vector2 tmpv2 = new Vector2();
    private Vector3 tmpv3 = new Vector3();
    private int finger = 0;

    public GameInputs(com.pocketbeach.game.screens.GameScreen screen) {
        this.cam = screen.camera;
        this.gameWorld = screen.gameWorld;
        this.screen = screen;
    }

    QueryCallback callback = new QueryCallback() {
        @Override
        public boolean reportFixture(Fixture fixture) {
            if (fixture.testPoint(testPoint.x, testPoint.y) && fixture.getBody().getUserData() instanceof Actor) {
                hitBody = fixture.getBody();
                return false;
            } else {
                return true;
            }
        }
    };

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        this.finger++;
        if (this.finger == 1) {
            this.testPoint.set(x, y, 0);
            this.screen.camera.unproject(this.testPoint);
            this.hitBody = null;
            this.gameWorld.physics.QueryAABB(callback, this.testPoint.x - 0.1f, this.testPoint.y - 0.1f, this.testPoint.x + 0.1f, this.testPoint.y + 0.1f);
            if (this.hitBody != null) {
                MouseJointDef def = new MouseJointDef();
                def.bodyA = this.hitBody;
                def.bodyB = this.hitBody;
                def.collideConnected = true;
                def.target.set(this.testPoint.x, this.testPoint.y);
                def.maxForce = 1000.0f * this.hitBody.getMass();
                this.mouseJoint = (MouseJoint) this.gameWorld.physics.createJoint(def);
                Actor a = (Actor) this.hitBody.getUserData();
                a.mouseJoint = this.mouseJoint;
                this.hitBody.setAwake(true);
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        if (this.mouseJoint != null && this.finger == 1) {
            Actor a = (Actor)this.hitBody.getUserData();
            a.mouseJoint = null;
            this.gameWorld.physics.destroyJoint(this.mouseJoint);
            this.mouseJoint = null;
        }
        this.finger--;
        if (this.finger < 0)
            this.finger = 0;
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (this.mouseJoint != null) {
            this.screen.camera.unproject(this.testPoint.set(x, y, 0));
            this.mouseJoint.setTarget(this.tmpv2.set(this.testPoint.x, this.testPoint.y));
        } else {
            if (this.dragMem.isZero()) {
                this.dragMem.set(this.cam.unproject(this.tmpv3.set(x, y, 0)));
                this.originalCam = this.cam.position;
            }
            this.newPos.set(this.cam.unproject(this.tmpv3.set(x, y, 0)));
            this.newPos.sub(this.dragMem);
            this.newPos.x = -this.newPos.x;
            this.newPos.y = -this.newPos.y;
            this.newPos.add(this.originalCam);
            this.cam.position.set(this.newPos);
            this.screen.clampCamera();
            this.cam.update();
        }
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        this.dragMem.setZero();
        return false;
    }

    @Override
    public boolean zoom (float originalDistance, float currentDistance) {
        if (originalDistance < currentDistance) {
            this.cam.zoom -= 0.01;
        } else if (originalDistance > currentDistance) {
            this.cam.zoom += 0.01;
        }
        this.screen.clampCamera();
        this.cam.update();
        return false;
    }

    @Override
    public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer){
        return false;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}