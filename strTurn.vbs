Dim file, fileData, textTurn, fileName, fold
fold = Wscript.Arguments(0)
textTurn = 30000

'テキストファイル読み込み
Set objFSO = WScript.CreateObject("Scripting.FileSystemObject")
set objFold=objFSO.GetFolder(fold)

for each fileName In objFold.Files

If Err.Number = 0 Then
    WScript.Echo "開始: " & fileName
    Set objFile = objFSO.OpenTextFile(fileName)
    If Err.Number = 0 Then
        Do While objFile.AtEndOfStream <> True
            fileData = objFile.ReadLine
        Loop
        objFile.Close
    Else
        WScript.Echo "ファイルオープンエラー: " & Err.Description
    End If
Else
    WScript.Echo "エラー: " & Err.Description
End If

Set objFile = Nothing
Set objFSO = Nothing

Dim strCount, roopCount
strCount = Len(fileData)

roopCount = strCount / textTurn

'ファイル出力
Set objFSO = WScript.CreateObject("Scripting.FileSystemObject")
If Err.Number = 0 Then
    Set objFile = objFSO.OpenTextFile("Done\" + objFSO.getFileName(fileName), 2, True)
    If Err.Number = 0 Then
    
    Dim strStart
      for i=0 to roopCount
        strStart = textTurn * i + 1
        objFile.WriteLine(Mid(fileData, strStart, textTurn))
      next
        objFile.Close
    Else
        WScript.Echo "ファイルオープンエラー: " & Err.Description
    End If
Else
    WScript.Echo "エラー: " & Err.Description
End If

Set objFile = Nothing
WScript.Echo "終了: " & fileName
Next
Set objFSO = Nothing
