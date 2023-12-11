package knight.arkham.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import knight.arkham.objects.structures.Checkpoint;
import knight.arkham.objects.Enemy;
import knight.arkham.objects.Player;
import static knight.arkham.helpers.Constants.MID_SCREEN_WIDTH;
import static knight.arkham.helpers.Constants.PIXELS_PER_METER;

public class TileMapHelper {
    private final World world;
    private final TiledMap tiledMap;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final TextureAtlas atlas;
    private final Player player;
    private final Array<Enemy> enemies;
    private final Array<Checkpoint> checkpoints;
    private int frontBounds;
    private int backBounds;
    private float accumulator;
    private final float TIME_STEP;

    public TileMapHelper(String mapFilePath, World world, TextureAtlas atlas) {

        tiledMap = new TmxMapLoader().load(mapFilePath);
        this.world = world;
        this.atlas = atlas;

        player = new Player(new Rectangle(450, 50, 32, 32), world, atlas);
        enemies = new Array<>();
        checkpoints = new Array<>();

        mapRenderer = setupMap();

        frontBounds = 900;
        backBounds = 450;

        TIME_STEP = 1/240f;
    }

    public OrthogonalTiledMapRenderer setupMap() {

        for (MapLayer mapLayer : tiledMap.getLayers())
            parseMapObjectsToBox2DBodies(mapLayer.getObjects(), mapLayer.getName());

        return new OrthogonalTiledMapRenderer(tiledMap, 1 / PIXELS_PER_METER);
    }

    private void parseMapObjectsToBox2DBodies(MapObjects mapObjects, String objectsName) {

        for (MapObject mapObject : mapObjects) {

            Rectangle rectangle = ((RectangleMapObject) mapObject).getRectangle();

            Rectangle box2DRectangle = getBox2dRectangle(rectangle);

            if (objectsName.equals("Enemies"))
                enemies.add(new Enemy(box2DRectangle, world, atlas));

            else if (objectsName.equals("Checkpoints"))
                checkpoints.add(new Checkpoint(box2DRectangle, world, atlas));

            else
                Box2DHelper.createBody(new Box2DBody(box2DRectangle, world));
        }
    }

    private Rectangle getBox2dRectangle(Rectangle rectangle){
        return new Rectangle(
            rectangle.x + rectangle.width / 2,
            rectangle.y + rectangle.height / 2,
            rectangle.width, rectangle.height
        );
    }

    private boolean isPlayerInsideMapBounds(Vector2 playerPosition) {

        MapProperties properties = tiledMap.getProperties();

        int mapWidth = properties.get("width", Integer.class);
        int tilePixelWidth = properties.get("tilewidth", Integer.class);

        int mapPixelWidth = mapWidth * tilePixelWidth;

        return playerPosition.x > MID_SCREEN_WIDTH && playerPosition.x < mapPixelWidth - MID_SCREEN_WIDTH;
    }

    public void updateCameraPosition(OrthographicCamera camera) {

        if (Gdx.input.isKeyJustPressed(Input.Keys.F3))
            camera.zoom += 0.2f;

        if (Gdx.input.isKeyJustPressed(Input.Keys.F4))
            camera.zoom -= 0.2f;

        boolean isPlayerInsideMapBounds = isPlayerInsideMapBounds(player.getPixelPosition());

        Vector2 actualPosition = player.getPixelPosition();

        if (actualPosition.x > frontBounds) {
            camera.position.set(player.getWorldPosition().x, 8.5f, 0);

            frontBounds += 450;
            backBounds += 450;
        }

        if (actualPosition.x < backBounds) {

            camera.position.set(player.getWorldPosition().x, 8.5f, 0);

            frontBounds -= 450;
            backBounds -= 450;
        }

        camera.update();
    }

    public void update(float deltaTime, OrthographicCamera camera){

        player.update(deltaTime);

        updateCameraPosition(camera);

        for (Enemy enemy : enemies)
            enemy.update(deltaTime);

        for (Checkpoint checkpoint : checkpoints)
            checkpoint.update(deltaTime);

        doPhysicsTimeStep(deltaTime);
    }

    private void doPhysicsTimeStep(float deltaTime) {

        float frameTime = Math.min(deltaTime, 0.25f);

        accumulator += frameTime;

        while(accumulator >= TIME_STEP) {
            world.step(TIME_STEP, 6,2);
            accumulator -= TIME_STEP;
        }
    }

    public void draw(OrthographicCamera camera){

        mapRenderer.setView(camera);

        mapRenderer.render();

        mapRenderer.getBatch().setProjectionMatrix(camera.combined);

        mapRenderer.getBatch().begin();

        player.draw(mapRenderer.getBatch());

        for (Enemy enemy : enemies)
            enemy.draw(mapRenderer.getBatch());

        for (Checkpoint checkpoint : checkpoints)
            checkpoint.draw(mapRenderer.getBatch());

        mapRenderer.getBatch().end();
    }

    public void dispose(){

        player.dispose();
        tiledMap.dispose();
        mapRenderer.dispose();
        world.dispose();
    }
}
