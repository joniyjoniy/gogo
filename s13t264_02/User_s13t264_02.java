package data.strategy.user.s13t264_02;

import sys.game.GameBoard;
import sys.game.GameCompSub;
import sys.game.GameHand;
import sys.game.GamePlayer;
import sys.game.GameState;
import sys.struct.GogoHand;
import sys.user.GogoCompSub;

public class User_s13t264_02 extends GogoCompSub {

//====================================================================
//  コンストラクタ
//====================================================================

  public User_s13t264_02(GamePlayer player) {
    super(player);
    name = "s13t264_02";    // プログラマが入力

  }

//--------------------------------------------------------------------
//  コンピュータの着手
//--------------------------------------------------------------------

  public synchronized GameHand calc_hand(GameState state, GameHand hand) {
    theState = state;
    theBoard = state.board;
    lastHand = hand;

    //--  置石チェック
    init_values(theState, theBoard);

    //--  評価値の計算
    calc_values(theState, theBoard);
    // 先手後手、取石数、手数(序盤・中盤・終盤)で評価関数を変える

    //--  着手の決定
    return deside_hand();

  }
//----------------------------------------------------------------
//  置石チェック
//----------------------------------------------------------------

  public void init_values(GameState prev, GameBoard board) {
    this.size = board.SX;
    values = new int[size][size];
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (board.get_cell(i, j) != board.SPACE) {
          values[i][j] = -2;
        } else {
          if (values[i][j] == -2) {
            values[i][j] = 0;
          }
        }
      }
    }
  }

//----------------------------------------------------------------
// 方向格納用クラス
//----------------------------------------------------------------
  public static class lineOfFire {
    public static boolean formerEnemy;
    public static int formerStrand;
    public static boolean backEnemy;
    public static int backStrand;
  }

  public static class lineFront {
    public static boolean enemy;
    public static int strand;
  }

//----------------------------------------------------------------
//  評価値の計算
//----------------------------------------------------------------

  public void calc_values(GameState prev, GameBoard board) {
    int [][] cell = board.get_cell_all();  // 盤面情報
    int mycolor;                  // 自分の石の色
    mycolor = role;
    final int FORMFIVE     = 10000;
    final int FORMFOUR     =   750;
    final int FORMPROFOUR  =   600;
    final int FORMTHREE    =   400;
    final int FORMPROTHREE =   350;
    final int FORMTWO      =   300;
    final int FORMONE      =   140;
    final int FORMPROONE   =   130;
    lineOfFire current = new lineOfFire();

    //--  各マスの評価値
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        // 埋まっているマスはスルー
        if (values[i][j] == -2) { continue; }
        // ---------------------------------------------
        // 自分の手を進める
        // ---------------------------------------------
        // TODO 33処理
        for ( int dx = -1; dx <= 1; dx++ ) {
          for ( int dy = -1; dy <= 1; dy++ ) {
            current = check_run(cell, mycolor, i, j, dx, dy);
            switch (current.formerStrand) {
              case 4:
                if ( current.backStrand == 0 ) {
                 values[i][j] += FORMFIVE;
                }
                break;
              case 3:
                switch (current.backStrand) {
                  case 0:
                    if ( current.backEnemy && !current.formerEnemy ) {
                      values[i][j] += FORMPROFOUR;
                      break;
                    }
                    if ( current.formerEnemy ) {
                      values[i][j] += FORMPROFOUR;
                      break;
                    }
                    values[i][j] += FORMFOUR;
                    break;
                  case 1:
                    values[i][j] += FORMFIVE;
                    break;
                  default:
                    break;
                }
                break;
              case 2:
                if ( current.formerEnemy ) {
                  values[i][j] += FORMPROTHREE;
                }
                switch (current.backStrand) {
                  case 2:
                    values[i][j] += FORMFIVE;
                    break;
                  case 1:
                    if (!current.formerEnemy && current.backEnemy) {
                      values[i][j] += FORMPROFOUR;
                      break;
                    }
                    if (current.formerEnemy) {
                      values[i][j] += FORMPROFOUR;
                    } else {
                      values[i][j] += FORMFOUR;
                    }
                    break;
                  case 0:
                    values[i][j] += FORMTHREE;
                  default:
                    break;
                }
                break;
              case 1:
                switch(current.backStrand) {
                  case 1:
                    if (current.backEnemy && !current.formerEnemy) {
                      values[i][j] += FORMPROTHREE;
                      break;
                    }
                    if (!current.backEnemy) {
                      if (current.formerEnemy) {
                        values[i][j] += FORMPROTHREE;
                      } else {
                        values[i][j] += FORMTHREE;
                      }
                    }
                    break;
                  case 0:
                    if (!current.formerEnemy && !current.backEnemy) {
                      values[i][j] += FORMTWO;
                    }
                    break;
                  default:
                    break;
                }
                break;
              default:
                break;
            }
          }
        }

        // ランダム
        /*
        if (values[i][j] == 0) {
          int aaa = (int) Math.round(Math.random() * 15);
          if (values[i][j] < aaa) { values[i][j] = aaa; }
        }
        */
      }
    }
  }

