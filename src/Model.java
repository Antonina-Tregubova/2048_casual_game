

import java.util.*;

public class Model {
    private final static int FIELD_WIDTH = 4;
    private Tile[][] gameTiles /*= {{new Tile(1),new Tile(2),new Tile(3),new Tile(4)},{new Tile(1),new Tile(2),new Tile(3),new Tile(4)},{new Tile(1),new Tile(2),new Tile(3),new Tile(4)},{new Tile(1),new Tile(2),new Tile(3),new Tile(4)}}*/;
    protected int score;
    protected int maxTile;
    private Stack<Tile[][]> previousStates = new Stack<Tile[][]>();
    private Stack<Integer> previousScores = new Stack<Integer>();
    private boolean isSaveNeeded = true;

    public Model() {
        resetGameTiles();
        this.score = 0;
        this.maxTile = 0;
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    private void saveState(Tile[][] tiles) { //метод сохранения игрового хода (поле + счет)
        Tile[][] fieldToSave = new Tile[tiles.length][tiles[0].length];
        isSaveNeeded = false;
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                fieldToSave[i][j] = new Tile(tiles[i][j].getValue());
            }
        }
        previousStates.push(fieldToSave);
        int scoreToSave = score;
        previousScores.push(scoreToSave);
    }

    public void rollback() { //метод отмены хода
        if(!previousScores.isEmpty()&&!previousStates.isEmpty()) {
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }

    public void resetGameTiles() { // сброс всех клеток
        gameTiles = new Tile[Model.FIELD_WIDTH][Model.FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    public void addTile() { // добавляет рандомно клетку 2 или 4 (соотношение 1 к 9)
        List<Tile> emptyTilesList = getEmptyTiles();
        if (!emptyTilesList.isEmpty()) {
            emptyTilesList.get((int) (emptyTilesList.size() * Math.random())).value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    private List<Tile> getEmptyTiles() { // вовзращает лист пустых клеток
        List<Tile> tileList = new ArrayList<>();
        for (Tile[] s : gameTiles) {
            for (Tile emptyTile : s) {
                if (emptyTile.value == 0) tileList.add(emptyTile);
            }
        }
        return tileList;
    }

    public boolean compressTiles(Tile[] tiles) { //сжатие одного ряда влево
        int count = 0;
        boolean somethingChanged = false;
        int[] values = new int[tiles.length];
        for (int g = 0; g < tiles.length; g++) values[g]=tiles[g].value;
        for (int i = 0; i < tiles.length; i++)
        {  if (tiles[i].value != 0)
            tiles[count++].value = tiles[i].value;}

        while (count < tiles.length)
        {tiles[count++].value = 0;
        }
        for (int f = 0; f < tiles.length; f++)
            if(!(values[f]==tiles[f].value))
                somethingChanged=true;
        return somethingChanged;
    }


    private boolean mergeTiles(Tile[] tiles) { // сложение клеток
        int oldScore = score;
        for (int i = 0; i < tiles.length - 1; i++) {
            if (tiles[i].value == tiles[i + 1].value&&tiles[i].value!=0) {
                tiles[i].value = tiles[i].value + tiles[i + 1].value;
                score+=tiles[i].value;
                if(tiles[i].value>this.maxTile) maxTile = tiles[i].value;
                tiles[i + 1].value = 0;
            }
        }
        int count = 0;
        for (int i = 0; i < tiles.length; i++)
            if (tiles[i].value != 0)
                tiles[count++].value = tiles[i].value;

        while (count < tiles.length)
            tiles[count++].value = 0;

        return oldScore!=score;
    }

    public void rotate() { // поворот матрицы на 90 градусов против часовой стрелки
        Tile[][] newArray = new Tile[FIELD_WIDTH][FIELD_WIDTH];

        for (int i = 0; i < FIELD_WIDTH; ++i) {
            for (int j = 0; j < FIELD_WIDTH; ++j) {
                newArray[i][ j] = gameTiles[4 - j - 1][ i];
            }
        }
        gameTiles = newArray;
    }

    //методы движения в четырех напрвлениях
    public void left() {
        if (isSaveNeeded) {
            saveState(gameTiles);
        }
        boolean someTilesHasChanged = false;
        for(Tile[]tiles:gameTiles) {
            if(compressTiles(tiles)|mergeTiles(tiles))someTilesHasChanged=true;
        }
        if(someTilesHasChanged) {
            addTile();
        }
        isSaveNeeded = true;
    }

    public void right() {
        saveState(gameTiles);
        rotate();
        rotate();
        left();
        rotate();
        rotate();
    }

    public void down() {
        saveState(gameTiles);
        rotate();
        left();
        rotate();
        rotate();
        rotate();
    }

    public void up() {
        saveState(gameTiles);
        rotate();
        rotate();
        rotate();
        left();
        rotate();
    }

    public boolean canMove() { //проверяет возможность хода
        if(!getEmptyTiles().isEmpty()) return true;
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 1; j < gameTiles.length; j++) {
                if (gameTiles[i][j].value == gameTiles[i][j - 1].value)
                    return true;
            }
        }
        for (int j = 0; j < gameTiles.length; j++) {
            for (int i = 1; i < gameTiles.length; i++) {
                if (gameTiles[i][j].value == gameTiles[i - 1][j].value)
                    return true;
            }
        }
        return false;
    }

    public void randomMove() { //дает игре возможность самостоятельно выбирать следующий ход
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n) {
            case (0):
                left();
                break;
            case (1):
                right();
                break;
            case (2):
                up();
                break;
            case (3):
                down();
                break;
        }
    }

    public boolean hasBoardChanged() {
        int weightGame = 0;
        int weightStack = 0;
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[i].length; j++) {
                weightGame += gameTiles[i][j].value;
            }
        }
        Tile[][] tiles = (Tile[][])previousStates.peek();
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                weightStack += tiles[i][j].value;
            }
        }
        if (weightGame != weightStack)
            return true;
        return false;
    }

    public MoveEfficiency getMoveEfficiency(Move move) {
        move.move();
        if (!hasBoardChanged()) {
            rollback();
            return new MoveEfficiency(-1, 0, move);
        }
        rollback();
        return new MoveEfficiency(getEmptyTiles().size(), score, move);
    }

    public void autoMove() {
        PriorityQueue<MoveEfficiency> queue = new PriorityQueue<>(4, Collections.reverseOrder());
        queue.add(getMoveEfficiency(this::up));
        queue.add(getMoveEfficiency(this::down));
        queue.add(getMoveEfficiency(this::right));
        queue.add(getMoveEfficiency(this::left));
        queue.peek().getMove().move();
    }
}
