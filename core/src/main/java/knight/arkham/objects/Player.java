package knight.arkham.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import knight.arkham.helpers.Box2DBody;
import knight.arkham.helpers.Box2DHelper;

public class Player extends GameObject {
    private enum AnimationState {FALLING, JUMPING, STANDING, RUNNING}
    private AnimationState actualState;
    private AnimationState previousState;
    private final TextureRegion jumpingRegion;
    private final Animation<TextureRegion> standingAnimation;
    private final Animation<TextureRegion> runningAnimation;
    private float animationTimer;
    private boolean isMovingRight;

    public Player(Rectangle bounds, World world, TextureAtlas atlas) {
        super(
            bounds, world,
            new TextureRegion(atlas.findRegion("Idle"), 0, 0, 32, 32)
        );

        previousState = AnimationState.STANDING;
        actualState = AnimationState.STANDING;

        standingAnimation = makeAnimationByFrameRange(atlas.findRegion("Idle"), 1, 10, 0.1f);

        jumpingRegion = new TextureRegion(atlas.findRegion("Jump"), 0, 0, 32, 32);

        runningAnimation = makeAnimationByFrameRange(atlas.findRegion("Run"), 1, 10, 0.1f);
    }

    private Animation<TextureRegion> makeAnimationByFrameRange(TextureRegion characterRegion, int initialFrame, int finalFrame, float frameDuration) {

        Array<TextureRegion> animationFrames = new Array<>();

        for (int i = initialFrame; i <= finalFrame; i++)
            animationFrames.add(new TextureRegion(characterRegion, i * 32, 0, 32, 32));

        return new Animation<>(frameDuration, animationFrames);
    }

    @Override
    protected Body createBody() {

        return Box2DHelper.createBody(
            new Box2DBody(actualBounds, 20, actualWorld, this)
        );
    }

    public void update(float deltaTime) {

        setActualRegion(getAnimationRegion(deltaTime));

        if (Gdx.input.isKeyPressed(Input.Keys.D) && body.getLinearVelocity().x <= 10)
            applyLinealImpulse(new Vector2(5, 0));

        else if (Gdx.input.isKeyPressed(Input.Keys.A) && body.getLinearVelocity().x >= -10)
            applyLinealImpulse(new Vector2(-5, 0));

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && body.getLinearVelocity().y == 0)
            applyLinealImpulse(new Vector2(0, 170));
    }

    private AnimationState getPlayerCurrentState() {

        boolean isPlayerMoving = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.D);

        if (body.getLinearVelocity().y > 0 || (body.getLinearVelocity().y < 0 && previousState == AnimationState.JUMPING))
            return AnimationState.JUMPING;

        else if (isPlayerMoving)
            return AnimationState.RUNNING;

        else if (body.getLinearVelocity().y < 0)
            return AnimationState.FALLING;

        else
            return AnimationState.STANDING;
    }

    private TextureRegion getAnimationRegion(float deltaTime) {

        actualState = getPlayerCurrentState();

        TextureRegion region;

        switch (actualState) {

            case JUMPING:
                region = jumpingRegion;
                break;

            case RUNNING:
                region = runningAnimation.getKeyFrame(animationTimer, true);
                break;

            case FALLING:
            case STANDING:
            default:
                region = standingAnimation.getKeyFrame(animationTimer, true);;
        }

        flipPlayerOnXAxis(region);

        animationTimer = actualState == previousState ? animationTimer + deltaTime : 0;
        previousState = actualState;

        return region;
    }

    private void flipPlayerOnXAxis(TextureRegion region) {

        if ((body.getLinearVelocity().x < 0 || !isMovingRight) && !region.isFlipX()) {

            region.flip(true, false);
            isMovingRight = false;
        } else if ((body.getLinearVelocity().x > 0 || isMovingRight) && region.isFlipX()) {

            region.flip(true, false);
            isMovingRight = true;
        }
    }
}
