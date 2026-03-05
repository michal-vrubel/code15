package cz.mv.code15;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.stream.Stream;

public class Warehouse {
	private BitSet boxMap;
	private BitSet wallMap;
	private Point robotLocation;
	private int width;
	private int height;

	private Warehouse() {
	}

	public static Warehouse loadWarehouse(File file) {
		try (
			InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file),
				StandardCharsets.UTF_8);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		) {
			String line;

			if ((line = bufferedReader.readLine()) == null) {
				throw new IllegalArgumentException("The input resource is empty");
			}

			if (!line.matches("^#{3,}$")) {
				throw new IllegalArgumentException("The first line in the input resource must be #{3,}");
			}

			int i = 0;

			Warehouse warehouse = new Warehouse();
			warehouse.width = line.length() - 2;
			warehouse.height = 0;
			warehouse.boxMap = new BitSet();
			warehouse.wallMap = new BitSet();

			while ((line = bufferedReader.readLine()) != null) {
				if (line.matches("^#[#@O.]{" + warehouse.width + "}#$")) {
					System.out.printf("%s\n", line);
					warehouse.height++;

					for (int j = 1; j <= line.length() - 2; j++) {
						char ch = line.charAt(j);

						int bitIndex = i * warehouse.width + warehouse.width - j;

						if (ch == 'O') {
							warehouse.boxMap.set(bitIndex);
						} else if (ch == '#') {
							warehouse.wallMap.set(bitIndex);
						} else if (ch == '.') {
							warehouse.boxMap.set(bitIndex, false);
						} else if (warehouse.robotLocation == null && ch == '@') {

							warehouse.robotLocation = new Point(j, i + 1);
						}
					}
					i++;
				}
			}

			return warehouse;
		} catch (IOException ex) {
			throw new RuntimeException("Failed to load warehouse", ex);
		}
	}
	
	public void loadAndRunCommands(File file) {
		try (
			InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file),
				StandardCharsets.UTF_8);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		) {
			String line = null;

			while ((line = bufferedReader.readLine()) != null) {
				Stream<Character> characterStream = line.chars().mapToObj(c -> (char) c);
				characterStream.forEach((ch) -> {
					if (ch == '<') {
						makeMove(Direction.LEFT);
					} else if (ch == '>') {
						makeMove(Direction.RIGHT);
					} else if (ch == 'v') {
						makeMove(Direction.DOWN);
					} else if (ch == '^') {
						makeMove(Direction.UP);
					}
				});
			}
		} catch (IOException ex) {
			throw new RuntimeException("Failed to load commands", ex);
		}
	}
	
	public void makeMove(Direction d) {
		int roomIndex = searchEmptyRoomToMove(d);
		if (roomIndex == -1) {
			return;
		}

		move(d, roomIndex);
	}
	
	private int searchEmptyRoomToMove(Direction d) {
		int robotBit = getRobotBit();

		if (d == Direction.LEFT) {
			int maxBit = robotLocation.y() * width - 1;
			int wallBit = wallMap.nextSetBit(robotBit);

			if (robotBit >= maxBit || robotBit + 1 == wallBit) {
				return -1;
			}

			wallBit = wallBit <= maxBit ? wallBit : -1;
			int clearBit = boxMap.nextClearBit(robotBit + 1);
			return clearBit <= maxBit && (wallBit == -1 || clearBit < wallBit) ? clearBit : -1;
		} else if (d == Direction.RIGHT) {
			int minBit = (robotLocation.y() - 1) * width;
			int wallBit = wallMap.previousSetBit(robotBit);

			if (robotBit <= minBit || robotBit - 1 == wallBit) {
				return -1;
			}

			wallBit = wallBit >= minBit ? wallBit : -1;
			int clearBit = boxMap.previousClearBit(robotBit - 1);
			return clearBit >= minBit && (wallBit == -1 || clearBit > wallBit) ? clearBit : -1;
		} else if (d == Direction.UP) {
			int minBit = width - 1;
			if (robotBit <= minBit) {
				return -1;
			}

			int i = 1;
			int upperBit;
			int stopBit = -1;

			while ((upperBit = robotBit - i * width) >= 0) {
				boolean isWall = wallMap.get(upperBit);
				boolean isSet = boxMap.get(upperBit);

				if (!isSet || isWall) {
					stopBit = upperBit;
					break;
				}

				i++;
			}

			boolean isWall = stopBit != -1 && wallMap.get(stopBit);
			return isWall ? -1 : stopBit;
		} else if (d == Direction.DOWN) {
			int totalBits = width * height;
			int maxBit = totalBits - width;

			if (robotBit >= maxBit) {
				return -1;
			}

			int i = 1;
			int lowerBit;
			int stopBit = -1;

			while ((lowerBit = robotBit + i * width) < totalBits) {
				boolean isWall = wallMap.get(lowerBit);
				boolean isSet = boxMap.get(lowerBit);

				if (!isSet || isWall) {
					stopBit = lowerBit;
					break;
				}

				i++;
			}

			boolean isWall = stopBit != -1 && wallMap.get(stopBit);
			return isWall ? -1 : stopBit;
		}

		return -1;
	}
	
	private void move(Direction d, int roomIndex) {
		int robotBit = getRobotBit();
		
		if (d == Direction.LEFT) {			
			boxMap.set(robotBit, robotBit + 2, false);

			if (robotBit + 2 < roomIndex + 1) {
				boxMap.set(robotBit + 2, roomIndex + 1);
			}

			robotLocation = new Point(robotLocation.x() - 1, robotLocation.y());			
		} else if (d == Direction.RIGHT) {
			boxMap.set(robotBit - 1, robotBit + 1, false);
			if (roomIndex < robotBit - 1) {
				boxMap.set(roomIndex, robotBit - 1);
			}

			robotLocation = new Point(robotLocation.x() + 1, robotLocation.y());			
		} else if (d == Direction.UP) {
			boxMap.set(robotBit, false);
			boxMap.set(robotBit - width, false);

			int i = 2;
			while ((robotBit - i * width) >= roomIndex) {
				boxMap.set(robotBit - i * width);
				i++;
			}

			robotLocation = new Point(robotLocation.x(), robotLocation.y() - 1);			
		} else if (d == Direction.DOWN) {
			boxMap.set(robotBit, false);
			boxMap.set(robotBit + width, false);

			int i = 2;
			while ((robotBit + i * width) <= roomIndex) {
				boxMap.set(robotBit + i * width);
				i++;
			}

			robotLocation = new Point(robotLocation.x(), robotLocation.y() + 1);			
		}
	}

	private int getRobotBit() {
		return robotLocation.y() * width - robotLocation.x();
	}

	public long getSumOfCoordinates() {
		long sum = 0L;

		for (int i = 1; i <= height; i++) {
			for (int j = 1; j <= width; j++) {
				int index = (i - 1) * width + (width - j);

				if (boxMap.get(index)) {
					sum += 100 * i + j;
				}
			}
		}

		return sum;
	}

	public void printWarehouse() {
		StringBuilder builder = new StringBuilder();
		builder.append("#".repeat(width + 2)).append("\n");

		for (int i = 1; i <= height; i++) {
			builder.append("#");
			for (int j = 1; j <= width; j++) {
				int index = (i - 1) * width + (width - j);

				if (robotLocation.x() == j && robotLocation.y() == i) {
					builder.append("@");
				} else if (wallMap.get(index)) {
					builder.append("#");
				} else if (boxMap.get(index)) {
					builder.append("O");
				} else {
					builder.append(".");
				}
			}
			builder.append("#\n");
		}

		System.out.println(builder.toString());
	}
}
