unit Main;

interface

uses
  Winapi.Windows, Winapi.Messages,
  System.SysUtils, System.Variants, System.Classes, System.IOUtils, System.Types,
  Vcl.Graphics, Vcl.Controls, Vcl.Forms, Vcl.Dialogs, Vcl.StdCtrls, Vcl.ComCtrls;

type
  TMainForm = class(TForm)
    gbMusic: TGroupBox;
    gbGeneralInfo: TGroupBox;
    lblPackTitle: TLabel;
    lblCodeSeed: TLabel;
    lblMods: TLabel;
    gbLevels: TGroupBox;
    lbMusic: TListBox;
    pcLevels: TPageControl;
    tsLevelGroup: TTabSheet;
    lbLevels: TListBox;
    edPackTitle: TEdit;
    edCodeSeed: TEdit;
    cbMods: TComboBox;
    btnAddMusicTrack: TButton;
    btnDeleteSelectedMusicTrack: TButton;
    btnMoveMusicTrackUp: TButton;
    btnMoveMusicTrackDown: TButton;
    btnAddGroup: TButton;
    btnDeleteGroup: TButton;
    btnMoveLevelDown: TButton;
    btnMoveLevelUp: TButton;
    btnDeleteLevel: TButton;
    btnAddLevel: TButton;
    btnGenerateSeed: TButton;
    btnGenerateLevelPackINI: TButton;
    btnRenameGroup: TButton;
    procedure FormCreate(Sender: TObject);
    procedure TextInputClick(Sender: TObject);
    procedure btnGenerateSeedClick(Sender: TObject);
    procedure btnAddMusicTrackClick(Sender: TObject);
    procedure btnDeleteSelectedMusicTrackClick(Sender: TObject);
    procedure btnMoveMusicTrackUpClick(Sender: TObject);
    procedure btnMoveMusicTrackDownClick(Sender: TObject);
    procedure btnAddGroupClick(Sender: TObject);
    procedure btnDeleteGroupClick(Sender: TObject);
    procedure btnRenameGroupClick(Sender: TObject);
    procedure btnAddLevelClick(Sender: TObject);
    procedure btnDeleteLevelClick(Sender: TObject);
    procedure btnMoveLevelUpClick(Sender: TObject);
    procedure btnMoveLevelDownClick(Sender: TObject);
    procedure btnGenerateLevelPackINIClick(Sender: TObject);
  private
    Version: String;
    RootPath: String;

    procedure PopulateModsDropdownList(Path: String);
    procedure GenerateLevelPackINI(const FileName: string);
    procedure MoveListItemsUp(List: TListBox);
    procedure MoveListItemsDown(List: TListBox);
    function GenerateRandomCodeSeed: String;
    function GetPath(const BasePath, FullPath: string): string;
    function GetActiveGroupList: TListBox;
    function GetVersion: String;
  public
    { Public declarations }
  end;

var
  MainForm: TMainForm;

const
  NO_MODS_TEXT = '< No Mods >';

implementation

{$R *.dfm}

procedure TMainForm.btnAddGroupClick(Sender: TObject);
var
  NewTab: TTabSheet;
  NewList: TListBox;
begin
  NewTab := TTabSheet.Create(pcLevels);
  NewTab.PageControl := pcLevels;
  NewTab.Caption := Format('Group %d', [pcLevels.PageCount]);

  NewList := TListBox.Create(NewTab);
  NewList.Parent := NewTab;
  NewList.Align := alClient;
  NewList.MultiSelect := True;

  pcLevels.ActivePage := NewTab;
end;

procedure TMainForm.btnRenameGroupClick(Sender: TObject);
var
  NewName: string;
begin
  if pcLevels.ActivePage = nil then Exit;

  NewName := InputBox('Rename Group',
                      'Enter new group name:',
                      pcLevels.ActivePage.Caption);

  if NewName <> '' then
    pcLevels.ActivePage.Caption := NewName;
end;

procedure TMainForm.btnDeleteGroupClick(Sender: TObject);
var
  Tab: TTabSheet;
begin
  Tab := pcLevels.ActivePage;
  if Tab = nil then Exit;

  if MessageDlg(Format('Delete group "%s"?', [Tab.Caption]),
     mtConfirmation, [mbYes, mbNo], 0) = mrYes then
    Tab.Free;
