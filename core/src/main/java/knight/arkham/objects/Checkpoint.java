package knight.arkham.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import knight.arkham.helpers.Box2DBody;
import knight.arkham.helpers.Box2DHelper;

public class Checkpoint extends GameObject {
    private final Animation<TextureRegion> animation;
    private float stateTimer;
    private boolean isActive;

    public Checkpoint(Rectangle rectangle, World world, TextureAtlas atlas) {
        super(
            rectangle, world,
            new TextureRegion(atlas.findRegion("No-Flag"), 0, 0, 64, 64)
        );

        animation = makeAnimationByFrameRange(atlas.findRegion("Flag"), 9, 64);
    }

    @Override
    protected Body createBody() {

        return Box2DHelper.createStaticBody(
            new Box2DBody(actualBounds, 0, actualWorld, this)
        );
    }

    public void update(float deltaTime) {

        stateTimer += deltaTime;

        if (isActive)
            actualRegion = animation.getKeyFrame(stateTimer, true);
    }

    public void createCheckpoint() {

        isActive = true;
    }
}
