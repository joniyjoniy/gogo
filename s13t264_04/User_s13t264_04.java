package data.strategy.user.s13t264_04;

import sys.game.GameBoard;
import sys.game.GameCompSub;
import sys.game.GameHand;
import sys.game.GamePlayer;
import sys.game.GameState;
import sys.struct.GogoHand;
import sys.user.GogoCompSub;

public class User_s13t264_04 extends GogoCompSub {

//====================================================================
//  コンストラクタ
//====================================================================

  public User_s13t264_04(GamePlayer player) {
    super(player);
    name = "s13t264_04";    // プログラマが入力

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
    final int FORMFOUR     =  2000; // 完4連
    final int FORMPROFOUR  =   900; // 仮4連
    final int FORMTHREE    =   500; // 完3連
    final int FORMPROTHREE =   450; // 仮3連
    final int FORMTWO      =   100; // 完2連
    final int FORMONE      =    70; // 完1連
    final int FORMPROONE   =    50; // 仮1連

    // ---------------------------------------------
    // 自分の手を進める
    // ---------------------------------------------
    //--  各マスの評価値
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        // 埋まっているマスはスルー
        if (values[i][j] == -2) { continue; }
        // TODO 33処理
        for ( int dx = -1; dx <= 1; dx++ ) {
          for ( int dy = -1; dy <= 1; dy++ ) {
            int formerLength = 0; //前方の連長
            int backLength = 0; // 後方の連長
            boolean formerEnemy = false; // 前方の敵石
            boolean backEnemy = false; // 後方の敵石
            boolean formerWall = false; // 前方の壁
            boolean backWall = false; // 後方の壁
            // 禁じ手もしくは取られる場所はスルー
            if (values[i][j] == -1) { continue; }
            if ( dx == 0 && dy == 0 ) { continue; }

            // 前方の連長
            formerLength = check_run_dir(cell, mycolor, i, j, dx, dy);
            // 後方の連長
            backLength = check_run_dir(cell, mycolor, i, j, dx*-1, dy*-1);
            // 前方の連の先の敵
            formerEnemy = check_enemy_dir(cell, mycolor*-1, i, j, dx, dy, formerLength);
            // 後方の連の先の敵
            backEnemy = check_enemy_dir(cell, mycolor*-1, i, j, dx, dy, backLength);
            // 前方の連の先の壁
            formerWall = check_wall(i, j, dx, dy, formerLength);
            // 後方の連の先の壁
            backWall = check_wall(i, j, dx*-1, dy*-1, backLength);

            // 前方の連長で場合分け
            switch (formerLength) {
              case 4: //4連
                if ( backLength == 0 ) {
                 if ( values[i][j] < FORMFIVE ) { values[i][j] = FORMFIVE; }
                }
                break;
              case 3: // 3連
                switch (backLength) {
                  case 0: // 3連のみ
                    if ( backEnemy && !formerEnemy ) {
                      if ( values[i][j] < FORMPROFOUR ) { values[i][j] = FORMPROFOUR; }
                      break;
                    }
                    if ( formerEnemy ) {
                      if ( values[i][j] < FORMPROFOUR ) { values[i][j] = FORMPROFOUR; }
                      break;
                    }
                    if ( values[i][j] < FORMFOUR ) { values[i][j] = FORMFOUR; }
                    break;
                  case 1: // 3空1
                  if ( values[i][j] < FORMFIVE ) { values[i][j] = FORMFIVE; }
                    break;
                  default:
                    break;
                }
                break;
              case 2: // 2連
                if ( formerWall && backWall ) {
                  values[i][j] = 0;
                  break;
                }
                if ( formerEnemy || formerWall || backWall ) {
                  if ( values[i][j] < FORMPROTHREE ) { values[i][j] = FORMPROTHREE; }
                  break;
                }
                switch (backLength) {
                  case 2: // 2空2
                  if ( values[i][j] < FORMFIVE ) { values[i][j] = FORMFIVE; }
                    break;
                  case 1: // 2空1
                    if (!formerEnemy && backEnemy) {
                      if ( values[i][j] < FORMPROFOUR ) { values[i][j] = FORMPROFOUR; }
                      break;
                    }
                    if (formerEnemy) {
                      if ( values[i][j] < FORMPROFOUR ) { values[i][j] = FORMPROFOUR; }
                    } else {
                      if ( values[i][j] < FORMFOUR ) { values[i][j] = FORMFOUR; }
                    }
                    break;
                  case 0: // 2のみ
                  if ( values[i][j] < FORMTHREE ) { values[i][j] = FORMTHREE; }
                  default:
                    break;
                }
                break;
              case 1: // 1連
                switch(backLength) {
                  case 1: // 1空1
                    if (backEnemy && !formerEnemy) {
                      if ( values[i][j] < FORMPROTHREE ) { values[i][j] = FORMPROTHREE; }
                      break;
                    }
                    if (!backEnemy) {
                      if (formerEnemy) {
                        if ( values[i][j] < FORMPROTHREE ) { values[i][j] = FORMPROTHREE; }
                      } else {
                        if ( values[i][j] < FORMTHREE ) { values[i][j] = FORMTHREE; }
                      }
                    }
                    break;
                  case 0: // 1のみ
                    if (!formerEnemy && !backEnemy) {
                      if ( values[i][j] < FORMTWO ) { values[i][j] = FORMTWO; }
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
        // 相手の手を崩す用の評価値
        final int INFIVE  = 11000; // 5連妨害
        final int STONEUP =  5000; // 石取り
        final int INONE   =  1000; // 1連妨害
        final int INFOUR  =  4000; // 4連妨害
        final int INTHREE =   500; // 3連妨害

        for ( int dx = -1; dx <= 1; dx++ ) {
          for ( int dy = -1; dy <= 1; dy++ ) {

            int formerLength = 0; //前方の連長
            int backLength = 0; // 後方の連長
            boolean formerEnemy = false; // 前方の自石
            boolean backEnemy = false; // 後方の自石
            boolean formerWall = false; // 前方の壁
            boolean backWall = false; // 後方の壁

            // 禁じ手もしくは取られる場所はスルー
            if (in_values[i][j] == -1) { continue; }
            if ( dx == 0 && dy == 0 ) { continue; }

            // 前方の連長
            formerLength = check_run_dir(cell, mycolor*-1, i, j, dx, dy);
            // 後方の連長
            backLength = check_run_dir(cell, mycolor*-1, i, j, dx*-1, dy*-1);
            // 前方の連の先の敵
            formerEnemy = check_enemy_dir(cell, mycolor, i, j, dx, dy, formerLength);
            // 後方の連の先の敵
            backEnemy = check_enemy_dir(cell, mycolor, i, j, dx, dy, backLength);
            // 前方の連の先の壁
            formerWall = check_wall(i, j, dx, dy, formerLength);
            // 後方の連の先の壁
            backWall = check_wall(i, j, dx*-1, dy*-1, backLength);

            // 前方の連長で場合分け
            switch (formerLength) {
              case 4: //4連
                if ( backLength == 0 ) {
                  // 5連阻止
                  if ( in_values[i][j] < INFIVE ) { in_values[i][j] = INFIVE; }
                }
                break;
		          case 3: // 3連
                switch (backLength) {
		              case 0: // 3連のみ
                    if (formerEnemy || backEnemy) {
                      // なんとかなる
			                break;
                    }
                    // 3連妨害
                    if ( in_values[i][j] < INTHREE ) { in_values[i][j] = INTHREE; }
                    break;
		              case 1: // 3空1
                    // 5連阻止
			              if ( in_values[i][j] < INFIVE ) { in_values[i][j] = INFIVE; }
                    break;
		              default:
                    break;
                }
                break;
		          case 2: // 2連
                if ( formerWall && backWall ) {
                  // 無視
			            break;
                }
                switch (backLength) {
		              case 2: // 2空2
                    // 5連阻止
			              if ( in_values[i][j] < INFIVE ) { in_values[i][j] = INFIVE; }
                    break;
		              case 1: // 2空1
                    if (!formerEnemy && !backEnemy) {
                      // ピンチ
                      if (in_values[i][j] < INFIVE) { in_values[i][j] = INFIVE; }
                    }
                    if (!formerEnemy && backEnemy) {
                      // 石取り前
			                if ( in_values[i][j] < INONE ) { in_values[i][j] = INONE; }
			                break;
                    }
                    if (formerEnemy) {
                      // 石取り
			                if ( in_values[i][j] < STONEUP ) { in_values[i][j] = STONEUP; }
                    }
                    break;
		              case 0: // 2のみ
                    // 石取り前
			              if ( in_values[i][j] < INONE ) { in_values[i][j] = INONE; }
		              default:
                    break;
                }
                break;
              case 1: // 1連
                switch(backLength) {
		              case 1: // 1空1
                    if (formerEnemy || backEnemy) {
                      // 気にしなくてもOK
                      break;
                    }
                    if (!formerEnemy && !backEnemy && !formerWall && !backWall) {
                      // 何も邪魔するものがなければ間に置く
                      if ( in_values[i][j] < INONE ) { in_values[i][j] = INONE; }
                    }
                    break;
		              case 0: // 1のみ
                    if (!formerEnemy && !backEnemy) {
                      // 石取り前
			                if ( in_values[i][j] < INONE ) { in_values[i][j] = INONE; }
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
