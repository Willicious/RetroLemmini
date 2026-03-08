program LemminiLevelPackCompiler;

uses
  Vcl.Forms,
  Main in 'Main.pas' {MainForm},
  System.SysUtils,
  System.Classes,
  System.IOUtils;

{$R *.res}

begin
  Application.Initialize;
  Application.MainFormOnTaskbar := True;
  Application.CreateForm(TMainForm, MainForm);

  if (ParamCount > 0) and SameText(ParamStr(1), '/WriteVersionFile') then
  begin
    TFile.WriteAllText(
      ChangeFileExt(ParamStr(0), '.version.txt'),
      MainForm.GetVersion
    );
    Exit; // Exit immediately after writing file
  end;

  Application.Run;
end.
