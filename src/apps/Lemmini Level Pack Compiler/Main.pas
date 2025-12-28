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
  private
    Version: String;
    RootPath: String;

    procedure PopulateModsDropdownList(Path: String);
    procedure GenerateRandomCodeSeed;
    procedure MoveListItemsUp(List: TListBox);
    procedure MoveListItemsDown(List: TListBox);
    function GetPath(const BasePath, FullPath: string): string;
    function GetActiveGroupList: TListBox;
    function GetVersion: String;
  public
    { Public declarations }
  end;

var
  MainForm: TMainForm;

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
  LevelPath: string;
  List: TListBox;
  I: Integer;
begin
  List := GetActiveGroupList;

  OpenDlg := TOpenDialog.Create(Self);
  try
    OpenDlg.Title := 'Select Level(s)';
    OpenDlg.InitialDir := TPath.Combine(RootPath, 'levels');

    OpenDlg.Filter := 'Level Files|*.ini;*.lvl;|All Files|*.*';
    OpenDlg.FilterIndex := 1;
    OpenDlg.Options := [ofFileMustExist, ofHideReadOnly, ofAllowMultiSelect];

    if OpenDlg.Execute then
    begin
      for I := 0 to OpenDlg.Files.Count - 1 do
      begin
        LevelPath := GetPath(OpenDlg.InitialDir, OpenDlg.Files[I]);

        if List.Items.IndexOf(LevelPath) = -1 then
          List.Items.Add(LevelPath);
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
  GenerateRandomCodeSeed;
end;

procedure TMainForm.FormCreate(Sender: TObject);
begin
  Version := GetVersion;
  RootPath := ExtractFilePath(Application.ExeName);

  Caption := 'Lemmini Level Pack Compiler ' + Version;

  PopulateModsDropdownList(RootPath);
  GenerateRandomCodeSeed;
  edPackTitle.Text := '';
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

procedure TMainForm.GenerateRandomCodeSeed;
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
  edCodeSeed.Text := s;
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
  cbMods.Items.Add('< No Mods >');

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

end.
