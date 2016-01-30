package data.strategy.user.s13t264_05;

import sys.game.GameBoard;
import sys.game.GameCompSub;
import sys.game.GameHand;
import sys.game.GamePlayer;
import sys.game.GameState;
import sys.struct.GogoHand;
import sys.user.GogoCompSub;

public class User_s13t264_05 extends GogoCompSub {

//====================================================================
//  コンストラクタ
//====================================================================

  public User_s13t264_05(GamePlayer player) {
    super(player);
    name = "s13t264_05";    // プログラマが入力

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
    //show_value();

    //--  着手の決定
    return deside_hand();

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
    int mycolor;                  // 自分の石の色
    mycolor = role;
    // 相手の妨害用評価配列
    int [][] in_values = new int[board.SX][board.SX];

    // 自分の手を進める用の評価値
    final int FORMFIVE     = 10000; // 完5連
    final int PREVUP       =  5000; // 石取り阻止
    final int FORMFOUR     =  2000; // 完4連
    final int FORMPROFOUR  =   900; // 仮4連
    final int FORMTHREE    =   500; // 完3連
    final int FORMPROTHREE =   450; // 仮3連
    final int FORMTWO      =   100; // 完2連
    final int FORMPROTWO   =    70; // 仮1連
    final int FORMONE      =    50; // 完1連
    final int FOUL         =    -1; // 禁じ手

    // System.out.printf("\nGoing My Way\n\n");
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
            int formerLength = 0; //前方の連長
            int backLength = 0; // 後方の連長
            boolean formerEnemy = false; // 前方の敵石
            boolean backEnemy = false; // 後方の敵石
            boolean formerWall = false; // 前方の壁
            boolean backWall = false; // 後方の壁
            // 禁じ手もしくは取られる場所はスルー
            if (values[i][j] == FOUL) { continue; }
            if (dx == 0 && dy == 0) { continue; }

            // 前方の連長
            formerLength = check_run_dir(cell, mycolor, i, j, dx, dy);
            // 後方の連長
            backLength = check_run_dir(cell, mycolor, i, j, dx*-1, dy*-1);
            // 前方の連の先の敵
            formerEnemy = check_enemy_dir(cell, mycolor*-1, i, j, dx, dy, formerLength);
            // 後方の連の先の敵
            backEnemy = check_enemy_dir(cell, mycolor*-1, i, j, dx*-1, dy*-1, backLength);
            // 前方の連の先の壁
            formerWall = check_wall(i, j, dx, dy, formerLength);
            // 後方の連の先の壁
            backWall = check_wall(i, j, dx*-1, dy*-1, backLength);

            // 前方の連長で場合分け
            switch (formerLength) {
              case 4: //4連
                if (backLength == 0) {
                  if (values[i][j] < FORMFIVE) {
                    // System.out.printf("4X\n");
                    values[i][j] = FORMFIVE;
                  }
                }
               break;
              case 3: // 3連
                switch (backLength) {
                  case 0: // 3連のみ
                    if (!formerEnemy && !backEnemy && !formerWall && !backWall) {
                      if (values[i][j] < FORMFOUR) {
                        // System.out.printf("3X\n");
                        values[i][j] = FORMFOUR;
                      }
                    } else if ( (formerEnemy || formerWall) && (backEnemy || backWall) ) {}
                    else {
                      if (values[i][j] < FORMPROFOUR) {
                        // System.out.printf("Z3X or 3XZ or W3X or 3XW\n");
                        values[i][j] = FORMPROFOUR;
                      }
                    }
                    break;
                  case 1: // 3空1
                    if (values[i][j] < FORMFIVE) {
                      // System.out.printf("3X1\n");
                      values[i][j] = FORMFIVE;
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
                      if ( check_33(cell, mycolor, i, j, dx, dy, 2) ) {
                        // System.out.printf("33\n");
                        values[i][j] = FOUL;
                      }
                      else if (values[i][j] < FORMTHREE) {
                        // System.out.printf("2X\n");
                        values[i][j] = FORMTHREE;
                      }
                    }
                    else if (formerEnemy) {
                      if (values[i][j] < PREVUP) {
                        // 石取り阻止
                        // System.out.printf("Z2X\n");
                        values[i][j] = PREVUP;
                      }
                    }
                    else if (formerWall && (backEnemy || backWall) ) {}
                    else {
                      if (values[i][j] < FORMPROTHREE) {
                        // System.out.printf("W2X or 2XW or 2XZ\n");
                        values[i][j] = FORMPROTHREE;
                      }
                    }
                    break;
                  case 1: // 2空1
                    if (!formerEnemy && !backEnemy && !formerWall && !backWall) {
                      if (values[i][j] < FORMFOUR) {
                        // System.out.printf("2X1\n");
                        values[i][j] = FORMFOUR;
                      }
                    }
                    else if (formerEnemy) {
                      if (values[i][j] < PREVUP) {
                        // 石取り阻止
                        // System.out.printf("Z2X1\n");
                        values[i][j] = PREVUP;
                      }
                    }
                    else if ( formerWall && (backEnemy || backWall) ) {}
                    else {
                      if (values[i][j] < FORMPROFOUR) {
                        // System.out.printf("2X1Z or 2X1W or W2X1\n");
                        values[i][j] = FORMPROFOUR;
                      }
                    }
                    break;
                  case 2: // 2空2
                    if (values[i][j] < FORMFIVE) {
                      // System.out.printf("2X2\n");
                      values[i][j] = FORMFIVE;
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
                      if (values[i][j] < FORMTWO) {
                        // System.out.printf("1X\n");
                        values[i][j] = FORMTWO;
                      }
                    } else if ( (formerWall || backWall) && (!formerEnemy && !backEnemy) ) {
                      if (values[i][j] < FORMPROTWO) {
                        // System.out.printf("W1X or 1XW\n");
                      }
                    }
                    break;
                  case 1: // 1空1
                    if (!formerEnemy && !backEnemy && !formerWall && !backWall) {
                      if ( check_33(cell, mycolor, i, j, dx, dy, 1) ) {
                        // System.out.printf("33\n");
                        values[i][j] = FOUL;
                      }
                      if (values[i][j] < FORMTHREE) {
                        // System.out.printf("1X1\n");
                        values[i][j] = FORMTHREE;
                      }
                    }
                    else if ( (formerEnemy || formerWall) && (backEnemy || backWall) ) {}
                    else {
                      if (values[i][j] < FORMPROTHREE) {
                        // System.out.printf("Z1X1 or W1X1 or 1X1Z or 1X1W\n");
                        values[i][j] = FORMPROTHREE;
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
        //// System.out.printf("\nInterference Enemy\n\n");

        // 相手の手を崩す用の評価値
        final int INFIVE  =  9000; // 5連妨害
        final int STONEUP =  5000; // 石取り
        final int INONE   =  1000; // 1連妨害
        final int INFOUR  =  4000; // 4連妨害
        final int INTHREE =  3000; // 3連妨害

        for (int dx = -1; dx <= 1; dx++) {
          for (int dy = -1; dy <= 1; dy++) {

            int formerLength = 0; //前方の連長
            int backLength = 0; // 後方の連長
            boolean formerEnemy = false; // 前方の自石
            boolean backEnemy = false; // 後方の自石
            boolean formerWall = false; // 前方の壁
            boolean backWall = false; // 後方の壁

            // 禁じ手もしくは取られる場所はスルー
            if (in_values[i][j] == -1) { continue; }
            if (dx == 0 && dy == 0) { continue; }

            // 前方の連長
            formerLength = check_run_dir(cell, mycolor*-1, i, j, dx, dy);
            // 後方の連長
            backLength = check_run_dir(cell, mycolor*-1, i, j, dx*-1, dy*-1);
            // 前方の連の先の敵
            formerEnemy = check_enemy_dir(cell, mycolor, i, j, dx, dy, formerLength);
            // 後方の連の先の敵
            backEnemy = check_enemy_dir(cell, mycolor, i, j, dx*-1, dy*-1, backLength);
            // 前方の連の先の壁
            formerWall = check_wall(i, j, dx, dy, formerLength);
            // 後方の連の先の壁
            backWall = check_wall(i, j, dx*-1, dy*-1, backLength);

            // 前方の連長で場合分け
            switch (formerLength) {
              case 4: //4連
                if (backLength == 0) {
                  // 5連阻止
                  if (in_values[i][j] < INFIVE) {
                    // System.out.printf("4X\n");
                    in_values[i][j] = INFIVE;
                  }
                }
                break;
		          case 3: // 3連
                switch (backLength) {
		              case 0: // 3連のみ
                    if (formerEnemy || backEnemy || formerWall || backWall) {
                      // System.out.printf("A3X or 3XA or W3X or 3XW\n");
                      // なんとかなる
			                break;
                    }
                    // 3連妨害
                    if (in_values[i][j] < INTHREE) {
                      // System.out.printf("3X\n");
                      in_values[i][j] = INTHREE;
                    }
                    break;
		              case 1: // 3空1
                    // 5連阻止
			              if (in_values[i][j] < INFIVE) {
                      // System.out.printf("3X1\n");
                      in_values[i][j] = INFIVE;
                    }
                    break;
		              default:
                    break;
                }
                break;
		          case 2: // 2連
                if (formerWall && backWall) {
                  // 無視
                  // System.out.printf("W2XW\n");
			            break;
                }
                switch (backLength) {
		              case 2: // 2空2
                    // 5連阻止
			              if (in_values[i][j] < INFIVE) {
                      // System.out.printf("2X2\n");
                      in_values[i][j] = INFIVE;}
                    break;
		              case 1: // 2空1
                    if (!formerEnemy && !backEnemy) {
                      // ピンチ
                      if (in_values[i][j] < INFIVE) {
                        // System.out.printf("2X1\n");
                        in_values[i][j] = INFIVE;
                      }
                    }
                    if (!formerEnemy && backEnemy) {
                      // 石取り前
			                if (in_values[i][j] < INONE) {
                        // System.out.printf("2X1Z\n");
                        in_values[i][j] = INONE;
                      }
			                break;
                    }
                    if (formerEnemy) {
                      // 石取り
			                if (in_values[i][j] < STONEUP) {
                        // System.out.printf("Z2X1\n");
                        in_values[i][j] = STONEUP;
                      }
                    }
                    break;
		              case 0: // 2のみ
                    // 石取り前
                    if (formerEnemy) {
                      if (in_values[i][j] < STONEUP) {
                        // System.out.printf("A2X\n");
                        in_values[i][j] = STONEUP;
                      }
                    }
			              if (in_values[i][j] < INONE) {
                      // System.out.printf("2X\n");
                      in_values[i][j] = INONE;
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
                      // System.out.printf("A1X1 or 1X1A\n");
                      break;
                    }
                    if (!formerEnemy && !backEnemy && !formerWall && !backWall) {
                      // 何も邪魔するものがなければ間に置く
                      if (in_values[i][j] < INONE) {
                        // System.out.printf("1X1\n");
                        in_values[i][j] = INONE;
                      }
                    }
                    break;
		              case 0: // 1のみ
                    if (!formerEnemy && !backEnemy) {
                      // 石取り前
			                if (in_values[i][j] < INONE) {
                        // System.out.printf("1X\n");
                        in_values[i][j] = INONE;
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
        if (in_values[i][j] > 0) {
          values[i][j] += in_values[i][j];
        }

      }
    }// 盤面ループ終わり
  }

//----------------------------------------------------------------
//  連の方向チェック(止連・端連・長連も含む、飛びは無視)
//----------------------------------------------------------------

  int check_run_dir(int[][] board, int color, int i, int j, int dx, int dy) {
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

boolean check_33(int[][] board, int color, int i, int j, int dx, int dy, int other) {
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
      afLength = check_run_dir(board, color, i, j, ax, ay);
      // 後方の連長
      abLength = check_run_dir(board, color, i, j, ax*-1, ay*-1);
      // 前方の連の先の敵
      afEnemy = check_enemy_dir(board, color*-1, i, j, ax, ay, afLength);
      // 後方の連の先の敵
      abEnemy = check_enemy_dir(board, color*-1, i, j, ax*-1, ay*-1, abLength);
      // 前方の連の先の壁
      afWall = check_wall(i, j, ax, ay, afLength);
      // 後方の連の先の壁
      abWall = check_wall(i, j, ax*-1, ay*-1, abLength);

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

boolean check_enemy_dir(int[][] board, int color, int i, int j, int dx, int dy, int len) {
  int x = i+dx*(len+1);
  int y = j+dy*(len+1);
  if (x < 0 || y < 0 || x >= size || y >= size) { return false; }
  if (board[x][y] == color) { return true; }
  return false;
}

//----------------------------------------------------------------
//  壁チェック
//----------------------------------------------------------------

boolean check_wall(int i, int j, int dx, int dy, int len) {
  int x = i+dx*(len+1);
  int y = j+dy*(len+1);
  if (x < 0 || y < 0 || x >= size || y >= size) { return true; }
  return false;
}

//----------------------------------------------------------------
//  評価値の表示
//----------------------------------------------------------------

  void show_value() {
    int i, j;
    for (i = 0; i < size; i++) {
      // System.out.printf(" ");
      for (j = 0; j < size; j++) {
        // System.out.printf("%5d ", values[j][i]);
      }
      // System.out.printf("\n");
    }
    // System.out.printf("\n");
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
