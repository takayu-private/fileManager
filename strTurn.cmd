@echo off

echo �����J�n

set fold=
set /P fold="�Ώۂ̃p�X�����: "'

echo %fold%�t�H���_�̕ҏW���J�n���܂��B

set fold=Done

if not exist .\%fold% (
  mkdir %fold%
)

cscript strTurn.vbs %file%

echo �����I��
