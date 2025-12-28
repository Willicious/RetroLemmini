object MainForm: TMainForm
  Left = 0
  Top = 0
  Caption = 'Lemmini Level Pack Compiler'
  ClientHeight = 520
  ClientWidth = 718
  Color = clBtnFace
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clWindowText
  Font.Height = -11
  Font.Name = 'Tahoma'
  Font.Style = []
  OldCreateOrder = False
  Position = poScreenCenter
  OnCreate = FormCreate
  PixelsPerInch = 96
  TextHeight = 13
  object gbMusic: TGroupBox
    Left = 8
    Top = 170
    Width = 305
    Height = 313
    Caption = 'Music'
    TabOrder = 0
    object lbMusic: TListBox
      Left = 16
      Top = 22
      Width = 177
      Height = 270
      ItemHeight = 13
      MultiSelect = True
      TabOrder = 0
    end
    object btnAddMusicTrack: TButton
      Left = 202
      Top = 24
      Width = 90
      Height = 25
      Caption = 'Add Track(s)'
      TabOrder = 1
      OnClick = btnAddMusicTrackClick
    end
    object btnDeleteSelectedMusicTrack: TButton
      Left = 202
      Top = 55
      Width = 87
      Height = 25
      Caption = 'Delete Track(s)'
      TabOrder = 2
      OnClick = btnDeleteSelectedMusicTrackClick
    end
    object btnMoveMusicTrackUp: TButton
      Left = 202
      Top = 86
      Width = 87
      Height = 25
      Caption = 'Move Up'
      TabOrder = 3
      OnClick = btnMoveMusicTrackUpClick
    end
    object btnMoveMusicTrackDown: TButton
      Left = 202
      Top = 117
      Width = 87
      Height = 25
      Caption = 'Move Down'
      TabOrder = 4
      OnClick = btnMoveMusicTrackDownClick
    end
  end
  object gbGeneralInfo: TGroupBox
    Left = 8
    Top = 8
    Width = 305
    Height = 156
    Caption = 'General Info'
    TabOrder = 1
    object lblPackTitle: TLabel
      Left = 16
      Top = 32
      Width = 45
      Height = 13
      Caption = 'Pack Title'
    end
    object lblCodeSeed: TLabel
      Left = 16
      Top = 67
      Width = 52
      Height = 13
      Caption = 'Code Seed'
    end
    object lblMods: TLabel
      Left = 16
      Top = 106
      Width = 25
      Height = 13
      Caption = 'Mods'
    end
    object edPackTitle: TEdit
      Left = 88
      Top = 29
      Width = 201
      Height = 21
      TabOrder = 0
      OnClick = TextInputClick
    end
    object edCodeSeed: TEdit
      Left = 88
      Top = 64
      Width = 106
      Height = 21
      CharCase = ecUpperCase
      MaxLength = 10
      TabOrder = 1
      Text = 'ABCDEFGHIJ'
      OnClick = TextInputClick
    end
    object cbMods: TComboBox
      Left = 88
      Top = 103
      Width = 201
      Height = 21
      Style = csDropDownList
      TabOrder = 2
    end
    object btnGenerateSeed: TButton
      Left = 202
      Top = 62
      Width = 87
      Height = 25
      Caption = 'Generate'
      TabOrder = 3
      OnClick = btnGenerateSeedClick
    end
  end
  object gbLevels: TGroupBox
    Left = 319
    Top = 8
    Width = 394
    Height = 487
    Caption = 'Levels'
    TabOrder = 2
    object pcLevels: TPageControl
      Left = 11
      Top = 25
      Width = 277
      Height = 433
      ActivePage = tsLevelGroup
      TabOrder = 0
      object tsLevelGroup: TTabSheet
        Caption = 'Group 1'
        object lbLevels: TListBox
          Left = 0
          Top = 0
          Width = 269
          Height = 405
          Align = alClient
          ItemHeight = 13
          MultiSelect = True
          TabOrder = 0
        end
      end
    end
    object btnAddGroup: TButton
      Left = 294
      Top = 49
      Width = 91
      Height = 25
      Caption = 'Add Group'
      TabOrder = 1
      OnClick = btnAddGroupClick
    end
    object btnDeleteGroup: TButton
      Left = 294
      Top = 111
      Width = 91
      Height = 25
      Caption = 'Delete Group'
      TabOrder = 2
      OnClick = btnDeleteGroupClick
    end
    object btnMoveLevelDown: TButton
      Left = 294
      Top = 277
      Width = 91
      Height = 25
      Caption = 'Move Down'
      TabOrder = 3
      OnClick = btnMoveLevelDownClick
    end
    object btnMoveLevelUp: TButton
      Left = 294
      Top = 246
      Width = 91
      Height = 25
      Caption = 'Move Up'
      TabOrder = 4
      OnClick = btnMoveLevelUpClick
    end
    object btnDeleteLevel: TButton
      Left = 294
      Top = 215
      Width = 91
      Height = 25
      Caption = 'Delete Level(s)'
      TabOrder = 5
      OnClick = btnDeleteLevelClick
    end
    object btnAddLevel: TButton
      Left = 294
      Top = 184
      Width = 91
      Height = 25
      Caption = 'Add Level(s)'
      TabOrder = 6
      OnClick = btnAddLevelClick
    end
    object btnRenameGroup: TButton
      Left = 294
      Top = 80
      Width = 91
      Height = 25
      Caption = 'Name Group'
      TabOrder = 7
      OnClick = btnRenameGroupClick
    end
  end
  object btnGenerateLevelPackINI: TButton
    Left = 33
    Top = 477
    Width = 669
    Height = 35
    Caption = 'Generate levelpack.ini'
    TabOrder = 3
    OnClick = btnGenerateLevelPackINIClick
  end
end
