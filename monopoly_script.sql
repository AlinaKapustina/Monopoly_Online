-- Oracle Application Express 4.0.2.00.09 SQL Script Export file
-- Exported 21:56 Tuesday January 10, 2017 by: MONOPOLY
-- Scripts included:
--      MONOPOLY
 
set define off
set verify off
set serveroutput on size 1000000
set feedback off
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
begin wwv_flow.g_import_in_progress := true; end; 
/
 
--       AAAA       PPPPP   EEEEEE  XX      XX
--      AA  AA      PP  PP  EE       XX    XX
--     AA    AA     PP  PP  EE        XX  XX
--    AAAAAAAAAA    PPPPP   EEEE       XXXX
--   AA        AA   PP      EE        XX  XX
--  AA          AA  PP      EE       XX    XX
--  AA          AA  PP      EEEEEE  XX      XX
prompt  Set Credentials...
 
begin
 
  -- Assumes you are running the script connected to SQL*Plus as the Oracle user APEX_040000 or as the owner (parsing schema) of the application.
  wwv_flow_api.set_security_group_id(p_security_group_id=>nvl(wwv_flow_application_install.get_workspace_id,4776420626003919));
 
end;
/

begin wwv_flow.g_import_in_progress := true; end;
/
begin 

select value into wwv_flow_api.g_nls_numeric_chars from nls_session_parameters where parameter='NLS_NUMERIC_CHARACTERS';

end;

/
begin execute immediate 'alter session set nls_numeric_characters=''.,''';

end;

/
begin wwv_flow.g_browser_language := 'en'; end;
/
prompt  Check Compatibility...
 
begin
 
-- This date identifies the minimum version required to import this file.
wwv_flow_api.set_version(p_version_yyyy_mm_dd=>'2010.05.13');
 
end;
/

begin wwv_flow.g_user := nvl(wwv_flow.g_user,'MONOPOLY'); end;
/
--application/sql/scripts
prompt ...exporting script file
--
begin
    wwv_flow_api.g_varchar2_table := wwv_flow_api.empty_varchar2_table;
    wwv_flow_api.g_varchar2_table(1) := '435245415445205441424C45202022555345525322200A2020202820202020224C4F47494E5F55534552222056415243484152322832303029204E4F54204E554C4C20454E41424C452C200A202020202250415353574F52442220564152434841523228';
    wwv_flow_api.g_varchar2_table(2) := '32303029204E4F54204E554C4C20454E41424C452C200A2020202022454D41494C222056415243484152322832303029204E4F54204E554C4C20454E41424C452C200A2020202020434F4E53545241494E54202255534552535F504B22205052494D4152';
    wwv_flow_api.g_varchar2_table(3) := '59204B45592028224C4F47494E5F55534552222920454E41424C450A20202029203B0A';
 
end;
/

 
declare
  l_name   varchar2(255);
begin
  l_name   := '4786806080046168/Create';
  wwv_flow_api.import_script(
    p_name          => l_name,
    p_varchar2_table=> wwv_flow_api.g_varchar2_table,
    p_pathid=> null,
    p_filename=> 'Create',
    p_title=> 'Create',
    p_mime_type=> 'text/plain',
    p_dad_charset=> '',
    p_deleted_as_of=> to_date('00010101000000','YYYYMMDDHH24MISS'),
    p_content_type=> 'BLOB',
    p_language=> '',
    p_description=> '',
    p_file_type=> 'SCRIPT',
    p_file_charset=> 'utf-8');
 
end;
/

--commit;
begin 
execute immediate 'begin dbms_session.set_nls( param => ''NLS_NUMERIC_CHARACTERS'', value => '''''''' || replace(wwv_flow_api.g_nls_numeric_chars,'''''''','''''''''''') || ''''''''); end;';
end;
/
set verify on
set feedback on
prompt  ...done
