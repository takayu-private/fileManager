Dim file, fileData, textTurn
file = Wscript.Arguments(0)
textTurn = 30000

'テキストファイル読み込み
Set objFSO = WScript.CreateObject("Scripting.FileSystemObject")
If Err.Number = 0 Then
    Set objFile = objFSO.OpenTextFile(file)
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

WScript.Echo strCount

roopCount = strCount / textTurn

'ファイル出力
Set objFSO = WScript.CreateObject("Scripting.FileSystemObject")
If Err.Number = 0 Then
    Set objFile = objFSO.OpenTextFile("Done\" + file, 2, True)
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
Set objFSO = Nothing


