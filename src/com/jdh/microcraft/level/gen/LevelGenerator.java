package com.jdh.microcraft.level.gen;

import com.jdh.microcraft.Global;
import com.jdh.microcraft.gfx.Font;
import com.jdh.microcraft.level.Level;
import com.jdh.microcraft.level.tile.Tile;
import com.jdh.microcraft.util.FMath;
import com.jdh.microcraft.util.OpenSimplexNoise;

import java.util.List;
import java.util.Random;

public abstract class LevelGenerator {
    public static final int OVERWORLD_LEVELS = 2; // includes level 0 (base overworld level)
    public static final int UNDERWORLD_LEVELS = 3;

    protected Level level;
    protected OpenSimplexNoise noise;
    protected Random random;
    
    public LevelGenerator(Level level) {
        this.level = level;
        this.noise = new OpenSimplexNoise(this.level.seed);
        this.random = new Random(this.level.seed);
    }

    public static StairsGenerator getStairsGenerator(Level lower, Level upper) {
        if (upper.depth == 1) {
            return new CloudStairsGenerator(lower, upper);
        }

        int lowerReqs = 0, upperReqs = 0;

        upperReqs |= upper.depth == 0 ? DefaultStairsGenerator.REQUIRE_ROCKS : 0;
        upperReqs |= upper.depth < 0 ? DefaultStairsGenerator.REQUIRE_OPEN : 0;

        lowerReqs |= lower.depth == 0 ? DefaultStairsGenerator.REQUIRE_ROCKS : 0;
        lowerReqs |= lower.depth < 0 ? DefaultStairsGenerator.REQUIRE_OPEN : 0;

        upperReqs |= DefaultStairsGenerator.REQUIRE_NO_LIQUID;
        lowerReqs |= DefaultStairsGenerator.REQUIRE_NO_LIQUID;

        return new DefaultStairsGenerator(
            lower.seed, lower, upper, Math.max(Math.max(lower.width, upper.width) / 8, 16),
            lowerReqs, upperReqs
        );
    }

    public static LevelGenerator getGenerator(Level level) {
        return switch (level.depth) {
            case 1 -> new CloudworldGenerator(level);
            case 0 -> new OverworldGenerator(level);
            case -1 -> new UnderworldGenerator(
                level,
                Tile.STONE, Tile.DIRT, Tile.ROCK,
                List.of(
                    new UnderworldGenerator.OreGenerationProperties(Tile.IRON_ORE, 48),
                    new UnderworldGenerator.OreGenerationProperties(Tile.GOLD_ORE, 448)
                ),
                3, 1
            );
            case -2 -> new UnderworldGenerator(
                level,
                Tile.STONE, Tile.DIRT, Tile.HARD_ROCK,
                List.of(
                    new UnderworldGenerator.OreGenerationProperties(Tile.IRON_ORE, 48),
                    new UnderworldGenerator.OreGenerationProperties(Tile.GOLD_ORE, 60),
                    new UnderworldGenerator.OreGenerationProperties(Tile.GEM_ORE, 384)
                ),
                3, 2
            );
            case -3 -> new UnderworldGenerator(
                level,
                Tile.STONE, Tile.STONE, Tile.BASALT,
                List.of(
                    new UnderworldGenerator.OreGenerationProperties(Tile.IRON_ORE, 60),
                    new UnderworldGenerator.OreGenerationProperties(Tile.GOLD_ORE, 60),
                    new UnderworldGenerator.OreGenerationProperties(Tile.GEM_ORE, 128),
                    new UnderworldGenerator.OreGenerationProperties(Tile.MITHRIL_ORE, 256)
                ),
                3, 5
            );
            default -> throw new IllegalArgumentException();
        };
    }

    protected void setProgress(String text, double progress) {
        if (Global.currentStateType == Global.StateType.MENU) {
            Global.mainMenu.loadingMenu.setProgress(text, progress);
        }
    }

    public void generate() {
    	this.setProgress(Font.Colors.GREY + "LEVEL " + this.level.depth + ":" + Font.Colors.YELLOW + "FORMING...", 0.0);
    	moreGenerate();
    };
    
    protected abstract void moreGenerate();
    
    protected double getDist(int y, int x, double nb, double nr) {
		double dist = FMath.norm(
		    Math.abs(x - (this.level.width / 2.0)) / (this.level.width / 2.0),
		    Math.abs(y - (this.level.height / 2.0)) / (this.level.height / 2.0)),
		    h = nb + (nr * 0.5) + (dist > (1.0 - (32 * (1.0 / this.level.width))) ? getNum() : 0.0);
		return h;
	}
    protected abstract double getNum();
}