end;

procedure TMainForm.btnAddLevelClick(Sender: TObject);
var
  OpenDlg: TOpenDialog;
  LevelFileName: string;
  List: TListBox;
  I: Integer;
begin
  List := GetActiveGroupList;
  if List = nil then Exit;

  OpenDlg := TOpenDialog.Create(Self);
  try
    OpenDlg.Title := 'Select Level(s)';
    OpenDlg.InitialDir := TPath.Combine(RootPath, 'levels');
    OpenDlg.Filter := 'Level Files (*.ini)|*.ini|All Files (*.*)|*.*';
    OpenDlg.DefaultExt := 'ini';
    OpenDlg.FilterIndex := 1;
    OpenDlg.Options := [ofFileMustExist, ofHideReadOnly, ofAllowMultiSelect];

    if OpenDlg.Execute then
    begin
      for I := 0 to OpenDlg.Files.Count - 1 do
      begin
        // Extract just the file name, without folders
        LevelFileName := TPath.GetFileName(OpenDlg.Files[I]);

        // Add to list if not already present
        if List.Items.IndexOf(LevelFileName) = -1 then
          List.Items.Add(LevelFileName);
      end;
    end;

  finally
    OpenDlg.Free;
  end;
end;

procedure TMainForm.btnDeleteLevelClick(Sender: TObject);
var
  i: Integer;
  List: TListBox;
begin
  List := GetActiveGroupList;
  if List = nil then Exit;

  for i := List.Items.Count - 1 downto 0 do
    if List.Selected[i] then
      List.Items.Delete(i);
end;

procedure TMainForm.btnMoveLevelUpClick(Sender: TObject);
begin
  MoveListItemsUp(GetActiveGroupList);
end;

procedure TMainForm.btnMoveLevelDownClick(Sender: TObject);
begin
  MoveListItemsDown(GetActiveGroupList);
end;

procedure TMainForm.btnAddMusicTrackClick(Sender: TObject);
var
  OpenDlg: TOpenDialog;
  MusicPath: string;
  I: Integer;
begin
  OpenDlg := TOpenDialog.Create(Self);
  try
    OpenDlg.Title := 'Select Music Track(s)';
    OpenDlg.InitialDir := TPath.Combine(RootPath, 'music');

    OpenDlg.Filter := 'Music Files|*.mp3;*.wav;*.ogg;*.mod|All Files|*.*';
    OpenDlg.FilterIndex := 1;
    OpenDlg.Options := [ofFileMustExist, ofHideReadOnly, ofAllowMultiSelect];

    if OpenDlg.Execute then
    begin
      for I := 0 to OpenDlg.Files.Count - 1 do
      begin
        MusicPath := GetPath(OpenDlg.InitialDir, OpenDlg.Files[I]);

        if lbMusic.Items.IndexOf(MusicPath) = -1 then
          lbMusic.Items.Add(MusicPath);
      end;
    end;

  finally
    OpenDlg.Free;
  end;
end;

procedure TMainForm.btnDeleteSelectedMusicTrackClick(Sender: TObject);
var
  i: Integer;
begin
  for i := lbMusic.Items.Count - 1 downto 0 do
    if lbMusic.Selected[i] then
      lbMusic.Items.Delete(i);
end;

procedure TMainForm.btnMoveMusicTrackUpClick(Sender: TObject);
begin
  MoveListItemsUp(lbMusic);
end;

procedure TMainForm.btnMoveMusicTrackDownClick(Sender: TObject);
begin
  MoveListItemsDown(lbMusic);
end;

procedure TMainForm.btnGenerateSeedClick(Sender: TObject);
begin
  edCodeSeed.Text := GenerateRandomCodeSeed;
end;

procedure TMainForm.btnGenerateLevelPackINIClick(Sender: TObject);
var
  SaveDlg: TSaveDialog;
begin
  SaveDlg := TSaveDialog.Create(Self);
  try
    SaveDlg.Title := 'Save Level Pack INI';
    SaveDlg.Filter := 'Level Files (*.ini)|*.ini|All Files (*.*)|*.*';
    SaveDlg.DefaultExt := 'ini';
    SaveDlg.FileName := 'levelpack.ini';
    SaveDlg.Options := [ofOverwritePrompt];

    if SaveDlg.Execute then
      GenerateLevelPackINI(SaveDlg.FileName);

  finally
    SaveDlg.Free;
  end;
end;

procedure TMainForm.FormCreate(Sender: TObject);
begin
  Version := GetVersion;
  RootPath := ExtractFilePath(Application.ExeName);

  Caption := 'Lemmini Level Pack Compiler ' + Version;

  edPackTitle.Text := '';
  edCodeSeed.Text := GenerateRandomCodeSeed;
  PopulateModsDropdownList(RootPath);
end;

procedure TMainForm.TextInputClick(Sender: TObject);
begin
  if Sender is TEdit then
    TEdit(Sender).SelectAll;
end;

function TMainForm.GetPath(const BasePath, FullPath: string): string;
var
  Base, Full: string;
begin
  Base := IncludeTrailingPathDelimiter(ExpandFileName(BasePath));
  Full := ExpandFileName(FullPath);

  if Pos(Base, Full) = 1 then
    Result := Copy(Full, Length(Base) + 1, MaxInt)
  else
    Result := Full;

  Result := StringReplace(Result, '\', '/', [rfReplaceAll]);
end;

function TMainForm.GetActiveGroupList: TListBox;
var
  i: Integer;
begin
  Result := nil;

  if (pcLevels.ActivePage = nil) then
    Exit;

  for i := 0 to pcLevels.ActivePage.ControlCount - 1 do
    if pcLevels.ActivePage.Controls[i] is TListBox then
      Exit(TListBox(pcLevels.ActivePage.Controls[i]));
end;

function TMainForm.GenerateRandomCodeSeed: String;
const
  Chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
var
  i: Integer;
  s: string;
begin
  Randomize;
  s := '';
  for i := 1 to 10 do
    s := s + Chars[Random(Length(Chars)) + 1];
  Result := s;
end;

function TMainForm.GetVersion: String;
var
  VerSize, Handle: DWORD;
  VerBuf: Pointer;
  VerInfo: PVSFixedFileInfo;
  VerInfoLen: UINT;
  Major, Minor: Word;
begin
  Result := '';
  VerSize := GetFileVersionInfoSize(PChar(Application.ExeName), Handle);
  if VerSize = 0 then Exit;

  GetMem(VerBuf, VerSize);
  try
    if GetFileVersionInfo(PChar(Application.ExeName), Handle, VerSize, VerBuf) then
      if VerQueryValue(VerBuf, '\', Pointer(VerInfo), VerInfoLen) then
      begin
        Major := HiWord(VerInfo^.dwFileVersionMS);
        Minor := LoWord(VerInfo^.dwFileVersionMS);
        Result := Format('%d.%d', [Major, Minor]);
      end;
  finally
    FreeMem(VerBuf);
  end;
end;

procedure TMainForm.PopulateModsDropdownList(Path: String);
var
  ModsPath: string;
  Folders: TStringDynArray;
  Folder: string;
begin
  cbMods.Items.Clear;
  cbMods.Items.Add(NO_MODS_TEXT);

  ModsPath := TPath.Combine(Path, 'mods');

  if not TDirectory.Exists(ModsPath) then
    Exit;

  Folders := TDirectory.GetDirectories(ModsPath);

  for Folder in Folders do
    cbMods.Items.Add(TPath.GetFileName(Folder));

  cbMods.ItemIndex := 0;
end;

procedure TMainForm.MoveListItemsUp(List: TListBox);
var
  i, FirstIndex: Integer;
  SelectedItems: TStringList;
begin
  SelectedItems := TStringList.Create;
  try
    FirstIndex := -1;
    for i := 0 to List.Items.Count - 1 do
      if List.Selected[i] then
      begin
        FirstIndex := i;
        Break;
      end;

    if (FirstIndex <= 0) then Exit;

    for i := 0 to List.Items.Count - 1 do
      if List.Selected[i] then
        SelectedItems.Add(List.Items[i]);

    for i := List.Items.Count - 1 downto 0 do
      if List.Selected[i] then
        List.Items.Delete(i);

    for i := 0 to SelectedItems.Count - 1 do
      List.Items.Insert(FirstIndex - 1 + i, SelectedItems[i]);

    for i := 0 to SelectedItems.Count - 1 do
      List.Selected[FirstIndex - 1 + i] := True;
  finally
    SelectedItems.Free;
  end;
end;

procedure TMainForm.MoveListItemsDown(List: TListBox);
var
  i, LastIndex: Integer;
  SelectedItems: TStringList;
begin
  SelectedItems := TStringList.Create;
  try
    LastIndex := -1;
    for i := List.Items.Count - 1 downto 0 do
      if List.Selected[i] then
      begin
        LastIndex := i;
        Break;
      end;

    if (LastIndex < 0) or (LastIndex >= List.Items.Count - 1) then Exit;

    for i := 0 to List.Items.Count - 1 do
      if List.Selected[i] then
        SelectedItems.Add(List.Items[i]);

    for i := List.Items.Count - 1 downto 0 do
      if List.Selected[i] then
        List.Items.Delete(i);

    for i := 0 to SelectedItems.Count - 1 do
      List.Items.Insert(LastIndex - SelectedItems.Count + 2 + i, SelectedItems[i]);

    for i := 0 to SelectedItems.Count - 1 do
      List.Selected[LastIndex - SelectedItems.Count + 2 + i] := True;
  finally
    SelectedItems.Free;
  end;
end;

procedure TMainForm.GenerateLevelPackINI(const FileName: string);

  function GetListBoxFromTab(Tab: TTabSheet): TListBox;
  var
    i: Integer;
  begin
    Result := nil;
    for i := 0 to Tab.ControlCount - 1 do
      if Tab.Controls[i] is TListBox then
        Exit(TListBox(Tab.Controls[i]));
  end;

  function StripExtension(const S: string): string;
  begin
    Result := ChangeFileExt(S, '');
  end;

  function ValidateName: String;
  begin
    if (edPackTitle.Text = '') then
      Result := 'Untitled' + #13#10
    else
      Result := edPackTitle.Text + #13#10;
  end;

  function ValidateCodeSeed: string;
  var
    i: Integer;
    S: String;
    IsValidCodeSeed: Boolean;
  begin
    IsValidCodeSeed := True;

    if (edCodeSeed.Text = '') then
      IsValidCodeSeed := False
    else for i := 1 to Length(edCodeSeed.Text) do
      if not (UpCase(edCodeSeed.Text[i]) in ['A'..'Z']) then
      begin
        IsValidCodeSeed := False;
        Break;
      end;

    if not IsValidCodeSeed then
      Result := GenerateRandomCodeSeed
    else
      Result := edCodeSeed.Text + #13#10;
  end;

  function ValidateMods: string;
  begin
    if (cbMods.Text = '') or (cbMods.Text = NO_MODS_TEXT) then
      Result := ''
    else
      Result := cbMods.Text + #13#10;
  end;

var
  SL: TStringList;
  i, j: Integer;
  Tab: TTabSheet;
  List: TListBox;
begin
  SL := TStringList.Create;
  try
    // General Info
    SL.Add('name = ' + ValidateName);
    SL.Add('codeSeed = ' + ValidateCodeSeed);
    SL.Add(ValidateMods);

    // Music
    SL.Add('# Music selection');
    for i := 0 to lbMusic.Items.Count - 1 do
      SL.Add(Format('music_%d = %s', [i, lbMusic.Items[i]]));
    SL.Add('');

    // Groups
    SL.Add('# Groups');
    for i := 0 to pcLevels.PageCount - 1 do
      SL.Add(Format('level_%d = %s', [i, pcLevels.Pages[i].Caption]));
    SL.Add('');

    // Levels
    SL.Add('# Levels: name, music ID');
    SL.Add('');

    // Levels in each group
    for i := 0 to pcLevels.PageCount - 1 do
    begin
      Tab := pcLevels.Pages[i];
      List := GetListBoxFromTab(Tab);
      if List = nil then
        Continue;

      SL.Add(Format('# Rating %d - %s', [i, Tab.Caption]));
      SL.Add('');

      for j := 0 to List.Items.Count - 1 do
      begin
        SL.Add(Format('# %s %d - %s',
          [Tab.Caption, j + 1, StripExtension(List.Items[j])]));

        SL.Add(Format('level_%d_%d = %s,%d',
          [i, j, List.Items[j], j]));
      end;

      SL.Add('');
    end;

    SL.SaveToFile(FileName, TEncoding.UTF8);

  finally
    SL.Free;
  end;
end;

end.
