package com.mygdx.gamestates;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.entities.Asteroid;
import com.mygdx.entities.Bullet;
import com.mygdx.entities.Player;
import com.mygdx.game.Game;
import com.mygdx.managers.GameKeys;
import com.mygdx.managers.GameStateManager;

import java.util.ArrayList;

public class PlayState extends GameState {

    private ShapeRenderer sr;

    private Player player;
    private ArrayList<Bullet> bullets;
    private ArrayList<Asteroid> asteroids;

    private int level;
    private int totalAsteroids;
    private int numAsteroidsLeft;

    public PlayState(GameStateManager gsm){
        super(gsm);
    }

    public void init(){

        sr = new ShapeRenderer();

        bullets = new ArrayList<Bullet>();

        player = new Player(bullets);

        asteroids = new ArrayList<Asteroid>();
        asteroids.add(new Asteroid(100, 100, Asteroid.LARGE));
        asteroids.add(new Asteroid(200, 100, Asteroid.MEDIUM));
        asteroids.add(new Asteroid(300, 100, Asteroid.SMALL));

        level = 1;
        spawnAsteroids();

    }

    private void splitAsteroids(Asteroid a){
        numAsteroidsLeft--;
        if(a.getType() == Asteroid.LARGE){
            asteroids.add(new Asteroid(a.getx(), a.gety(), Asteroid.MEDIUM));
            asteroids.add(new Asteroid(a.getx(), a.gety(), Asteroid.MEDIUM));
        }
        if(a.getType() == Asteroid.MEDIUM){
            asteroids.add(new Asteroid(a.getx(), a.gety(), Asteroid.SMALL));
            asteroids.add(new Asteroid(a.getx(), a.gety(), Asteroid.SMALL));
        }
    }

    private void spawnAsteroids(){

        asteroids.clear();

        int numToSpawn = 4 + level - 1;
        totalAsteroids = numToSpawn * 7;
        numAsteroidsLeft = totalAsteroids;

        for (int i = 0; i < numToSpawn; i++){

            float x = MathUtils.random(Game.WIDTH);
            float y = MathUtils.random(Game.HEIGHT);

            float dx = x - player.getx();
            float dy = y - player.gety();
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            while(dist < 100){
                x = MathUtils.random(Game.WIDTH);
                y = MathUtils.random(Game.HEIGHT);
                dx = x - player.getx();
                dy = y - player.gety();
                dist = (float) Math.sqrt(dx * dx + dy * dy);
            }

            asteroids.add(new Asteroid(x, y, Asteroid.LARGE));

        }

    }

    public void update(float dt){

        //get user input
        handleInput();

        //update player
        player.update(dt);
        if(player.isDead()){
            player.reset();
            return;
        }

        //update player bullets
        for(int i=0; i<bullets.size(); i++){
            bullets.get(i).update(dt);
            if(bullets.get(i).shouldRemove()){
                bullets.remove(i);
                i--;
            }
        }

        // update asteroids
        for(int i=0; i<asteroids.size(); i++){
            asteroids.get(i).update(dt);
            if(asteroids.get(i).shouldRemove()){
                asteroids.remove(i);
                i--;
            }
        }

        // check collisions
        checkCollisions();

    }

    private void checkCollisions(){

        // player-asteroid collision
        if(!player.isHit()){
            for(int i=0; i<asteroids.size(); i++){
                Asteroid a = asteroids.get(i);
                if(a.intersects(player)){
                    player.hit();
                    asteroids.remove(i);
                    i--;
                    splitAsteroids(a);
                    break;
                }
            }
        }

        // bullet-asteroid collision
        for(int i = 0; i < bullets.size(); i++){
            Bullet b = bullets.get(i);
            for(int j=0; j<asteroids.size(); j++){
                Asteroid a = asteroids.get(j);
                if(a.contains(b.getx(), b.gety())){
                    bullets.remove(i);
                    i--;
                    asteroids.remove(j);
                    j--;
                    splitAsteroids(a);
                    break;
                }
            }
        }

    }

    public void draw(){

        //draw player
        player.draw(sr);

        //draw bullets
        for(int i = 0; i<bullets.size(); i++){
            bullets.get(i).draw(sr);
        }

        //draw asteroids
        for(int i=0; i<asteroids.size(); i++){
            asteroids.get(i).draw(sr);
        }
    }

    public void handleInput(){
        player.setLeft(GameKeys.isDown(GameKeys.LEFT));
        player.setRight(GameKeys.isDown(GameKeys.RIGHT));
        player.setUp(GameKeys.isDown(GameKeys.UP));
        if(GameKeys.isPressed(GameKeys.SPACE)){
            player.shoot();
        }
    }

    public void dispose(){}

}
