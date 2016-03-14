package data.strategy.user.s13t264_07;

import sys.game.GameBoard;
import sys.game.GameCompSub;
import sys.game.GameHand;
import sys.game.GamePlayer;
import sys.game.GameState;
import sys.struct.GogoHand;
import sys.user.GogoCompSub;

public class User_s13t264_07 extends GogoCompSub {
  private static final boolean DEBUG      = false; // デバッグ出力用
  // 自分の手を進める用の評価値
  private static final int FORM_FIVE      = 10000; // 完5連
  private static final int PREV_UP        =  5000; // 石取り阻止
  private static final int FORM_FOUR      =  2500; // 完4連
  private static final int FORM_PRO_FOUR  =   900; // 仮4連
  private static final int FORM_THREE     =  2000; // 完3連
  private static final int FORM_PRO_THREE =   450; // 仮3連
  private static final int FORM_TWO       =   100; // 完2連
  private static final int FORM_PRO_TWO   =    70; // 仮1連
  private static final int FORM_ONE       =    50; // 完1連
  private static final int FOUL           =    -1; // 禁じ手
  // 相手の手を崩す用の評価値
  private static final int IN_FIVE        =  9000; // 5連妨害
  private static final int STONE_UP       =  5000; // 石取り
  private static final int IN_ONE         =  1000; // 1連妨害
  private static final int IN_FOUR        =  4000; // 4連妨害
  private static final int IN_THREE       =  3000; // 3連妨害

//====================================================================
//  コンストラクタ
//====================================================================

  public User_s13t264_07(GamePlayer player) {
    super(player);
    name = "s13t264_07";    // プログラマが入力
  }

//--------------------------------------------------------------------
//  コンピュータの着手
//--------------------------------------------------------------------

  public synchronized GameHand calc_hand(GameState state, GameHand hand) {
    theState = state;
    theBoard = state.board;
    lastHand = hand;

    //--  置石チェック
    initValues(theState, theBoard);
    //--  評価値の計算
    calcValues(theState, theBoard);
    // TODO 先手後手、取石数、手数(序盤・中盤・終盤)で評価関数を変える
    // 評価値の出力
    //showValue();
    //--  着手の決定
    return desideHand();
  }

//----------------------------------------------------------------
//  置石チェック
//----------------------------------------------------------------

