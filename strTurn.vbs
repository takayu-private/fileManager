Dim file, fileData, textTurn, fileName, fold
fold = Wscript.Arguments(0)
textTurn = 30000

'�e�L�X�g�t�@�C���ǂݍ���
Set objFSO = WScript.CreateObject("Scripting.FileSystemObject")
set objFold=objFSO.GetFolder(fold)

for each fileName In objFold.Files

If Err.Number = 0 Then
    WScript.Echo "�J�n: " & fileName
    Set objFile = objFSO.OpenTextFile(fileName)
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

roopCount = strCount / textTurn

'�t�@�C���o��
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
        WScript.Echo "�t�@�C���I�[�v���G���[: " & Err.Description
    End If
Else
    WScript.Echo "�G���[: " & Err.Description
End If

Set objFile = Nothing
WScript.Echo "�I��: " & fileName
Next
Set objFSO = Nothing
