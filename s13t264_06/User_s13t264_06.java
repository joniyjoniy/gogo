package data.strategy.user.s13t264_06;

import sys.game.GameBoard;
import sys.game.GameCompSub;
import sys.game.GameHand;
import sys.game.GamePlayer;
import sys.game.GameState;
import sys.struct.GogoHand;
import sys.user.GogoCompSub;

public class User_s13t264_06 extends GogoCompSub {

//====================================================================
//  コンストラクタ
//====================================================================

  public User_s13t264_06(GamePlayer player) {
    super(player);
    name = "s13t264_06";    // プログラマが入力

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

    // 評価値の出力
    //showValue();

    //--  着手の決定
    return desideHand();

  }
//----------------------------------------------------------------
//  置石チェック
//----------------------------------------------------------------

  public void init_values(GameState prev, GameBoard board) {
    this.size = board.SX;
    // 自分進行用評価配列
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
    int mycolor = role;  // 自分の石の色
    // 相手の妨害用評価配列
    int [][] inValues = new int[board.SX][board.SX];

    // 自分の手を進める用の評価値
    final int FORM_FIVE      = 10000; // 完5連
    final int PREV_UP        =  5000; // 石取り阻止
    final int FORM_FOUR      =  2000; // 完4連
    final int FORM_PRO_FOUR  =   900; // 仮4連
    final int FORM_THREE     =   500; // 完3連
    final int FORM_PRO_THREE =   450; // 仮3連
    final int FORM_TWO       =   100; // 完2連
    final int FORM_PRO_TWO   =    70; // 仮1連
    final int FORM_ONE       =    50; // 完1連
    final int FOUL           =    -1; // 禁じ手

    final boolean DEBUG = false; // デバッグ出力用

    if (DEBUG) { System.out.printf("\nGoing My Way\n\n"); }
    // ---------------------------------------------
    // 自分の手を進める
    // ---------------------------------------------
    //--  各マスの評価値
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        // 埋まっているマスはスルー
        if (values[i][j] == -2) { continue; }
        // TODO 33処理
        for (int dx = -1; dx <= 1; dx++) {
          for (int dy = -1; dy <= 1; dy++) {
            int formerLength    = 0;     // 前方の連長
            int backLength      = 0;     // 後方の連長
            boolean formerEnemy = false; // 前方の敵石
            boolean backEnemy   = false; // 後方の敵石
            boolean formerWall  = false; // 前方の壁
            boolean backWall    = false; // 後方の壁
            
            // 禁じ手もしくは取られる場所はスルー
            if (values[i][j] == FOUL) { continue; }
            if (dx == 0 && dy == 0) { continue; }

            // 前方の連長
            formerLength = checkRunDir(cell, mycolor, i, j, dx, dy);
            // 後方の連長
            backLength = checkRunDir(cell, mycolor, i, j, dx*-1, dy*-1);
            // 前方の連の先の敵
            formerEnemy = checkEnemyDir(cell, mycolor*-1, i, j, dx, dy, formerLength);
            // 後方の連の先の敵
            backEnemy = checkEnemyDir(cell, mycolor*-1, i, j, dx*-1, dy*-1, backLength);
            // 前方の連の先の壁
            formerWall = checkWall(i, j, dx, dy, formerLength);
            // 後方の連の先の壁
            backWall = checkWall(i, j, dx*-1, dy*-1, backLength);

            // 前方の連長で場合分け
            switch (formerLength) {
              case 4: //4連
                if (backLength == 0) {
                  if (values[i][j] < FORM_FIVE) {
                    if (DEBUG) { System.out.printf("4X\n"); }
                    values[i][j] = FORM_FIVE;
                  }
                }
               break;
              case 3: // 3連
                switch (backLength) {
                  case 0: // 3連のみ
                    if (!formerEnemy && !backEnemy && !formerWall && !backWall) {
                      if (values[i][j] < FORM_FOUR) {
                        if (DEBUG) { System.out.printf("3X\n"); }
                        values[i][j] = FORM_FOUR;
                      }

                    } else if ( (formerEnemy || formerWall) && (backEnemy || backWall) ) {}
                    else {
                      if (values[i][j] < FORM_PRO_FOUR) {
                        if (DEBUG) { System.out.printf("Z3X or 3XZ or W3X or 3XW\n"); }
                        values[i][j] = FORM_PRO_FOUR;
                      }
                    }
                    break;
                  case 1: // 3空1
                    if (values[i][j] < FORM_FIVE) {
                      if (DEBUG) { System.out.printf("3X1\n"); }
                      values[i][j] = FORM_FIVE;
                    }
                    break;
                  default: // その他
                    break;
                }
                break;
              case 2: // 2連
                switch (backLength) {
                  case 0: // 2連のみ
                    if (!formerEnemy && !backEnemy && !formerWall && !backWall) {
                      if ( is33(cell, mycolor, i, j, dx, dy, 2) ) {
                        if (DEBUG) { System.out.printf("33\n"); }
                        values[i][j] = FOUL;
                      }
                      else if (values[i][j] < FORM_THREE) {
                        if (DEBUG) { System.out.printf("2X\n"); }
                        values[i][j] = FORM_THREE;
                      }
                    }
                    else if (formerEnemy) {
                      if (values[i][j] < PREV_UP) {
                        // 石取り阻止
                        if (DEBUG) { System.out.printf("Z2X\n"); }
                        values[i][j] = PREV_UP;
                      }
                    }
                    else if (formerWall && (backEnemy || backWall) ) {}
                    else {
                      if (values[i][j] < FORM_PRO_THREE) {
                        if (DEBUG) { System.out.printf("W2X or 2XW or 2XZ\n"); }
                        values[i][j] = FORM_PRO_THREE;
                      }
                    }
                    break;
                  case 1: // 2空1
                    if (!formerEnemy && !backEnemy && !formerWall && !backWall) {
                      if (values[i][j] < FORM_FOUR) {
                        if (DEBUG) { System.out.printf("2X1\n"); }
                        values[i][j] = FORM_FOUR;
                      }
                    }
                    else if (formerEnemy) {
                      if (values[i][j] < PREV_UP) {
                        // 石取り阻止
                        if (DEBUG) { System.out.printf("Z2X1\n"); }
                        values[i][j] = PREV_UP;
                      }
                    }
                    else if ( formerWall && (backEnemy || backWall) ) {}
                    else {
                      if (values[i][j] < FORM_PRO_FOUR) {
                        if (DEBUG) { System.out.printf("2X1Z or 2X1W or W2X1\n"); }
                        values[i][j] = FORM_PRO_FOUR;
                      }
                    }
                    break;
                  case 2: // 2空2
                    if (values[i][j] < FORM_FIVE) {
                      if (DEBUG) { System.out.printf("2X2\n"); }
                      values[i][j] = FORM_FIVE;
                    }
                    break;
                  default: // その他
                    break;
                }
                break;
              case 1: // 1連
                switch(backLength) {
                  case 0: // 1のみ
                    if (!formerEnemy && !backEnemy && !formerWall && !backWall) {
                      if (values[i][j] < FORM_TWO) {
                        if (DEBUG) { System.out.printf("1X\n"); }
                        values[i][j] = FORM_TWO;
                      }
                    } else if ( (formerWall || backWall) && (!formerEnemy && !backEnemy) ) {
                      if (values[i][j] < FORM_PRO_TWO) {
                        if (DEBUG) { System.out.printf("W1X or 1XW\n"); }
                      }
                    }
                    break;
                  case 1: // 1空1
                    if (!formerEnemy && !backEnemy && !formerWall && !backWall) {
                      if ( is33(cell, mycolor, i, j, dx, dy, 1) ) {
                        if (DEBUG) { System.out.printf("33\n"); }
                        values[i][j] = FOUL;
                      }
                      if (values[i][j] < FORM_THREE) {
                        if (DEBUG) { System.out.printf("1X1\n"); }
                        values[i][j] = FORM_THREE;
                      }
                    }
                    else if ( (formerEnemy || formerWall) && (backEnemy || backWall) ) {}
                    else {
                      if (values[i][j] < FORM_PRO_THREE) {
                        if (DEBUG) { System.out.printf("Z1X1 or W1X1 or 1X1Z or 1X1W\n"); }
                        values[i][j] = FORM_PRO_THREE;
                      }
                    }
                    break;
                  default: // その他
                    break;
                }
                break;
              default: // 連無し
                break;
            }
          }
        } // 方向ループ終わり
        // --------------------------------------------------------------------


        // --------------------------------------------------------------------
        // 相手の手を崩す
        // --------------------------------------------------------------------
        //System.out.printf("\nInterference Enemy\n\n");

        // 相手の手を崩す用の評価値
        final int IN_FIVE  =  9000; // 5連妨害
        final int STONE_UP =  5000; // 石取り
        final int IN_ONE   =  1000; // 1連妨害
        final int IN_FOUR  =  4000; // 4連妨害
        final int IN_THREE =  3000; // 3連妨害

        for (int dx = -1; dx <= 1; dx++) {
          for (int dy = -1; dy <= 1; dy++) {

            int formerLength    = 0;     // 前方の連長
            int backLength      = 0;     // 後方の連長
            boolean formerEnemy = false; // 前方の自石
            boolean backEnemy   = false; // 後方の自石
            boolean formerWall  = false; // 前方の壁
            boolean backWall    = false; // 後方の壁

            // 禁じ手もしくは取られる場所はスルー
            if (inValues[i][j] == -1) { continue; }
            if (dx == 0 && dy == 0) { continue; }

            // 前方の連長
            formerLength = checkRunDir(cell, mycolor*-1, i, j, dx, dy);
            // 後方の連長
            backLength = checkRunDir(cell, mycolor*-1, i, j, dx*-1, dy*-1);
            // 前方の連の先の敵
            formerEnemy = checkEnemyDir(cell, mycolor, i, j, dx, dy, formerLength);
            // 後方の連の先の敵
            backEnemy = checkEnemyDir(cell, mycolor, i, j, dx*-1, dy*-1, backLength);
            // 前方の連の先の壁
            formerWall = checkWall(i, j, dx, dy, formerLength);
            // 後方の連の先の壁
            backWall = checkWall(i, j, dx*-1, dy*-1, backLength);

            // 前方の連長で場合分け
            switch (formerLength) {
              case 4: //4連
                if (backLength == 0) {
                  // 5連阻止
                  if (inValues[i][j] < IN_FIVE) {
                    if (DEBUG) { System.out.printf("4X\n"); }
                    inValues[i][j] = IN_FIVE;
                  }
                }
                break;
		          case 3: // 3連
                switch (backLength) {
		              case 0: // 3連のみ
                    if (formerEnemy || backEnemy || formerWall || backWall) {
                      if (DEBUG) { System.out.printf("A3X or 3XA or W3X or 3XW\n"); }
                      // なんとかなる
			                break;
                    }
                    // 3連妨害
                    if (inValues[i][j] < IN_THREE) {
                      if (DEBUG) { System.out.printf("3X\n"); }
                      inValues[i][j] = IN_THREE;
                    }
                    break;
		              case 1: // 3空1
                    // 5連阻止
			              if (inValues[i][j] < IN_FIVE) {
                      if (DEBUG) { System.out.printf("3X1\n"); }
                      inValues[i][j] = IN_FIVE;
                    }
                    break;
		              default:
                    break;
                }
                break;
		          case 2: // 2連
                if (formerWall && backWall) {
                  // 無視
                  if (DEBUG) { System.out.printf("W2XW\n"); }
			            break;
                }
                switch (backLength) {
		              case 2: // 2空2
                    // 5連阻止
			              if (inValues[i][j] < IN_FIVE) {
                      if (DEBUG) { System.out.printf("2X2\n"); }
                      inValues[i][j] = IN_FIVE;}
                    break;
		              case 1: // 2空1
                    if (!formerEnemy && !backEnemy) {
                      // ピンチ
                      if (inValues[i][j] < IN_FIVE) {
                        if (DEBUG) { System.out.printf("2X1\n"); }
                        inValues[i][j] = IN_FIVE;
                      }
                    }
                    if (!formerEnemy && backEnemy) {
                      // 石取り前
			                if (inValues[i][j] < IN_ONE) {
                        if (DEBUG) { System.out.printf("2X1Z\n"); }
                        inValues[i][j] = IN_ONE;
                      }
			                break;
                    }
                    if (formerEnemy) {
                      // 石取り
			                if (inValues[i][j] < STONE_UP) {
                        if (DEBUG) { System.out.printf("Z2X1\n"); }
                        inValues[i][j] = STONE_UP;
                      }
                    }
                    break;
		              case 0: // 2のみ
                    // 石取り前
                    if (formerEnemy) {
                      if (inValues[i][j] < STONE_UP) {
                        if (DEBUG) { System.out.printf("A2X\n"); }
                        inValues[i][j] = STONE_UP;
                      }
                    }
			              if (inValues[i][j] < IN_ONE) {
                      if (DEBUG) { System.out.printf("2X\n"); }
                      inValues[i][j] = IN_ONE;
                    }
		              default:
                    break;
                }
                break;
              case 1: // 1連
                switch(backLength) {
		              case 1: // 1空1
                    if (formerEnemy || backEnemy) {
                      // 気にしなくてもOK
                      if (DEBUG) { System.out.printf("A1X1 or 1X1A\n"); }
                      break;
                    }
                    if (!formerEnemy && !backEnemy && !formerWall && !backWall) {
                      // 何も邪魔するものがなければ間に置く
                      if (inValues[i][j] < IN_ONE) {
                        if (DEBUG) { System.out.printf("1X1\n"); }
                        inValues[i][j] = IN_ONE;
                      }
                    }
                    break;
		              case 0: // 1のみ
                    if (!formerEnemy && !backEnemy) {
                      // 石取り前
			                if (inValues[i][j] < IN_ONE) {
                        if (DEBUG) { System.out.printf("1X\n"); }
                        inValues[i][j] = IN_ONE;
                      }
                    }
                    break;
		              default: // その他
                    break;
                }
                break;
		          default: // 連無し
                break;
            }
	        }
	      } // 方向ループ終わり
        // -------------------------------------

        // ランダム
        if (values[i][j] == 0) {
          int aaa = (int) Math.round(Math.random() * 10);
          if (values[i][j] < aaa) { values[i][j] += aaa; }
        }
        // 評価値に妨害値を加える
        if (inValues[i][j] > 0) {
          values[i][j] += inValues[i][j];
        }

      }
    }// 盤面ループ終わり
  }