  public void initValues(GameState prev, GameBoard board) {
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

  public void calcValues(GameState prev, GameBoard board) {
    int [][] cell = board.get_cell_all();  // 盤面情報
    int mycolor = role;  // 自分の石の色

    //--  各マスの評価
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        // 埋まっているマスはスルー
        if (values[i][j] == -2) { continue; }
        // 自分の手を進める
        //if (DEBUG) { System.out.printf("\nProceed My Hand\n\n"); }
        values[i][j] = proceedMyHand(cell, i, j, mycolor);
        // 相手の手を崩す
        if (values[i][j] == FOUL) { continue; }
        //if (DEBUG) { System.out.printf("\nInterference Enemy\n\n"); }
        values[i][j] += interferenceOpponent(cell, i, j, mycolor);
        // ランダム
        if (values[i][j] == 0) {
          values[i][j] = (int) Math.round(Math.random() * 15);
        }
      } // jループ
    } // iループ
  }

  //----------------------------------------------------------------
  //  自分の手を進める
  //----------------------------------------------------------------

  int proceedMyHand(int[][] cell, int i, int j, int color) {
    int proceedValue    = 0;     // 自手評価値
    int dirValue        = 0;     // 方向評価値
    int formerLength    = 0;     // 前方の連長
    int backLength      = 0;     // 後方の連長
    boolean formerEnemy = false; // 前方の敵石
    boolean backEnemy   = false; // 後方の敵石
    boolean formerWall  = false; // 前方の壁
    boolean backWall    = false; // 後方の壁
    boolean completeRun  = false; // 完連
    for (int dx = -1; dx <= 1; dx++) {
      for (int dy = -1; dy <= 1; dy++) {
        // 禁じ手もしくは取られる場所はスルー
        if (proceedValue == FOUL) { continue; }
        if (dx == 0 && dy == 0) { continue; }
        formerLength = isRunDir(cell, color, i, j, dx, dy);
        backLength   = isRunDir(cell, color, i, j, dx*-1, dy*-1);
        formerEnemy  = isEnemyDir(cell, color*-1, i, j, dx, dy, formerLength);
        backEnemy    = isEnemyDir(cell, color*-1, i, j, dx*-1, dy*-1, backLength);
        formerWall   = isWall(i, j, dx, dy, formerLength);
        backWall     = isWall(i, j, dx*-1, dy*-1, backLength);
        completeRun  = isCompleteRun(formerEnemy, backEnemy, formerWall, backWall);

        // is33 form 2*2
        if (formerLength == 2 && backLength == 0 && completeRun) {
          if (is33(cell, color, i, j, dx, dy, 2)) { return proceedValue = FOUL; }
          else if (proceedValue < FORM_THREE) { proceedValue = FORM_THREE; continue; }
        }
        // is33 from 1*1*1*1 or 1*1*2
        if (formerLength == 1 && backLength == 1 && completeRun) {
          if (is33(cell, color, i, j, dx, dy, 1)) { return proceedValue = FOUL; }
          else if (proceedValue < FORM_THREE) { proceedValue = FORM_THREE; continue; }
        }
        // is43
        if (formerLength == 3 && !isBetweenObstacle(formerEnemy, backEnemy, formerWall, backWall)) {
          if (is43(cell, color, i, j, dx, dy, 3)) { return proceedValue = FORM_FIVE; }
        }

        dirValue = proceedMyHandDir(formerLength, backLength, formerEnemy, backEnemy, formerWall, backWall);
        if (proceedValue < dirValue) {
          proceedValue = dirValue;
          if (DEBUG) {
            showRecognition(i, j, dx, dy, formerLength, backLength, formerEnemy, backEnemy, formerWall, backWall);
          }
        }
      }
    } // 方向ループ終わり
    return proceedValue;
  }

  //----------------------------------------------------------------
  //  相手妨害評価
  //----------------------------------------------------------------

  int interferenceOpponent(int[][] cell, int i, int j, int color) {
    int inValue         = 0;     // 妨害評価値
    int dirValue        = 0;     // 方向評価
    int formerLength    = 0;     // 前方の連長
    int backLength      = 0;     // 後方の連長
    boolean formerEnemy = false; // 前方の自石
    boolean backEnemy   = false; // 後方の自石
    boolean formerWall  = false; // 前方の壁
    boolean backWall    = false; // 後方の壁
    boolean completeRun = false; // 完連
    for (int dx = -1; dx <= 1; dx++) {
      for (int dy = -1; dy <= 1; dy++) {
        if (inValue == FOUL) { continue; }
        if (dx == 0 && dy == 0) { continue; }
        formerLength = isRunDir(cell, color*-1, i, j, dx, dy);
        backLength = isRunDir(cell, color*-1, i, j, dx*-1, dy*-1);
        formerEnemy = isEnemyDir(cell, color, i, j, dx, dy, formerLength);
        backEnemy = isEnemyDir(cell, color, i, j, dx*-1, dy*-1, backLength);
        formerWall = isWall(i, j, dx, dy, formerLength);
        backWall = isWall(i, j, dx*-1, dy*-1, backLength);
        completeRun = isCompleteRun(formerEnemy, backEnemy, formerWall, backWall);
        // is43
        if (formerLength == 3 && !isBetweenObstacle(formerEnemy, backEnemy, formerWall, backWall)) {
          if (is43(cell, color, i, j, dx, dy, 3)) { return inValue = IN_FIVE; }
        }

        dirValue = interferenceOpponentDir(formerLength, backLength, formerEnemy, backEnemy, formerWall, backWall);
        if (inValue < dirValue) {
          inValue = dirValue;
          if (DEBUG) {
            showRecognition(i, j, dx, dy, formerLength, backLength, formerEnemy, backEnemy, formerWall, backWall);
          }
        }
      }
    } // 方向ループ終わり
    return inValue;
  }

  //----------------------------------------------------------------
  //  方向自手評価
  //----------------------------------------------------------------

  int proceedMyHandDir(int fL, int bL, boolean fE, boolean bE, boolean fW, boolean bW) {
    // 4X
    if (fL == 4 && bL == 0) { return FORM_FIVE; }
    // 4X1~
    if (fL == 4) { return 0; }
    // 3X
    if (fL == 3 && bL == 0 && isCompleteRun(fE, bE, fW, bW)) { return FORM_FOUR; }
    // Z3XZ Z3XW W3XZ W3XW
    if (fL == 3 && bL == 0 && isBetweenObstacle(fE, bE, fW, bW)) { return 0; }
    // Z3X or 3XZ or W3X or 3XW\
    if (fL == 3 && bL == 0) { return FORM_PRO_FOUR; }
    // 3X1
    if (fL == 3 && bL == 1) { return FORM_FIVE; }
    // 3X2~
    if (fL == 3) { return 0; }
    // is33 and 2X in upper method
    // 2XZ
    if (fL == 2 && bL == 0 && fE) { return PREV_UP; }
    //
    if (fL == 2 && bL == 0 && fW && (bE || bW)) { return 0; }
    // W2X or 2XW or 2XZ
    if (fL == 2 && bL == 0) { return FORM_PRO_THREE; }
    // 2X1
    if (fL == 2 && bL == 1 && isCompleteRun(fE, bE, fW, bW)) { return FORM_FOUR; }
    // Z2X1
    if (fL == 2 && bL == 1 && fE) { return PREV_UP; }
    //
    if (fL == 2 && bL == 1 && fW && (bE || bW)) { return 0; }
    // 2X1Z or 2X1W or W2X1
    if (fL == 2 && bL == 1) { return FORM_PRO_FOUR; }
    // 2X2
    if (fL == 2 && bL == 2) { return FORM_FIVE; }
    // 2X3~
    if (fL == 2) { return 0; }
    // 1X
    if (fL == 1 && bL == 0 && isCompleteRun(fE, bE, fW, bW)) { return FORM_TWO; }
    // W1X 1XW
    if (fL == 1 && bL == 0 && (fW || bW) && (!fE && !bE)) { return FORM_PRO_TWO; }
    // is33 and 1X1 in upper method
    //
    if (fL == 1 && bL == 1 && (fW || bW) && (!fE && !bE)) { return 0; }
    // Z1X1 or W1X1 or 1X1Z or 1X1W
    if (fL == 1 && bL == 1) { return FORM_PRO_THREE; }
    return 0;
  }

  //----------------------------------------------------------------
  //  方向妨害評価
  //----------------------------------------------------------------

  int interferenceOpponentDir(int fL, int bL, boolean fE, boolean bE, boolean fW, boolean bW) {
    // 4X
    if (fL == 4 && bL == 0) { return IN_FIVE; }
    // 4X1~
    if (fL == 4) { return 0; }
    // A3X or 3XA or W3X or 3XW
    if (fL == 3 && bL == 0 && isAnyObstacle(fE, bE, fW, bW)) { return 0; }
    // 3X
    if (fL == 3 && bL == 0) { return IN_THREE; }
    // 3X1
    if (fL == 3 && bL == 1) { return IN_FIVE; }
    // W2XW
    if (fL == 2 && fW && bW) { return 0; }
    // 2X2
    if (fL == 2 && bL == 2) { return IN_FIVE; }
    // 2X1
    if (fL == 2 && bL == 1 && !fE && !bE) { return IN_FIVE; }
    // 2X1A TODO think value
    if (fL == 2 && bL == 1 && !fE && bE) { return IN_ONE; }
    // A2X1
    if (fL == 2 && bL == 1 && fE) { return STONE_UP; }
    // A2X
    if (fL == 2 && bL == 0 && fE) { return STONE_UP; }
    // 2X TODO think value
    if (fL == 2 && bL == 0) { return IN_ONE; }
    // A1X1 1X1A
    if (fL == 1 && bL == 1 && (fE || bE)) { return 0; }
    // 1X1 TODO think value
    if (fL == 1 && bL == 1 && isCompleteRun(fE, bE, fW, bW)) { return IN_ONE; }
    // 1 TODO think value
    if (fL == 1 && bL == 0 && !fE && !bE) { return IN_ONE; }
    return 0;
  }

  //----------------------------------------------------------------
  //  43チェック
  //----------------------------------------------------------------

  boolean is43(int[][] board, int color, int i, int j, int dx, int dy, int now) {
    int otherFormerLength    = 0;     //前方の連長
    int otherBackLength      = 0;     // 後方の連長
    boolean otherFormerEnemy = false; // 前方の敵石
    boolean otherBackEnemy   = false; // 後方の敵石
    boolean otherFormerWall  = false; // 前方の壁
    boolean otherBackWall    = false; // 後方の壁
    boolean completeRun      = false; // 完連判定
    for (int ax = -1; ax <= 1; ax++) {
      for (int ay = -1; ay <= 1; ay++) {
        if (ax == dx && ay == dy) { continue; }
        if (ax == dx*-1 && ay == dy*-1) { continue; }
        otherFormerLength = isRunDir(board, color, i, j, ax, ay);
        otherBackLength = isRunDir(board, color, i, j, ax*-1, ay*-1);
        otherFormerEnemy = isEnemyDir(board, color*-1, i, j, ax, ay, otherFormerLength);
        otherBackEnemy = isEnemyDir(board, color*-1, i, j, ax*-1, ay*-1, otherBackLength);
        otherFormerWall = isWall(i, j, ax, ay, otherFormerLength);
        otherBackWall = isWall(i, j, ax*-1, ay*-1, otherBackLength);
        completeRun = isCompleteRun(otherFormerEnemy, otherBackEnemy, otherFormerWall, otherBackWall);
        // 3*2のとき
        if (now == 3 && otherFormerLength == 2 && otherBackLength == 0 && completeRun) { return true; }
      }
    }
    return false;
  }

  //----------------------------------------------------------------
  //  33チェック
  //----------------------------------------------------------------

  boolean is33(int[][] board, int color, int i, int j, int dx, int dy, int now) {
    int otherFormerLength    = 0;     //前方の連長
    int otherBackLength      = 0;     // 後方の連長
    boolean otherFormerEnemy = false; // 前方の敵石
    boolean otherBackEnemy   = false; // 後方の敵石
    boolean otherFormerWall  = false; // 前方の壁
    boolean otherBackWall    = false; // 後方の壁
    boolean completeRun      = false; // 完連判定
    for (int ax = -1; ax <= 1; ax++) {
      for (int ay = -1; ay <= 1; ay++) {
        if (ax == dx && ay == dy) { continue; }
        if (ax == dx*-1 && ay == dy*-1) { continue; }
        otherFormerLength = isRunDir(board, color, i, j, ax, ay);
        otherBackLength = isRunDir(board, color, i, j, ax*-1, ay*-1);
        otherFormerEnemy = isEnemyDir(board, color*-1, i, j, ax, ay, otherFormerLength);
        otherBackEnemy = isEnemyDir(board, color*-1, i, j, ax*-1, ay*-1, otherBackLength);
        otherFormerWall = isWall(i, j, ax, ay, otherFormerLength);
        otherBackWall = isWall(i, j, ax*-1, ay*-1, otherBackLength);
        completeRun = isCompleteRun(otherFormerEnemy, otherBackEnemy, otherFormerWall, otherBackWall);
        // 11*11のとき
        if (now == 1 && otherFormerLength == 1 && otherBackLength == 1 && completeRun) { return true; }
        // 11*2 or 2*2のとき
        if ((now == 1 || now == 2) && otherFormerLength == 2 && otherBackLength == 0 && completeRun) { return true; }
      }
    }
    return false;
  }

  //----------------------------------------------------------------
  //  連の方向チェック(止連・端連・長連も含む、飛びは無視)
  //----------------------------------------------------------------

  int isRunDir(int[][] board, int color, int i, int j, int dx, int dy) {
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
  //  相手石チェック
  //----------------------------------------------------------------

  boolean isEnemyDir(int[][] board, int color, int i, int j, int dx, int dy, int len) {
    int x = i+dx*(len+1);
    int y = j+dy*(len+1);
    if (isWall(i, j, dx, dy, len)) { return false; }
    return (board[x][y] == color);
  }

  //----------------------------------------------------------------
  //  壁チェック
  //----------------------------------------------------------------

  boolean isWall(int i, int j, int dx, int dy, int len) {
    int x = i+dx*(len+1);
    int y = j+dy*(len+1);
    return (x < 0 || y < 0 || x >= size || y >= size);
  }

  //----------------------------------------------------------------
  //  完連チェック
  //----------------------------------------------------------------

  boolean isCompleteRun(boolean fE, boolean bE, boolean fW, boolean bW) {
    return (!fE && !bE && !fW && !bW);
  }

  //----------------------------------------------------------------
  //  障害物チェック
  //----------------------------------------------------------------

  boolean isAnyObstacle(boolean fE, boolean bE, boolean fW, boolean bW) {
    return (fE || bE || fW || bW);
  }

  //----------------------------------------------------------------
  //  障害物k間チェック
  //----------------------------------------------------------------

  boolean isBetweenObstacle(boolean fE, boolean bE, boolean fW, boolean bW) {
    return ((fE || fW) && (bE || bW));
  }

  //----------------------------------------------------------------
  //  認識値表示
  //----------------------------------------------------------------

  void showRecognition(int i, int j, int dx, int dy, int fL, int bL, boolean fE, boolean bE, boolean fW, boolean bW) {
    System.out.printf("[%2d,%2d],%2d,%2d,%2d,%2d,%s,%s,%s,%s\n",
      i, j, dx, dy, fL, bL, String.valueOf(fE), String.valueOf(bE), String.valueOf(fW), String.valueOf(bW));
  }

  //----------------------------------------------------------------
  //  評価値の表示
  //----------------------------------------------------------------

  void showValue() {
    int i, j;
    for (i = 0; i < size; i++) {
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
