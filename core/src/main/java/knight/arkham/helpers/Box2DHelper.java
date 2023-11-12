package knight.arkham.helpers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import knight.arkham.objects.Player;
import static knight.arkham.helpers.Constants.*;

public class Box2DHelper {

    private static Body createBox2DBodyByType(Box2DBody box2DBody) {

        BodyDef bodyDef = new BodyDef();

        bodyDef.type = box2DBody.bodyType;

        bodyDef.position.set(box2DBody.bounds.x / PIXELS_PER_METER, box2DBody.bounds.y / PIXELS_PER_METER);

        bodyDef.fixedRotation = true;

        return box2DBody.world.createBody(bodyDef);
    }

    public static Body createBody(Box2DBody box2DBody) {

        PolygonShape shape = new PolygonShape();

        FixtureDef fixtureDef = createStandardFixtureDef(box2DBody, shape);

        Body body = createBox2DBodyByType(box2DBody);

        if (box2DBody.userData instanceof Player)
            createPlayerBody(box2DBody, fixtureDef, body);

        else {

            fixtureDef.filter.categoryBits = GROUND_BIT;

            body.createFixture(fixtureDef);
        }

        shape.dispose();

        return body;
    }

    private static FixtureDef createStandardFixtureDef(Box2DBody box2DBody, PolygonShape shape) {

        shape.setAsBox(box2DBody.bounds.width / 2 / PIXELS_PER_METER, box2DBody.bounds.height / 2 / PIXELS_PER_METER);

        FixtureDef fixtureDef = new FixtureDef();

        fixtureDef.shape = shape;

        fixtureDef.density = box2DBody.density;
        return fixtureDef;
    }

    private static EdgeShape getPlayerHeadCollider(FixtureDef fixtureDefinition) {

        EdgeShape headCollider = new EdgeShape();

        headCollider.set(new Vector2(-15 / PIXELS_PER_METER, 19 / PIXELS_PER_METER),
            new Vector2(15 / PIXELS_PER_METER, 19 / PIXELS_PER_METER));

        fixtureDefinition.shape = headCollider;

        fixtureDefinition.isSensor = true;

        fixtureDefinition.filter.categoryBits = MARIO_HEAD_BIT;

        return headCollider;
    }

    private static void createPlayerBody(Box2DBody box2DBody, FixtureDef fixtureDef, Body body) {

        fixtureDef.filter.categoryBits = PLAYER_BIT;

        fixtureDef.filter.maskBits = (short) (GROUND_BIT | CHECKPOINT_BIT | FINISH_BIT | ENEMY_BIT | ENEMY_HEAD_BIT);

        fixtureDef.friction = 1;

        body.createFixture(fixtureDef).setUserData(box2DBody.userData);

        EdgeShape headCollider = getPlayerHeadCollider(fixtureDef);

        body.createFixture(fixtureDef).setUserData(box2DBody.userData);

        headCollider.dispose();
    }
}