//----------------------------------------------------------------
//  連の方向チェック(止連・端連・長連も含む、飛びは無視)
//----------------------------------------------------------------

  int checkRunDir(int[][] board, int color, int i, int j, int dx, int dy) {
    int len = 0;
    for (int k = 1; k < 5; k++) {
      int x = i+k*dx;
      int y = j+k*dy;
      if (x < 0 || y < 0 || x >= size || y >= size) { return len; }
      if (board[x][y] == color) {
        len++;
        continue;
      }
      break;
    }
    return len;
  }

//----------------------------------------------------------------
//  33チェック
//----------------------------------------------------------------

boolean is33(int[][] board, int color, int i, int j, int dx, int dy, int other) {
  for (int ax = -1; ax <= 1; ax++) {
    for (int ay = -1; ay <= 1; ay++) {
      if (ax == dx && ay == dy) { continue; }
      if (ax == dx*-1 && ay == dy*-1) { continue; }

      int afLength = 0; //前方の連長
      int abLength = 0; // 後方の連長
      boolean afEnemy = false; // 前方の敵石
      boolean abEnemy = false; // 後方の敵石
      boolean afWall = false; // 前方の壁
      boolean abWall = false; // 後方の壁

      // 前方の連長
      afLength = checkRunDir(board, color, i, j, ax, ay);
      // 後方の連長
      abLength = checkRunDir(board, color, i, j, ax*-1, ay*-1);
      // 前方の連の先の敵
      afEnemy = checkEnemyDir(board, color*-1, i, j, ax, ay, afLength);
      // 後方の連の先の敵
      abEnemy = checkEnemyDir(board, color*-1, i, j, ax*-1, ay*-1, abLength);
      // 前方の連の先の壁
      afWall = checkWall(i, j, ax, ay, afLength);
      // 後方の連の先の壁
      abWall = checkWall(i, j, ax*-1, ay*-1, abLength);

      switch(other) {
        case 1: // 11の時
          switch(afLength) {
            case 1: // 11 11?
              if (abLength == 1 && !afEnemy && !abEnemy) {
                return true;
              }
              break;
            case 2:  // 11 2?
              if (abLength == 0 && !afEnemy && !abEnemy) {
                return true;
              }
              break;
            default:
              break;
          }
          break;
        case 2: // 2のとき
            if (abLength == 2 && !afEnemy && !abEnemy) {
              return true;
            }
          break;
        default:
          break;
      }
    }
  }
  return false;
}

//----------------------------------------------------------------
//  相手石チェック
//----------------------------------------------------------------

boolean checkEnemyDir(int[][] board, int color, int i, int j, int dx, int dy, int len) {
  int x = i+dx*(len+1);
  int y = j+dy*(len+1);
  if (x < 0 || y < 0 || x >= size || y >= size) { return false; }
  if (board[x][y] == color) { return true; }
  return false;
}

//----------------------------------------------------------------
//  壁チェック
//----------------------------------------------------------------

boolean checkWall(int i, int j, int dx, int dy, int len) {
  int x = i+dx*(len+1);
  int y = j+dy*(len+1);
  if (x < 0 || y < 0 || x >= size || y >= size) { return true; }
  return false;
}

//----------------------------------------------------------------
//  評価値の表示
//----------------------------------------------------------------

  void showValue() {
    int i, j;
    for (i = 0; i < size; i++) {
      System.out.printf(" ");
      for (j = 0; j < size; j++) {
        System.out.printf("%5d ", values[j][i]);
      }
      System.out.printf("\n");
    }
    System.out.printf("\n");
  }
//----------------------------------------------------------------
//  着手の決定
//----------------------------------------------------------------

  GameHand desideHand() {
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
