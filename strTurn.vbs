Dim file, fileData, textTurn
file = Wscript.Arguments(0)
textTurn = 30000

'�e�L�X�g�t�@�C���ǂݍ���
Set objFSO = WScript.CreateObject("Scripting.FileSystemObject")
If Err.Number = 0 Then
    Set objFile = objFSO.OpenTextFile(file)
    If Err.Number = 0 Then
        Do While objFile.AtEndOfStream <> True
            fileData = objFile.ReadLine
        Loop
        objFile.Close
    Else
        WScript.Echo "�t�@�C���I�[�v���G���[: " & Err.Description
    End If
Else
    WScript.Echo "�G���[: " & Err.Description
End If

Set objFile = Nothing
Set objFSO = Nothing

Dim strCount, roopCount
strCount = Len(fileData)

WScript.Echo strCount

roopCount = strCount / textTurn

'�t�@�C���o��
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
        WScript.Echo "�t�@�C���I�[�v���G���[: " & Err.Description
    End If
Else
    WScript.Echo "�G���[: " & Err.Description
End If

Set objFile = Nothing
Set objFSO = Nothing


