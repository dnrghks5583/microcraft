package com.jdh.microcraft.level.gen;

import com.jdh.microcraft.gfx.Font;
import com.jdh.microcraft.level.Level;
import com.jdh.microcraft.level.tile.Tile;
import com.jdh.microcraft.util.FMath;

public class OverworldGenerator extends LevelGenerator {
    public OverworldGenerator(Level level) {
        super(level);
    }

    // lay down basic terrain
    private void base() {
        double oh = this.random.nextDouble() * 1024.0f,
            or = this.random.nextDouble() * 1024.0f,
            ot = this.random.nextDouble() * 1024.0f,
            ob = this.random.nextDouble() * 1024.0f;

        for (int y = 0; y < this.level.height; y++) {
            for (int x = 0; x < this.level.width; x++) {
                double nh = FMath.safepow(this.noise.eval(x / 24.0f, y / 24.0f, oh), 1.1),
                    nr = this.noise.eval(x / 8.0f, y / 8.0f, or),
                    nt = FMath.safepow(this.noise.eval(x / 8.0f, y / 8.0f, ot), 1.2),
                    nb = FMath.safepow(this.noise.eval(x / 32.0f, y / 32.0f, ob), 1.3);

                // compute height, drop off close to edges
                double h = getDist(y, x, nh, nr);

                int tile = getTileClass(nt, nb, h);
                level.setTile(x, y, tile);
            }
        }
    }
    
    @Override
	protected double getNum() {
		return -0.3;
	}

	private int getTileClass(double nt, double nb, double h) {
		if (h <= 0.02) {
		    return Tile.WATER.id;
		} else if (nt <= -0.25) {
			return Tile.ROCK.id;
		} else if (h <= 0.10 && nt >= -0.14) {
			return Tile.SAND.id;
		} else if (nb >= 0.32) {
			return Tile.SAND.id;
		} else {
			return Tile.GRASS.id;
		}
	}

    // erode: tiles near water become water
    private void erode() {
        for (int i = 0; i < 8; i++) {
            for (int y = 0; y < this.level.height; y++) {
                for (int x = 0; x < this.level.width; x++) {
                    if (level.getTile(x, y) == Tile.WATER.id) {
                        continue;
                    }

                    int[] borders = getBorders(x, y);

                    int waterCount = 0;
                    for (int t : borders) {
                        if (t == Tile.WATER.id) {
                            waterCount++;
                        }
                    }

                    if (waterCount > 0 && this.random.nextInt(waterCount) > 1) {
                        level.setTile(x, y, Tile.WATER.id);
                    }
                }
            }
        }
    }


    // smooth: tiles are more likely to become like those near them
    private void smooth() {
        for (int i = 0; i < 8; i++) {
            for (int y = 0; y < this.level.height; y++) {
                for (int x = 0; x < this.level.width; x++) {
                    int t = level.getTile(x, y);
                    if (t == Tile.WATER.id || t == Tile.ROCK.id) {
                        continue;
                    }

                    int[] borders = getBorders(x, y);

                    int[] counts = new int[Tile.TILE_MAX];
                    for (int b : borders) {
                        counts[b]++;
                    }

                    // weight towards keeping the tile the same
                    counts[t] += 2;

                    int maxValue = 0, maxTile = t;
                    for (int b : borders)  {
                        int value = this.random.nextInt(counts[b]);
                        if (value > maxValue) {
                            maxValue = value;
                            maxTile = b;
                        }
                    }

                    this.level.setTile(x, y, maxTile);
                }
            }
        }
    }

    private int[] getBorders(int x, int y) {
		int[] borders = {
		    level.getTile(x - 1, y - 1),
		    level.getTile(x + 1, y - 1),
		    level.getTile(x + 1, y + 1),
		    level.getTile(x - 1, y + 1),
		};
		return borders;
	}
    
    // flora: flowers, tall grass
    private void flora() {
        
        for (int y = 0; y < this.level.height; y++) {
            for (int x = 0; x < this.level.width; x++) {
                tileChange(y, x);
            }
        }
    }

	private void tileChange(int y, int x) {
		double of = this.random.nextDouble() * 128.0;
		if (level.getTile(x, y) == Tile.SAND.id) {
		    if (this.random.nextInt(128) == 0) {
		        level.setTile(x, y, Tile.CACTUS.id);
		    }
		} else if (level.getTile(x, y) == Tile.GRASS.id) {
		    double nf = FMath.safepow(this.noise.eval(x / 8.0, y / 8.0, of), 1.1);

		    if (nf >= 0.2) {
		        level.setTile(x, y, Tile.TREE.id);
		    } else if (nf <= 0.0 && nf >= -0.08 && this.random.nextInt(2) != 0) {
		        level.setTile(x, y, Tile.TALL_GRASS.id);
		    } else if (this.random.nextInt(32) == 0) {
		        generatorFlower(y, x);
		    }
		}
	}

	private void generatorFlower(int y, int x) {
		int s = 4 + this.random.nextInt(3),
		    l = (s - 2) + this.random.nextInt(4),
		    w = (s - 2) + this.random.nextInt(4);

		int flower = this.random.nextBoolean() ? Tile.DAISY.id : Tile.POPPY.id,
		    other  = flower == Tile.DAISY.id ? Tile.POPPY.id : Tile.DAISY.id;

		for (int yy = 0; yy < l; yy++) {
		    for (int xx = 0; xx < w; xx++) {
		        if (this.random.nextInt(6) != 0 &&
		            level.getTile(x + xx, y + yy) == Tile.GRASS.id) {
		            level.setTile(
		                x + xx, y + yy,
		                this.random.nextInt(5) == 0 ? flower : other
		            );
		        }
		    }
		}
	}

    @Override
    public void generate() {
        super.generate();
    }

	@Override
	protected void moreGenerate() {
		this.base();
        this.setProgress(
            Font.Colors.GREY + "LEVEL " + this.level.depth + ":" +
                Font.Colors.YELLOW + "ERODING...", 0.0);
        this.erode();
        this.setProgress(
            Font.Colors.GREY + "LEVEL " + this.level.depth + ":" +
                Font.Colors.YELLOW + "PLANTING...", 0.0);
        this.flora();
	}
}
