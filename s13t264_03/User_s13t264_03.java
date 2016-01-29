package data.strategy.user.s13t264_03;

import sys.game.GameBoard;
import sys.game.GameCompSub;
import sys.game.GameHand;
import sys.game.GamePlayer;
import sys.game.GameState;
import sys.struct.GogoHand;
import sys.user.GogoCompSub;

public class User_s13t264_03 extends GogoCompSub {

//====================================================================
//  コンストラクタ
//====================================================================

  public User_s13t264_03(GamePlayer player) {
    super(player);
    name = "s13t264_03";    // プログラマが入力

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

    show_value();

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
//  評価値の計算
//----------------------------------------------------------------

  public void calc_values(GameState prev, GameBoard board) {
    int [][] cell = board.get_cell_all();  // 盤面情報
    int mycolor;                  // 自分の石の色
    mycolor = role;
    final int FORMFIVE     = 10000;
    final int FORMFOUR     =  2000;
    final int FORMPROFOUR  =   900;
    final int FORMTHREE    =   500;
    final int FORMPROTHREE =   450;
    final int FORMTWO      =   100;
    final int FORMONE      =    70;
    final int FORMPROONE   =    50;

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
            int formerLength = 0; //前方の連長
            int backLength = 0; // 後方の連長
            boolean formerEnemy = false; // 前方の敵石
            boolean backEnemy = false; // 後方の敵石
            boolean formerWall = false; // 前方の壁
            boolean backWall = false; // 後方の壁
            if ( dx == 0 && dy == 0 ) { continue; }
            formerLength = check_run_dir(cell, mycolor, i, j, dx, dy);
            backLength = check_run_dir(cell, mycolor, i, j, dx*-1, dy*-1);
            formerEnemy = check_enemy_dir(cell, mycolor*-1, i, j, dx, dy, formerLength);
            backEnemy = check_enemy_dir(cell, mycolor*-1, i, j, dx, dy, backLength);
            formerWall = check_wall(i, j, dx, dy, formerLength);
            backWall = check_wall(i, j, dx*-1, dy*-1, backLength);
            switch (formerLength) {
              case 4:
                if ( backLength == 0 ) {
                 values[i][j] += FORMFIVE;
                }
                break;
              case 3:
                switch (backLength) {
                  case 0:
                    if ( backEnemy && !formerEnemy ) {
                      values[i][j] += FORMPROFOUR;
                      break;
                    }
                    if ( formerEnemy ) {
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
              case 2: // 2連時の処理
                if ( formerWall && backWall ) {
                  values[i][j] = 0; break;
                }
                if ( formerEnemy || formerWall || backWall ) {
                  values[i][j] += FORMPROTHREE; break;
                }
                switch (backLength) {
                  case 2:
                    values[i][j] += FORMFIVE;
                    break;
                  case 1:
                    if (!formerEnemy && backEnemy) {
                      values[i][j] += FORMPROFOUR;
                      break;
                    }
                    if (formerEnemy) {
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
                switch(backLength) {
                  case 1:
                    if (backEnemy && !formerEnemy) {
                      values[i][j] += FORMPROTHREE;
                      break;
                    }
                    if (!backEnemy) {
                      if (formerEnemy) {
                        values[i][j] += FORMPROTHREE;
                      } else {
                        values[i][j] += FORMTHREE;
                      }
                    }
                    break;
                  case 0:
                    if (!formerEnemy && !backEnemy) {
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
          int aaa = (int) Math.round(Math.random() * 10);
          if (values[i][j] < aaa) { values[i][j] += aaa; }
        }
        */
      }
    }
  }

//----------------------------------------------------------------
//  連の方向チェック(止連・端連・長連も含む、飛びは無視)
//----------------------------------------------------------------

  int check_run_dir(int[][] board, int color, int i, int j, int dx, int dy) {
    int len = 0;
    for ( int k = 1; k < 5; k++ ) {
      int x = i+k*dx;
      int y = j+k*dy;
      if ( x < 0 || y < 0 || x >= size || y >= size ) { return len; }
      if ( board[x][y] == color ) {
        len++;
        continue;
      }
      break;
    }
    return len;
  }


//----------------------------------------------------------------
//  相手石チェック
//----------------------------------------------------------------

boolean check_enemy_dir(int[][] board, int color, int i, int j, int dx, int dy, int len) {
  int x = i+dx*len;
  int y = j+dy*len;
  if ( x < 0 || y < 0 || x >= size || y >= size ) { return false; }
  if ( board[x][y] == color ) { return true; }
  return false;
}

//----------------------------------------------------------------
//  壁チェック
//----------------------------------------------------------------

boolean check_wall(int i, int j, int dx, int dy, int len) {
  int x = i+dx*(len+1);
  int y = j+dy*(len+1);
  if ( x < 0 || y < 0 || x >= size || y >= size ) { return true; }
  return false;
}

//----------------------------------------------------------------
//  評価値の表示
//----------------------------------------------------------------

  void show_value() {
    int i, j;
    for ( i = 0; i < size; i++ ) {
      System.out.printf("|");
      for ( j = 0; j < size; j++ ) {
        System.out.printf("%2d|", values[j][i]);
      }
      System.out.printf("\n");
    }
    System.out.printf("\n");
  }
//----------------------------------------------------------------
//  着手の決定
//----------------------------------------------------------------

  public GameHand deside_hand() {
    GogoHand hand = new GogoHand();
    hand.set_hand(size-1, size-1);  // 左上をデフォルトのマスとする
    int value = -1;       // 評価値のデフォルト
    //--  評価値が最大となるマス
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (value < values[i][j]) {
          hand.set_hand(i, j);
          value = values[i][j];
        }
      }
    }
    return hand;
  }

}
