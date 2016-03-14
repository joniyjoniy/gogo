## 戦略s13t264_08
### 概要
s13t264_07を元にしている   
先読み戦略を実装することが目標   
アルゴリズムはミニマックス法(ネガマックス法)で実装する   

### DONE追加戦略

### TODO追加戦略
#### 飛び4手前
AXXAの時にXXのどちらかに置くことで21飛三を作る   
####先読み
ネガマックス法実装   
検討 支配戦略

### 自手評価値

|評価値名|評価値|
|:--|--:|
|FORM_FIVE|10000|
|PREUP|5000|
|FORM_FOUR|2000|
|FORM_PROFOUR|900|
|FORM_THREE|500|
|FORM_PROTHREE|450|
|FORM_TWO|100|
|FORM_ONE|70|
|FORM_PROONE|50|

### 相手妨害評価値

|評価値名|評価値|
|:--|--:|
|INFIVE|9000|
|STONEUP|5000|
|INONE|1000|
|INFOUR|4000|
|INTHREE|3000|

### パターン一覧
戦略内で評価している盤面一覧を示す。   

連を数字 ~で以上 相手石をZ 自分石をA 空白をX 壁をW で表現する。   
0はスルーする。   
※印は要検討を示す。   

#### 自手パターン


##### 4連
|パターン|評価値|備考|
|:--|:--|:--|
|4X|FORM_FIVE||
|4X1~|0|||
##### 3連
|パターン|評価値|備考|
|:--|:--|:--|
|3X|FORM_FOUR||
|Z3XZ|0||
|Z3XW|0||
|W3XZ|0||
|W3XW|0||
|3XZ|FORM_PROFOUR||
|Z3X|FORM_PROFOUR||
|W3X|FORM_PROFOUR||
|3XW|FORM_PROFOUR||
|3X1|FORM_FIVE||
|3X2~|0|||
##### 2連
|パターン|評価値|備考|
|:--|:--|:--|
|2X|FORM_THREE||
|Z2X|PREVUP||
|W2XZ|0||
|W2XW|0||
|2XZ|FORM_PROTHREE||
|2XW|FORM_PROTHREE||
|W2X|FORM_PROTHREE||
|2X1|FORM_FOUR||
|Z2X1|PREVUP||
|W2X1W|0||
|W2X1Z|0||
|2X1Z|FORM_PROFOUR||
|2X1W|FORM_PROFOUR||
|W2X1|FORM_PROFOUR||
|2X2|FORM_FIVE||
|2X3~|0|||
##### 1連
|パターン|評価値|備考|
|:--|:--|:--|
|1X|FORM_TWO||
|W1X|FORM_PROTWO||
|1XW|FORM_PROTWO||
|Z1XZ|0||
|Z1XW|0||
|Z1X|0|*|
|1XZ|0|*|
|1X1|FORM_THREE||
|Z1X1Z|0||
|Z1X1W|0||
|W1X1Z|0||
|W1X1W|0||
|Z1X1|FORM_PROTHREE||
|W1X1|FORM_PROTHREE||
|1X1Z|FORM_PROTHREE||
|1X1W|FORM_PROTHREE||
|1X2~|0|||

#### 相手パターン
|パターン|評価値|備考|
|:--|:--|:--|
|4X|INFIVE||
|3X|INTHREE||
|3X1|INFIVE||
|2X2|INFIVE||
|2X1|INFIVE||
|2X1A|INONE|*|
|A2X1|STONEUP||
|A2X|STONEUP||
|2X|INONE||
|1X1|INONE||
|1X|INONE|||
