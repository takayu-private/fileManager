@echo off

echo 処理開始

set fold=
set /P fold="対象のパスを入力: "'

echo %fold%フォルダの編集を開始します。

set fold=Done

if not exist .\%fold% (
  mkdir %fold%
)

cscript strTurn.vbs %file%

echo 処理終了
