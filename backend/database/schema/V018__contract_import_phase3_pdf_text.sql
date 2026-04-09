SET NOCOUNT ON;
GO

USE [DigitalHRM];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'CK_ct_contract_import_session_source_type'
      AND parent_object_id = OBJECT_ID(N'dbo.ct_contract_import_session')
)
BEGIN
    ALTER TABLE dbo.ct_contract_import_session DROP CONSTRAINT CK_ct_contract_import_session_source_type;
END
GO

ALTER TABLE dbo.ct_contract_import_session
ADD CONSTRAINT CK_ct_contract_import_session_source_type
CHECK (source_type IN ('DOCX', 'PDF_TEXT'));
GO

SET NOCOUNT OFF;
GO