//----------------------------------------------------------------
//  連の全周チェック
//----------------------------------------------------------------

  lineOfFire check_run(int[][] board, int color, int i, int j, int dx, int dy) {
    // [i][j]の盤面上の現在地から8方向に連判定
    lineOfFire line = new lineOfFire();
    lineFront front = new lineFront();

    front = check_run_dir(board, color, i, j, dx, dy);
    line.formerEnemy = front.enemy;
    line.formerStrand = front.strand;
    front = check_run_dir(board, color, i, j, dx*-1, dy*-1);
    line.backEnemy = front.enemy;
    line.backStrand = front.strand;
    return line;
  }

//----------------------------------------------------------------
//  連の方向チェック(止連・端連・長連も含む、飛びは無視)
//----------------------------------------------------------------

  lineFront check_run_dir(int[][] board, int color, int i, int j, int dx, int dy) {
    lineFront now = new lineFront();
    for ( int k = 1; k < 5; k++ ) {
      int x = i+k*dx;
      int y = j+k*dy;
      if ( x < 0 || y < 0 || x >= size || y >= size ) { return now; }
      if ( board[x][y] == 0) { return now; }
      if ( board[x][y] == color*-1 ) {
        now.enemy = true;
        return now;
      }
      now.strand++;
    }
    return now;
  }

//----------------------------------------------------------------
//  取の全周チェック(ダブルの判定は無し)
//----------------------------------------------------------------

  boolean check_rem(int [][] board, int color, int i, int j) {
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( check_rem_dir(board, color, i, j, dx, dy) ) { return true; }
      }
    }
    return false;
  }

//----------------------------------------------------------------
//  取の方向チェック
//----------------------------------------------------------------

  boolean check_rem_dir(int[][] board, int color, int i, int j, int dx, int dy) {
    int len = 3;
    for ( int k = 1; k <= len; k++ ) {
      int x = i+k*dx;
      int y = j+k*dy;
      if ( x < 0 || y < 0 || x >= size || y >= size ) { return false; }
      if ( board[x][y] != color ) { return false; }
      if (k == len-1) { color *= -1; }
    }
    return true;
  }
//----------------------------------------------------------------
//  着手の決定
//----------------------------------------------------------------

  public GameHand deside_hand() {
    GogoHand hand = new GogoHand();
    hand.set_hand(size-1, size-1);  // 左上をデフォルトのマスとする
    int value = -1;       // 評価値のデフォルト
    //--  評価値が最大となるマス
    for (int i = size-1; i >= 0; i--) {
      for (int j = size-1; j >= 0; j--) {
        if (value < values[i][j]) {
          hand.set_hand(i, j);
          value = values[i][j];
        }
      }
    }
    return hand;
  }

}
